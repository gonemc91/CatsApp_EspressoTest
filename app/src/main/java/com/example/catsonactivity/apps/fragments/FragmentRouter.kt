package com.example.catsonactivity.apps.fragments

interface FragmentRouter {

    /**
     * Go back to the previous screen
     */
    fun goBack()

    /**
     * Launch cat details screen
     */
    fun showDetails(catId: Long)

}