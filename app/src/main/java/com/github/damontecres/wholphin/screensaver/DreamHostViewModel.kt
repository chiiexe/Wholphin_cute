package com.github.damontecres.wholphin.screensaver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.github.damontecres.wholphin.data.ServerRepository
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.services.ImageUrlService
import com.github.damontecres.wholphin.ui.SlimItemFields
import com.github.damontecres.wholphin.util.ApiRequestPager
import com.github.damontecres.wholphin.util.GetItemsRequestHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.request.GetItemsRequest
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DreamHostViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val imageUrlService: ImageUrlService
) : ViewModel() {

    private val _backdropUrl = MutableStateFlow<String?>(null)
    val backdropUrl = _backdropUrl.asStateFlow()

    init {
        startScreensaverLoop()
    }

    private fun startScreensaverLoop() {
        viewModelScope.launch {
            while (true) {
                val items = fetchRandomLibraryItems()
                if (items.isNotEmpty()) {
                    for (item in items) {
                        val imageUrl = imageUrlService.getItemImageUrl(item, ImageType.BACKDROP)
                        _backdropUrl.value = imageUrl
                        delay(60.seconds)
                    }
                } else {
                    // if no items, wait a bit before retrying
                    delay(30.seconds)
                }
            }
        }
    }

    suspend fun fetchRandomLibraryItems(): List<BaseItem> {
        return try {
            val currentUser = serverRepository.current.asFlow().filterNotNull().first()
            val api = serverRepository.apiClient
            val userViews by api.userViewsApi.getUserViews(userId = currentUser.user.id)

            val allItems = mutableListOf<BaseItem>()
            userViews.items.forEach { view ->
                val request = GetItemsRequest(
                    parentId = view.id,
                    sortBy = listOf(ItemSortBy.RANDOM),
                    includeItemTypes = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
                    recursive = true,
                    fields = SlimItemFields,
                    limit = 20
                )
                val pager = ApiRequestPager(api, request, GetItemsRequestHandler, viewModelScope)
                pager.init()
                for (i in 0 until pager.size) {
                    pager.getBlocking(i)?.let { allItems.add(it) }
                }
            }
            allItems.shuffled()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching items for screensaver")
            emptyList()
        }
    }
}
