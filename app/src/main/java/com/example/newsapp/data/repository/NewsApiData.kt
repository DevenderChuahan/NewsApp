package com.example.newsapp.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.newsapp.data.network.NewsApiDataResult
import com.example.newsapp.utils.utils.TAG
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import javax.inject.Inject

class NewsApiData @Inject constructor() {
    private val _newsApiData = MutableLiveData<NewsApiDataResult<String>>()

    val newApiLiveData: MutableLiveData<NewsApiDataResult<String>> get() = _newsApiData

    fun getNews(baseUrl: String) {
        _newsApiData.postValue(NewsApiDataResult.Loading())

        val connection = URL(baseUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.readTimeout = 10000
        connection.connectTimeout = 10000
        connection.doInput = true
        val responseCode = connection.responseCode
        Log.d(TAG, "responseCode $responseCode")
        if (responseCode == HTTP_OK) {
            try {
                val reader = InputStreamReader(connection.inputStream)
                reader.use { it ->
                    val response = StringBuilder()
                    val bufferReader = BufferedReader(it)
                    bufferReader.forEachLine {
                        response.append(it.trim())
                    }



                    _newsApiData.postValue(NewsApiDataResult.Success(response.toString()))

                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception ${e.localizedMessage}")
                _newsApiData.postValue(NewsApiDataResult.Error(e.localizedMessage?.toString() ?:"Unknown Error" ))
            }
            connection.disconnect()
        } else {
            Log.d(TAG, "Response failed , responseCode $responseCode")

            _newsApiData.postValue(NewsApiDataResult.Error(responseCode.toString()))
        }
    }


}