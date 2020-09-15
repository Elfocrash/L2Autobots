package dev.l2j.autobots.skills

import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.utils.hasEffect
import net.sf.l2j.gameserver.model.L2Skill
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player

internal class BotSkill(val skillId: Int) {
    
    var condition: ((Player, L2Skill, Creature?) -> Boolean) = { _, _, _ -> true }
    var forceTargetSelf = false
    var isTogglableSkill: Boolean = false
    
    constructor(skillId: Int, isConditionalSkill: Boolean, isTogglableSkill: Boolean, useWhenEffectIsNotPresent:Boolean = false, forceTargetSelf: Boolean = false) : 
            this(skillId, { player, skill, _ -> 
                (!isConditionalSkill || (isConditionalSkill && player.getCombatBehavior().validateConditionalSkill(skill))) &&
                (!isTogglableSkill || (isTogglableSkill && player.getCombatBehavior().skillPreferences.togglableSkills[skillId]!!)) &&
                (!useWhenEffectIsNotPresent || (useWhenEffectIsNotPresent && !player.hasEffect(skillId)))
                 }) {
        this.isTogglableSkill = isTogglableSkill
        this.forceTargetSelf = forceTargetSelf
    }
    
    constructor(skillId: Int, condition: (player: Player, skill: L2Skill, target: Creature?) -> Boolean) : this(skillId){
        this.condition = condition
    }

    constructor(skillId: Int, condition: (player: Player, skill: L2Skill, target: Creature?) -> Boolean, isTogglableSkill: Boolean) : this(skillId){
        this.condition = condition
        this.isTogglableSkill = isTogglableSkill
    }

    constructor(skillId: Int, forceTargetSelf: Boolean = false, condition: (player: Player, skill: L2Skill, target: Creature?) -> Boolean) : this(skillId, condition){
        this.condition = condition
        this.forceTargetSelf = forceTargetSelf
    }
}