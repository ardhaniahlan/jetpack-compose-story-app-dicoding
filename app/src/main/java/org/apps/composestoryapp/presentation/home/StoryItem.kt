package org.apps.composestoryapp.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.apps.composestoryapp.formatDate
import org.apps.composestoryapp.model.StoryUi
import org.apps.composestoryapp.presentation.story.StoryViewModel

@Composable
fun StoryItem(
    storyUi : StoryUi,
    storyViewModel: StoryViewModel,
    onClick: () -> Unit
) {
    val story = storyUi.story
    var locationName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storyUi.story.id) {
        val lat = storyUi.story.lat
        val lon = storyUi.story.lon
        storyViewModel.resolveLocation(lat.toDouble(), lon.toDouble()) {
            locationName = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick)
            .background(Color.White)
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

        if (locationName != null) {
            Text(
                text = "📍$locationName",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AsyncImage(
            model = story.photoUrl,
            contentDescription = story.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = story.description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun StoryItemPreview(){
//    ComposeStoryAppTheme{
//        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
//            StoryItem(
//                storyUi = StoryUi(
//                    story = Story(
//                        id = "1asdd",
//                        name = "dhan",
//                        photoUrl = null,
//                        description = "LoremPsum",
//                        createdAt = "asdasda",
//                        lat = 0F,
//                        lon = 0F
//                    ),
//                ),
//                onClick = {},
//                storyViewModel =
//            )
//        }
//    }
//}