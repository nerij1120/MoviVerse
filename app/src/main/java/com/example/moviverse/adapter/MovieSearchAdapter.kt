package com.example.moviverse.adapter

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
import com.example.moviverse.model.Movie
import com.example.moviverse.utils.Constant.Companion.IMAGE_URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MovieSearchAdapter(val moviesList: ArrayList<Movie>, val onClick:(Movie, View, View)->Unit, private val fragmentNumber: Int): RecyclerView.Adapter<MovieSearchAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(MovieItemDiscoveryBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = moviesList[position]
        val date = item.releaseDate
        val format = SimpleDateFormat("yyyy", Locale.getDefault())
        val fm = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val txt = date.let { it1 -> fm.parse(it1)?.let { t2 -> format.format(t2) } }
            holder.binding.movieTitle.text = "${item.title} (${txt})"
        }
        catch (e: ParseException){
            e.printStackTrace()
        }
        Glide.with(holder.itemView.context)
            .asBitmap()
            .placeholder(R.drawable.ic_movie_icon)
            .error(R.drawable.ic_movie_error)
            .load("$IMAGE_URL/w342${item.posterPath}")
            .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(holder.itemView.context))))
            .into(holder.binding.moviePoster)

        holder.binding.moviePoster.transitionName = "movie_poster_${item.id}_${fragmentNumber}"
        holder.binding.movieTitle.transitionName = "movie_title_${item.id}_${fragmentNumber}"

        holder.binding.root.setOnClickListener {
            onClick.invoke(item, holder.binding.moviePoster, holder.binding.movieTitle)
        }
    }

    private fun dipToPx(context: Context): Int {
        return (36f * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    class Holder(val binding: MovieItemDiscoveryBinding): RecyclerView.ViewHolder(binding.root)
}