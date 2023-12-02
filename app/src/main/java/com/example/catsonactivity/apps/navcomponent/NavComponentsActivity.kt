package com.example.catsonactivity.apps.navcomponent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.catsonactivity.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavComponentActivity : AppCompatActivity() {

    private val navController: NavController
        get() {
            val fragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            return fragment.navController
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_component)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

}