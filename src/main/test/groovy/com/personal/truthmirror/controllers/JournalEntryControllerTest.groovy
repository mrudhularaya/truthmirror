package com.personal.truthmirror.controllers

import com.personal.truthmirror.services.JournalEntryService
import com.personal.truthmirror.vos.request.JournalEntryRequest
import com.personal.truthmirror.vos.response.JournalEntryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class JournalEntryControllerTest extends Specification {

    JournalEntryController journalEntryController
    JournalEntryService journalEntryService
    JournalEntryRequest journalEntryRequest
    JournalEntryResponse journalEntryResponse

    def setup() {
        journalEntryService = Mock(JournalEntryService)
        journalEntryController = new JournalEntryController(journalEntryService: journalEntryService)
        journalEntryRequest = new JournalEntryRequest(content: "Test entry.")
        journalEntryResponse = new JournalEntryResponse(
            message: "Journal entry created successfully",
            content: "Test entry.",
            sentimentLabel: "Positive"
        )
    }

    def "getAllJournalEntries"() {
        given:

        when:
        ResponseEntity<List<JournalEntryResponse>> response = journalEntryController.getAllJournalEntries()

        then:
        1 * journalEntryService.getAllJournalEntries() >> [journalEntryResponse]
        response.statusCode == HttpStatus.OK
    }

    def "getJournalEntry"() {
        given:

        when:
        ResponseEntity<JournalEntryResponse> response = journalEntryController.getJournalEntry()

        then:
        1 * journalEntryService.getJournalEntry() >> journalEntryResponse
        response.statusCode == HttpStatus.OK
    }

    def "createJournalEntry"() {
        given:

        when:
        ResponseEntity<JournalEntryResponse> response = journalEntryController.createJournalEntry(journalEntryRequest)

        then:
        1 * journalEntryService.createJournalEntry(journalEntryRequest) >> journalEntryResponse
        response.statusCode == HttpStatus.CREATED
    }
}
