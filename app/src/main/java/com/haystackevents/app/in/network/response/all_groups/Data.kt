package com.haystackevents.app.`in`.network.response.all_groups

data class Data(
    val dat: String,
    val gdesc: String,
    val gname: String,
    val id: String,
    val member: List<Member>,
    val membercount: String,
    val status: String,
    val userid: String
)