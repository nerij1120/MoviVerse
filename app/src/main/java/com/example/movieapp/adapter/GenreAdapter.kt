package com.example.movieapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.databinding.GenreItemBinding
import com.example.movieapp.model.Genre

class GenreAdapter(private val genreList: ArrayList<Genre>): RecyclerView.Adapter<GenreAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(GenreItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = genreList[position]
        holder.binding.genreName.text = item.name
    }

    override fun getItemCount(): Int {
        return genreList.size
    }

    class Holder(val binding: GenreItemBinding): RecyclerView.ViewHolder(binding.root)
}