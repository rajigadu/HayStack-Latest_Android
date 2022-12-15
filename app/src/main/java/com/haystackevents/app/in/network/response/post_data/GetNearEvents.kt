package com.haystackevents.app.`in`.network.response.post_data

data class GetNearEvents(
    var currentDate: String?,
    var deviceType: String?,
    var distanceInMile: String?,
    var searchType: String?,
    var city: String?,
    var lat: String?,
    var lon: String?,
    var endTime: String?,
    var category: String?,
    var id: String?,
    var deviceId: String?,
    var nationWide: String?,
    var deviceToken: String?
){
    constructor(): this("", "", "0", "", "", "","",
    "", "", "", "", "0", "")
}
