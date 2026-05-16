package com.stremflix.ui.util

import com.stremflix.core.domain.model.Result
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.model.ContentItem
import com.stremflix.data.repository.ContentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

suspend fun checkTraktEnabled(preferencesDataSource: PreferencesDataSource): Boolean {
    val prefs = preferencesDataSource.preferencesFlow.first()
    return !prefs.traktClientId.isNullOrBlank()
}

suspend fun populateImages(items: List<ContentItem>, contentRepository: ContentRepository): List<ContentItem> {
    val result = mutableListOf<ContentItem>()
    val chunks = items.chunked(3)

    for (chunk in chunks) {
        result.addAll(
            coroutineScope {
                chunk.map { item ->
                    async {
                        if (item.posterUrl.isNullOrEmpty()) {
                            (contentRepository.getDetails(item.id, item.type) as? Result.Success)?.data ?: item
                        } else item
                    }
                }.awaitAll()
            }
        )
        delay(300L)
    }
    return result
}