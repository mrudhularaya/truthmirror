package com.personal.truthmirror.services

import com.personal.truthmirror.entities.JournalEntry
import com.personal.truthmirror.entities.MoodLog
import com.personal.truthmirror.repositories.JournalEntryRepository
import com.personal.truthmirror.repositories.MoodLogRepository
import com.personal.truthmirror.vos.request.JournalEntryRequest
import com.personal.truthmirror.vos.response.JournalEntryResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
class JournalEntryService {

    @Autowired
    JournalEntryRepository journalEntryRepository

    private static final Logger log = LoggerFactory.getLogger(JournalEntryService)


    //Fetches all journal entries by userId
    List<JournalEntryResponse> getAllJournalEntries(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Fetching all journal entries for user: ${userId}")

        List<JournalEntryResponse> journalEntries = journalEntryRepository.findByUserIdOrderByTimestampDesc(userId)
                .collect { entry ->
                    log.debug("Processing journal entry: ${entry} for user: ${userId}")
                    new JournalEntryResponse(
                        content: entry.content,
                        sentimentLabel: entry.sentimentLabel,
                        journalEntryId: entry.id,
                        timeStamp: entry.timestamp.toLocalDate()
                    )
                }
        if(journalEntries.isEmpty()) {
            return []
        }
        return journalEntries
    }

    //Fetches today's journal entry by userId
    JournalEntryResponse getJournalEntry() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Fetching today's journal entry for user: ${userId}")

        def todayEntry = journalEntryRepository.findByUserIdAndDate(userId, LocalDateTime.now().toLocalDate())

        if (todayEntry) {
            log.debug("Found today's journal entry for user: ${userId}")
            return new JournalEntryResponse(
                content: todayEntry.content,
                sentimentLabel: todayEntry.sentimentLabel,
                journalEntryId: todayEntry.id,
            )
        } else {
            log.debug("No journal entry found for today for user: ${userId}")
            return new JournalEntryResponse(
                content: "",
                sentimentLabel: null,
                journalEntryId: null,
            )
        }
    }

    //Creates or updates a new journal entry for today
    JournalEntryResponse createJournalEntry(JournalEntryRequest journalEntryRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Creating journal entry for user: ${userId}")

        //If entry exists then update it or if new entry then create it

        def existingEntry = journalEntryRepository.findByUserIdAndDate(userId, LocalDateTime.now().toLocalDate())

        if(existingEntry){
            existingEntry.with {
                content = journalEntryRequest.content
                timestamp = LocalDateTime.now()
            }

            journalEntryRepository.save(existingEntry)
            log.info("Updated existing journal entry for user: ${userId}")

            return new JournalEntryResponse(
                    message: "Journal entry updated successfully",
                    content: existingEntry.content,
                    sentimentLabel: existingEntry.sentimentLabel,
                    journalEntryId: existingEntry.id
            )
        }
        else{
            def newEntry = new JournalEntry(
                    content: journalEntryRequest.content,
                    userId: userId,
                    timestamp: LocalDateTime.now(),
            )

            journalEntryRepository.save(newEntry)
            log.info("Created new journal entry for user: ${userId}")

            return new JournalEntryResponse(
                    message: "Journal entry created successfully",
                    content: newEntry.content,
                    sentimentLabel: newEntry.sentimentLabel,
                    journalEntryId: newEntry.id
            )
        }

    }
}
