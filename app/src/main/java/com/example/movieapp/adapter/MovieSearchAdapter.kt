package com.example.movieapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.databinding.MovieItemDiscoveryBinding
import com.example.movieapp.model.Movie
import com.example.movieapp.utils.Constant.Companion.IMAGE_URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class MovieSearchAdapter(private val moviesList: ArrayList<Movie>, val onClick:(Movie)->Unit): RecyclerView.Adapter<MovieSearchAdapter.Holder>() {
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
            .into(holder.binding.moviePoster)

        holder.binding.root.setOnClickListener {
            onClick.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return moviesList.size
    }

    class Holder(val binding: MovieItemDiscoveryBinding): RecyclerView.ViewHolder(binding.root)
}