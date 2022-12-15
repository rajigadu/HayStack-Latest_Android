package com.haystackevents.app.`in`.network.response.search_events

data class SearchEvents(
    val data: List<SearchEventsData>,
    val status: String,
    val message: String
)