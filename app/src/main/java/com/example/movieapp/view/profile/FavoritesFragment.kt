package com.example.movieapp.view.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.adapter.FavoritesVPAdapter
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.databinding.FragmentFavoritesBinding
import com.example.movieapp.utils.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayout


class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFavoritesBinding
        get() = {
            layoutInflater, viewGroup, b ->
            FragmentFavoritesBinding.inflate(layoutInflater, viewGroup, b)
        }
    private lateinit var adapter: FavoritesVPAdapter

    override fun setView() {
        adapter = FavoritesVPAdapter(this)

        binding.favoritesViewPager.adapter = adapter

        binding.favoritesViewPager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun setEvent() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    binding.favoritesViewPager.currentItem = it
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.favoritesViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

}