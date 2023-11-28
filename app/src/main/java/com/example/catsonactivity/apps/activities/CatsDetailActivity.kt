package com.example.catsonactivity.apps.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.elveum.elementadapter.setTintColor
import com.example.catsonactivity.R
import com.example.catsonactivity.databinding.FragmentCatDetailsBinding
import com.example.catsonactivity.viewmodel.CatDetailsViewModel
import com.example.catsonactivity.viewmodel.base.assistedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CatDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: CatDetailsViewModel.Factory

    private val viewModel by assistedViewModel {
        val catId = intent.getLongExtra(EXTRA_CAT_ID, -1)
        factory.create(catId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = FragmentCatDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.catLiveData.observe(this) { cat ->
            binding.catNameTextView.text = cat.name
            binding.catDescriptionTextView.text = cat.description
            binding.catImageView.load(cat.photoUrl) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.circle)
            }
            binding.favoriteImageView.setImageResource(
                if (cat.isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_not
            )
            binding.favoriteImageView.setTintColor(
                if (cat.isFavorite) R.color.highlighted_action
                else R.color.action
            )
        }

        binding.goBackButton.setOnClickListener {
            finish()
        }
        binding.favoriteImageView.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    companion object {
        const val EXTRA_CAT_ID = "catId"
    }

}