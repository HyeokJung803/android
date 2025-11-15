package com.studyapp.android.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyapp.android.ui.home.GroupCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupsScreen(
    viewModel: GroupViewModel = viewModel(),
    onNavigateToGroupDetail: (Long) -> Unit = {}
) {
    val myGroupsState by viewModel.myGroupsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "내 모임",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black
            )
        )

        // 내 그룹 리스트
        when (myGroupsState) {
            is GroupsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is GroupsState.Success -> {
                val groups = (myGroupsState as GroupsState.Success).groups
                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "가입한 그룹이 없습니다",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "홈에서 그룹을 찾아보세요!",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groups) { group ->
                            GroupCard(
                                group = group,
                                onJoinClick = { /* 이미 가입됨 */ },
                                onCardClick = onNavigateToGroupDetail
                            )
                        }
                    }
                }
            }
            is GroupsState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (myGroupsState as GroupsState.Error).message,
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyGroupsScreenPreview() {
    MyGroupsScreen()
}