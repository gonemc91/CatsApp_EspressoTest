package com.example.catsonactivity.apps.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catsonactivity.CatsAdapterListener
import com.example.catsonactivity.catsAdapter
import com.example.catsonactivity.databinding.FragmentsCatsBinding
import com.example.catsonactivity.viewmodel.CatListItem
import com.example.catsonactivity.viewmodel.base.CatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CatsListActivity : AppCompatActivity(), CatsAdapterListener {

    private val viewModel by viewModels<CatsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =  FragmentsCatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = catsAdapter(this)
        binding.catsRecyclerView.layoutManager = LinearLayoutManager(this)
        (binding.catsRecyclerView.itemAnimator as? DefaultItemAnimator)
            ?.supportsChangeAnimations = false
        binding.catsRecyclerView.adapter = adapter
        viewModel.catsLiveData.observe(this) {
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
        val intent = Intent(this, CatDetailsActivity::class.java)
        intent.putExtra(CatDetailsActivity.EXTRA_CAT_ID, cat.id)
        startActivity(intent)
    }

}