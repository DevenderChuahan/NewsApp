package com.example.newsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.models.NewsModel
import com.example.newsapp.data.network.NewsApiDataResult
import com.example.newsapp.utils.utils.TAG
import com.example.newsapp.viewModel.NewsAdapter
import com.example.newsapp.viewModel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var pBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var nestedRv: NestedScrollView
    private lateinit var jsonString: String
    lateinit var newsAdapter: NewsAdapter
    private lateinit var toolbar: Toolbar
    private val newsDataList = mutableListOf<NewsModel>()


    var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nestedRv)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        pBar = findViewById<ProgressBar>(R.id.progressBar)
        recyclerView = findViewById<RecyclerView>(R.id.rv)
        nestedRv = findViewById<NestedScrollView>(R.id.nestedRv)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsViewModel.networkStatus.observer().collect() {

                    when (it.name) {
                        "Available" -> {
                        }

                        "Unavailable" -> {
                            pBar.visibility = View.GONE
                            Snackbar.make(
                                findViewById(R.id.main),
                                "Network ${it.name}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        "Losing" -> {
                            pBar.visibility = View.GONE
                            Snackbar.make(
                                findViewById(R.id.main),
                                "Network ${it.name}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        "Lost" -> {
                            pBar.visibility = View.GONE
                            Snackbar.make(
                                findViewById(R.id.main),
                                "Network ${it.name}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }

        newsViewModel.newsApiLiveData.observe(this@MainActivity) {

            when (it) {

                is NewsApiDataResult.Loading -> {
                    pBar.visibility = View.VISIBLE
                }
                is NewsApiDataResult.Success -> {
                    Log.d(TAG, "onCreate: ${it.data}")
                    jsonString = it.data
                    parseJson(it.data, page)
                }
                is NewsApiDataResult.Error -> {
                    pBar.visibility = View.GONE
                    Log.d(TAG, "onCreate: ${it.error}")

                }

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.new_item -> {
                Log.d(TAG, "Original list: ${newsDataList}")

                val nList = newsDataList.sortedBy {
                    it.publishedAt
                }

                newsAdapter.updateList(nList.toMutableList())

                Log.d(TAG, "New list: ${nList}")

            }

            R.id.old_item -> {
                val oList = newsDataList.sortedByDescending {
                    it.publishedAt
                }
                newsAdapter.updateList(oList.toMutableList())

                Log.d(TAG, "Old list: ${oList}")
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun parseJson(it: String, page: Int) {
        lifecycleScope.launch {
            //  parsing logic
            val jsonObject = JSONObject(it)
            val articles = jsonObject.getJSONArray("articles")

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

                Log.d(
                    TAG,
                    "id: $id, name: $name, author: $author, title: $title, description: $description, url: $url, urlToImage: $urlToImage, publishedAt: $publishedAt, content: $content"
                )


            }
//            startingIndex = pageSize * 10

            withContext(Dispatchers.Main)
            {

                recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 1)
                recyclerView.setHasFixedSize(true)
                newsAdapter = NewsAdapter(newsDataList, this@MainActivity)
                recyclerView.adapter = newsAdapter
//            newsAdapter.notifyDataSetChanged()
                pBar.visibility = View.GONE
            }


        }
    }
}
