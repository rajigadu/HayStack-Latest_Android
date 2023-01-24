package com.haystackevents.app.`in`.network.response.invited_events

data class InvitedEvents(
    val data: List<InvitedEventsData>,
    val status: String?,
    val message: String?
)