package org.apps.composestoryapp.model

data class StoryResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>
)

data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String?,
    val createdAt: String
)

data class StoryDetailResponse(
    val error: Boolean,
    val message: String,
    val story: Story
)

data class AddStoryResponse(
    val error: Boolean,
    val message: String,
)
