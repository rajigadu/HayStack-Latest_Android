package com.haystackevents.app.`in`.network.response.interest_events

data class InterestEvents(
    val data: List<InterestEventsData>,
    val message: String,
    val status: String
)