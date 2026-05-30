package com.stremflix.data.remote

import com.stremflix.core.util.ApiEndpoints
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val ADVISORY_MAPPING = mapOf(
    "VIOLENCE" to mapOf("mild" to "Violence", "moderate" to "Intense Violence", "severe" to "Graphic Violence"),
    "SEXUAL_CONTENT" to mapOf("mild" to "Sexual Situations", "moderate" to "Sexual Content", "severe" to "Explicit Sexual Content"),
    "PROFANITY" to mapOf("mild" to "Mild Language", "moderate" to "Strong Language", "severe" to "Inadequate Language"),
    "ALCOHOL_DRUGS" to mapOf("mild" to "Substance References", "moderate" to "Substance Use", "severe" to "Heavy Substance Use"),
    "FRIGHTENING_INTENSE_SCENES" to mapOf("mild" to "Tense Scenes", "moderate" to "Frightening Scenes", "severe" to "Extreme Terror")
)
@Serializable
data class SeverityBreakdown(
    val severityLevel: String,
    val voteCount: Int = 0
)

@Serializable
data class ParentsGuideCategory(
    val category: String,
    val severityBreakdowns: List<SeverityBreakdown> // or List<SeverityBreakdown>
)

@Serializable
data class ParentsGuideResponse(
    val parentsGuide: List<ParentsGuideCategory> // may be null or missing
)

@Singleton
class ImdbDevApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    suspend fun getParentsGuide(imdbId: String):List<String>? {
        return try {
            val url = ApiEndpoints.IMDB_BASE + "titles/${imdbId}/parentsGuide"
            val responseString: String = httpClient.get(url).body() // get raw string

            val json = Json { ignoreUnknownKeys = true }
            val response = json.decodeFromString<ParentsGuideResponse>(responseString)

            val reasons = mutableListOf<String>()

            for (categoryData in response.parentsGuide) {
                val categoryName = categoryData.category
                val breakdowns = categoryData.severityBreakdowns

                // Find the severity level with the highest vote count
                var bestSeverity: String? = null
                var maxVotes = -1

                for (breakdown in breakdowns) {
                    val severity = breakdown.severityLevel.lowercase()
                    val votes = breakdown.voteCount
                    if (votes > maxVotes) {
                        maxVotes = votes
                        bestSeverity = severity
                    }
                }

                // Only include if there's a severity with votes (ignore "none" if it has 0 votes and no other votes)
                // Also skip if bestSeverity is "none" (usually not an advisory)
                if (bestSeverity != null && maxVotes > 0 && bestSeverity != "none") {
                    // Map category + severity to your internal advisory string
                    val reason = ADVISORY_MAPPING[categoryName]?.get(bestSeverity)
                    if (reason != null) {
                        reasons.add(reason)
                    } else {
                        // Log missing mapping for debugging
                        println("No mapping for $categoryName -> $bestSeverity")
                    }
                }
            }

            reasons.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}