package org.apps.composestoryapp.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.formatDate
import org.apps.composestoryapp.model.Story

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    storyId: String,
    storyViewModel: StoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by storyViewModel.uiState.collectAsState()
    val isLiked by storyViewModel.isFavorite(storyId).collectAsState(initial = false)

    LaunchedEffect(storyId) {
        storyViewModel.getStoryDetail(storyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text ="Detail Story",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState.storyState) {
                is ViewState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ViewState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is ViewState.Success -> {
                    val storyUi = state.data

                    StoryDetailContent(
                        story = storyUi.story,
                        locationName = storyUi.locationName,
                        isLiked = isLiked,
                        onToggleFavorite = {
                            storyViewModel.toggleFavorite(storyUi.story, isLiked)
                        },
                        onImageClick = { imageUrl ->
                            storyViewModel.showImagePreview(imageUrl)
                        }
                    )
                }
                else -> Unit
            }
        }
    }

    ImagePreviewDialog(
        imageUrl = uiState.imagePreview.imageUrl,
        onDismiss = { storyViewModel.hideImagePreview() }
    )
}

@Composable
fun StoryDetailContent(
    story: Story,
    locationName: String? = null,
    isLiked: Boolean,
    onToggleFavorite: () -> Unit,
    onImageClick: (String) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            AsyncImage(
                model = story.photoUrl,
                contentDescription = story.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        onImageClick(story.photoUrl)
                    },
                contentScale = ContentScale.Fit
            )
        }

        item {
            if (locationName != null) {
                Text(
                    text = "📍 $locationName",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = 2
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp).weight(1f)
                ) {
                    Text(
                        text = story.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                    )

                    Text(
                        text = formatDate(story.createdAt),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }

                IconButton(
                    onClick = onToggleFavorite
                ) {
                    Icon(
                        imageVector = if (isLiked)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = story.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    if (imageUrl != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.95f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Close button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Image
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}


//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun StoryDetailPreview_Success() {
//    // Data Dummy
//    val dummyStory = Story(
//        id = "1",
//        name = "John Doe",
//        description = "Ini adalah deskripsi cerita yang sangat menarik tentang perjalanan ke pegunungan.",
//        photoUrl = "https://example.com/photo.jpg",
//        createdAt = "2023-10-27T10:00:00Z",
//        lat = -6.2F,
//        lon = 106.8F
//    )
//
//    MaterialTheme {
//        StoryDetailContent(
//            story = dummyStory,
//            locationName = "Jakarta, Indonesia",
//            isLiked = true,
//            onToggleFavorite = {}
//        )
//    }
//}