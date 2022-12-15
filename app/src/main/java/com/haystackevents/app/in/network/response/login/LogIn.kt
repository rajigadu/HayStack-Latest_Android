package com.haystackevents.app.`in`.network.response.login


data class LogIn(
    val data: List<LogInData>,
    val status: String,
    val message: String
)