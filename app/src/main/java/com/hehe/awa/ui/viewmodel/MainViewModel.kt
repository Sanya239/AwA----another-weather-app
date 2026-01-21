package com.hehe.awa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hehe.awa.data.Friend
import com.hehe.awa.data.FriendRequest
import com.hehe.awa.data.FriendRequestRepository
import com.hehe.awa.data.FriendsRepository
import com.hehe.awa.data.UpdateResult
import com.hehe.awa.data.UserProfile
import com.hehe.awa.data.UserProfileRepository
import com.hehe.awa.data.Weather
import com.hehe.awa.data.WeatherRepository
import com.hehe.awa.data.UserWeatherRepository
import com.hehe.awa.data.getCurrentLocation
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val profileRepository = UserProfileRepository()
    private val friendRequestRepository = FriendRequestRepository()
    private val friendsRepository = FriendsRepository()
    private val weatherRepository = WeatherRepository()
    private val userWeatherRepository = UserWeatherRepository()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather.asStateFlow()

    private val _requests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val requests: StateFlow<List<FriendRequest>> = _requests.asStateFlow()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _requestUserNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val requestUserNames: StateFlow<Map<String, String>> = _requestUserNames.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun loadProfile(uid: String, fallbackName: String?) {
        viewModelScope.launch {
            _profile.value = profileRepository.getOrCreate(uid, fallbackName)
        }
    }

    fun saveProfile(uid: String, newProfile: UserProfile) {
        viewModelScope.launch {
            val result = profileRepository.save(uid, newProfile)
            if (result is UpdateResult.Success) {
                _profile.value = newProfile
            }
        }
    }

    fun updateTag(uid: String, newTag: String) {
        viewModelScope.launch {
            profileRepository.updateTag(uid, newTag)
        }
    }

    private suspend fun loadRequests(uid: String) {
        val loadedRequests = friendRequestRepository.getPendingRequests(uid)
        _requests.value = loadedRequests

        val namesMap = mutableMapOf<String, String>()
        loadedRequests.forEach { request ->
            val otherUid = if (request.fromUid == uid) request.toUid else request.fromUid
            if (!namesMap.containsKey(otherUid)) {
                profileRepository.getUserName(otherUid)?.let { userName ->
                    namesMap[otherUid] = userName
                }
            }
        }
        _requestUserNames.value = namesMap
    }

    private suspend fun loadFriends(uid: String) {
        _friends.value = friendsRepository.getFriends(uid)
    }

    fun refreshData(uid: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                loadRequests(uid)
                loadFriends(uid)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createRequest(uid: String, toUid: String) {
        viewModelScope.launch {
            val result = friendRequestRepository.createRequest(uid, toUid)
            if (result is UpdateResult.Success) {
                loadRequests(uid)
            }
        }
    }

    fun acceptRequest(uid: String, requestId: String) {
        viewModelScope.launch {
            val result = friendRequestRepository.acceptRequest(requestId)
            if (result is UpdateResult.Success) {
                loadRequests(uid)
                loadFriends(uid)
            }
        }
    }

    fun rejectRequest(uid: String, requestId: String) {
        viewModelScope.launch {
            val result = friendRequestRepository.rejectRequest(requestId)
            if (result is UpdateResult.Success) {
                loadRequests(uid)
            }
        }
    }

    fun removeFriend(uid: String, friendUid: String) {
        viewModelScope.launch {
            val result = friendRequestRepository.removeFriend(uid, friendUid)
            if (result is UpdateResult.Success) {
                loadFriends(uid)
            }
        }
    }

    fun getUserName(uid: String): String? {
        return null
    }

    suspend fun getUserNameSuspend(uid: String): String? {
        return profileRepository.getUserName(uid)
    }

    fun loadWeather(context: Context, uid: String) {
        viewModelScope.launch {
            val userLocation = getCurrentLocation(context)
            if (userLocation != null) {
                val weather = weatherRepository.getCurrentWeather(
                    userLocation.latitude,
                    userLocation.longitude
                )
                _weather.value = weather

                val profile = _profile.value
                if (weather != null && profile != null && !profile.isPrivate) {
                    userWeatherRepository.saveUserWeather(uid, weather)
                    loadFriends(uid)
                }
            }
        }
    }

    fun clearData() {
        _profile.value = null
        _requests.value = emptyList()
        _friends.value = emptyList()
        _requestUserNames.value = emptyMap()
        _weather.value = null
    }
}

