package com.example.myphone

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.myphone.ui.theme.DarkBackground
import com.example.myphone.ui.theme.DarkIsSelected
import com.example.myphone.ui.theme.DarkSecondaryGray

@Preview
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