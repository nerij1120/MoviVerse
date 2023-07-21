package com.example.movieapp.adapter

import android.graphics.Bitmap
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.movieapp.R
import com.example.movieapp.databinding.EmptyMovieBinding
import com.example.movieapp.databinding.MovieItemHomeBinding
import com.example.movieapp.model.Movie
import com.example.movieapp.utils.Constant.Companion.IMAGE_URL
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur

class MovieAdapter(private val moviesList: ArrayList<Movie>, private val onClick:(Movie)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        const val EMPTY = 0
        const val NORMAL = 1
        const val radius = 4f
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if(viewType == EMPTY)
            return Empty(EmptyMovieBinding.inflate(inflater, parent,false))
        return Holder(MovieItemHomeBinding.inflate(inflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0 || position == moviesList.size - 1)
            return EMPTY
        return NORMAL
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == EMPTY)
        {
            return
        }
        else{
            val item = moviesList[position]
            val vh = holder as Holder
            vh.binding.movieTitle.text = item.title
            vh.binding.movieRating.text = "${item.voteAverage}"
            Glide.with(vh.itemView.context)
                .asBitmap()
                .error(R.drawable.ic_movie_error)
                .load("$IMAGE_URL/w342${item.posterPath}")
                .listener(object: RequestListener<Bitmap>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                            vh.binding.titleBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                            vh.binding.titleBlurView.clipToOutline = true
                            vh.binding.titleBlurView.setupWith(vh.binding.root, RenderEffectBlur())
                                .setBlurRadius(radius)
                            vh.binding.ratingBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                            vh.binding.ratingBlurView.clipToOutline = true
                            vh.binding.ratingBlurView.setupWith(vh.binding.root, RenderEffectBlur())
                                .setBlurRadius(radius)
                        }else{
                            vh.binding.titleBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                            vh.binding.titleBlurView.clipToOutline = true
                            vh.binding.titleBlurView.setupWith(vh.binding.root, RenderScriptBlur(vh.itemView.context))
                                .setBlurRadius(radius)
                            vh.binding.ratingBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                            vh.binding.ratingBlurView.clipToOutline = true
                            vh.binding.ratingBlurView.setupWith(vh.binding.root, RenderScriptBlur(vh.itemView.context))
                                .setBlurRadius(radius)
                        }
                        return false
                    }
                })
                .into(vh.binding.moviePoster)

            vh.binding.root.setOnClickListener {
                onClick.invoke(item)
            }
        }

    }


    override fun getItemCount(): Int {
        return moviesList.size
    }

    inner class Holder(val binding: MovieItemHomeBinding): RecyclerView.ViewHolder(binding.root)
    inner class Empty(binding: EmptyMovieBinding): RecyclerView.ViewHolder(binding.root)
}