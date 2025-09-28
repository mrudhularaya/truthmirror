package com.personal.truthmirror.vos.response

enum MoodType {
    POSITIVE('positive'),
    NEGATIVE('negative'),
    NEUTRAL('neutral')

    private String moodType

    MoodType(String moodType) {
        this.moodType = moodType
    }
}