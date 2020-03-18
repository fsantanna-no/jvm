package org.freechains.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.math.max

typealias Hash = String

enum class State {
    ACCEPTED, PENDING, REJECTED, BANNED, MISSING
}

fun State.toString_ () : String {
    return when (this) {
        State.ACCEPTED -> "accepted"
        State.PENDING  -> "pending"
        State.REJECTED -> "rejected"
        State.BANNED   -> "banned"
        State.MISSING  -> "missing"
    }
}

fun String.toState () : State {
    return when (this) {
        "accepted" -> State.ACCEPTED
        "pending"  -> State.PENDING
        "rejected" -> State.REJECTED
        "banned"   -> State.BANNED
        "missing"  -> State.MISSING
        else       -> error("bug found")
    }
}

enum class LikeType {
    POST, PUBKEY
}

@Serializable
data class Like (
    val n    : Int,     // +X: like, -X: dislike
    val ref  : Hash     // target post hash
)

@Serializable
data class Signature (
    val hash : String,    // signature
    val pub  : HKey       // of pubkey (if "", assumes pub of chain)
)

@Serializable
data class Immut (
    val time    : Long,           // TODO: ULong
    val like    : Like?,
    val code    : String,         // payload encoding
    val crypt   : Boolean,        // payload is encrypted (method depends on chain)
    val payload : String,
    val backs   : Array<Hash>     // back links (previous blocks)
) {
    val height  : Int = this.backs.backsToHeight()
}

@Serializable
data class Block (
    val immut     : Immut,              // things to hash
    val fronts    : MutableList<Hash>,  // front links (next blocks)
    val sign      : Signature?,
    val hash      : Hash                // hash of immut
) {
    var localTime : Long = getNow()     // local time
}

fun Array<Hash>.backsToHeight () : Int {
    return when {
        this.isEmpty() -> 0
        else -> 1 + this.map { it.toHeight() }.max()!!
    }
}

fun Immut.toJson (): String {
    @UseExperimental(UnstableDefault::class)
    val json = Json(JsonConfiguration(prettyPrint=true))
    return json.stringify(Immut.serializer(), this)
}

fun Block.toJson (): String {
    @UseExperimental(UnstableDefault::class)
    val json = Json(JsonConfiguration(prettyPrint=true))
    return json.stringify(Block.serializer(), this)
}

fun String.jsonToBlock (): Block {
    @UseExperimental(UnstableDefault::class)
    val json = Json(JsonConfiguration(prettyPrint=true))
    return json.parse(Block.serializer(), this)
}

private fun Hash.toHeight () : Int {
    val (height,_) = this.split("_")
    return height.toInt()
}

fun Hash.hashIsBlock () : Boolean {
    return this.contains('_')   // otherwise is pubkey
}
