package org.apps.composestoryapp.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.apps.composestoryapp.model.Story
import org.apps.composestoryapp.remote.StoryApiService

class StoryPagingSource(
    private val api: StoryApiService
) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {
            val page = params.key ?: 1
            val response = api.getAllStories(
                page = page,
                size = params.loadSize,
                location = null
            )

            LoadResult.Page(
                data = response.listStory,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.listStory.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}
