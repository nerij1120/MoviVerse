package com.example.moviverse.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.moviverse.R
import com.example.moviverse.adapter.CreditAdapter
import com.example.moviverse.databinding.FragmentCreditBinding
import com.example.moviverse.model.Person
import com.example.moviverse.viewmodel.DetailsViewModel
import java.util.*

class CreditFragment : Fragment() {
    private val viewModel: DetailsViewModel by activityViewModels()
    private var _binding: FragmentCreditBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CreditAdapter
    private var creditsList: ArrayList<Person> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreditBinding.inflate(layoutInflater)

        adapter = CreditAdapter(ArrayList(), onClick = { person, poster, title->
            viewModel.getPersonDetails(person.id)
            val extras = FragmentNavigatorExtras(
                poster to "person_poster_${person.id}_0",
                title to "person_name_${person.id}_0"
            )
            findNavController().navigate(
                R.id.toDetails,
                null,
                null,
                extras
            )
        }, 0)

        binding.moviesSearchRecView.adapter = adapter
        binding.moviesSearchRecView.apply{
            postponeEnterTransition()
            viewTreeObserver
                .addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
        }
        val layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        binding.moviesSearchRecView.layoutManager = layoutManager

        binding.searchEditTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.personList.clear()
                adapter.notifyDataSetChanged()
                showShimmer()
                binding.noDataImg.visibility = View.GONE
                binding.noDataTxt.visibility = View.GONE
            }

            private val delay: Long = 1000
            private var timer = Timer()
            override fun afterTextChanged(p0: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object: TimerTask(){
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            if(_binding != null){
                                val q = p0?.toString()?.trim()
                                if (q != null && q != "") {
                                    val arrayList: ArrayList<Person> = ArrayList()
                                    for(item in creditsList){
                                        if(item.name.lowercase().contains(q.lowercase())){
                                            arrayList.add(item)
                                        }
                                    }
                                    if(arrayList.size == 0){
                                        binding.noDataImg.visibility = View.VISIBLE
                                        binding.noDataTxt.visibility = View.VISIBLE
                                        if(adapter.personList.size != 0){
                                            adapter.personList.clear()
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                    else{
                                        adapter.personList = arrayList
                                        adapter.notifyDataSetChanged()
                                    }
                                    stopShimmer()
                                }
                                else{
                                    adapter.personList.clear()
                                    adapter.personList.addAll(creditsList)
                                    adapter.notifyDataSetChanged()
                                    stopShimmer()
                                }
                            }
                        }
                    }
                }, delay)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.details.observe(viewLifecycleOwner){
            if(it.isSuccessful){
                val movie = it.body()
                if(movie != null){
                    val credits = movie.credits
                    val cast = credits?.cast
                    val crew = credits?.crew
                    adapter.personList.clear()
                    creditsList.clear()
                    if (cast != null) {
                        creditsList.addAll(cast)
                    }
                    if (crew != null) {
                        creditsList.addAll(crew)
                    }
                    adapter.personList.addAll(creditsList)
                    adapter.notifyDataSetChanged()
                    if(adapter.personList.size == 0){
                        binding.noDataImg.visibility = View.VISIBLE
                        binding.noDataTxt.visibility = View.VISIBLE
                    }
                    else{
                        binding.noDataImg.visibility = View.GONE
                        binding.noDataTxt.visibility = View.GONE
                    }
                    stopShimmer()
                }
            }
            else{
                Toast.makeText(context, it.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                stopShimmer()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showShimmer(){
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
    }

    private fun stopShimmer(){
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
    }
}