package com.personal.truthmirror.entities

import com.personal.truthmirror.vos.EmotionEntry
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

import java.time.LocalDateTime

@Entity
@Table(name = "mood_log")
class MoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mood_log_id_seq")
    @SequenceGenerator(name = "mood_log_id_seq", sequenceName = "mood_log_id_seq", allocationSize = 1)    @Column(name = "id")
    Long id

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion", nullable = false, columnDefinition = "jsonb")
    List<EmotionEntry> emotion

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scores", nullable = false, columnDefinition = "jsonb")
    Map<String,Double> scores

    @Column(name = "user_id", nullable = false)
    String userId

    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp = LocalDateTime.now()

    @Column(name = "journal_entry_id", nullable = false)
    Long journalEntryId

    @ManyToOne
    @JoinColumn(name = "journal_entry_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_mood_logs_journal_entry"))
    JournalEntry journalEntry


}
