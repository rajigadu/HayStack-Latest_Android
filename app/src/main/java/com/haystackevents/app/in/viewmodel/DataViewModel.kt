package com.haystackevents.app.`in`.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haystackevents.app.`in`.network.response.event.Event

class DataViewModel: ViewModel() {


    var eventsData: Event? = Event()
}