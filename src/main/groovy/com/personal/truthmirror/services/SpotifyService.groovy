package com.personal.truthmirror.services

import com.personal.truthmirror.vos.response.PlaylistResponse
import com.personal.truthmirror.vos.response.SpotifyPlaylist
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate


@Service
class SpotifyService {

    @Value('${spotify.clientId}')
    private String clientId

    @Value('${spotify.clientSecret}')
    private String clientSecret

    @Value('${spotify.authUrl}')
    private String authUrl

    @Value('${spotify.searchUrl}')
    private String searchUrl

    private final RestTemplate restTemplate = new RestTemplate()
    private String accessToken
    private long tokenExpiry = 0

    private static final Logger log = LoggerFactory.getLogger(SpotifyService)

    private final Map<String, Map<String, Object>> playlistCache = [:]

    private static final long CACHE_TTL_MS = 10 * 60 * 1000 // 10 minutes


    private static final Map<String, String> MOOD_QUERY_MAP = [
            positive: "happy upbeat energy motivation",
            negative: "calm relaxing soothing chill healing",
            neutral: "focus study background"
    ]

    //Mood specific fallbacks
    private static final Map<String, Map<String, Object>> MOOD_FALLBACK_MAP = [
            positive: [
                    id      : "5RZLrWrQKYSuhapddiGeey",
                    name    : "High energy mix 🔥",
                    url     : "https://open.spotify.com/playlist/5RZLrWrQKYSuhapddiGeey",
                    imageUrl: "https://mosaic.scdn.co/640/ab67616d00001e0232e4bffd94689ab199ecd1f0ab67616d00001e025675e83f707f1d7271e5cf8aab67616d00001e025d29fc4c51a450bd32135de1ab67616d00001e02be82673b5f79d9658ec0a9fd",
                    owner   : "Jay"
            ],
            negative: [
                    id      : "6sPkDFYJLQ1eNNjURZbAoZ",
                    name    : "Deep Sleep Music 528 Hz 😴",
                    url     : "https://open.spotify.com/playlist/6sPkDFYJLQ1eNNjURZbAoZ",
                    imageUrl: "https://image-cdn-ak.spotifycdn.com/image/ab67706c0000d72c1afc0586377d28c482e656c6",
                    owner   : "Calmly"
            ],
            neutral: [
                    id      : "14KtkIpsvzDSCXR24EqHCL",
                    name    : "Deep Focus Music",
                    url     : "https://open.spotify.com/playlist/14KtkIpsvzDSCXR24EqHCL",
                    imageUrl: "https://image-cdn-ak.spotifycdn.com/image/ab67706c0000d72cbaf016c6f09cc2e9eb6ba345",
                    owner   : "Brainy"
            ]
    ]

    private String getAccessToken() {
        long currentTime = System.currentTimeMillis()

        if(accessToken && currentTime < tokenExpiry) {
            return accessToken
        }

        HttpHeaders headers = new HttpHeaders(contentType: MediaType.APPLICATION_FORM_URLENCODED)
        String auth = "${clientId}:${clientSecret}".bytes.encodeBase64().toString()
        headers.set("Authorization", "Basic ${auth}")

        def body = new LinkedMultiValueMap<String, String>()
        body.add("grant_type", "client_credentials")

        def entity = new HttpEntity<>(body, headers)

        def response = restTemplate.exchange(authUrl, HttpMethod.POST, entity, Map)

        def data = response.body

        if (data != null && data.access_token != null) {
            accessToken = data.access_token as String
            int expiration = data.expires_in as Integer
            tokenExpiry = currentTime + (expiration - 60) * 1000 // Refresh 1 minute before expiry

        }
        return accessToken
    }

    PlaylistResponse getPlaylistsForMood(String mood) {
        String token = getAccessToken()

        long currentTime = System.currentTimeMillis()

        def cached = playlistCache[mood]
        if (cached && cached.expiry as Long > currentTime) {
            log.debug("Returning cached playlists for mood: ${mood}")
            return new PlaylistResponse(
                    mood: mood.toLowerCase(),
                    playlists: cached.playlists as List<SpotifyPlaylist>,
                    fromCache: true
            )
        }
        String query = MOOD_QUERY_MAP.get(mood?.toLowerCase(), "focus study background")
        HttpHeaders headers = new HttpHeaders(authorization: "Bearer ${token}".toString())
        def entity = new HttpEntity<>(headers)
        try {
            def response = restTemplate.exchange(
                    "${searchUrl}?q=${query}&type=playlist&limit=10&market=US",
                    HttpMethod.GET,
                    entity,
                    Map
            ).body as Map

            def items = response.playlists?.items as List<Map<String, Object>>
            def playlists = items?.findAll { item -> item != null }?.collect({ item ->
                new SpotifyPlaylist(
                        id: item.id,
                        name: item.name,
                        url: item.external_urls?.spotify,
                        imageUrl: (item.images && item.images.size() > 0) ? item.images[0].url : null,
                        owner: item.owner?.display_name
                )
            })

            if (!playlists || playlists.isEmpty()) {
                return new PlaylistResponse(
                        mood: mood,
                        playlists: [fallbackPlaylist(mood)],
                        fromCache: false
                )
            }

            playlistCache[mood] = [
                    playlists: playlists,
                    expiry   : currentTime + CACHE_TTL_MS
            ]

            return new PlaylistResponse(
                    mood: mood,
                    playlists: playlists,
                    fromCache: false
            )
        }catch (Exception e) {
            log.error("Error fetching playlists from Spotify: ${e.message}")
            log.info("Returning fallback playlist for mood: ${mood}")
            return new PlaylistResponse(
                    mood: mood,
                    playlists: [fallbackPlaylist(mood)],
                    fromCache: false
            )
        }
    }

    private static SpotifyPlaylist fallbackPlaylist(String mood){
        def fb = MOOD_FALLBACK_MAP[mood] ?: MOOD_FALLBACK_MAP["neutral"]
        return new SpotifyPlaylist(
                id: fb.id,
                name: fb.name,
                url: fb.url,
                imageUrl: fb.imageUrl,
                owner: fb.owner
        )
    }

}
