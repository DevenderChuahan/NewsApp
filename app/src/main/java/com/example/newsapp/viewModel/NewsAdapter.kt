package com.example.newsapp.viewModel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.data.models.NewsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class NewsAdapter(
    private val list: MutableList<NewsModel> = ArrayList<NewsModel>(),
    private val context: Context
) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsAdapter.NewsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_layout, parent, false)
        return NewsViewHolder(view)
    }

    fun updateList(list: MutableList<NewsModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: NewsAdapter.NewsViewHolder, position: Int) {
        try {
            holder.header.text = list[position].title
            holder.description.text = list[position].description

            if(list[position].author!="null")
            {
                holder.auther.text = list[position].author
            }



            holder.date.text = list[position].publishedAt

            CoroutineScope(Dispatchers.IO).launch {
                try {
                val url = URL(list[position].urlToImage)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (bitmap != null) {
                    val resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                   withContext(Dispatchers.Main)
                   {
                       holder.image.setImageBitmap(resized)
                   }

                }

                }catch (e:Exception)
                {
                    Log.d("TAG >>",e.message.toString())
                    withContext(Dispatchers.Main)
                    {
                        holder.image.setImageResource(R.drawable.baseline_broken_image_24)
                    }

                }

                withContext(Dispatchers.Main)
                {
//                    holder.header.paint?.isUnderlineText=true
                    holder.header.paintFlags = holder.header.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
                    holder.header.setOnClickListener {
                        val intent= Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(list[position].url))
                        context.startActivity(intent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("TAG >>", e.message.toString())
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header = itemView.findViewById<TextView>(R.id.tv_header)
        val description = itemView.findViewById<TextView>(R.id.tv_description)
        val auther = itemView.findViewById<TextView>(R.id.tv_authername)
        val date = itemView.findViewById<TextView>(R.id.tv_timestamp)
        val image = itemView.findViewById<ImageView>(R.id.imageView)
    }
}