package com.example.movieapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.databinding.CreditItemBinding
import com.example.movieapp.model.Person
import com.example.movieapp.utils.Constant.Companion.IMAGE_URL

class CreditAdapter(
    private val personList: ArrayList<Person>,
    private val onClick: (Person)->Unit
    ): RecyclerView.Adapter<CreditAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        return Holder(CreditItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val person = personList[position]
        holder.binding.cast1Name.text = person.name
        Glide.with(holder.itemView.context)
            .asBitmap()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .load("$IMAGE_URL/w185${person.profilePath}")
            .into(holder.binding.cast1Img)

        holder.binding.root.setOnClickListener {
            onClick.invoke(person)
        }
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    inner class Holder(val binding: CreditItemBinding) : RecyclerView.ViewHolder(binding.root)
}