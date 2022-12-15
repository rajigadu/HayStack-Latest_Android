package com.haystackevents.app.`in`.network.response.event

import java.io.Serializable

data class AllMembers(
    var member: String,
    var email: String,
    var number: String
): Serializable
