package com.example.newsapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.network.NetworkConnectivityObserver
import com.example.newsapp.data.repository.NewsApiData
import com.example.newsapp.utils.utils.BASE_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val newsApiData: NewsApiData
) :
    ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            newsApiData.getNews(BASE_URL)

        }
    }

    val networkStatus = networkConnectivityObserver
    val newsApiLiveData = newsApiData.newApiLiveData


}