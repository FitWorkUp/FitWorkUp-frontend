package com.fitworkup.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitworkup.app.ui.screens.profile.components.AddFriendBottomSheet
import com.fitworkup.app.ui.screens.profile.components.FriendsListBottomSheet
import com.fitworkup.app.ui.screens.profile.components.ProfileBadgesSection
import com.fitworkup.app.ui.screens.profile.components.ProfileFriendsHubCard
import com.fitworkup.app.ui.screens.profile.components.ProfileHeaderInfo
import com.fitworkup.app.ui.screens.profile.components.ProfileStatsRow
import com.fitworkup.app.ui.screens.profile.components.ProfileTopBar
import com.fitworkup.app.ui.screens.profile.components.ProfileXpProgressCard
import com.fitworkup.app.domain.model.FriendItem
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {}
) {
    var showAddFriendSheet by remember { mutableStateOf(false) }
    var showFriendsListSheet by remember { mutableStateOf(false) }

    var friendsList by remember {
        mutableStateOf(
            listOf(
                FriendItem("1", "Carlos Silva", "carlos_fit", 15),
                FriendItem("2", "Mariana Souza", "mari_runner", 9),
                FriendItem("3", "João Pedro", "jp_trainer", 22)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileTopBar(onSettingsClick)
            Spacer(modifier = Modifier.height(16.dp))

            ProfileHeaderInfo()
            Spacer(modifier = Modifier.height(20.dp))

            ProfileXpProgressCard()
            Spacer(modifier = Modifier.height(16.dp))

            ProfileFriendsHubCard(
                friendCount = friendsList.size,
                onClick = { showFriendsListSheet = true },
                onAddClick = { showAddFriendSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileStatsRow()
            Spacer(modifier = Modifier.height(20.dp))

            ProfileBadgesSection()
        }

        if (showAddFriendSheet) {
            AddFriendBottomSheet(
                onDismissRequest = { showAddFriendSheet = false },
                onSendFriendRequest = { userTag: String ->
                    // TODO: Chamar Retrofit/ViewModel para enviar a requisição de amizade
                }
            )
        }

        if (showFriendsListSheet) {
            FriendsListBottomSheet(
                friendsList = friendsList,
                onDismissRequest = { showFriendsListSheet = false },
                onRemoveFriend = { friendId ->
                    friendsList = friendsList.filter { it.id != friendId }
                }
            )
        }
    }
}