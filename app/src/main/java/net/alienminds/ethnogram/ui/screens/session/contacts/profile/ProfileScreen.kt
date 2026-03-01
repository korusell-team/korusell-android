package net.alienminds.ethnogram.ui.screens.session.contacts.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import net.alienminds.ethnogram.R
import net.alienminds.ethnogram.mappers.copyToClipboard
import net.alienminds.ethnogram.mappers.displayValue
import net.alienminds.ethnogram.mappers.localName
import net.alienminds.ethnogram.mappers.openInApp
import net.alienminds.ethnogram.mappers.roundIcon
import net.alienminds.ethnogram.mappers.title
import net.alienminds.ethnogram.service.data.entities.Category
import net.alienminds.ethnogram.service.feed.entities.Author
import net.alienminds.ethnogram.service.feedback.entities.UserFeedback
import net.alienminds.ethnogram.service.user.entities.UserSocialType
import net.alienminds.ethnogram.ui.extentions.buttons.BackButton
import net.alienminds.ethnogram.ui.extentions.buttons.DropdownButton
import net.alienminds.ethnogram.ui.extentions.custom.PageIndicator
import net.alienminds.ethnogram.ui.extentions.transitions.PageTransitionScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.all_feedbacks.AllFeedbacksScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.edit_profile.EditProfileScreen
import net.alienminds.ethnogram.ui.screens.session.contacts.send_feedback.SendFeedbackScreen
import net.alienminds.ethnogram.ui.screens.session.feed.components.AuthorContent
import net.alienminds.ethnogram.ui.screens.session.messages.chat.ChatScreen
import net.alienminds.ethnogram.ui.theme.AppColor
import net.alienminds.ethnogram.utils.IntentActions
import net.alienminds.ethnogram.utils.openLinkExternal
import net.alienminds.ethnogram.utils.rememberRelativeTime
import java.time.Instant
import kotlin.math.max
import kotlin.math.roundToInt

class ProfileScreen(
    private val userId: String? = null
): PageTransitionScreen {

    override val position: Int
        get() = 2

    @Composable
    override fun Content() = Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        val navigator = LocalNavigator.current
        val vm = rememberScreenModel { ProfileViewModel(userId, navigator) }

        val density = LocalDensity.current
        val scrollState = rememberScrollState()
        val scrollDp by remember { derivedStateOf { with(density){ scrollState.value.toDp() } } }

        val toolbarImageState by remember { derivedStateOf { vm.user?.image.isNullOrEmpty() } }
        val toolbarScrollState by remember { derivedStateOf { scrollDp >= 22.dp } }

        val toolbarAlpha by animateFloatAsState(when{
            toolbarScrollState -> 1f
            else -> 0f
        })

        val toolbarColor by animateColorAsState(when(toolbarScrollState){
            true -> AppColor.gray50
            false -> AppColor.gray900.copy(0.8f)
        })

        val toolbarTint by animateColorAsState(when{
            toolbarImageState || toolbarScrollState -> AppColor.blue600
            else -> AppColor.gray300
        })

        if (vm.loading) {
            Dialog(
                onDismissRequest = {}
            ) {
                CircularProgressIndicator()
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Column {
                AvatarBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 14.dp,
                                bottomEnd = 14.dp
                            )
                        ),
                    loading = vm.loading,
                    images = vm.user?.image ?: emptyList(),
                    isSponsored = vm.user?.isSponsored == true
                )

                HeaderNameBlock(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    fullName = vm.user?.fullName.orEmpty(),
                    city = vm.city.joinToString { it.localName },
                    link = vm.user?.link.orEmpty(),
                    address = vm.user?.address,
                    location = vm.user?.run {
                        latitude?.let { lat ->
                            longitude?.let { lng ->
                                LatLng(lat, lng)
                            }
                        }
                    },
                    onGoChat = {
                        vm.getChatId {
                            navigator?.push(ChatScreen(it))
                        }
                    }
                )
                HeaderBioBlock(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    categories = vm.categories,
                    bio = vm.user?.bio.orEmpty()
                )

                if (vm.user?.run {
                        info.isNullOrEmpty().not() && social.activeLinks.isNotEmpty()
                    } == true) {
                    HorizontalDivider(
                        modifier = Modifier.padding(16.dp)
                    )
                }
                if (vm.user?.info.isNullOrEmpty().not()) {
                    BioBlock(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        bio = vm.user?.info.orEmpty()
                    )
                }
                if (vm.user?.social?.activeLinks.isNullOrEmpty().not()) {
                    LinksBlock(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .padding(horizontal = 16.dp),
                        links = vm.user?.social?.activeLinks.orEmpty()
                    )
                }
                HorizontalDivider(Modifier.padding(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = buildAnnotatedString {
                        append(stringResource(R.string.rating))
                        append(": ")
                        withStyle(SpanStyle(
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )){
                            if (vm.feedbacks.isNotEmpty()) {
                                vm.user?.avgRating?.roundToInt()?.toString()?.let{
                                    append("⭐\uFE0F$it")
                                }
                            }
                            append(stringResource(R.string.feedbacks_count, vm.feedbacks.size))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                HorizontalDivider(Modifier.padding(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.feedbacks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )
                    if (vm.feedbacks.isNotEmpty()) {
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { vm.user?.uid?.let { navigator?.push(AllFeedbacksScreen(it)) } }
                        ) {
                            Text(
                                text = stringResource(R.string.all_feedbacks),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                if (vm.isMe.not()) {
                    OutlinedButton(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        onClick = { vm.user?.uid?.let { navigator?.push(SendFeedbackScreen(
                            userId = it,
                            currentRating = vm.myFeedback?.rating,
                            currentComment = vm.myFeedback?.comment,
                        )) } }
                    ) {
                        Text(
                            text = when(vm.myFeedback == null) {
                                true -> stringResource(R.string.write_feedback)
                                false -> stringResource(R.string.edit_feedback)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 16.sp
                        )
                    }
                }
                vm.feedbacks.take(5).forEach { feedback ->
                    FeedbackItem(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                            .fillMaxWidth(),
                        feedback = feedback,
                        author = feedback.fromUserId?.let { vm.authors[it] }
                    )
                }

            }
            Column {
                if(vm.isMe){
                    Button(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = 32.dp
                            )
                            .shadow(
                                elevation = 4.dp,
                                shape = MaterialTheme.shapes.large
                            )
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.large,
                        onClick = { vm.logout(navigator) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                }
                FooterText(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(top = 32.dp, bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                )
            }
        }

        Toolbar(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            toolbarColor,
                            AppColor.gray50.copy(toolbarAlpha)
                        )
                    )
                )
                .statusBarsPadding(),
            isMe = vm.isMe,
            isFavorite = vm.isFavorite,
            tint = toolbarTint,
            onFavoriteChange = vm::changeFavorite,
            onBlock = { vm.blockUser(navigator) },
            onReport = { vm.reportUser(navigator) },
            onEditProfile = { navigator?.push(EditProfileScreen()) }
        )
    }

    @Composable
    private fun FeedbackItem(
        modifier: Modifier = Modifier,
        feedback: UserFeedback,
        author: Author?
    ) = Column(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row {
            AuthorContent(
                modifier = Modifier.weight(1f),
                author = author,
                avatarSize = 32.dp,
                textStyle = MaterialTheme.typography.titleSmall,
                textColor = MaterialTheme.colorScheme.onBackground
            ) {
                Text(
                    text = "⭐\uFE0F ${feedback.rating.roundToInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            RelativeTime(
                instant = feedback.updatedAt?: feedback.createdAt
            )
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = feedback.comment.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline
        )
    }

    @Composable
    private fun Toolbar(
        modifier: Modifier = Modifier,
        isMe: Boolean,
        isFavorite: Boolean,
        tint: Color,
        onFavoriteChange:  (Boolean) -> Unit,
        onBlock: () -> Unit,
        onReport: () -> Unit,
        onEditProfile: () -> Unit
    ) = Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 8.dp),
    ){
        BackButton(
            text = stringResource(R.string.contacts),
            tint = tint
        )

        if (isMe){
            TextButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onEditProfile
            ) {
                Text(
                    text = stringResource(R.string.edit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tint
                )
            }
        } else{
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onFavoriteChange(isFavorite.not()) }
                ) {
                    AnimatedContent(isFavorite) { favorite ->
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = when (favorite) {
                                true -> Icons.Filled.Favorite
                                false -> Icons.Outlined.FavoriteBorder
                            },
                            tint = when (favorite) {
                                true -> AppColor.red500
                                false -> tint
                            },
                            contentDescription = null
                        )
                    }
                }
                DropdownButton(
                    painter = painterResource(R.drawable.ic_more_horiz),
                    tint = tint,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    ItemButton(
                        text = stringResource(R.string.block),
                        icon = painterResource(R.drawable.ic_pan_tool),
                        onClick = onBlock
                    )
                    HorizontalDivider()
                    ItemButton(
                        text = stringResource(R.string.report),
                        icon = painterResource(R.drawable.ic_feedback),
                        onClick = onReport
                    )
                }
            }
        }
    }

    @Composable
    private fun AvatarBlock(
        modifier: Modifier = Modifier,
        loading: Boolean,
        images: List<String>,
        isSponsored: Boolean
    ) = Box(
        modifier = modifier
            .background(AppColor.gray200)
    ){
        if (images.isEmpty()){
            if (loading.not()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\uD83D\uDE25",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = stringResource(R.string.no_photo_placeholder),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState { max(images.size, 1) }
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = images.getOrNull(it),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
            PageIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth(),
                pagerState = pagerState,
                tint = AppColor.gray300
            )
        }
        if(isSponsored){
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(
                            Color(230f / 255f, 232f / 255f, 235f / 255f),
                            Color(200f / 255f, 202f / 255f, 205f / 255f),
                            Color(245f / 255f, 247f / 255f, 250f / 255f),
                            Color(180f / 255f, 182f / 255f, 185f / 255f)
                        )),
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 1.dp,
                        color = AppColor.gray400,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ){
                Text(
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    text = stringResource(R.string.badge_plus),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.gray800
                )
            }
        }
        //Badge P L U S
    }

    @Composable
    private fun HeaderNameBlock(
        modifier: Modifier = Modifier,
        fullName: String,
        city: String,
        link: String,
        address: String?,
        location: LatLng?,
        onGoChat: () -> Unit
    ) = Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        val context = LocalContext.current
        var showNavigatorMenu by remember { mutableStateOf(false) }
        val params = location?.let {
            IntentActions.Navigation.LocationParams(
                latitude = it.latitude,
                longitude = it.longitude,
                address = address
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Text(
                text = fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = city,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            @Composable
            fun CircleIconButton(
                icon: Painter,
                accentColor: Color,
                visible: Boolean = true,
                onClick: () -> Unit,
            ) = AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = onClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = icon,
                        contentDescription = null
                    )
                }
            }

            Box {
                CircleIconButton(
                    visible = location != null,
                    icon = painterResource(R.drawable.ic_route),
                    accentColor = AppColor.purple600,
                    onClick = { showNavigatorMenu = true }
                )
                DropdownMenu(
                    expanded = showNavigatorMenu,
                    onDismissRequest = { showNavigatorMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    offset = DpOffset(
                        x = 0.dp,
                        y = 8.dp
                    )
                ) {
                    val navigators = IntentActions.Navigation.Navigator.entries
                    navigators.forEach { navigator ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = navigator.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Image(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape),
                                    painter = navigator.icon,
                                    contentDescription = null
                                )
                            },
                            onClick = { params?.let {
                                IntentActions.Navigation.openRoute(context, navigator, params)
                                showNavigatorMenu = false
                            } }
                        )
                        if(navigator != navigators.lastOrNull()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            CircleIconButton(
                icon = painterResource(R.drawable.ic_share),
                accentColor = AppColor.blue600,
                onClick = { IntentActions.shareText(context, fullName, link) }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )
            if (userId != null) {
                CircleIconButton(
                    icon = painterResource(R.drawable.ic_chat_bubble),
                    accentColor = AppColor.green700,
                    onClick = onGoChat
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun HeaderBioBlock(
        modifier: Modifier = Modifier,
        categories: List<Category>,
        bio: String
    ) = Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        if (categories.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    Box(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(4.dp),
                            text = cat.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = bio,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }

    @Composable
    private fun BioBlock(
        modifier: Modifier = Modifier,
        bio: String,
        minLength: Int = 200
    ){
        var expandedBio by remember { mutableStateOf(false) }

        val length by animateIntAsState(
            targetValue = when(expandedBio){
                true -> bio.length
                false -> minLength
            }
        )

        @Composable
        fun AnnotatedString.Builder.appendTitle() = withStyle(SpanStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )) { append("О cебе:\n") }

        @Composable
        fun AnnotatedString.Builder.appendMoreButton() = withLink(
            link = LinkAnnotation.Clickable(
                tag = "more",
                styles = TextLinkStyles(SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )),
                linkInteractionListener = { expandedBio = expandedBio.not() }
            ),
            block = { when(expandedBio) {
                true -> append("\nсвернуть")
                false -> append("eще")
            } }
        )


        Text(
            modifier = modifier,
            text = buildAnnotatedString {
                appendTitle()
                append(bio.take(length))
                if (minLength < bio.length) {
                    if (expandedBio.not()) append("...  ")
                    appendMoreButton()
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun LinksBlock(
        modifier: Modifier = Modifier,
        links: Map<UserSocialType, String>
    ) = Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        val context = LocalContext.current
        links.forEach { (type, value) ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = when (isPressed) {
                            true -> 1.dp
                            false -> 2.dp
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onLongClick = {
                            context.copyToClipboard(
                                label = type.name,
                                link = type.displayValue(value)
                            )
                        },
                        onClick = { type.openInApp(context, value) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp),
                    painter = type.roundIcon,
                    contentScale = ContentScale.Fit,
                    contentDescription = null
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ){
                    Text(
                        text = type.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                    Text(
                        text = type.displayValue(value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = contentColorFor(MaterialTheme.colorScheme.surfaceContainerLow),
                        maxLines = 1
                    )
                }
            }
        }
    }
    
    @Composable
    private fun FooterText(
        modifier: Modifier = Modifier
    ){
        val context = LocalContext.current
        val supportGmail = stringResource(R.string.support_gmail)

        fun AnnotatedString.Builder.appendGmailLink() = withLink(
            link = LinkAnnotation.Url(
                url = supportGmail,
                styles = TextLinkStyles(SpanStyle(
                    color = AppColor.blue300,
                    fontWeight = FontWeight.Bold
                )),
                linkInteractionListener = {
                    context.openLinkExternal("mailto:$supportGmail")
                }
            ),
            block = { append(supportGmail) }
        )
        
        Text(
            modifier = modifier,
            text = buildAnnotatedString { 
                append(stringResource(R.string.objectionable_policy0))
                appendGmailLink()
                append(stringResource(R.string.objectionable_policy1))
            },
            style = MaterialTheme.typography.bodySmall,
            color = AppColor.gray500,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun RelativeTime(
        modifier: Modifier = Modifier,
        instant: Instant?
    ){
        instant?.let { createdAt ->
            val relativeTime by createdAt.rememberRelativeTime()
            Text(
                modifier = modifier,
                text = relativeTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }

    val IntentActions.Navigation.Navigator.title
        get() = when(this){
            IntentActions.Navigation.Navigator.KAKAO -> "Kakao map"
            IntentActions.Navigation.Navigator.NAVER -> "Naver map"
        }

    val IntentActions.Navigation.Navigator.icon: Painter
        @Composable get() = when(this){
            IntentActions.Navigation.Navigator.KAKAO -> painterResource(R.drawable.ic_kakao_maps)
            IntentActions.Navigation.Navigator.NAVER -> painterResource(R.drawable.ic_naver_maps)
        }


}