package com.personal.truthmirror.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

import java.time.LocalDateTime

@Entity
@Table(name = "journal_entry")
class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journal_entry_id_seq")
    @SequenceGenerator(name = "journal_entry_id_seq", sequenceName = "journal_entry_id_seq", allocationSize = 1)
    @Column(name = "id")
    Long id

    @Column(name = "content", columnDefinition = "TEXT")
    String content

    @Column(name = "user_id")
    String userId

    @Column(name = "timestamp")
    LocalDateTime timestamp

    @Column(name = "sentiment_score")
    double sentimentScore

    @Column(name = "sentiment_label")
    String sentimentLabel

}
