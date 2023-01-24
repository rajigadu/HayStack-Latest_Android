package com.haystackevents.app.`in`.network.response.my_events

import java.io.Serializable

data class MyEventsData(
    val category: String,
    val city: String,
    val contactinfo: String,
    val country: String,
    val dat: String,
    val distance_km: String,
    val distance_miles: String,
    val enddate: String,
    val enddate1: String,
    val endtime: String,
    val endtime1: String,
    val event_description: String,
    val event_name: String,
    val hostname: String,
    val hosttype: String,
    val id: String,
    val latitude: String,
    val longitude: String,
    val membercount: String,
    val photo: String? = null,
    val shortd: String,
    val startdate: String,
    val startdate1: String,
    val starttime: String,
    val starttime1: String,
    val state: String,
    val status: String,
    val streetaddress: String,
    val typ: String,
    val userid: String,
    val zipcode: String
): Serializable