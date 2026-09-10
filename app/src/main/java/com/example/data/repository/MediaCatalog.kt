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
            id = "vid_1",
            title = "Top Viral Hits 2026 - Latin Pop & Urban Mix Oficial",
            channel = "Latin Vibes Records",
            duration = "4:18",
            viewCount = "14.2M vistas",
            publishedTime = "hace 2 días",
            thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(24.5)
        ),
        VideoItem(
            id = "vid_2",
            title = "GTA 6 - Nuevo Gameplay Oficial 4K HDR Ray Tracing 60FPS",
            channel = "GamerZone World",
            duration = "12:45",
            viewCount = "8.9M vistas",
            publishedTime = "hace 5 horas",
            thumbnailUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            category = "Gaming",
            qualityOptions = getDefaultQualityOptions(68.0)
        ),
        VideoItem(
            id = "vid_3",
            title = "Deep Focus Synthwave & Lofi Beats para Programar y Estudiar",
            channel = "ChillZone Beats",
            duration = "35:10",
            viewCount = "3.1M vistas",
            publishedTime = "hace 1 semana",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/car-detection.mp4",
            category = "Música",
            qualityOptions = getDefaultQualityOptions(42.0)
        ),
        VideoItem(
            id = "vid_4",
            title = "Los mejores trucos ocultos de Android 15 y One UI que debes activar",
            channel = "Tech Review Pro",
            duration = "8:22",
            viewCount = "950K vistas",
            publishedTime = "hace 3 días",
            thumbnailUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/bolt-detection.mp4",
            category = "Tecnología",
            qualityOptions = getDefaultQualityOptions(31.2)
        ),
        VideoItem(
            id = "vid_5",
            title = "Remix Tendencia TikTok 2026 - Bass Boosted Ultra Dance",
            channel = "TikTok Sounds HQ",
            duration = "2:55",
            viewCount = "22.5M vistas",
            publishedTime = "hace 1 día",
            thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
            category = "Tendencias",
            qualityOptions = getDefaultQualityOptions(18.0)
        ),
        VideoItem(
            id = "vid_6",
            title = "Momentos Históricos del Fútbol Mundial - Goles Imposibles",
            channel = "Sports Arena HD",
            duration = "10:14",
            viewCount = "5.4M vistas",
            publishedTime = "hace 4 días",
            thumbnailUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/classroom.mp4",
            category = "Deportes",
            qualityOptions = getDefaultQualityOptions(52.4)
        ),
        VideoItem(
            id = "vid_7",
            title = "Aprende Inteligencia Artificial desde Cero en 2026",
            channel = "DevCode Master",
            duration = "18:30",
            viewCount = "1.2M vistas",
            publishedTime = "hace 6 días",
            thumbnailUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/store-aisle-detection.mp4",
            category = "Tecnología",
            qualityOptions = getDefaultQualityOptions(45.0)
        ),
        VideoItem(
            id = "vid_8",
            title = "Memes Animados y Caídas Graciosas de la Semana #42",
            channel = "Laugh Lab",
            duration = "6:40",
            viewCount = "4.8M vistas",
            publishedTime = "hace 2 días",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop",
            videoUrl = "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            category = "Tendencias",
            qualityOptions = getDefaultQualityOptions(28.0)
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
        "Bad Bunny remix",
        "GTA 6 trailer 4K",
        "Musica para estudiar",
        "TikTok viral hits",
        "Gym motivation playlist",
        "Anime opening 2026",
        "Lofi hip hop beats",
        "Canciones de fiesta"
    )
}
