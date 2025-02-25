import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.5.0"
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    signingConfigs {
        create("release") {
        }
    }

    compileSdk = 31
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete"
        minSdk = 21
        targetSdk = 31
        versionCode = 4100
        versionName = "41.0-beta1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        all {
            isMinifyEnabled = true
            isShrinkResources = true
            // don't use proguard-android-optimize.txt, it is too aggressive, it is more trouble than it is worth
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        create("releaseGooglePlay") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "true")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    lint {
        // there is currently always an internal error "Unexpected lint invalid arguments" when executing lintAnalyze*, so whatever, disable this then!
        isCheckReleaseBuilds = false
        disable("MissingTranslation")
        ignore("UseCompatLoadingForDrawables") // doesn't make sense for minSdk >= 21
        isAbortOnError = false
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    val props = Properties()
    props.load(FileInputStream(keystorePropertiesFile))
    val releaseSigningConfig = android.signingConfigs.getByName("release")
    releaseSigningConfig.storeFile = file(props.getProperty("storeFile"))
    releaseSigningConfig.storePassword = props.getProperty("storePassword")
    releaseSigningConfig.keyAlias = props.getProperty("keyAlias")
    releaseSigningConfig.keyPassword = props.getProperty("keyPassword")
}

repositories {
    google()
    mavenCentral()
    jcenter {
        content {
            includeGroup("org.sufficientlysecure")
        }
    }
}

configurations {
    // it's already included in Android
    all {
        exclude(group = "net.sf.kxml", module = "kxml2")
    }
}

dependencies {
    val kotlinVersion = "1.6.10"
    val mockitoVersion = "3.12.4"
    val kotlinxVersion = "1.6.0"
    val koinVersion = "3.1.5"

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.assertj:assertj-core:2.8.0")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    androidTestImplementation("org.assertj:assertj-core:2.8.0")

    // dependency injection
    implementation("io.insert-koin:koin-android-compat:$koinVersion")
    implementation("io.insert-koin:koin-androidx-workmanager:$koinVersion")

    // Android stuff
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.annotation:annotation:1.3.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.viewpager:viewpager:1.0.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // photos
    implementation("androidx.exifinterface:exifinterface:1.3.3")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")

    // scheduling background jobs
    implementation("androidx.work:work-runtime:2.7.1")

    // finding in which country we are for country-specific logic
    implementation("de.westnordost:countryboundaries:1.5")
    // finding a name for a feature without a name tag
    implementation("de.westnordost:osmfeatures-android:4.0")
    // talking with the OSM API
    implementation("de.westnordost:osmapi-map:2.0")
    implementation("de.westnordost:osmapi-changesets:2.0")
    implementation("de.westnordost:osmapi-notes:2.0")
    implementation("de.westnordost:osmapi-user:2.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("se.akerfeldt:okhttp-signpost:1.1.0")

    // widgets
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.grantland:autofittextview:0.2.1")
    // html-textview not maintained anymore, only available on jcenter - should be replaced in the long term
    implementation("org.sufficientlysecure:html-textview:3.9")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // box2d view
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.charleskorn.kaml:kaml:0.40.0")

    // map and location
    implementation("com.mapzen.tangram:tangram:0.17.1")

    // opening hours parser
    implementation("ch.poole:OpeningHoursParser:0.26.0")

    // measuring distance with AR
    implementation("com.google.ar:core:1.29.0")
    implementation("com.google.ar.sceneform:core:1.17.1")
}

/** Localizations that should be pulled from POEditor etc. */
val bcp47ExportLanguages = setOf(
    "am", "ar", "ast", "bg", "bs", "ca", "cs", "da", "de", "el", "en", "en-AU", "en-GB", "es", "eu",
    "fa", "fi", "fr", "gl", "hr", "hu", "id", "it", "ja", "ko", "lt", "ml", "nb", "no", "nl", "nn",
    "pl", "pt", "pt-BR", "ro", "ru", "sk", "sr-cyrl", "sv", "th", "tr", "uk", "zh", "zh-CN", "zh-HK", "zh-TW"
)

// see https://github.com/osmlab/name-suggestion-index/tags for latest version
val nsiVersion = "v6.0.20220131"
// see https://github.com/openstreetmap/id-tagging-schema/releases for latest version
val presetsVersion = "v3.2.1"

tasks.register("updateAvailableLanguages") {
    group = "streetcomplete"
    doLast {
        val fileWriter = FileWriter("$projectDir/src/main/res/raw/languages.yml", false)
        fileWriter.write(bcp47ExportLanguages.joinToString("\n") { "- $it" })
        fileWriter.write("\n")
        fileWriter.close()
    }
}

tasks.register<GetTranslatorCreditsTask>("updateTranslatorCredits") {
    group = "streetcomplete"
    targetFile = "$projectDir/src/main/res/raw/credits_translators.yml"
    languageCodes = bcp47ExportLanguages
    cookie = properties["POEditorCookie"] as String
    phpsessid = properties["POEditorPHPSESSID"] as String
}

tasks.register<UpdatePresetsTask>("updatePresets") {
    group = "streetcomplete"
    version = presetsVersion
    languageCodes = bcp47ExportLanguages
    targetDir = "$projectDir/src/main/assets/osmfeatures/default"
}

tasks.register<UpdateNsiPresetsTask>("updateNsiPresets") {
    group = "streetcomplete"
    version = nsiVersion
    targetDir = "$projectDir/src/main/assets/osmfeatures/brands"
}

tasks.register<UpdateAppTranslationsTask>("updateTranslations") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    apiToken = properties["POEditorAPIToken"] as String
    targetFiles = { "$projectDir/src/main/res/values-$it/strings.xml" }
}

tasks.register<UpdateAppTranslationCompletenessTask>("updateTranslationCompleteness") {
    group = "streetcomplete"
    apiToken = properties["POEditorAPIToken"] as String
    targetFiles = { "$projectDir/src/main/res/values-$it/translation_info.xml" }
}

tasks.register<GenerateMetadataByCountry>("generateMetadataByCountry") {
    group = "streetcomplete"
    sourceDir = "$rootDir/res/country_metadata"
    targetDir = "$projectDir/src/main/assets/country_metadata"
}

tasks.register("copyDefaultStringsToEnStrings") {
    doLast {
        File("$projectDir/src/main/res/values/strings.xml")
            .copyTo(File("$projectDir/src/main/res/values-en/strings.xml"), true)
    }
}
