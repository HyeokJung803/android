package com.studyapp.android.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyapp.android.StudyApp
import com.studyapp.android.data.api.model.repository.GroupRepository
import com.studyapp.android.data.api.model.repository.Category
import com.studyapp.android.data.api.model.repository.GroupDetailResponse
import com.studyapp.android.data.api.model.repository.GroupRequest
import com.studyapp.android.data.api.model.repository.GroupResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val _groupDetailState = MutableStateFlow<GroupDetailState>(GroupDetailState.Loading)
    val groupDetailState: StateFlow<GroupDetailState> = _groupDetailState

    private val repository = GroupRepository()

    private val _groupsState = MutableStateFlow<GroupsState>(GroupsState.Loading)
    val groupsState: StateFlow<GroupsState> = _groupsState

    private val _myGroupsState = MutableStateFlow<GroupsState>(GroupsState.Loading)
    val myGroupsState: StateFlow<GroupsState> = _myGroupsState

    private val _createGroupState = MutableStateFlow<CreateGroupState>(CreateGroupState.Idle)
    val createGroupState: StateFlow<CreateGroupState> = _createGroupState

    private val currentUserId: Long
        get() = StudyApp.currentUserId

    fun getAllGroups() {
        viewModelScope.launch {
            _groupsState.value = GroupsState.Loading
            val result = repository.getAllGroups(currentUserId)

            _groupsState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                GroupsState.Success(response.groups)
            } else {
                GroupsState.Error("그룹을 불러오는데 실패했습니다.")
            }
        }
    }

    fun getGroupsByCategory(category: Category) {
        viewModelScope.launch {
            _groupsState.value = GroupsState.Loading
            val result = repository.getGroupsByCategory(category.name, currentUserId)

            _groupsState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                GroupsState.Success(response.groups)
            } else {
                GroupsState.Error("그룹을 불러오는데 실패했습니다.")
            }
        }
    }

    fun getMyGroups() {
        viewModelScope.launch {
            _myGroupsState.value = GroupsState.Loading
            val result = repository.getMyGroups(currentUserId)

            _myGroupsState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                GroupsState.Success(response.groups)
            } else {
                GroupsState.Error("내 그룹을 불러오는데 실패했습니다.")
            }
        }
    }

    fun joinGroup(groupId: Long) {
        viewModelScope.launch {
            val result = repository.joinGroup(groupId, currentUserId)
            if (result.isSuccess) {
                getAllGroups()
            }
        }
    }

    fun createGroup(groupName: String, description: String, category: Category, maxMembers: Int) {
        viewModelScope.launch {
            _createGroupState.value = CreateGroupState.Loading

            val request = GroupRequest(
                groupName = groupName,
                description = description,
                category = category,
                maxMembers = maxMembers
            )

            val result = repository.createGroup(request, currentUserId)

            _createGroupState.value = if (result.isSuccess) {
                CreateGroupState.Success("그룹이 생성되었습니다!")
            } else {
                CreateGroupState.Error(result.exceptionOrNull()?.message ?: "그룹 생성에 실패했습니다.")
            }
        }
    }

    fun resetCreateGroupState() {
        _createGroupState.value = CreateGroupState.Idle
    }

    // ✅ 그룹 상세 조회
    fun getGroupDetail(groupId: Long) {
        viewModelScope.launch {
            _groupDetailState.value = GroupDetailState.Loading

            val userId = StudyApp.currentUserId

            if (userId <= 0) {
                println("🔴 GroupViewModel - 로그인 안 됨! userId=$userId")
                _groupDetailState.value = GroupDetailState.Error("로그인이 필요합니다")
                return@launch
            }

            println("🔵 GroupViewModel - getGroupDetail: groupId=$groupId, userId=$userId")

            val result = repository.getGroupDetail(groupId, userId)

            _groupDetailState.value = if (result.isSuccess) {
                println("🟢 GroupViewModel - 그룹 조회 성공")
                GroupDetailState.Success(result.getOrNull()!!)
            } else {
                val error = result.exceptionOrNull()?.message ?: "오류가 발생했습니다."
                println("🔴 GroupViewModel - 그룹 조회 실패: $error")
                GroupDetailState.Error(error)
            }
        }
    }

    // ✅ 가입인사와 함께 그룹 가입
    fun joinGroupWithGreeting(groupId: Long, greeting: String) {
        viewModelScope.launch {
            val result = repository.joinGroupWithGreeting(groupId, currentUserId, greeting)

            if (result.isSuccess) {
                // 성공하면 그룹 상세 정보 새로고침
                getGroupDetail(groupId)  // ✅ 이미 내부에서 userId 처리함
            } else {
                _groupDetailState.value = GroupDetailState.Error(
                    result.exceptionOrNull()?.message ?: "가입에 실패했습니다."
                )
            }
        }
    }

    // ✅ 멤버 강퇴
    fun kickMember(groupId: Long, targetUserId: Long) {
        viewModelScope.launch {
            val result = repository.kickMember(
                groupId = groupId,
                leaderId = currentUserId,      // ✅ 그룹장 ID
                targetUserId = targetUserId    // ✅ 강퇴할 멤버 ID
            )

            if (result.isSuccess) {
                // 성공하면 그룹 상세 정보 새로고침
                getGroupDetail(groupId)  // ✅ 이미 내부에서 userId 처리함
            } else {
                _groupDetailState.value = GroupDetailState.Error(
                    result.exceptionOrNull()?.message ?: "멤버 강퇴에 실패했습니다."
                )
            }
        }
    }
}

sealed class GroupsState {
    object Loading : GroupsState()
    data class Success(val groups: List<GroupResponse>) : GroupsState()
    data class Error(val message: String) : GroupsState()
}

sealed class CreateGroupState {
    object Idle : CreateGroupState()
    object Loading : CreateGroupState()
    data class Success(val message: String) : CreateGroupState()
    data class Error(val message: String) : CreateGroupState()
}

sealed class GroupDetailState {
    object Loading : GroupDetailState()
    data class Success(val detail: GroupDetailResponse) : GroupDetailState()
    data class Error(val message: String) : GroupDetailState()
}