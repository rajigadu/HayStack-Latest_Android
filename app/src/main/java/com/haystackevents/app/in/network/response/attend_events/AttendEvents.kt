package com.haystackevents.app.`in`.network.response.attend_events

data class AttendEvents(
    val data: List<AttendEventsData>,
    val status: String,
    val message: String
)