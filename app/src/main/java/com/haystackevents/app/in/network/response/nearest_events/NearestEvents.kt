package com.haystackevents.app.`in`.network.response.nearest_events

data class NearestEvents(
    val data: List<NearestEventData>,
    val message: String,
    val status: String
)