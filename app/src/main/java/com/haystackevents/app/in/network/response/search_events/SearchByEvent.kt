package com.haystackevents.app.`in`.network.response.search_events

import java.io.Serializable

data class SearchByEvent(
    var id: String? = null,
    var searchType: String? = null,
    var country: String? = null,
    var state: String? = null,
    var city: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var distanceMile: String? = null,
    var nationWide: String? = null,
    var latitude: String? = null,
    var longitude: String? = null,
    var category: String? = null,
    var zipcode: String? = null,
    var address: String? = null
): Serializable