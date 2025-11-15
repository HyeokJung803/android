package com.studyapp.android.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyapp.android.ui.auth.LoginScreen
import com.studyapp.android.ui.auth.SignupScreen
import com.studyapp.android.ui.group.CreateGroupScreen
import com.studyapp.android.ui.group.GroupDetailScreen
import com.studyapp.android.ui.group.MyGroupsScreen
import com.studyapp.android.ui.home.ChangePasswordScreen
import com.studyapp.android.ui.home.EditProfileScreen
import com.studyapp.android.ui.home.HomeScreen
import com.studyapp.android.ui.home.ProfileScreen
import com.studyapp.android.ui.theme.StudyappTheme

sealed class Screen(val route: String) {
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Main : Screen("main")
    object Home : Screen("home")
    object MyGroups : Screen("my_groups")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object CreateGroup : Screen("create_group")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "홈", Icons.Default.Home)
    object MyGroups : BottomNavItem(Screen.MyGroups.route, "내 모임", Icons.Default.Menu)
    object Profile : BottomNavItem(Screen.Profile.route, "프로필", Icons.Default.Person)
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // 로그인 화면
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        // 회원가입 화면
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 메인 화면 (하단 네비게이션 포함) - 로그아웃 콜백 추가
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit = {} // 로그아웃 콜백 파라미터 추가
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyGroups,
        BottomNavItem.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF6B8CFF),
                            selectedTextColor = Color(0xFF6B8CFF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE8F0FF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToCreateGroup = {
                        navController.navigate(Screen.CreateGroup.route)
                    },
                    onNavigateToGroupDetail = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                )
            }
            composable(BottomNavItem.MyGroups.route) {
                MyGroupsScreen(
                    onNavigateToGroupDetail = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin // 로그아웃 콜백 전달
                )
            }
            composable(Screen.CreateGroup.route) {
                CreateGroupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGroupCreated = {
                        navController.popBackStack()
                    }
                )
            }

            // 그룹 상세 화면 추가!
            composable(
                route = "group_detail/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.LongType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                GroupDetailScreen(
                    groupId = groupId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

// ProfileScreen 호출 시 콜백 전달
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(Screen.ChangePassword.route)
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    StudyappTheme {
        MainScreen()
    }
}