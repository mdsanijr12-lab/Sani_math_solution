package com.example.data.model

data class SolutionStep(
    val stepNumber: Int = 1,
    val title: String = "",
    val calculation: String = "",
    val reasoning: String = ""
)

data class MathSolution(
    val isMathQuestion: Boolean = true,
    val isAmbiguous: Boolean = false,
    val nonMathExplanation: String = "",
    val topic: String = "সাধারণ গণিত",
    val questionSummary: String = "",
    val shortAnswer: String = "",
    val givenInfo: List<String> = emptyList(),
    val requiredResult: String = "",
    val formula: List<String> = emptyList(),
    val steps: List<SolutionStep> = emptyList(),
    val reasoning: String = "",
    val finalAnswer: String = "",
    val verificationNote: String = ""
)
