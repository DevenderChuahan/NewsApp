package com.example.newsapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.newsapp.data.models.NewsModel
import com.example.newsapp.data.network.NewsApiDataResult
import com.example.newsapp.utils.utils.TAG
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL
import javax.inject.Inject

class NewsApiData @Inject constructor() {
    private val _newsApiData = MutableLiveData<NewsApiDataResult<MutableList<NewsModel>>>()
    val newApiLiveData: LiveData<NewsApiDataResult<MutableList<NewsModel>>> get() = _newsApiData
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

                    //  parsing logic
                    val jsonObject = JSONObject(response.toString())
                    val articles = jsonObject.getJSONArray("articles")

                    /** make a list to hold the required data only **/
                    val newsDataList = mutableListOf<NewsModel>()
                    for (i in 0 until articles.length()) {
                        val source = articles.getJSONObject(i).getJSONObject("source")
                        val id = source.getString("id")
                        val name = source.getString("name")

                        val author = articles.getJSONObject(i).getString("author")
                        val title = articles.getJSONObject(i).getString("title")
                        val description = articles.getJSONObject(i).getString("description")
                        val url = articles.getJSONObject(i).getString("url")
                        val urlToImage = articles.getJSONObject(i).getString("urlToImage")
                        val publishedAt = articles.getJSONObject(i).getString("publishedAt")
                        newsDataList.add(
                            NewsModel(
                                author,
                                title,
                                description,
                                url,
                                urlToImage,
                                publishedAt
                            )
                        )
                        val content = articles.getJSONObject(i).getString("content")
                        Log.d(TAG,"id: $id, name: $name, author: $author, title: $title, description: $description, url: $url, urlToImage: $urlToImage, publishedAt: $publishedAt, content: $content")
                    }
                    _newsApiData.postValue(NewsApiDataResult.Success(newsDataList))

                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception ${e.localizedMessage}")
                _newsApiData.postValue(
                    NewsApiDataResult.Error(
                        e.localizedMessage?.toString() ?: "Unknown Error"
                    )
                )
            }
            connection.disconnect()
        } else {
            Log.d(TAG, "Response failed , responseCode $responseCode")
            _newsApiData.postValue(NewsApiDataResult.Error(responseCode.toString()))
        }
    }


}