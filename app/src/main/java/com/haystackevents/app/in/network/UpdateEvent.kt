package com.haystackevents.app.`in`.network

data class UpdateEvent(
    var event_name: String,
    var streetaddress: String,
    var city: String,
    var id: String?,
    var state: String,
    var zipcode: String,
    var startDate: String,
    var startTime: String,
    var endDate: String,
    var endTime: String,
    var hostname:  String,
    var contactInfo: String,
    var hosttype: String,
    var eventType: String,
    var country: String,
    var latitude: String,
    var longitude: String,
    var category: String,
    var eventId: String,
    var userid: String
){
    constructor():this(
        "", "", "", "", "", "", "", "", "",
    "", "", "", "", "", "", "", "",
        "", "", ""
    )
}