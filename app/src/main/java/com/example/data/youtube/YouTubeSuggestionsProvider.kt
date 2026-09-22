package com.example.data.youtube

object YouTubeSuggestionsProvider {

    /**
     * Built-in curated suggestions for wedding and event sound operators.
     * No scraping of unofficial autocomplete endpoints.
     */
    val BUILT_IN_SUGGESTIONS = listOf(
        "M7lc1UVf-VE",
        "Mehndi Songs",
        "Mehndi Dance",
        "Baraat Entry",
        "Bride Entry",
        "Groom Entry",
        "Walima Songs",
        "Wedding Dance",
        "Slow Songs",
        "DJ Songs",
        "Party Songs",
        "Pakistani Wedding Songs",
        "Indian Wedding Songs",
        "Aaj Ki Party",
        "Arijit Singh",
        "Baraat Dhol Beat",
        "Shehnai Mangal Dhwani",
        "Sufi Wedding Qawwali",
        "First Dance Couple Song",
        "Cocktail Party DJ Mashup"
    )

    /**
     * Generates sensible YouTube search queries mapped from Sound Operator categories.
     */
    fun getCategoryYouTubeQuery(categorySlug: String): String {
        return when (categorySlug.trim().lowercase()) {
            "mehndi" -> "Mehndi Wedding Songs"
            "baraat" -> "Baraat Entry Songs"
            "walima" -> "Walima Songs"
            "dance" -> "Wedding Dance Songs"
            "slow" -> "Slow Romantic Wedding Songs"
            "entry" -> "Bride Groom Entry Songs"
            "dj" -> "DJ Wedding Songs"
            else -> "$categorySlug Wedding Songs"
        }
    }

    /**
     * Generates instant local suggestions as the user types based on:
     * 1. Recent searches
     * 2. Frequently searched queries
     * 3. Built-in wedding & event suggestions
     * 4. App categories
     */
    fun getSuggestions(
        rawQuery: String,
        recentQueries: List<String> = emptyList(),
        categorySlugs: List<String> = listOf("mehndi", "baraat", "walima", "dance", "slow", "entry", "dj")
    ): List<String> {
        val query = rawQuery.trim().replace("\\s+".toRegex(), " ").lowercase()

        // If query is empty, show top recent queries or fallback to primary built-in
        if (query.isEmpty()) {
            return recentQueries.take(8).ifEmpty { BUILT_IN_SUGGESTIONS.take(8) }
        }

        val allCandidates = LinkedHashSet<String>()
        // 1. Recent user queries first
        allCandidates.addAll(recentQueries)
        // 2. Built-in wedding event suggestions
        allCandidates.addAll(BUILT_IN_SUGGESTIONS)
        // 3. Category queries
        categorySlugs.forEach { slug ->
            allCandidates.add(getCategoryYouTubeQuery(slug))
        }

        val startsWithMatches = mutableListOf<String>()
        val wordStartsMatches = mutableListOf<String>()
        val containsMatches = mutableListOf<String>()

        for (candidate in allCandidates) {
            val lower = candidate.lowercase()
            // Avoid suggesting exact identical string
            if (lower == query) continue

            if (lower.startsWith(query)) {
                startsWithMatches.add(candidate)
            } else {
                val words = lower.split("\\s+".toRegex())
                if (words.any { it.startsWith(query) }) {
                    wordStartsMatches.add(candidate)
                } else if (lower.contains(query)) {
                    containsMatches.add(candidate)
                }
            }
        }

        return (startsWithMatches + wordStartsMatches + containsMatches)
            .distinctBy { it.lowercase() }
            .take(8)
    }
}
