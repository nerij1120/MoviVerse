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
import com.example.moviverse.databinding.CreditItemBinding
import com.example.moviverse.model.Person
import com.example.moviverse.utils.Constant.Companion.IMAGE_URL

class CreditAdapter(
    var personList: ArrayList<Person>,
    val onClick: (Person, View, View)->Unit,
    private val fragmentNumber: Int
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
            .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(holder.itemView.context))))
            .into(holder.binding.cast1Img)

        holder.binding.cast1Img.transitionName = "person_poster_${person.id}_$fragmentNumber"
        holder.binding.cast1Name.transitionName = "person_name_${person.id}_$fragmentNumber"

        holder.binding.root.setOnClickListener {
            onClick.invoke(person, holder.binding.cast1Img, holder.binding.cast1Name)
        }
    }

    private fun dipToPx(context: Context): Int {
        return (24f * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    class Holder(val binding: CreditItemBinding) : RecyclerView.ViewHolder(binding.root)
}