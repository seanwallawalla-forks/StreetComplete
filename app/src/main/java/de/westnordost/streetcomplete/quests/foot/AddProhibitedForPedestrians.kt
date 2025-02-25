package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_PAVED
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.HAS_SEPARATE_SIDEWALK
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.IS_LIVING_STREET
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.NO
import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.YES

class AddProhibitedForPedestrians : OsmFilterQuestType<ProhibitedForPedestriansAnswer>() {

    override val elementFilter = """
        ways with (
          sidewalk:both ~ none|no
          or sidewalk ~ none|no
          or (sidewalk:left ~ none|no and sidewalk:right ~ none|no)
        )
        and shoulder != yes
        and !foot
        and access !~ private|no
        """ +
        /* asking for any road without sidewalk is too much. Main interesting situations are
           certain road sections within large intersections, overpasses, underpasses,
           inner segregated lanes of large streets, connecting/linking road way sections and so
           forth. See https://lists.openstreetmap.org/pipermail/tagging/2019-February/042852.html

           #2472 documents that there have been continuous misuderstandings when roads are really
           forbidden to walk on legally, which is why since v34.0, so the question is not asked for
           roads that are simply lit anymore but only if they are tunnels, bridges, links (=oneways)
           or if already bicycle=no/sidepath */
        // only roads where foot=X is not (almost) implied
        "and motorroad != yes " +
        "and highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified " +
        // road probably not developed enough to issue a prohibition for pedestrians
        "and surface ~ ${ANYTHING_PAVED.joinToString("|")} " +
        // fuzzy filter for above mentioned situations + developed-enough / non-rural roads
        "and ( oneway ~ yes|-1 or bridge = yes or tunnel = yes or bicycle ~ no|use_sidepath )"

    override val changesetComment = "Add whether roads are prohibited for pedestrians"
    override val wikiLink = "Key:foot"
    override val icon = R.drawable.ic_quest_no_pedestrians
    override val isSplitWayEnabled = true

    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_accessible_for_pedestrians_title_prohibited

    override fun createForm() = AddProhibitedForPedestriansForm()

    override fun applyAnswerTo(answer: ProhibitedForPedestriansAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            // the question is whether it is prohibited, so YES -> foot=no etc
            YES -> tags["foot"] = "no"
            NO -> tags["foot"] = "yes"
            HAS_SEPARATE_SIDEWALK -> tags["sidewalk"] = "separate"
            IS_LIVING_STREET -> tags["highway"] = "living_street"
        }
    }
}
