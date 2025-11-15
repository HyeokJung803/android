package com.studyapp.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.studyapp.android.data.api.model.repository.Category
import com.studyapp.android.data.api.model.repository.GroupResponse
import com.studyapp.android.ui.group.GroupViewModel
import com.studyapp.android.ui.group.GroupsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToGroupDetail: (Long) -> Unit = {},
    viewModel: GroupViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val groupsState by viewModel.groupsState.collectAsState()

    LaunchedEffect(selectedCategory) {
        if (selectedCategory == null) {
            viewModel.getAllGroups()
        } else {
            viewModel.getGroupsByCategory(selectedCategory!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 검색바
        TopAppBar(
            title = {
                Text(
                    text = "StudyApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: 검색 기능 */ }) {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                }
                IconButton(onClick = onNavigateToCreateGroup) {  // + 버튼 추가
                    Icon(Icons.Default.Add, contentDescription = "그룹 생성")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black
            )
        )

        // 카테고리 탭
        CategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 그룹 리스트
        when (groupsState) {
            is GroupsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is GroupsState.Success -> {
                val groups = (groupsState as GroupsState.Success).groups
                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 그룹이 없습니다",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groups) { group ->
                            GroupCard(
                                group = group,
                                onJoinClick = {
                                    viewModel.joinGroup(group.groupId)
                                },
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
                        text = (groupsState as GroupsState.Error).message,
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 전체 탭
        item {
            CategoryChip(
                text = "전체",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }

        // 카테고리 탭들
        items(Category.values()) { category ->
            CategoryChip(
                text = category.displayName,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF6B8CFF) else Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun GroupCard(
    group: GroupResponse,
    onJoinClick: () -> Unit,
    onCardClick: (Long) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(group.groupId) },  // 클릭만 추가!
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 카테고리 태그
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE8F0FF)
            ) {
                Text(
                    text = group.category.displayName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color(0xFF6B8CFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 그룹명
            Text(
                text = group.groupName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 설명
            if (group.description.isNotEmpty()) {
                Text(
                    text = group.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 그룹장, 인원
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "그룹장: ${group.leaderNickname}",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${group.currentMembers}/${group.maxMembers}명",
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }

                // 가입 버튼
                if (!group.isMember) {
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B8CFF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = group.currentMembers < group.maxMembers
                    ) {
                        Text(
                            text = if (group.currentMembers < group.maxMembers) "가입하기" else "정원초과",
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F0FF)
                    ) {
                        Text(
                            text = "가입됨",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = Color(0xFF6B8CFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // 더미 데이터로 Preview
    val dummyGroups = listOf(
        GroupResponse(
            groupId = 1,
            groupName = "자바 스터디",
            description = "자바 기초부터 심화까지",
            category = Category.PROGRAMMING,
            leaderId = 1,
            leaderNickname = "테스트유저",
            maxMembers = 5,
            currentMembers = 1,
            createdAt = "2025-10-21",
            isMember = false
        ),
        GroupResponse(
            groupId = 2,
            groupName = "영어 회화",
            description = "영어 스터디",
            category = Category.LANGUAGE,
            leaderId = 1,
            leaderNickname = "테스트유저",
            maxMembers = 10,
            currentMembers = 3,
            createdAt = "2025-10-21",
            isMember = true
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        CategoryTabs(
            selectedCategory = null,
            onCategorySelected = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyGroups) { group ->
                GroupCard(
                    group = group,
                    onJoinClick = {}
                )
            }
        }
    }
}