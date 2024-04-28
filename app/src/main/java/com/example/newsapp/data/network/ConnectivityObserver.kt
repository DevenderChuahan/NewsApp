package com.example.newsapp.data.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observer():Flow<Status>

    enum class Status{
        Available, Unavailable, Losing, Lost
    }
}