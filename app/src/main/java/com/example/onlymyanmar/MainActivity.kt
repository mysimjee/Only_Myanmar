package com.example.onlymyanmar

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.onlymyanmar.ui.theme.OnlyMyanmarTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import com.google.android.exoplayer2.Player


data class Model(
    val imageUrl: String,
    val title: String,
    val description: String
)
sealed class BottomNavList(val route: String, var title: String, var icon: ImageVector) {
    object Home : BottomNavList("home", "Home", Icons.Default.Home)
    object Place : BottomNavList("place", "Link", Icons.Default.Place)
    object About : BottomNavList("about", "About", Icons.Default.Info)
}

@Composable
fun IndicatorDot(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun DotsIndicator(
    modifier: Modifier = Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color = Color.Yellow,
    unSelectedColor: Color = Color.Gray,
    dotSize: Dp
) {
    LazyRow(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
    ) {
        items(totalDots) { index ->
            IndicatorDot(
                size = dotSize,
                color = if (index == selectedIndex) selectedColor else unSelectedColor,
            )
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier,
    autoSlideDuration: Long = 3000L,
    pagerState: PagerState = remember { PagerState() },
    itemsCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
    showDialog: MutableState<Boolean>
) {
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(pagerState.currentPage, showDialog.value) {
        delay(autoSlideDuration)
        if (!isDragged && showDialog.value == false) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % itemsCount)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(count = itemsCount, state = pagerState) { page ->
            itemContent(page);
        }

        Surface(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.BottomCenter),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            DotsIndicator(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                totalDots = itemsCount,
                selectedIndex = if (isDragged) pagerState.currentPage else pagerState.targetPage,
                dotSize = 8.dp
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnlyMyanmarTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigation(navController = navController) }
                ) { innerPadding ->
                    NavHost(navController, startDestination = BottomNavList.Home.route) {
                        composable(BottomNavList.Home.route) {
                            HomePage(innerPadding)
                        }
                        composable(BottomNavList.Place.route) {

                        }
                        composable(BottomNavList.About.route) {
                            Greeting(name = BottomNavList.About.title)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(BottomNavList.Home, BottomNavList.Place, BottomNavList.About)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            AddItem(
                screen = item,
                isSelected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomNavList,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title
            )
        },
        label = {
            Text(screen.title)
        }
    )
}
@Composable
fun MyDialog(
    showDialog: MutableState<Boolean>,
    index: Int?,
    model: List<Model>,
    onClose: () -> Unit
) {
    if (showDialog.value && index != null) {
        AlertDialog(
            onDismissRequest = onClose,
            title = {
                Text(model[index].title)
            },
            text = {
                Text(model[index].description)
            },
            confirmButton = {
                Button(
                    onClick = onClose,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun VideoPlayer(videos: List<Uri>) {
    val doubleClickTimeout = 300L // Adjust timeout as needed

    var lastClickTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videos.first()))
            prepare()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer;
                exoPlayer.play()
                useController = false;
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            currentIndex = (currentIndex + 1) % videos.size
                            exoPlayer.setMediaItem(MediaItem.fromUri(videos[currentIndex]))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    }
                })
                setOnClickListener {
                    val clickTime = System.currentTimeMillis()

                    if (clickTime - lastClickTime < doubleClickTimeout) {
                        // Double click detected
                        currentIndex = (currentIndex + 1) % videos.size
                        exoPlayer.setMediaItem(MediaItem.fromUri(videos[currentIndex]))
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                        Toast.makeText(context, "Next Video Coming!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Single click
                        if (exoPlayer.playWhenReady) {
                            exoPlayer.playWhenReady = false

                        } else {
                            exoPlayer.playWhenReady = true
                            Toast.makeText(context, "Playing", Toast.LENGTH_SHORT).show()
                        }
                    }
                    lastClickTime = clickTime
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )


}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomePage(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = stringResource(id = R.string.homepage_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally),
            fontSize = 16.sp
        )


        val showDialog = remember {
            mutableStateOf(false)
        }

        val selectedImageIndex = remember { mutableStateOf<Int?>(null) }
        val models = listOf(
            Model(
                imageUrl = "https://thanhnien.mediacdn.vn/Uploaded/tuyenth/2022_08_07/h1-7106.jpeg",
                title = "Thinzar Wint Kyaw & Nang Mwe San: Myanmar's Rising Stars on OnlyFans",
                description = "Meet Myanmar's digital icons, Nang Mwe San and Thinzar Wint Kyaw, who have captivated audiences worldwide with their unique styles and powerful presence on OnlyFans."
            ),
            Model(
                imageUrl = "https://www.irrawaddy.com/wp-content/uploads/2023/08/feat_TZWK.png",
                title = "Nang Mwe San",
                description = "Known for her captivating charm and artistic flair, Nang Mwe San has redefined beauty in Myanmar's digital landscape. With a passion for fashion and a knack for connecting with her audience, she brings a unique blend of elegance and authenticity to OnlyFans."
            ),
            Model(
                imageUrl = "https://i2-prod.dailystar.co.uk/incoming/article27677783.ece/ALTERNATES/s1200e/0_nzarwintkyaw_237217870_130188512571818_5021043640057911493_n.jpg",
                title = "Thinzar Wint Kyaw",
                description = "A trailblazer in Myanmar's modeling scene, Thinzar Wint Kyaw has captivated fans worldwide with her daring photoshoots and empowering messages. Her journey on OnlyFans showcases not just her stunning looks but also her advocacy for self-expression and body positivity."
            )
        )

        AutoSlidingCarousel(
            itemsCount = models.size,
            showDialog = showDialog,
            itemContent = { index ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(models[index].imageUrl)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(200.dp)
                        .clickable {
                            showDialog.value = true;
                            selectedImageIndex.value = index

                        }
                )
            }
        )
        Text(
            text = "Exclusive Behind-the-Scenes Footage",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        val videoUris = listOf(
            Uri.parse("android.resource://com.example.onlymyanmar/raw/video2"),
            Uri.parse("android.resource://com.example.onlymyanmar/raw/video1")
        )
        VideoPlayer(videos = videoUris)
        MyDialog(
            showDialog = showDialog,
            index = selectedImageIndex.value,
            model = models,
            onClose = { showDialog.value = false }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OnlyMyanmarTheme {
        Greeting("Android")
    }
}
