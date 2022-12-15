package com.haystackevents.app.`in`.network.response.create_group

data class Group(
    val data: List<Data>,
    val groupid: Int,
    val message: String,
    val status: String
)