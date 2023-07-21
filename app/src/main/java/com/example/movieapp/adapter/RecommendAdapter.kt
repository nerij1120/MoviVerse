package com.example.movieapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemRecommendBinding
import com.example.movieapp.model.Item
import com.example.movieapp.model.Movie
import com.example.movieapp.model.TvShows
import com.example.movieapp.utils.Constant

class RecommendAdapter(private val moviesList: ArrayList<Item>? = null, private val onClick:(Item)->Unit): RecyclerView.Adapter<RecommendAdapter.RecommendVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendVH {
        return RecommendVH(
            ItemRecommendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecommendVH, position: Int) {
        val item = moviesList?.get(position)
        if (holder.itemViewType == 1){
            when(item){
                is Movie ->{
                    Glide.with(holder.itemView.context)
                        .load("${Constant.IMAGE_URL}/w300${item.backdropPath}")
                        .error(R.drawable.ic_movie_error)
                        .into(holder.binding.moviePoster)
                }
                is TvShows ->{
                    Glide.with(holder.itemView.context)
                        .load("${Constant.IMAGE_URL}/w300${item.backdropPath}")
                        .error(R.drawable.ic_movie_error)
                        .into(holder.binding.moviePoster)
                }
            }

            holder.binding.root.setOnClickListener {
                if (item != null) {
                    onClick.invoke(item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return moviesList?.size?:0
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 || position == moviesList?.size?.minus(1))
            return 0
        return 1
    }

    inner class RecommendVH(val binding: ItemRecommendBinding): RecyclerView.ViewHolder(binding.root)
}