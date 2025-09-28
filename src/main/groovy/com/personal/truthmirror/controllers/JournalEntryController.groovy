package com.personal.truthmirror.controllers

import com.personal.truthmirror.entities.MoodLog
import com.personal.truthmirror.services.EmotionAnalysisService
import com.personal.truthmirror.services.JournalEntryService
import com.personal.truthmirror.services.MoodLogService
import com.personal.truthmirror.services.SpotifyService
import com.personal.truthmirror.vos.request.AnalyzeJournalRequest
import com.personal.truthmirror.vos.request.JournalEntryRequest
import com.personal.truthmirror.vos.response.EmotionResponse
import com.personal.truthmirror.vos.response.JournalEntryResponse
import com.personal.truthmirror.vos.response.MoodStreakResponse
import com.personal.truthmirror.vos.response.PlaylistResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/journals"])
class JournalEntryController {

    @Autowired
    JournalEntryService journalEntryService

    @Autowired
    EmotionAnalysisService emotionAnalysisService

    @Autowired
    MoodLogService moodLogService

    @Autowired
    SpotifyService spotifyService

    //Gets a list of all past entries associated with the authenticated user
    @GetMapping("/history")
    ResponseEntity<List<JournalEntryResponse>> getAllJournalEntries() {
        return new ResponseEntity<List<JournalEntryResponse>>(journalEntryService.getAllJournalEntries(), HttpStatus.OK)
    }

    //Gets the journal entry for today, if it exists
    @GetMapping("/today")
    ResponseEntity<JournalEntryResponse> getJournalEntry() {
        return new ResponseEntity<JournalEntryResponse>(journalEntryService.getJournalEntry(), HttpStatus.OK)
    }

    //Get MoodLog for today, if it exists
    @GetMapping('/mood/today')
    ResponseEntity<MoodLog> getTodayMoodLog() {
        return new ResponseEntity<MoodLog>(moodLogService.getTodayMoodLog(), HttpStatus.OK)
    }

    //Get MoodLog history
    @GetMapping('/moods')
    ResponseEntity<List<MoodLog>> getMoodLogs(@RequestParam(name="range", defaultValue = "7d") String range) {
        return new ResponseEntity<List<MoodLog>>(moodLogService.getMoodLogs(range), HttpStatus.OK)
    }

    //Get MoodLog daily history
    @GetMapping('/moods/daily')
    ResponseEntity<List<MoodLog>> getMoodCalendar(@RequestParam(name="range", defaultValue = "1m") String range) {
        return new ResponseEntity<List<MoodLog>>(moodLogService.getMoodCalendar(range), HttpStatus.OK)
    }

    //Calculate streak
    @GetMapping('/moods/streak')
    ResponseEntity<MoodStreakResponse> getMoodStreak() {
        return new ResponseEntity<MoodStreakResponse>(moodLogService.calculateStreak(), HttpStatus.OK)
    }

    //Get Spotify playlists
    @GetMapping('/playlists')
    ResponseEntity<PlaylistResponse> getSpotifyPlaylists(@RequestParam(name="mood", required = true) String mood) {
        return new ResponseEntity<PlaylistResponse>(spotifyService.getPlaylistsForMood(mood), HttpStatus.OK)
    }


    //Create today's journal entry
    @PostMapping()
    ResponseEntity<JournalEntryResponse> createJournalEntry(
            @RequestBody(required = true) JournalEntryRequest journalEntryRequest
    ) {
        return new ResponseEntity<JournalEntryResponse>(journalEntryService.createJournalEntry(journalEntryRequest), HttpStatus.CREATED)
    }

    //Analyze the journal entry
    @PostMapping('/analyze')
    ResponseEntity<EmotionResponse> analyzeJournalEntry(@RequestBody AnalyzeJournalRequest analyzeJournalRequest) {
        return new ResponseEntity<EmotionResponse>(emotionAnalysisService.analyzeEmotion(analyzeJournalRequest.journalEntryId), HttpStatus.CREATED)
    }

}
