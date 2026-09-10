package com.example.data.repository

import com.example.data.model.DownloadQualityOption
import com.example.data.model.MediaType
import com.example.data.model.VideoItem
import com.example.data.model.WhatsAppStatusItem

object MediaCatalog {

    fun getDefaultQualityOptions(baseSizeMb: Double): List<DownloadQualityOption> {
        return listOf(
            // Music options
            DownloadQualityOption(
                id = "mp3_70k",
                format = "MP3",
                qualityLabel = "70k",
                mediaType = MediaType.AUDIO,
                approximateSizeBytes = (baseSizeMb * 0.12 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.12)
            ),
            DownloadQualityOption(
                id = "mp3_128k",
                format = "MP3",
                qualityLabel = "128k",
                mediaType = MediaType.AUDIO,
                approximateSizeBytes = (baseSizeMb * 0.22 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.22),
                isRecommended = true
            ),
            DownloadQualityOption(
                id = "mp3_160k",
                format = "MP3",
                qualityLabel = "160k HQ",
                mediaType = MediaType.AUDIO,
                approximateSizeBytes = (baseSizeMb * 0.28 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.28)
            ),
            DownloadQualityOption(
                id = "mp3_320k",
                format = "MP3",
                qualityLabel = "320k HD",
                mediaType = MediaType.AUDIO,
                approximateSizeBytes = (baseSizeMb * 0.55 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.55)
            ),
            DownloadQualityOption(
                id = "m4a",
                format = "M4A",
                qualityLabel = "256k Lossless",
                mediaType = MediaType.AUDIO,
                approximateSizeBytes = (baseSizeMb * 0.45 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.45)
            ),
            // Video options
            DownloadQualityOption(
                id = "mp4_360p",
                format = "MP4",
                qualityLabel = "360p",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 0.4 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.4)
            ),
            DownloadQualityOption(
                id = "mp4_480p",
                format = "MP4",
                qualityLabel = "480p",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 0.65 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 0.65)
            ),
            DownloadQualityOption(
                id = "mp4_720p",
                format = "MP4",
                qualityLabel = "720p HD",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb),
                isRecommended = true
            ),
            DownloadQualityOption(
                id = "mp4_1080p",
                format = "MP4",
                qualityLabel = "1080p FHD",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 2.1 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 2.1)
            ),
            DownloadQualityOption(
                id = "mp4_2k",
                format = "MP4",
                qualityLabel = "2K 60fps",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 4.2 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 4.2)
            ),
            DownloadQualityOption(
                id = "mp4_4k",
                format = "MP4",
                qualityLabel = "4K Ultra HD",
                mediaType = MediaType.VIDEO,
                approximateSizeBytes = (baseSizeMb * 7.5 * 1024 * 1024).toLong(),
                approximateSizeFormatted = String.format("%.1f MB", baseSizeMb * 7.5)
            )
        )
    }

    val sampleVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "DyDfgMOUjCI",
            title = "Billie Eilish - bad guy (Official Music Video)",
            channel = "Billie Eilish",
            duration = "3:26",
            viewCount = "1.3B vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/DyDfgMOUjCI/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=DyDfgMOUjCI",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(28.5)
        ),
        VideoItem(
            id = "d5gf9dXbPi0",
            title = "Billie Eilish - BIRDS OF A FEATHER",
            channel = "Billie Eilish",
            duration = "3:10",
            viewCount = "820M vistas",
            publishedTime = "Reciente",
            thumbnailUrl = "https://i.ytimg.com/vi/d5gf9dXbPi0/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=d5gf9dXbPi0",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(24.0)
        ),
        VideoItem(
            id = "pbMwTqkKSps",
            title = "Billie Eilish - when the party's over",
            channel = "Billie Eilish",
            duration = "3:14",
            viewCount = "940M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/pbMwTqkKSps/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=pbMwTqkKSps",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(22.8)
        ),
        VideoItem(
            id = "BY_XwvKogC8",
            title = "Billie Eilish - CHIHIRO (Official Music Video)",
            channel = "Billie Eilish",
            duration = "5:06",
            viewCount = "95M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/BY_XwvKogC8/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=BY_XwvKogC8",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(36.0)
        ),
        VideoItem(
            id = "suAR1PYFNYA",
            title = "Dua Lipa - Houdini (Official Music Video)",
            channel = "Dua Lipa",
            duration = "3:06",
            viewCount = "215M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/suAR1PYFNYA/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=suAR1PYFNYA",
            category = "Tendencias",
            qualityOptions = getDefaultQualityOptions(25.5)
        ),
        VideoItem(
            id = "4NRXx6U8ABQ",
            title = "The Weeknd - Blinding Lights (Official Video)",
            channel = "The Weeknd",
            duration = "4:20",
            viewCount = "870M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=4NRXx6U8ABQ",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(34.2)
        ),
        VideoItem(
            id = "_51kJ-49aoM",
            title = "Bad Bunny - MONACO (Official Video)",
            channel = "Bad Bunny",
            duration = "4:27",
            viewCount = "380M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/_51kJ-49aoM/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=_51kJ-49aoM",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(38.0)
        ),
        VideoItem(
            id = "eVli-tstM5E",
            title = "Sabrina Carpenter - Espresso (Official Video)",
            channel = "Sabrina Carpenter",
            duration = "3:20",
            viewCount = "410M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/eVli-tstM5E/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=eVli-tstM5E",
            category = "Tendencias",
            qualityOptions = getDefaultQualityOptions(27.0)
        ),
        VideoItem(
            id = "ic8j13piAhQ",
            title = "Taylor Swift - Cruel Summer (Official Audio)",
            channel = "Taylor Swift",
            duration = "3:30",
            viewCount = "390M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/ic8j13piAhQ/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=ic8j13piAhQ",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(29.0)
        ),
        VideoItem(
            id = "T6eK-2OQtew",
            title = "Kendrick Lamar - Not Like Us",
            channel = "Kendrick Lamar",
            duration = "4:34",
            viewCount = "250M vistas",
            publishedTime = "Oficial",
            thumbnailUrl = "https://i.ytimg.com/vi/T6eK-2OQtew/hqdefault.jpg",
            videoUrl = "https://www.youtube.com/watch?v=T6eK-2OQtew",
            category = "Tendencias",
            qualityOptions = getDefaultQualityOptions(37.5)
        )
    )

    val sampleWhatsAppStatuses: List<WhatsAppStatusItem> = listOf(
        WhatsAppStatusItem(
            id = "wa_status_1",
            mediaType = MediaType.VIDEO,
            duration = "0:30",
            timestamp = "Hace 12 min",
            thumbnailUrl = "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=500&auto=format&fit=crop"
        ),
        WhatsAppStatusItem(
            id = "wa_status_2",
            mediaType = MediaType.AUDIO,
            duration = "0:45",
            timestamp = "Hace 25 min",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop"
        ),
        WhatsAppStatusItem(
            id = "wa_status_3",
            mediaType = MediaType.VIDEO,
            duration = "0:15",
            timestamp = "Hace 1 hora",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=500&auto=format&fit=crop"
        ),
        WhatsAppStatusItem(
            id = "wa_status_4",
            mediaType = MediaType.VIDEO,
            duration = "0:28",
            timestamp = "Hace 2 horas",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&auto=format&fit=crop"
        ),
        WhatsAppStatusItem(
            id = "wa_status_5",
            mediaType = MediaType.AUDIO,
            duration = "0:35",
            timestamp = "Hace 3 horas",
            thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop"
        ),
        WhatsAppStatusItem(
            id = "wa_status_6",
            mediaType = MediaType.VIDEO,
            duration = "0:22",
            timestamp = "Hace 4 horas",
            thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop"
        )
    )

    data class QuickSite(
        val name: String,
        val url: String,
        val iconColor: Long,
        val symbolLetter: String,
        val isWaStatus: Boolean = false
    )

    val quickSites: List<QuickSite> = listOf(
        QuickSite("YouTube", "https://m.youtube.com", 0xFFFF0000, "YT"),
        QuickSite("Instagram", "https://www.instagram.com", 0xFFE1306C, "IG"),
        QuickSite("TikTok", "https://www.tiktok.com", 0xFF000000, "TT"),
        QuickSite("Facebook", "https://m.facebook.com", 0xFF1877F2, "FB"),
        QuickSite("WhatsApp Status", "whatsapp://status", 0xFF25D366, "WA", isWaStatus = true),
        QuickSite("X / Twitter", "https://x.com", 0xFF000000, "X"),
        QuickSite("SoundCloud", "https://m.soundcloud.com", 0xFFFF5500, "SC"),
        QuickSite("Vimeo", "https://vimeo.com", 0xFF1AB7EA, "VM")
    )

    val categories = listOf("Todo", "Tendencias", "Música", "Gaming", "Tecnología", "Deportes")

    val trendingKeywords = listOf(
        "Billie Eilish",
        "Bad Bunny",
        "Taylor Swift",
        "Dua Lipa",
        "Kendrick Lamar",
        "The Weeknd",
        "Sabrina Carpenter",
        "TikTok viral hits"
    )
}
