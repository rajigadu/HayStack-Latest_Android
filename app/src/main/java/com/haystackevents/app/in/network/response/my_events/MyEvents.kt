package com.haystackevents.app.`in`.network.response.my_events

data class MyEvents(
    val data: List<MyEventsData>,
    val message: String,
    val status: String
)