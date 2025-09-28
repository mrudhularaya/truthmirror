package com.personal.truthmirror.vos.response

import com.personal.truthmirror.vos.EmotionEntry

import java.time.LocalDate

class DailyMoodResponse {
    LocalDate date
    List<EmotionEntry> emotion
    Map<String, Double> scores
    Long journalEntryId
    String primaryEmotion
    String valence

}
