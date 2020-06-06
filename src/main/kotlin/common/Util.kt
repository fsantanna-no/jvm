package org.freechains.common

import java.net.Socket
import java.time.Instant

const val MAJOR    = 0
const val MINOR    = 6
const val REVISION = 2
const val VERSION  = "v$MAJOR.$MINOR.$REVISION"
const val PRE      = "FC $VERSION"

const val PORT_8330 = 8330 //8888  // TODO: back to 8330

const val ms   = 1.toLong()
const val sec  = 1000*ms
const val min  = 60*sec
const val hour = 60*min
const val day  = 24*hour

// TODO
const val T90D_rep    = 90*day          // consider last 90d for reputation
const val T120D_past  = 4*T90D_rep/3    // reject posts +120d in the past
const val T30M_future = 30*min          // refuse posts +30m in the future
const val T1D_reps    = 1*day           // account to reputation posts older than 1 day only (count negatively otherwise)

const val LK30_max     = 30
const val LK5_dislikes = 5
const val LK2_factor   = 2

const val S128_pay = 128000             // 128 KBytes maximum size of payload

const val N16_blockeds = 16             // hold at most 16 blocked blocks locally

fun String.pvtToPub () : String {
    return this.substring(this.length/2)
}

var NOW : Long? = null

fun setNow (t: Long) {
    NOW = Instant.now().toEpochMilli() - t
}

fun getNow () : Long {
    return Instant.now().toEpochMilli() - (if (NOW == null) 0 else NOW!!)
}

fun String.nowToTime () : Long {
    return if (this == "now") getNow() else this.toLong()
}

fun Socket_5s (host: String, port: Int) : Socket {
    val s = Socket(host,port)
    s.soTimeout = 5000
    return s
}