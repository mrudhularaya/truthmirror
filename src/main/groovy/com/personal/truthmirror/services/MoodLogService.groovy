package com.personal.truthmirror.services

import com.personal.truthmirror.entities.MoodLog
import com.personal.truthmirror.repositories.MoodLogRepository
import com.personal.truthmirror.vos.EmotionEntry
import com.personal.truthmirror.vos.response.DailyMoodResponse
import com.personal.truthmirror.vos.response.MoodStreakResponse
import com.personal.truthmirror.vos.response.MoodType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MoodLogService {

    @Autowired
    MoodLogRepository moodLogRepository

    private static final Logger log = LoggerFactory.getLogger(MoodLogService)

    //Classify emotion into valence category
    static String classifyValence(String emotion) {
        if (emotion == null) return "neutral"

        Set<String> positive = Set.of("joy", "love", "surprise", "excitement", "optimism")
        Set<String> negative = Set.of("anger", "sadness", "fear", "disgust", "pessimism")

        if (positive.contains(emotion.toLowerCase())) {
            return MoodType.POSITIVE
        } else if (negative.contains(emotion.toLowerCase())) {
            return MoodType.NEGATIVE
        } else {
            return MoodType.NEUTRAL
        }
    }

    //Get Mood Log today
    MoodLog getTodayMoodLog(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Fetching today's mood log for user: ${userId}")

        def todayMood = moodLogRepository.findByUserIdAndDate(userId, LocalDateTime.now().toLocalDate())

        if (todayMood) {
            log.debug("Found today's mood log for user: ${userId}")
            return todayMood
        } else {
            log.debug("No mood log found for today for user: ${userId}")
            return null
        }
    }

    //Get Mood Log history
    List<MoodLog> getMoodLogs(String range){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Fetching mood logs for user: ${userId}")

        LocalDateTime now = LocalDateTime.now()
        LocalDateTime startDate;
        switch (range){
            case "7d":
                startDate = now.minusDays(7)
                break
            case "1m":
                startDate = now.minusMonths(1)
                break
            case "3m":
                startDate = now.minusMonths(3)
                break
            default:
                startDate = now.minusDays(7)
                break
        }
        return moodLogRepository.findInRangeByUserIdOrderByTimestamp(userId, startDate, now)
    }

    //Get Mood Log calendar (one entry per day)
    List<MoodLog> getMoodCalendar(String range){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Fetching mood calendar for user: ${userId}")

        List<MoodLog> allMoods = getMoodLogs(range)

        return allMoods.stream().map( moodLog -> {
            EmotionEntry topEmotion = moodLog.getEmotion().stream().max(Comparator.comparingDouble { it.confidence }).orElse(null)
            return new DailyMoodResponse(
                    date: moodLog.timestamp.toLocalDate(),
                    emotion: moodLog.emotion,
                    scores: moodLog.scores,
                    journalEntryId: moodLog.journalEntryId,
                    primaryEmotion: topEmotion != null ? topEmotion.emotion : null,
                    valence: topEmotion != null ? classifyValence(topEmotion?.emotion): null

            )
        }).toList()
    }

    MoodStreakResponse calculateStreak() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName()
        log.debug("Calculating mood streak for user: ${userId}")

        List<MoodLog> allMoods = moodLogRepository.findByUserIdOrderByTimestampDesc(userId)

        if (allMoods.isEmpty()) {
            return new MoodStreakResponse(MoodType.NEUTRAL, 0)
        }

        String currentMoodType = classifyValence(allMoods.get(0).emotion?.get(0)?.emotion)

        int streakCount = 1

        LocalDate prevDate = allMoods.get(0).timestamp.toLocalDate()

        for(int i = 1; i < allMoods.size(); i++) {
            MoodLog moodLog = allMoods.get(i)
            LocalDate moodDate = moodLog.timestamp.toLocalDate()
            String moodType = classifyValence(moodLog.emotion?.get(0)?.emotion)

            // Check if the mood type is the same and the date is consecutive
            if (moodType == currentMoodType && (prevDate.minusDays(1).isEqual(moodDate))) {
                streakCount++
                prevDate = moodDate
            } else if (moodType != currentMoodType) {
                break // Streak broken by different mood
            } else if (!prevDate.minusDays(1).isEqual(moodDate)) {
                break // Streak broken by non-consecutive date
            }
        }

        return new MoodStreakResponse(moodType: currentMoodType, length: streakCount)

    }

}
