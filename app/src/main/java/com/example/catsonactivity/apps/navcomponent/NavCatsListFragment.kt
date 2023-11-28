package com.example.catsonactivity.apps.navcomponent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catsonactivity.CatsAdapterListener
import com.example.catsonactivity.R
import com.example.catsonactivity.catsAdapter
import com.example.catsonactivity.databinding.FragmentsCatsBinding
import com.example.catsonactivity.viewmodel.CatListItem
import com.example.catsonactivity.viewmodel.base.CatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavCatsListFragment : Fragment(R.layout.fragments_cats),
    CatsAdapterListener {

    private val viewModel by viewModels<CatsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentsCatsBinding.bind(view)

        val adapter = catsAdapter(this)
        binding.catsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        (binding.catsRecyclerView.itemAnimator as? DefaultItemAnimator)
            ?.supportsChangeAnimations = false
        binding.catsRecyclerView.adapter = adapter
        viewModel.catsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onCatDelete(cat: CatListItem.Cat) {
        viewModel.deleteCat(cat)
    }

    override fun onCatToggleFavorite(cat: CatListItem.Cat) {
        viewModel.toggleFavorite(cat)
    }

    override fun onCatChosen(cat: CatListItem.Cat) {
        val direction = NavCatsListFragmentDirections
            .actionNavCatsListFragmentToNavCatDetailsFragment(cat.id)
        findNavController().navigate(direction)
    }

}