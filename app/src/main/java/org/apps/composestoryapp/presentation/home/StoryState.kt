package org.apps.composestoryapp.presentation.home

import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.model.Story

data class StoryState(
    val storyListState: ViewState<List<Story>> = ViewState.Idle,
    val storyState: ViewState<Story> = ViewState.Idle
)
