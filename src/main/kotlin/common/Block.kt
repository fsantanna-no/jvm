package org.freechains.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.math.max

typealias Hash = String

enum class State {
    MISSING, ACCEPTED, PENDING, REJECTED, BANNED
}

fun State.toString_ () : String {
    return when (this) {
        State.ACCEPTED -> "accepted"
        State.PENDING  -> "pending"
        State.REJECTED -> "rejected"
        else -> error("bug found: unexpected state")
    }
}

fun String.toState () : State {
    return when (this) {
        "missing"  -> State.MISSING
        "accepted" -> State.ACCEPTED
        "pending"  -> State.PENDING
        "rejected" -> State.REJECTED
        "banned"   -> State.BANNED
        else       -> error("bug found")
    }
}

enum class LikeType {
    POST, PUBKEY
}

@Serializable
data class Like (
    val n    : Int,       // +X: like, -X: dislike
    val type : LikeType,
    val ref  : String     // target public key or post hash
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
    val refs    : Array<String>,  // post hash or user pubkey
    val backs   : Array<Hash>     // back links (previous blocks)
) {
    val height  : Int = this.backs.backsToHeight()
}

@Serializable
data class Block (
    val immut     : Immut,              // things to hash
    val fronts    : MutableList<Hash>,  // front links (next blocks)
    val sign      : Signature?,
    val accepted  : Boolean,            // TODO: remove
    val hash      : Hash                // hash of immut
) {
    val localTime : Long = getNow()     // local time
}

fun Array<Hash>.backsToHeight () : Int {
    return when {
        this.isEmpty() -> 0
        else -> this.fold(0, { cur, hash -> max(cur, hash.toHeight()) }) + 1
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

fun Immut.isLikePub () : Boolean {
    return this.like!=null && this.like.ref.hashIsBlock()
}