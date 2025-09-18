package com.example.myphone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myphone.ui.theme.DarkBackground
import com.example.myphone.ui.theme.DarkIsSelected
import com.example.myphone.ui.theme.DarkSecondaryGray

//@Preview
@Composable
fun DrawerPanel(){

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(.8f),
        drawerShape = DrawerDefaults.shape,
        drawerContainerColor = DarkBackground,
        drawerContentColor = Color.White,

    ) {
        NavigationDrawerItem(
            label = { Text("Contacts") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = true,
//            colors = NavigationDrawerItemDefaults.colors(
//                unselectedContainerColor = DarkSecondaryGray,
//                selectedContainerColor = DarkIsSelected,
//                selectedTextColor = Color.White,
//                selectedIconColor = Color.White,
//                unselectedTextColor = Color.White,
//                unselectedIconColor = Color.White
//            ),
            onClick = {
//                onItemClick("Match")
//                navController.navigate()
            }
        )

        NavigationDrawerItem(
            label = { Text("Favourites") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            selected = false,
//            colors = NavigationDrawerItemDefaults.colors(
//                unselectedContainerColor = DarkSecondaryGray,
//                selectedContainerColor = DarkIsSelected,
//                selectedTextColor = Color.White,
//                selectedIconColor = Color.White,
//                unselectedTextColor = Color.White,
//                unselectedIconColor = Color.White
//            ),
            onClick = {
//                onItemClick("Match")
//                navController.navigate()
            }
        )
    }
}

@Preview
@Composable
fun BottomAppBar(){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ){
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "",
            modifier = Modifier
                .size(50.dp)
                .padding(all = 10.dp)
                .clickable {}
        )

        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "",
            modifier = Modifier
                .size(50.dp)
                .padding(all = 10.dp)
                .clickable {}
        )

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "",
            modifier = Modifier
                .size(50.dp)
                .padding(all = 10.dp)
                .clickable {}
        )

    }
}

