package com.example.movieapp.view.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.movieapp.R
import com.example.movieapp.adapter.ViewPagerAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentDiscoveryBinding
import com.example.movieapp.utils.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator

class DiscoveryFragment : BaseFragment<FragmentDiscoveryBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDiscoveryBinding
        get() = {
            inflater, container, attach ->
            FragmentDiscoveryBinding.inflate(inflater, container, attach)
        }

    override fun setView() {
        binding.viewPager.adapter = ViewPagerAdapter(this)
        binding.viewPager.apply {
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        //not optimized
        //binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.setPageTransformer(ZoomOutPageTransformer())

        val listTitle  = listOf("MOVIES", "TV SHOWS", "ACTION", "COMEDY", "HORROR", "ROMANCE", "ANIMATION")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = listTitle[position]
        }.attach()
    }

    override fun setEvent() {
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
    }
}