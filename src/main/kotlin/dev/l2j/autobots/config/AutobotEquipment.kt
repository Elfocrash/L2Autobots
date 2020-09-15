package dev.l2j.autobots.config

import com.fasterxml.jackson.annotation.JsonProperty
import net.sf.l2j.gameserver.enums.actors.ClassId

data class AutobotEquipment(
        @JsonProperty("classid") val classId: ClassId,
        @JsonProperty("minLevel") val minLevel: Int,
        @JsonProperty("maxLevel") val maxLevel: Int,
        @JsonProperty("rhand") val rightHand: Int,
        @JsonProperty("lhand") val leftHand: Int,
        @JsonProperty("head") val head: Int,
        @JsonProperty("chest") val chest: Int,
        @JsonProperty("legs") val legs: Int,
        @JsonProperty("hands") val hands: Int,
        @JsonProperty("feet") val feet: Int,
        @JsonProperty("neck") val neck: Int,
        @JsonProperty("lear") val leftEar: Int,
        @JsonProperty("rear") val rightEar: Int,
        @JsonProperty("lring") val leftRing: Int,
        @JsonProperty("rring") val rightRing: Int)
