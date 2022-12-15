package com.haystackevents.app.`in`.network.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.haystackevents.app.`in`.BuildConfig
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.manager.SessionManager
import com.haystackevents.app.`in`.network.ApiClient
import com.haystackevents.app.`in`.network.ApiInterface
import com.haystackevents.app.`in`.network.UpdateEvent
import com.haystackevents.app.`in`.network.response.add_attend_events.AddAttendEvent
import com.haystackevents.app.`in`.network.response.add_interest_events.AddInterestEvents
import com.haystackevents.app.`in`.network.response.all_groups.AllGroups
import com.haystackevents.app.`in`.network.response.attend_events.AttendEvents
import com.haystackevents.app.`in`.network.response.categories.AllCategories
import com.haystackevents.app.`in`.network.response.countries.Countries
import com.haystackevents.app.`in`.network.response.create_group.Group
import com.haystackevents.app.`in`.network.response.event.Event
import com.haystackevents.app.`in`.network.response.event.EventCreated
import com.haystackevents.app.`in`.network.response.group_members.DefaultResponse
import com.haystackevents.app.`in`.network.response.group_members.GroupMembers
import com.haystackevents.app.`in`.network.response.interest_events.InterestEvents
import com.haystackevents.app.`in`.network.response.invited_events.InvitedEvents
import com.haystackevents.app.`in`.network.response.login.LogIn
import com.haystackevents.app.`in`.network.response.my_events.MyEvents
import com.haystackevents.app.`in`.network.response.near_events.NearEvents
import com.haystackevents.app.`in`.network.response.nearest_events.NearestEvents
import com.haystackevents.app.`in`.network.response.post_data.GetNearEvents
import com.haystackevents.app.`in`.network.response.search_events.SearchByEvent
import com.haystackevents.app.`in`.network.response.search_events.SearchEvents
import com.haystackevents.app.`in`.network.response.soldier_signup.SignUpResponse
import com.haystackevents.app.`in`.network.response.states.States
import com.haystackevents.app.`in`.utils.AppConstants.DEVICE_TYPE
import com.haystackevents.app.`in`.utils.Extensions.getRealPathFromURIAPI
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

object Repository {

    private val client by lazy { ApiClient.retrofitService }
    private val authClient by lazy {
        ApiClient.createBearerAuthService(
            ApiInterface::class.java, false)
    }



    fun soldierRegistration(fName: String, lName: String, email: String,
                            address: String, zipcode: String, password: String,
                            number: String, country: String, state: String, city: String,accType: String): Call<SignUpResponse> {
        return client.soldierRegistration(
            fName, lName, email, address, zipcode, password, number, country, state, city, accType)
    }

    fun userLogIn(userName: String, password: String, deviceId: String): Call<LogIn> {
        val deviceType = DEVICE_TYPE
        val deviceToken = SessionManager.instance.getUserToken()
        return client.userLogIn(userName, password, deviceType, deviceId, deviceToken)
    }

    fun changePassword(oldPassword: String, newPassword: String, userId: String): Call<DefaultResponse>{
        val loginUser = SessionManager.instance.getLoginUser()
        return client.changePassword(oldPassword, newPassword, userId, "Soldier")
    }

    fun forgotPassword(email: String): Call<DefaultResponse> = client.forgotPassword(email)

    fun editProfile(firstName: String, lastName: String, userNumber: String, userId: String):
            Call<DefaultResponse>{
        return client.editUserProfile(firstName,lastName, userNumber, userId)
    }

    fun createNewGroup(groupName: String, groupDesc: String, userId: String): Call<Group>{
        return client.createGroup(groupName, groupDesc, userId)
    }

    fun contactUs(fullName: String, email: String, message: String) = client.contactUs(fullName, email, message)

    fun getAllGroupsList(id: String): Call<AllGroups> = client.getAllGroups(id)

    fun editGroup(groupName: String, groupDesc: String, groupId: String, userId: String): Call<DefaultResponse> =
        client.editGroup(groupName, groupDesc, groupId, userId)

    fun addMemberToGroup(groupId: String, userId: String, member: String, number: String, email: String):
            Call<DefaultResponse> = client.addMemberToGroup(groupId, userId, member, number, email)

    fun getGroupMembers(groupId: String, userId: String): Call<GroupMembers> = client.getGroupMembers(groupId, userId)

    fun editGroupMember(groupId: String, member: String, number: String, email: String, memberId: String) =
        client.editGroupMember(groupId, member, number, email, memberId)

    fun deleteGroupMember(groupId: String, memberId: String, userId: String): Call<DefaultResponse> =
        client.deleteGroupMember(groupId, userId, memberId)

    fun deleteGroup(groupId: String, userId: String): Call< DefaultResponse> = client.deleteGroup(groupId, userId)

    fun getAllCategories(): Call<AllCategories> = client.getAllCategories()

    fun createNewEvent(event: Event, requireContext: Context): Call<EventCreated> {
        val multipartMap = HashMap<String, RequestBody>()

        val rqEventName = RequestBody.create(MediaType.parse("text/plain"), event.event_name)
        val rqEventDesc = RequestBody.create(MediaType.parse("text/plain"), event.event_description)
        val rqStreetAddress = RequestBody.create(MediaType.parse("text/plain"), event.streetaddress)
        val rqUserId = RequestBody.create(MediaType.parse("text/plain"), event.id)
        val rqState = RequestBody.create(MediaType.parse("text/plain"), event.state)
        val rqZipCode = RequestBody.create(MediaType.parse("text/plain"), event.zipcode)
        val rqCity = RequestBody.create(MediaType.parse("text/plain"), event.city)
        val rqStartDate = RequestBody.create(MediaType.parse("text/plain"), event.startdate)
        val rqStartTime = RequestBody.create(MediaType.parse("text/plain"), event.starttime)
        val rqEndDate = RequestBody.create(MediaType.parse("text/plain"), event.enddate)
        val rqEndTime = RequestBody.create(MediaType.parse("text/plain"), event.endtime)
        val rqHostName = RequestBody.create(MediaType.parse("text/plain"), event.hostname)
        val rqContactInfo = RequestBody.create(MediaType.parse("text/plain"), event.contactinfo)
        val rqHostType = RequestBody.create(MediaType.parse("text/plain"), event.hosttype)
        val rqEventType = RequestBody.create(MediaType.parse("text/plain"), event.eventtype)
        val rqCountry = RequestBody.create(MediaType.parse("text/plain"), event.country)
        val rqLatitude = RequestBody.create(MediaType.parse("text/plain"), event.latitude)
        val rqLongitude = RequestBody.create(MediaType.parse("text/plain"), event.longitude)
        val rqCategory = RequestBody.create(MediaType.parse("text/plain"), event.category)


        multipartMap["event_name"] = rqEventName
        multipartMap["event_description"] = rqEventDesc
        multipartMap["streetaddress"] = rqStreetAddress
        multipartMap["id"] = rqUserId
        multipartMap["state"] = rqState
        multipartMap["city"] = rqCity
        multipartMap["zipcode"] = rqZipCode
        multipartMap["startdate"] = rqStartDate
        multipartMap["starttime"] = rqStartTime
        multipartMap["enddate"] = rqEndDate
        multipartMap["endtime"] = rqEndTime
        multipartMap["hostname"] = rqHostName
        multipartMap["contactinfo"] = rqContactInfo
        multipartMap["hosttype"] = rqHostType
        multipartMap["eventtype"] = rqEventType
        multipartMap["country"] = rqCountry
        multipartMap["latitude"] = rqLatitude
        multipartMap["longitude"] = rqLongitude
        multipartMap["category"] = rqCategory

        var body: MultipartBody.Part? = null
        var file: File? = null
        Log.e("TAG", "eventImage: ${event.image}")

        if (event.image.isNullOrEmpty()) {

            try {
                val uri = Uri.parse(
                    "android.resource://"+ BuildConfig.APPLICATION_ID+"/" + R.drawable.events_default_bg_)
                file = File(getRealPathFromURIAPI(requireContext, uri)!!)

            }catch (e: Exception){e.printStackTrace()}

        } else {
            Log.e("TAG", "image not null")
            file =
                File(getRealPathFromURIAPI(requireContext, event.image.toUri())!!)
        }

        if (file != null) {
            val reqBody = RequestBody.create(
                MediaType.parse("multipart/form-data"), file)
            body = MultipartBody.Part.createFormData("image", file.name, reqBody)
        }


        for (elements in 0 until event.allmembers.size){
            multipartMap["allmembers[$elements][member]"] = RequestBody.create(MediaType.parse("text/plain"), event.allmembers[elements].member)
            multipartMap["allmembers[$elements][email]"] = RequestBody.create(MediaType.parse("text/plain"), event.allmembers[elements].email)
            multipartMap["allmembers[$elements][number]"] = RequestBody.create(MediaType.parse("text/plain"), event.allmembers[elements].number)
        }

        return client.createNewEvent(multipartMap, body)
    }

    fun updateEvent(context: Context, event: UpdateEvent): Call<DefaultResponse> {

        val multipartMap = HashMap<String, RequestBody>()

        val rqEventName = RequestBody.create(MediaType.parse("text/plain"), event.event_name)
        //val rqEventDesc = RequestBody.create(MediaType.parse("text/plain"), event.event_description)
        val rqStreetAddress = RequestBody.create(MediaType.parse("text/plain"), event.streetaddress)
        val rqId = RequestBody.create(MediaType.parse("text/plain"), event.id)
        val rqUserId = RequestBody.create(MediaType.parse("text/plain"), event.userid)
        val rqEventId = RequestBody.create(MediaType.parse("text/plain"), event.eventId)
        val rqState = RequestBody.create(MediaType.parse("text/plain"), event.state)
        val rqZipCode = RequestBody.create(MediaType.parse("text/plain"), event.zipcode)
        val rqCity = RequestBody.create(MediaType.parse("text/plain"), event.city)
        val rqStartDate = RequestBody.create(MediaType.parse("text/plain"), event.startDate)
        val rqStartTime = RequestBody.create(MediaType.parse("text/plain"), event.startTime)
        val rqEndDate = RequestBody.create(MediaType.parse("text/plain"), event.endDate)
        val rqEndTime = RequestBody.create(MediaType.parse("text/plain"), event.endTime)
        val rqHostName = RequestBody.create(MediaType.parse("text/plain"), event.hostname)
        val rqContactInfo = RequestBody.create(MediaType.parse("text/plain"), event.contactInfo)
        val rqHostType = RequestBody.create(MediaType.parse("text/plain"), event.hosttype)
        //val rqEventType = RequestBody.create(MediaType.parse("text/plain"), event.eventtype)
        val rqCountry = RequestBody.create(MediaType.parse("text/plain"), event.country)
        val rqLatitude = RequestBody.create(MediaType.parse("text/plain"), event.latitude)
        val rqLongitude = RequestBody.create(MediaType.parse("text/plain"), event.longitude)
        val rqCategory = RequestBody.create(MediaType.parse("text/plain"), event.category)

        multipartMap["event_name"] = rqEventName
        //multipartMap["event_description"] = rqEventDesc
        multipartMap["streetaddress"] = rqStreetAddress
        multipartMap["city"] = rqCity
        multipartMap["id"] = rqId
        multipartMap["state"] = rqState
        multipartMap["zipcode"] = rqZipCode
        multipartMap["startdate"] = rqStartDate
        multipartMap["starttime"] = rqStartTime
        multipartMap["enddate"] = rqEndDate
        multipartMap["endtime"] = rqEndTime
        multipartMap["hostname"] = rqHostName
        multipartMap["contactinfo"] = rqContactInfo
        multipartMap["hosttype"] = rqHostType
        multipartMap["country"] = rqCountry
        multipartMap["latitude"] = rqLatitude
        multipartMap["longitude"] = rqLongitude
        multipartMap["category"] = rqCategory
        multipartMap["userid"] = rqUserId
        multipartMap["eventid"] = rqEventId

        Log.e("TAG", "multipart: $multipartMap")

        return client.editEvent(multipartMap)
    }

    fun getAllCountries(): Call<Countries> = client.getCountries()

    fun getAllStatesOfTheCountry(countryName: String): Call<States> = client.getStates(countryName)

    fun getAttendEvents(currentDate: String, endTime: String): Call<AttendEvents> = client.attendEvents(
        SessionManager.instance.getUserId(),
        currentDate, endTime
    )

    fun getInvitedEvents(currentDate: String, endTime: String): Call<InvitedEvents> = client.invitedEvents(
        SessionManager.instance.getUserId(),
        currentDate, endTime
    )

    fun getInterestEvents(currentDate: String, endTime: String): Call<InterestEvents> = client.interestEvents(
        SessionManager.instance.getUserId(),
        currentDate, endTime
    )

    fun getMyEvents(currentDate: String, endTime: String?): Call<MyEvents> {
        val userId = SessionManager.instance.getUserId()
        return client.myEvents(userId, currentDate, endTime!!)
    }

    fun getNearestEvents(deviceId: String, latitude: String, longitude: String, category: String, searchType: String,
                         currentDate: String, endTime: String): Call<NearestEvents> {
        val deviceType = DEVICE_TYPE
        val deviceToken = SessionManager.instance.getUserToken()
        return client.nearestEvents(deviceId, deviceType, deviceToken, latitude, longitude, category,
            searchType, currentDate, endTime)
    }

    fun searchEvent(searchEvents: SearchByEvent): Call<SearchEvents> {
        val userId = SessionManager.instance.getUserId()
        return client.searchEvents(userId,
            searchEvents.searchType!!, searchEvents.country!!, searchEvents.state!!,
        searchEvents.city!!, searchEvents.startDate, searchEvents.endDate, searchEvents.startTime,
        searchEvents.endTime, searchEvents.distanceMile, searchEvents.nationWide, searchEvents.latitude,
            searchEvents.longitude, searchEvents.category, searchEvents.zipcode)
    }

    fun eventAddToInterested(eventId: String, hostId: String): Call<AddInterestEvents> = client.addInterestEvents(
        eventId, hostId, SessionManager.instance.getUserId())

    fun eventAddToAttend(eventId: String, hostId: String): Call<AddAttendEvent> = client.addAttendEvents(
        eventId, hostId, SessionManager.instance.getUserId())

    fun getNearEvents(nearEvents: GetNearEvents): Call<NearEvents> = client.nearEvents(
        nearEvents.currentDate, nearEvents.distanceInMile, nearEvents.searchType,
        nearEvents.city, nearEvents.lat, nearEvents.lon, nearEvents.endTime, nearEvents.category,
        nearEvents.id, nearEvents.nationWide
    )

    fun deleteMyEvents(eventId: String, userId: String): Call<DefaultResponse> = client.deleteMyEvents(eventId, userId)

    fun deleteOtherEvents(eventId: String, userId: String, eventType: String): Call<DefaultResponse> =
        client.deleteOtherEvents(eventId, userId, eventType)

    fun referFriendNow(id: String, name: String, email: String, number: String):
            Call<DefaultResponse> = client.referFriend(id,name,email,number)
}