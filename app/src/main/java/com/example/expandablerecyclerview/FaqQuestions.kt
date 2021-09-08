package com.example.expandablerecyclerview

import androidx.annotation.StringRes

enum class FaqQuestions(@StringRes val question: Int, @StringRes val answer: Int) {
    QUESTION_ONE(R.string.one, R.string.answerOne),
    QUESTION_TWO(R.string.two, R.string.answerTwo),
    QUESTION_THREE(
        R.string.three,
        R.string.answerThree
    )
}