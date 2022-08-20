package com.example.moviverse.view.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.moviverse.R
import com.example.moviverse.databinding.FragmentDiscoveryBinding


class DiscoveryFragment : Fragment() {
    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)



        binding.searchLayout.setOnClickListener {
            val extras = FragmentNavigatorExtras(
                it to "search_box"
            )
            findNavController().navigate(
                R.id.action_discoveryFragment_to_searchFragment,
                null,
                null,
                extras
            )
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}