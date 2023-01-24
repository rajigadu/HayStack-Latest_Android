package com.haystackevents.app.`in`.network

data class ContactInfo(
    var displayName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var workPhoneNumber: String? = null,
    var homePhoneNumber: String? = null,
    var mobilePhoneNumber: String? = null,
    var homeEmailId: String? = null,
    var workEmailId: String? = null,
    var nickName: String? = null,
    var country: String? = null,
    var address1: String? = null,
    var address2: String? = null,
    var city: String? = null,
    var state: String? = null,
    var nationality: String? = null,
    var relation: String? = null
) {
    override fun toString(): String {
        return "ContactInfo(displayName=$displayName, " +
                "firstName=$firstName, middleName=$middleName, " +
                "lastName=$lastName, " +
                "workPhoneNumber=$workPhoneNumber, " +
                "homePhoneNumber=$homePhoneNumber, " +
                "mobilePhoneNumber=$mobilePhoneNumber, " +
                "homeEmailId=$homeEmailId, " +
                "workEmailId=$workEmailId, " +
                "nickName=$nickName, " +
                "country=$country, " +
                "address1=$address1, " +
                "address2=$address2, " +
                "city=$city, " +
                "state=$state, " +
                "nationality=$nationality, " +
                "relation=$relation)"
    }
}
