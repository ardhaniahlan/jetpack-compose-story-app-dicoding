package org.apps.composestoryapp.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.presentation.story.StoryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    storyViewModel: StoryViewModel = hiltViewModel()
){
    val stories = storyViewModel.stories.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        storyViewModel.eventFlow.collect { story ->
            when(story){
                is UiEvent.NavigateStoryDetail -> {
                    navController.navigate("storydetail/${story.storyId}")
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        stories.refresh()
    }

    LaunchedEffect(stories.itemCount) {
        if (stories.itemCount > 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Stories",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { navController.navigate("map") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "map",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(
                count = stories.itemCount,
                key = { i -> stories[i]?.story?.id ?: i }
            ) { i ->
                stories[i]?.let { storyUi ->
                    StoryItem(
                        storyUi = storyUi,
                        storyViewModel = storyViewModel,
                        onClick = {
                            navController.navigate("storydetail/${storyUi.story.id}")
                        }
                    )
                }
            }

            stories.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    loadState.refresh is LoadState.Error -> {
                        val e = loadState.refresh as LoadState.Error
                        item {
                            Text(
                                text = e.error.message ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}