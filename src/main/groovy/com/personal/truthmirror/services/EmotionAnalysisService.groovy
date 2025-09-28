package com.personal.truthmirror.services

import com.personal.truthmirror.entities.JournalEntry
import com.personal.truthmirror.entities.MoodLog
import com.personal.truthmirror.repositories.JournalEntryRepository
import com.personal.truthmirror.repositories.MoodLogRepository
import com.personal.truthmirror.vos.EmotionEntry
import com.personal.truthmirror.vos.request.EmotionRequest
import com.personal.truthmirror.vos.response.EmotionResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

import java.time.LocalDateTime


@Service
class EmotionAnalysisService {

    @Autowired
    MoodLogRepository moodLogRepository

    @Autowired
    JournalEntryRepository journalEntryRepository

    private final RestTemplate restTemplate
    private final String baseUrl

    private static final Logger log = LoggerFactory.getLogger(EmotionAnalysisService)


    EmotionAnalysisService(@Value('${emotion.service.baseUrl}') String baseUrl) {
        this.restTemplate = new RestTemplate()
        this.baseUrl = baseUrl
    }

    EmotionResponse analyzeEmotion(Long journalEntryId) {
        try {
            //Get journal with ID
            String userId = SecurityContextHolder.getContext().getAuthentication().getName()
            JournalEntry journalEntry = journalEntryRepository.findByUserIdAndId(userId, journalEntryId)

            if(journalEntry){
                log.info("Analyzing emotion for journal entry ID: ${journalEntryId} for user: ${userId}")
                def request = new EmotionRequest(text: journalEntry.content)
                EmotionResponse response = restTemplate.postForObject(
                        "${baseUrl}/analyze",
                        request,
                        EmotionResponse.class
                )
                log.info("Emotion analysis response: ${response}")

                if (response == null || response.emotions == null || response.emotions.isEmpty()) {
                    log.warn("Invalid response received from emotion analysis service for journalEntryId: ${journalEntryId}")
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from emotion analysis service"
                    )
                }

                //Update journal entry with sentiment score and sentiment label -- most confident emotion
                journalEntry.with {
                    it.sentimentScore = response.emotions.getFirst()?.confidence
                    it.sentimentLabel = response.emotions.getFirst()?.emotion
                }
                journalEntryRepository.save(journalEntry)
                log.info("Journal entry updated.")

                MoodLog existingMoodLog = moodLogRepository.findByUserIdAndJournalEntryId(userId, journalEntryId)

                if(existingMoodLog){
                    log.info(("Updating the moodLog info with new emotion analysis"))
                    existingMoodLog.with {
                        it.emotion = response.emotions
                        it.scores = response.scores
                        it.timestamp = LocalDateTime.now()
                    }
                }else{
                    //Create a new MoodLog entry
                    def moodLog = new MoodLog(
                            userId: SecurityContextHolder.getContext().getAuthentication().getName(),
                            emotion: response.emotions,
                            scores: response.scores,
                            journalEntryId:  journalEntryId,
                            timestamp: LocalDateTime.now()
                    )
                    moodLogRepository.save(moodLog)
                }

                log.info("Emotion analysis completed for journalEntryId: ${journalEntryId}, Emotion: ${response.emotions}")
                return response

            } else {
                log.warn("Journal entry with ID ${journalEntryId} not found for user ${userId}.")
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Journal entry with ID ${journalEntryId} not found"
                )
            }
        }
        catch(Exception ex){
            log.error("Error analyzing emotion for journalEntryId: ${journalEntryId}", ex)
            //Defaults to neutral in case emotion analysis fails
            return new EmotionResponse(emotions: [new EmotionEntry(emotion: 'neutral', confidence: 0.0)], scores: [:])
        }

    }


}
