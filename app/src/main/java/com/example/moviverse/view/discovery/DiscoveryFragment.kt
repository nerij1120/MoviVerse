package com.example.moviverse.view.discovery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.moviverse.R
import com.example.moviverse.adapter.ViewPagerAdapter
import com.example.moviverse.databinding.FragmentDiscoveryBinding
import com.example.moviverse.utils.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayout


class DiscoveryFragment : Fragment() {
    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)

        viewPagerAdapter = activity?.supportFragmentManager?.let { ViewPagerAdapter(it, lifecycle) }!!

        binding.viewPager.adapter = viewPagerAdapter
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

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position!!
                Log.e("tabLayout", "${tab.position}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
                Log.e("viewpager", "$position")
            }
        })


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