package com.example.moviverse.adapter

import android.app.appsearch.SearchResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.moviverse.R
import com.example.moviverse.databinding.MovieItemDiscoveryBinding
import com.example.moviverse.utils.Constant.Companion.IMAGE_URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchAdapter(val searchList: ArrayList<SearchResult>, val onClick:(SearchResult, View, View)->Unit): RecyclerView.Adapter<SearchAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(MovieItemDiscoveryBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = searchList[position]
        val format = SimpleDateFormat("yyyy", Locale.getDefault())
        val fm = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            when(item.type){
                "movie"->{
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .placeholder(R.drawable.ic_movie_icon)
                        .error(R.drawable.ic_movie_error)
                        .load("${IMAGE_URL}/w342${item.posterPath}")
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(36f, holder.itemView.context))))
                        .into(holder.binding.moviePoster)
                    val date = item.releaseDate
                    val txt = date.let { it1 -> fm.parse(it1)?.let { t2 -> format.format(t2) } }
                    holder.binding.movieTitle.text = "${item.title} (${txt})"
                    holder.binding.moviePoster.transitionName = "movie_poster_${item.id}_8"
                    holder.binding.movieTitle.transitionName = "movie_title_${item.id}_8"
                }
                "tv"->{
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .placeholder(R.drawable.ic_movie_icon)
                        .error(R.drawable.ic_movie_error)
                        .load("${IMAGE_URL}/w342${item.posterPath}")
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(36f, holder.itemView.context))))
                        .into(holder.binding.moviePoster)
                    val date = item.firstAirDate
                    val txt = date.let { it1 -> fm.parse(it1)?.let { t2 -> format.format(t2) } }
                    holder.binding.movieTitle.text = "${item.name} (${txt})"
                    holder.binding.moviePoster.transitionName = "tv_poster_${item.id}_8"
                    holder.binding.movieTitle.transitionName = "tv_title_${item.id}_8"
                }
                "person"->{
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .load("${IMAGE_URL}/w185${item.profilePath}")
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(24f, holder.itemView.context))))
                        .into(holder.binding.moviePoster)
                    holder.binding.movieTitle.text = item.name
                    holder.binding.moviePoster.transitionName = "person_poster_${item.id}_8"
                    holder.binding.movieTitle.transitionName = "person_name_${item.id}_8"
                }
            }
        }
        catch (e: ParseException){
            e.printStackTrace()
        }

        holder.binding.root.setOnClickListener {
            onClick.invoke(item, holder.binding.moviePoster, holder.binding.movieTitle)
        }
    }

    private fun dipToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    class Holder(val binding: MovieItemDiscoveryBinding): RecyclerView.ViewHolder(binding.root)
}