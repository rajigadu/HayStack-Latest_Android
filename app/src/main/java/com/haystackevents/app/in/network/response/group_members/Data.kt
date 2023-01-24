package com.haystackevents.app.`in`.network.response.group_members

import java.io.Serializable

data class Data(
    val dat: String,
    val email: String,
    val groupid: String,
    val id: String,
    val member: String,
    val number: String,
    val status: String,
    val message: String,
    val userid: String
): Serializable {
    constructor():this("","","", "","", "", "","","")}