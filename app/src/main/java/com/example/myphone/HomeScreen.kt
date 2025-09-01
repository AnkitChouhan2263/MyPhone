package com.example.myphone

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import  androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myphone.ui.theme.DarkBackground
import com.example.myphone.ui.theme.DarkIsSelected
import com.example.myphone.ui.theme.DarkPrimaryGray
import com.example.myphone.ui.theme.DarkSecondaryGray

@Preview
@Composable
fun HomeScreen() {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(color = DarkBackground)
            .padding(8.dp),

    ){
        val l = listOf<String>("All", "Missed","Contacts","Favourites","Unknown", "Non-Spam" , "Spam", )
        var selectedItem by rememberSaveable { mutableStateOf("All") }

        var isFavouriteExpanded by rememberSaveable { mutableStateOf(true) }
        SearchBar()
        Spacer(modifier = Modifier.height(16.dp))
        Filter(
            selected = selectedItem,
            onItemClick = { selectedItem = it },
            options = l
        )

        Spacer(modifier = Modifier.height(16.dp))
        FavouriteContacts(
            isFavouriteExpanded = isFavouriteExpanded,
            onDropDown = {isFavouriteExpanded = !isFavouriteExpanded},
            onViewContacts = {}
        )
    }
}

@Composable
fun SearchBar(){
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),

        color = DarkPrimaryGray,
        onClick = {},

    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,

        ){
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp).padding(horizontal = 8.dp)
            ){
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Button",
                    tint = Color.LightGray,
                )
                Text(
                    text = "Search contacts",
                    color = Color.LightGray,
                    modifier = Modifier.padding(8.dp).padding(horizontal = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Microphone",
                tint = Color.LightGray,
                modifier = Modifier.padding(8.dp)
            )

        }

    }
}

@Composable
fun Filter(
    selected: String,
    options: List<String>,
    onItemClick: (String) -> Unit
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState())
    ){
        options.forEach(
            {
                FilterTab(selected,it , onItemClick= onItemClick)
                Spacer(modifier = Modifier.width(8.dp))
            }

        )
    }
}

@Composable
fun FilterTab(
    selected: String,
    value: String,
    onItemClick: (String) -> Unit,
){
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected == value) DarkIsSelected else DarkSecondaryGray,
        onClick = { onItemClick(value)}
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ){
            Text(
                text = value,
                color = Color.LightGray,
                modifier = Modifier.padding(4.dp).padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun FavouriteContacts(
    isFavouriteExpanded: Boolean,
    onDropDown: () -> Unit,
    onViewContacts: Any
) {

    Row(
        modifier = Modifier.fillMaxWidth(.8f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.clickable(
                onClick = onDropDown
            )
        ){
            Text(
                text = "Favourites",
                color = Color.LightGray
            )
            Icon(
                imageVector = if (isFavouriteExpanded){Icons.Default.KeyboardArrowUp}else Icons.Default.KeyboardArrowDown,
                contentDescription = "DropDownIcon",
                tint = Color.LightGray,
            )
        }

        Surface(
            onClick = {},
            shape = RoundedCornerShape(24.dp),
            color = DarkSecondaryGray,
        ) {
            Text(
                text = "View contacts",
                color = Color.LightGray,
                modifier = Modifier.padding(8.dp).padding(horizontal = 8.dp)

            )
        }
    }

}