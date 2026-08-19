package com.brochurecraft.app.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brochurecraft.app.ui.components.BrochureCraftBottomBar
import com.brochurecraft.app.ui.screens.*
import com.brochurecraft.app.util.rememberApp

private val bottomBarRoutes = BottomDestination.values().map { it.route }.toSet()

@Composable
fun AppNavGraph(startAtOnboarding: Boolean) {
    val navController = rememberNavController()
    val app = rememberApp()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(onFinished = {
                        val dest = if (startAtOnboarding) Routes.ONBOARDING_WELCOME else Routes.HOME
                        navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    })
                }
                composable(Routes.ONBOARDING_WELCOME) {
                    OnboardingWelcomeScreen(
                        onNext = { navController.navigate(Routes.ONBOARDING_BUSINESS) },
                        onSkip = { navController.navigate(Routes.ONBOARDING_BUSINESS) }
                    )
                }
                composable(Routes.ONBOARDING_BUSINESS) {
                    OnboardingBusinessScreen(onDone = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    })
                }
                composable(Routes.HOME) {
                    HomeDashboardScreen(
                        onCreateNew = { navController.navigate(Routes.editorForNewBlank()) },
                        onOpenDesign = { id -> navController.navigate(Routes.editorForDesign(id)) },
                        onOpenTemplate = { id, name -> navController.navigate(Routes.editorForTemplate(id, name)) },
                        onSeeAllTemplates = { navController.navigate(Routes.TEMPLATES) },
                        onOpenProfile = { navController.navigate(Routes.PROFILE) }
                    )
                }
                composable(Routes.TEMPLATES) {
                    TemplateExplorerScreen(onOpenTemplate = { id, name -> navController.navigate(Routes.editorForTemplate(id, name)) })
                }
                composable(Routes.GALLERY) {
                    MyDesignsGalleryScreen(
                        onOpenDesign = { id -> navController.navigate(Routes.editorForDesign(id)) },
                        onCreateNew = { navController.navigate(Routes.editorForNewBlank()) }
                    )
                }
                composable(Routes.BRAND_KIT) { BrandKitScreen() }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                        onOpenBrandKit = { navController.navigate(Routes.BRAND_KIT) }
                    )
                }
                composable(Routes.PREMIUM) {
                    PremiumUpgradeScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Routes.EDITOR,
                    arguments = listOf(
                        navArgument("designId") { defaultValue = "-1" },
                        navArgument("templateId") { defaultValue = "-1" },
                        navArgument("name") { defaultValue = "" }
                    )
                ) { entry ->
                    val designId = entry.arguments?.getString("designId")?.toLongOrNull() ?: -1L
                    val templateId = entry.arguments?.getString("templateId")?.toLongOrNull() ?: -1L
                    val name = android.net.Uri.decode(entry.arguments?.getString("name") ?: "")
                    DesignEditorScreen(
                        designId = if (designId > 0) designId else null,
                        templateId = if (templateId > 0) templateId else null,
                        initialName = name,
                        onBack = { navController.popBackStack() },
                        onExport = { id -> navController.navigate(Routes.export(id)) },
                        onBrowseTemplates = { navController.navigate(Routes.TEMPLATES) }
                    )
                }
                composable(
                    route = Routes.EXPORT,
                    arguments = listOf(navArgument("designId") { })
                ) { entry ->
                    val designId = entry.arguments?.getString("designId")?.toLongOrNull() ?: 0L
                    ExportShareScreen(designId = designId, onBack = { navController.popBackStack() })
                }
            }
        }

        if (currentRoute in bottomBarRoutes) {
            val current = BottomDestination.values().first { it.route == currentRoute }
            BrochureCraftBottomBar(current = current) { dest ->
                if (dest.route != currentRoute) {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}
