package com.example.moviverse.view

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.moviverse.R
import com.example.moviverse.databinding.FragmentPersonDetailsBinding
import com.example.moviverse.utils.Constant.Companion.IMAGE_URL
import com.example.moviverse.viewmodel.DetailsViewModel
import eightbitlab.com.blurview.RenderScriptBlur
import java.text.SimpleDateFormat
import java.util.*


class PersonDetailsFragment : Fragment() {
    private var _binding: FragmentPersonDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsViewModel by activityViewModels()
    private var isShowing = false
    private val navArgs: PersonDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = TransitionInflater.from(context).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPersonDetailsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.dropdownLayout.setOnClickListener {
            if(isShowing){
                isShowing = false
                binding.dropdownIcon.setImageDrawable(context?.let { it1 ->
                    AppCompatResources.getDrawable(
                        it1, R.drawable.ic_baseline_arrow_drop_down_24
                    )
                })
                binding.overviewTxt.maxLines = 3
            }
            else{
                isShowing = true
                binding.dropdownIcon.setImageDrawable(context?.let { it1 ->
                    AppCompatResources.getDrawable(
                        it1, R.drawable.ic_baseline_arrow_drop_up_24
                    )
                })
                binding.overviewTxt.maxLines = Int.MAX_VALUE
            }
        }

        viewModel.personDetails.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                val person = it.body()
                if(person != null){
                    val fragmentNumber = navArgs.fragmentNumber
                    binding.profilePic.transitionName = "person_poster_${person.id}_$fragmentNumber"
                    binding.castName.transitionName = "person_name_${person.id}_$fragmentNumber"

                    Log.e("observe", person.name)

                    context?.let { it1 ->
                        Glide.with(it1)
                            .asBitmap()
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .load("$IMAGE_URL/original${person.profilePath}")
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(dipToPx(it1))))
                            .listener(object : RequestListener<Bitmap> {
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
                                    Handler(Looper.myLooper()!!).postDelayed(
                                        {
                                            if(_binding != null){
                                                binding.backBtnBlurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                                                binding.backBtnBlurView.clipToOutline = true
                                                binding.backBtnBlurView.setupWith(binding.constraintLayout, RenderScriptBlur(context))
                                                    .setBlurRadius(1f)
                                                    .setBlurAutoUpdate(true)
                                            }
                                        },500
                                    )
                                    return false
                                }
                            })
                            .into(binding.profilePic)
                    }
                    binding.castName.text = person.name
                    binding.knownTxt.text = person.knownFor
                    binding.overviewTxt.text = person.biography
                    Log.e("lines", "${binding.overviewTxt.lineCount}")
                    if(binding.overviewTxt.lineCount > 3){
                        binding.dropdownLayout.visibility = View.VISIBLE
                        binding.overviewTxt.maxLines = 3
                    }
                    else{
                        binding.dropdownLayout.visibility = View.GONE
                    }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dipToPx(context: Context): Int {
        return (1f * context.resources.displayMetrics.density).toInt()
    }

}