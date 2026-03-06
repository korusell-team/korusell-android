package net.alienminds.ethnogram.ui.screens.session.messages.chat_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.data.model.chat.Chat
import net.alienminds.ethnogram.ui.extentions.custom.Avatar
import net.alienminds.ethnogram.ui.screens.session.NavBarScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.profile.ProfileScreen
import net.alienminds.ethnogram.ui.screens.session.messages.chat.ChatScreen

object ChatListScreen : NavBarScreen {
    
    private fun readResolve(): Any = ChatListScreen

    override val title: @Composable (() -> String)
        get() = { stringResource(R.string.chats) }
    
    override val icon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_messages) }
    
    override val activeIcon: @Composable (() -> Painter)
        get() = { painterResource(R.drawable.ic_messages_fill) }
    
    override val position: Int
        get() = 3

    @Composable
    override fun Content() = Column(
        modifier = Modifier.fillMaxSize()
    ){
        val vm = rememberScreenModel { ChatListModel() }

        ChatsToolbar(
            modifier = Modifier.statusBarsPadding()
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            chats(vm.chats)
            if (vm.chats.isEmpty()){
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        if (vm.isLoadingChats) {
                            CircularProgressIndicator()
                        } else{
                            Text(
                                text = "У вас пока нет чатов",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            if (vm.chatsMeta?.hasNext == true) {
                item {
                    LaunchedEffect(Unit) {
                        vm.loadNextChats()
                    }
                    Box(
                        modifier = Modifier.fillParentMaxWidth(),
                        contentAlignment = Alignment.Center,
                        content = { CircularProgressIndicator() }
                    )
                }
            }
        }
    }

    private fun LazyListScope.chats(
        chats: List<Chat>
    ) = items(chats){ chat ->
        val navigator = LocalNavigator.current
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable{ navigator?.push(ChatScreen(chat.id)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                modifier = Modifier
                    .align(Alignment.Top)
                    .size(52.dp),
                model = chat.interlocutorAvatar,
                initials = chat.interlocutorName.split(' ')
                    .joinToString(""){ it.firstOrNull()?.toString().orEmpty() },
                contentScale = ContentScale.Crop,
                onClick = {
                    navigator?.push(ProfileScreen(userId = chat.interlocutorId))
                },
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth()
            ) {
                Row{
                    val time = chat.lastMessageDate
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Format {
                            hour()
                            char(':')
                            minute()
                        })
                    Text(
                        modifier = Modifier.weight(1f),
                        text = chat.interlocutorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp)
                ){
                    Text(
                        modifier = Modifier.weight(1f),
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    AnimatedVisibility(chat.unreadMessagesCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.CenterVertically)
                        ){
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                text = chat.unreadMessagesCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                autoSize = TextAutoSize.StepBased(4.sp, 12.sp)
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    @Composable
    fun ChatsToolbar(
        modifier: Modifier = Modifier,
    ) = Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(44.dp)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(vertical = 8.dp),
            text = title(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        
    }

    

}