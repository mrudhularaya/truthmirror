package com.personal.truthmirror.services

import com.personal.truthmirror.entities.JournalEntry
import com.personal.truthmirror.repositories.JournalEntryRepository
import com.personal.truthmirror.vos.request.JournalEntryRequest
import com.personal.truthmirror.vos.response.JournalEntryResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

import java.time.LocalDateTime

class JournalEntryServiceTest extends Specification{
    JournalEntryService journalEntryService
    JournalEntryRepository journalEntryRepository
    JournalEntryRequest journalEntryRequest
    JournalEntryRequest updatedJournalEntryRequest
    JournalEntryResponse journalEntryResponse
    JournalEntryResponse createdJournalEntryResponse
    SecurityContextHolder securityContextHolder
    SecurityContext securityContext
    Authentication authentication

    def setup() {
        journalEntryRepository = Mock(JournalEntryRepository)
        journalEntryService = new JournalEntryService(journalEntryRepository: journalEntryRepository)
        journalEntryRequest = new JournalEntryRequest(content: "Test entry.")
        updatedJournalEntryRequest = new JournalEntryRequest(content: "Existing entry.")
        journalEntryResponse = new JournalEntryResponse(
            content: "Test entry.",
            sentimentLabel: "Positive",
            journalEntryId: 1L
        )
        createdJournalEntryResponse = new JournalEntryResponse(
                message: "Journal entry created successfully",
                content: "Test entry.",
                sentimentLabel: "Positive",
                journalEntryId: 1L
        )
        securityContext = Mock(SecurityContext)
        securityContextHolder = Mock(SecurityContextHolder)
        authentication = Mock(Authentication)
    }

    def "getAllJournalEntries"() {
        given:
        authentication.getName() >> "testUser" // Define the expected behavior of getName()
        securityContext.getAuthentication() >> authentication // Link Authentication to SecurityContext
        securityContextHolder.setContext(securityContext)
        journalEntryRepository.findByUserIdOrderByTimestampDesc(_) >> [
            new JournalEntry(content: "Test entry 1", sentimentLabel: "Positive", timestamp: LocalDateTime.now()),
            new JournalEntry(content: "Test entry 2", sentimentLabel: "Negative", timestamp: LocalDateTime.now())
        ]

        when:
        List<JournalEntryResponse> entries = journalEntryService.getAllJournalEntries()

        then:
        entries != null
        entries.size() >= 0
    }

    def "getJournalEntry"() {
        given:
        authentication.getName() >> "testUser" // Define the expected behavior of getName()
        securityContext.getAuthentication() >> authentication // Link Authentication to SecurityContext
        securityContextHolder.setContext(securityContext)
        journalEntryRepository.findByUserIdAndDate(_,_) >> new JournalEntry(
                content: "Test entry.",
                sentimentLabel: "Happy",
                timestamp: LocalDateTime.now()
        )

        when:
        JournalEntryResponse entry = journalEntryService.getJournalEntry()

        then:
        entry != null
    }

    def "createJournalEntry creates a new journal entry"() {
        given:
        authentication.getName() >> "testUser" // Define the expected behavior of getName()
        securityContext.getAuthentication() >> authentication // Link Authentication to SecurityContext
        securityContextHolder.setContext(securityContext)
        journalEntryRepository.findByUserIdAndDate(_, _) >> null // Simulate no existing entry for today

        when:
        JournalEntryResponse response = journalEntryService.createJournalEntry(journalEntryRequest)

        then:
        response != null
        response.content == "Test entry."
    }

    def "createJournalEntry updates an existing entry"() {
        given:
        authentication.getName() >> "testUser" // Define the expected behavior of getName()
        securityContext.getAuthentication() >> authentication // Link Authentication to SecurityContext
        securityContextHolder.setContext(securityContext)
        journalEntryRepository.findByUserIdAndDate(_, _) >> new JournalEntry(content: "Existing entry.")

        when:
        JournalEntryResponse response = journalEntryService.createJournalEntry(updatedJournalEntryRequest)

        then:
        response != null
        response.content == "Existing entry."
    }
}
