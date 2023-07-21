package com.example.movieapp.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.base.BaseActivity
import com.example.movieapp.databinding.ActivityPersonBinding
import com.example.movieapp.utils.Constant
import com.example.movieapp.viewmodel.DetailsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

class PersonActivity : BaseActivity<ActivityPersonBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityPersonBinding
        get() = {
            ActivityPersonBinding.inflate(it)
        }
    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSupportActionBar(binding.toolbar)
//        binding.toolbarLayout.title = title

        intent?.extras?.let {
            val id = it.getInt("id")
            viewModel.getPersonDetails(id)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.personDetails.observe(this){
            if(it.isSuccessful){
                val person = it.body()
                if(person != null){
                    Glide.with(this)
                        .asBitmap()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .load("${Constant.IMAGE_URL}/original${person.profilePath}")
                        .into(binding.backdropImage)
                    binding.toolbarLayout.title = person.name
                    binding.knownTxt.text = person.knownFor
                    binding.overviewTxt.text = person.biography
                    if(person.placeOfBirth != null)
                        binding.placeOfBirthTxt.text = person.placeOfBirth
                    else
                        binding.placeOfBirthTxt.text = getString(R.string.unknown)
                    val format = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                    val fm = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    if(person.birthday != null && person.deathDay != null){
                        val birthDate = fm.parse(person.birthday)?.let { it1 -> format.format(it1) }
                        val deathDate = fm.parse(person.deathDay)?.let { it1 -> format.format(it1) }
                        binding.birthDeathDate.text = "$birthDate - $deathDate"
                    }
                    else{
                        var birthDate = getString(R.string.unknown)
                        var deathDate = ""
                        if(person.birthday != null)
                            birthDate =
                                fm.parse(person.birthday)?.let { it1 -> format.format(it1) }.toString()
                        if(person.deathDay != null)
                            deathDate =
                                fm.parse(person.deathDay)?.let { it1 -> format.format(it1) }.toString()
                        binding.birthDeathDate.text = "$birthDate - $deathDate"
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.errorFlow.collectLatest {
                displayError(it)
            }
        }
    }
}