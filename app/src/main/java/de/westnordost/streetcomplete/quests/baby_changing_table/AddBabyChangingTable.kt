package de.westnordost.streetcomplete.quests.baby_changing_table

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddBabyChangingTable : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways with
        (
          (
            (amenity ~ restaurant|cafe|fuel|fast_food or shop ~ mall|department_store)
            and name
            and toilets = yes
          )
          or amenity = toilets
        )
        and !diaper and !changing_table
    """
    override val changesetComment = "Add baby changing table"
    override val wikiLink = "Key:changing_table"
    override val icon = R.drawable.ic_quest_baby
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_baby_changing_table_title
        else
            R.string.quest_baby_changing_table_toilets_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["changing_table"] = answer.toYesNo()
    }
}
