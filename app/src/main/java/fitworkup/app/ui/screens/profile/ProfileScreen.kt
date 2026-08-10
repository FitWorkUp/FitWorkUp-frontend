package com.fitworkup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitworkup.app.ui.screens.profile.components.AddFriendBottomSheet
import com.fitworkup.app.ui.screens.profile.components.FriendsListBottomSheet
import com.fitworkup.app.ui.screens.profile.components.ProfileBadgesSection
import com.fitworkup.app.ui.screens.profile.components.ProfileFriendsHubCard
import com.fitworkup.app.ui.screens.profile.components.ProfileHeaderInfo
import com.fitworkup.app.ui.screens.profile.components.ProfileStatsRow
import com.fitworkup.app.ui.screens.profile.components.ProfileTopBar
import com.fitworkup.app.ui.screens.profile.components.ProfileXpProgressCard

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    var showAddFriendSheet by remember { mutableStateOf(false) }
    var showFriendsListSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileTopBar(onSettingsClick)
            Spacer(modifier = Modifier.height(16.dp))

            ProfileHeaderInfo(profile = uiState.profile)
            Spacer(modifier = Modifier.height(20.dp))

            ProfileXpProgressCard(profile = uiState.profile)
            Spacer(modifier = Modifier.height(16.dp))

            ProfileFriendsHubCard(
                friendCount = uiState.friends.size,
                onClick = { showFriendsListSheet = true },
                onAddClick = { showAddFriendSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileStatsRow(profile = uiState.profile)
            Spacer(modifier = Modifier.height(20.dp))

            ProfileBadgesSection(badges = uiState.badges)
        }

        if (showAddFriendSheet) {
            AddFriendBottomSheet(
                onDismissRequest = { showAddFriendSheet = false },
                onSendFriendRequest = { userTag ->
                    viewModel.sendFriendRequest(userTag)
                }
            )
        }

        if (showFriendsListSheet) {
            FriendsListBottomSheet(
                friendsList = uiState.friends,
                onDismissRequest = { showFriendsListSheet = false },
                onRemoveFriend = { friendId ->
                    viewModel.removeFriend(friendId)
                }
            )
        }
    }
}
