package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import java.util.concurrent.FutureTask

class AddPlaceName(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<PlaceNameAnswer> {

    private val filter by lazy { ("""
        nodes, ways, relations with
        (
          shop and shop !~ no|vacant
          or craft
          or office
          or amenity = recycling and recycling_type = centre
          or tourism = information and information = office
          or """ +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "name only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",                    // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",           // commercial
                "car_wash", "car_rental", "fuel",                                                                      // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health
                "animal_boarding", "animal_shelter", "animal_breeding",                                                // animals
                "coworking_space",                                                                                     // work

                // name & opening hours
                "boat_rental",

                // name & wheelchair
                "theatre",                             // culture
                "conference_centre", "arts_centre",    // events
                "police", "ranger_station",            // civic
                "ferry_terminal",                      // transport
                "place_of_worship",                    // religious
                "hospital",                            // health care

                // name only
                "studio",                                                                // culture
                "events_venue", "exhibition_centre", "music_venue",                      // events
                "prison", "fire_station",                                                // civic
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
                "monastery",                                                             // religious
                "kindergarten", "school", "college", "university", "research_institute", // education
                "driving_school", "dive_centre", "language_school", "music_school",      // learning
                "brothel", "gambling", "love_hotel", "stripclub"                         // bad stuff
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",

                // name & wheelchair
                "attraction",
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet" // accommodations

                // and tourism = information, see above
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",

                // name & wheelchair
                "sports_centre", "stadium",

                // name & opening hours
                "horse_riding",

                // name only
                "dance", "nature_reserve", "marina",
            ),
            "landuse" to arrayOf(
                "cemetery", "allotments"
            ),
            "military" to arrayOf(
                "airfield", "barracks", "training_area"
            ),
            "healthcare" to arrayOf(
                // common
                "audiologist", "optometrist", "counselling", "speech_therapist",
                "sample_collection", "blood_donation",

                // name & opening hours
                "physiotherapist", "podiatrist",
            ),
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ") + "\n" + """
        )
        and !name and !brand and noname != yes and name:signed != no
    """).toElementFilterExpression() }

    override val changesetComment = "Determine place names"
    override val wikiLink = "Key:name"
    override val icon = R.drawable.ic_quest_label
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeName_title_name

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>) =
        arrayOfNotNull(featureName.value)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && hasFeatureName(element.tags)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddPlaceNameForm()

    override fun applyAnswerTo(answer: PlaceNameAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is NoPlaceNameSign -> {
                tags["name:signed"] = "no"
            }
            is PlaceName -> {
                for ((languageTag, name) in answer.localizedNames) {
                    val key = when (languageTag) {
                        "" -> "name"
                        "international" -> "int_name"
                        else -> "name:$languageTag"
                    }
                    tags[key] = name
                }
            }
        }
    }

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
