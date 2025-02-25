package de.westnordost.streetcomplete.quests.pitch_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddPitchLit : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with (leisure = pitch or leisure = track)
        and (access !~ private|no)
        and indoor != yes and (!building or building = no)
        and (
          !lit
          or lit = no and lit older today -8 years
          or lit older today -16 years
        )
    """

    override val changesetComment = "Add whether pitch is lit"
    override val wikiLink = "Key:lit"
    override val icon = R.drawable.ic_quest_pitch_lantern

    override val questTypeAchievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["leisure"] == "track")
            R.string.quest_pitchLit_title_track
        else
            R.string.quest_pitchLit_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("lit", answer.toYesNo())
    }
}
