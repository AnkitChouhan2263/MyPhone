package com.example.myphone.ui.screens

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeScreenViewModel() : ViewModel(){

    var filterOptions = mutableStateListOf("All", "Missed","Contacts","Favourites","Unknown", "Non-Spam" , "Spam", )
        private set

    var selected : MutableState<String> = mutableStateOf("All")

}

data class HomeScreenItems(
    var filterOptions : MutableList<String> = mutableStateListOf("All", "Missed","Contacts","Favourites","Unknown", "Non-Spam" , "Spam", )

)