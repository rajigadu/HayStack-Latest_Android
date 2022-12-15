package com.haystackevents.app.`in`.network.response.search_events

import java.io.Serializable

data class SearchByEvent(
    var id: String?,
    var searchType: String?,
    var country: String?,
    var state: String?,
    var city: String?,
    var startDate: String?,
    var endDate: String?,
    var startTime: String?,
    var endTime: String?,
    var distanceMile: String?,
    var nationWide: String?,
    var latitude: String?,
    var longitude: String?,
    var category: String?,
    var zipcode: String?
): Serializable {
    constructor(): this(
        "", "", "", "", "", "", "","",
        "", "", "", "", "", "", ""
    )
}