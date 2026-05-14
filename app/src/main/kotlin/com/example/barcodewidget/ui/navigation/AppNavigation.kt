package com.example.barcodewidget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.barcodewidget.ui.screens.AddCardScreen
import com.example.barcodewidget.ui.screens.BarcodeDetailScreen
import com.example.barcodewidget.ui.screens.CardListScreen

sealed class Screen(val route: String) {
    object CardList : Screen("card_list")
    object AddCard : Screen("add_card")
    object BarcodeDetail : Screen("barcode_detail/{cardId}") {
        fun createRoute(cardId: Long) = "barcode_detail/$cardId"
    }
}

@Composable
fun AppNavigation(initialCardId: Long? = null) {
    val navController = rememberNavController()

    LaunchedEffect(initialCardId) {
        if (initialCardId != null) {
            navController.navigate(Screen.BarcodeDetail.createRoute(initialCardId))
        }
    }

    NavHost(navController = navController, startDestination = Screen.CardList.route) {
        composable(Screen.CardList.route) {
            CardListScreen(
                onAddCard = { navController.navigate(Screen.AddCard.route) },
                onCardTap = { cardId -> navController.navigate(Screen.BarcodeDetail.createRoute(cardId)) }
            )
        }
        composable(Screen.AddCard.route) {
            AddCardScreen(
                onNavigateBack = { navController.popBackStack() },
                onCardSaved = { cardId ->
                    navController.popBackStack()
                    navController.navigate(Screen.BarcodeDetail.createRoute(cardId))
                }
            )
        }
        composable(
            route = Screen.BarcodeDetail.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            BarcodeDetailScreen(
                cardId = cardId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
