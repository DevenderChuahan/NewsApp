package com.example.newsapp.data.network

sealed class NewsApiDataResult<T> {
    data class Success<T>(val data: T) : NewsApiDataResult<T>()
    data class Error<T>(val error: T) : NewsApiDataResult<T>()
     class Loading<T> : NewsApiDataResult<T>()


}