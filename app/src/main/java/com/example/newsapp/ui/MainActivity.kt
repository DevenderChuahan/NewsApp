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
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var pBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var nestedRv: NestedScrollView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var toolbar: Toolbar
    private var newsDataList = mutableListOf<NewsModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nestedRv)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        pBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.rv)
        nestedRv = findViewById(R.id.nestedRv)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        newsViewModel = ViewModelProvider(this)[NewsViewModel::class.java]
         /**   check Network status  **/
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    newsViewModel.networkStatus.observer().collect {

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

                } catch (e: Exception) {
                    e.localizedMessage?.let { Log.d(TAG, it) }
                }
            }

        }

        /**   fetch data from api   **/
        newsViewModel.newsApiLiveData.observe(this@MainActivity) {

            when (it) {
                is NewsApiDataResult.Loading -> {
                    pBar.visibility = View.VISIBLE
                }
                is NewsApiDataResult.Success -> {
                    Log.d(TAG, "onCreate: ${it.data}")
                       newsDataList=it.data as MutableList<NewsModel>
                        recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 1)
                        recyclerView.setHasFixedSize(true)
                        newsAdapter = NewsAdapter(newsDataList, this@MainActivity)
                        recyclerView.adapter = newsAdapter
                        pBar.visibility = View.GONE
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
                Log.d(TAG, "Original list: $newsDataList")
                val nList = newsDataList.sortedBy {
                    it.publishedAt
                }
                newsAdapter.updateList(nList.toMutableList())
                Log.d(TAG, "New list: $nList")
            }

            R.id.old_item -> {
                val oList = newsDataList.sortedByDescending {
                    it.publishedAt
                }
                newsAdapter.updateList(oList.toMutableList())
                Log.d(TAG, "Old list: $oList")
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
