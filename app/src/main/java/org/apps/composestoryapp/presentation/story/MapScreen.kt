package org.apps.composestoryapp.presentation.story

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.Story
import com.google.maps.android.compose.Marker
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    storyViewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by storyViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        storyViewModel.loadStoriesForMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story Map") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        when (val state = uiState.mapState) {

            is ViewState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ViewState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }

            is ViewState.Success<*> -> {
                StoryGoogleMap(
                    stories = state.data as List<Story>,
                    modifier = Modifier.padding(padding)
                )
            }

            else -> Unit
        }
    }
}

@Composable
fun StoryGoogleMap(
    stories: List<Story>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                stories.first().lat.toDouble(),
                stories.first().lon.toDouble()
            ),
            4f
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        stories.forEach { story ->
            Marker(
                state = MarkerState(
                    position = LatLng(
                        story.lat.toDouble(),
                        story.lon.toDouble()
                    )
                ),
                title = story.name,
                snippet = story.description
            )
        }
    }
}

