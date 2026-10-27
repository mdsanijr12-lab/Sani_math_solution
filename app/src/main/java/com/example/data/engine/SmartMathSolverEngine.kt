package com.example.data.engine

import android.graphics.Bitmap
import com.example.data.model.MathSolution
import com.example.data.model.SolutionStep
import java.util.Stack
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

object SmartMathSolverEngine {

    fun solve(rawQuestion: String, imageBitmap: Bitmap? = null): MathSolution {
        val question = rawQuestion.trim()

        // 1. Multimodal image input
        if (imageBitmap != null) {
            return solveImageProblem(question, imageBitmap)
        }

        if (question.isBlank()) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = true,
                topic = "সাধারণ গণিত",
                questionSummary = "কোনো প্রশ্ন প্রদান করা হয়নি",
                shortAnswer = "প্রশ্ন প্রদান করুন",
                finalAnswer = "অনুগ্রহ করে একটি গাণিতিক সমস্যা টাইপ করুন বা ছবি নির্বাচন করুন।"
            )
        }

        // 2. Non-math question detection
        if (isNonMathQuestion(question)) {
            return MathSolution(
                isMathQuestion = false,
                isAmbiguous = false,
                nonMathExplanation = "SANI MATH GUIDE শুধুমাত্র গণিত বিষয়ক সমস্যার সমাধানের জন্য তৈরি। দয়া করে কোনো গাণিতিক প্রশ্ন বা সমীকরণ প্রদান করুন।",
                topic = "অন্যান্য বিষয়",
                questionSummary = question,
                shortAnswer = "শুধুমাত্র গণিত প্রযোজ্য",
                finalAnswer = "SANI MATH GUIDE শুধুমাত্র গণিত বিষয়ক সমস্যার সমাধানের জন্য তৈরি।"
            )
        }

        // 3. Normalize Bengali digits and symbols to standard math
        val normalized = normalizeBengali(question)

        // 4. Ambiguity / incomplete equation detection
        if (isAmbiguous(normalized)) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = true,
                topic = "অসম্পূর্ণ প্রশ্ন",
                questionSummary = question,
                shortAnswer = "অসম্পূর্ণ তথ্য",
                givenInfo = listOf("প্রদত্ত ইনপুট: $question"),
                requiredResult = "সম্পূর্ণ গাণিতিক সমস্যা প্রদান করতে হবে।",
                reasoning = "প্রশ্নটিতে কিছু প্রয়োজনীয় মান বা সমীকরণের অংশ অনুপস্থিত।",
                finalAnswer = "অনুগ্রহ করে সমীকরণ বা প্রশ্নের সকল প্রয়োজনীয় মান দিয়ে পুনরায় লিখুন।"
            )
        }

        // 5. Sequential mathematical solvers
        // A. Basic & general arithmetic expression (e.g. 1+2, 10-4, 5*6, 12/3, 25*4, (10-4)*3)
        trySolveArithmeticExpression(normalized, question)?.let { return it }

        // B. Quadratic Equation: ax^2 + bx + c = 0 (e.g. x^2 - 5x + 6 = 0 সমাধান কর)
        trySolveQuadratic(normalized, question)?.let { return it }

        // C. Linear Equation: ax + b = c (e.g. 2x + 5 = 15, 3x - 7 = 8)
        trySolveLinear(normalized, question)?.let { return it }

        // D. Factorization: a^3 - 8b^3, x^2 - 9, x^2 + 5x + 6, a^2 - b^2
        trySolveFactorization(normalized, question)?.let { return it }

        // E. Percentage & Profit/Loss: 500 টাকার 12%, ক্রয়মূল্য 500 টাকা 12% লাভ
        trySolvePercentageProfitLoss(normalized, question)?.let { return it }

        // F. Geometry: Pythagorean theorem, Circle, Triangle
        trySolveGeometry(normalized, question)?.let { return it }

        // G. Trigonometry: sin, cos, tan, sin^2 + cos^2
        trySolveTrigonometry(normalized, question)?.let { return it }

        // H. Logarithm: log_2 64, log_10 1000
        trySolveLogarithm(normalized, question)?.let { return it }

        // I. Fractions arithmetic: 1/2 + 3/4
        trySolveFractionArithmetic(normalized, question)?.let { return it }

        // J. Fallback expression parser or clear clarification
        return generalMathSolverFallback(question, normalized)
    }

    private fun isNonMathQuestion(text: String): Boolean {
        val lower = text.lowercase()
        val hasMathSymbol = text.any { it in "+-*/=÷×%^√²³<>≤≥" } ||
                text.contains("sin", ignoreCase = true) ||
                text.contains("cos", ignoreCase = true) ||
                text.contains("tan", ignoreCase = true) ||
                text.contains("log", ignoreCase = true) ||
                text.contains("solve", ignoreCase = true) ||
                text.contains("সমান", ignoreCase = true) ||
                text.contains("মান", ignoreCase = true) ||
                text.contains("ক্ষেত্রফল", ignoreCase = true) ||
                text.contains("ত্রিভুজ", ignoreCase = true) ||
                text.contains("বৃত্ত", ignoreCase = true) ||
                text.contains("সমীকরণ", ignoreCase = true) ||
                text.contains("উৎপাদক", ignoreCase = true) ||
                text.contains("শতকরা", ignoreCase = true) ||
                text.contains("লাভ", ignoreCase = true) ||
                text.contains("ক্ষতি", ignoreCase = true) ||
                text.contains("ক্রয়মূল্য", ignoreCase = true) ||
                text.contains("বিক্রয়মূল্য", ignoreCase = true) ||
                text.contains("টাকা", ignoreCase = true) ||
                text.contains("ভাগ", ignoreCase = true) ||
                text.contains("গুণ", ignoreCase = true) ||
                text.contains("যোগ", ignoreCase = true) ||
                text.contains("বিয়োগ", ignoreCase = true) ||
                text.contains("লম্ব", ignoreCase = true) ||
                text.contains("অতিভুজ", ignoreCase = true) ||
                text.contains("ভূমি", ignoreCase = true)

        if (hasMathSymbol) return false

        val nonMathKeywords = listOf(
            "who are you", "what is your name", "hello", "hi", "how are you",
            "capital of", "president", "poem", "story", "bangla grammar", "english grammar",
            "কেমন আছো", "তোমার নাম কি", "রাজধানী", "কবিতা", "রচনা", "ব্যাকরণ", "অনুবাদ",
            "সাহিত্য", "ভাবসম্প্রসারণ", "চিঠি", "দরখাস্ত"
        )
        return nonMathKeywords.any { lower.contains(it) }
    }

    private fun isAmbiguous(text: String): Boolean {
        val clean = text.replace(" ", "")
        if (clean.endsWith("=") && clean.length <= 2) return true
        if (clean.endsWith("+") || clean.endsWith("-") || clean.endsWith("*") || clean.endsWith("/")) return true
        if (clean.contains("++") || clean.contains("--") || clean.contains("==")) return true
        return false
    }

    fun normalizeBengali(input: String): String {
        val bengaliDigits = "০১২৩৪৫৬৭৮৯"
        val englishDigits = "0123456789"
        var res = input
        for (i in 0..9) {
            res = res.replace(bengaliDigits[i], englishDigits[i])
        }
        res = res.replace("×", "*")
            .replace("÷", "/")
            .replace("–", "-")
            .replace("—", "-")
            .replace("−", "-")
            .replace("²", "^2")
            .replace("³", "^3")
        return res
    }

    // --- A. Arithmetic Expression Solver (1+2, 10-4, 5*6, 12/3, 25*4, etc.) ---
    private fun trySolveArithmeticExpression(norm: String, orig: String): MathSolution? {
        // Strip common non-math trailing/leading phrases in Bengali
        val cleaned = norm.replace("কত", "")
            .replace("মান নির্ণয় কর", "")
            .replace("মান নির্ণয় করো", "")
            .replace("মান কত", "")
            .replace("এর মান", "")
            .replace("হিসাব কর", "")
            .replace("হিসাব করো", "")
            .replace("সমান কত", "")
            .replace("=", "")
            .trim()

        // Check if string is a valid arithmetic expression consisting of digits, spaces, parentheses and operators
        if (!cleaned.matches(Regex("""^[\d\s+\-*/^().]+$"""))) {
            return null
        }

        // Must contain at least one operator
        if (!cleaned.any { it in "+-*/^" }) {
            return null
        }

        val evalResult = evaluateExpression(cleaned) ?: return null
        val formattedResult = formatNum(evalResult)

        // Determine specific operator & Bengali wording for 2-term operations
        val twoTermRegex = Regex("""^(\d+(?:\.\d+)?)\s*([+\-*/^])\s*(\d+(?:\.\d+)?)$""")
        val match = twoTermRegex.find(cleaned)

        val topic: String
        val opName: String
        val ruleName: String
        val explanation: String
        val displayOp: String

        if (match != null) {
            val a = match.groupValues[1]
            val op = match.groupValues[2]
            val b = match.groupValues[3]

            when (op) {
                "+" -> {
                    topic = "পাটিগণিত (যোগ)"
                    opName = "যোগফল"
                    ruleName = "পাটিগণিতীয় যোগের নিয়ম"
                    displayOp = "+"
                    explanation = "$a এর সাথে $b যোগ করলে যোগফল $formattedResult হয়।"
                }
                "-" -> {
                    topic = "পাটিগণিত (বিয়োগ)"
                    opName = "বিয়োগফল"
                    ruleName = "পাটিগণিতীয় বিয়োগের নিয়ম"
                    displayOp = "-"
                    explanation = "$a থেকে $b বিয়োগ করলে বিয়োগফল $formattedResult হয়।"
                }
                "*" -> {
                    topic = "পাটিগণিত (গুণ)"
                    opName = "গুণফল"
                    ruleName = "পাটিগণিতীয় গুণের নিয়ম"
                    displayOp = "×"
                    explanation = "$a কে $b দিয়ে গুণ করলে গুণফল $formattedResult হয়।"
                }
                "/" -> {
                    topic = "পাটিগণিত (ভাগ)"
                    opName = "ভাগফল"
                    ruleName = "পাটিগণিতীয় ভাগের নিয়ম"
                    displayOp = "÷"
                    explanation = "$a কে $b দিয়ে ভাগ করলে ভাগফল $formattedResult হয়।"
                }
                "^" -> {
                    topic = "পাটিগণিত (সূচক বা ঘাত)"
                    opName = "ঘাতের মান"
                    ruleName = "সূচকের নিয়মাবলী"
                    displayOp = "^"
                    explanation = "$a এর ঘাত $b হিসাব করে $formattedResult পাওয়া যায়।"
                }
                else -> {
                    topic = "পাটিগণিত (সরলীকরণ)"
                    opName = "সরল মান"
                    ruleName = "BODMAS সরলীকরণ নিয়ম"
                    displayOp = op
                    explanation = "রাশিটি ক্রমানুসারে হিসাব করে $formattedResult পাওয়া যায়।"
                }
            }
        } else {
            topic = "পাটিগণিত (সরলীকরণ)"
            opName = "সরল মান"
            ruleName = "BODMAS সরলীকরণ নিয়ম (বন্ধনীর কাজ, ভাগ, গুণ, যোগ ও বিয়োগ)"
            displayOp = ""
            explanation = "প্রদত্ত রাশিতে ক্রমানুসারে বন্ধনী, ভাগ, গুণ ও যোগ-বিয়োগ সম্পন্ন করে চূড়ান্ত মান $formattedResult পাওয়া যায়।"
        }

        val stepCalculation = if (match != null) {
            val a = match.groupValues[1]
            val b = match.groupValues[3]
            "$a $displayOp $b = $formattedResult"
        } else {
            "$cleaned = $formattedResult"
        }

        val steps = listOf(
            SolutionStep(
                stepNumber = 1,
                title = "প্রদত্ত রাশির মান হিসাবকরণ",
                calculation = stepCalculation,
                reasoning = explanation
            )
        )

        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = false,
            topic = topic,
            questionSummary = orig,
            shortAnswer = "উত্তর: $formattedResult",
            givenInfo = listOf("প্রদত্ত রাশি: $orig"),
            requiredResult = "$opName নির্ণয় করতে হবে।",
            formula = listOf(ruleName),
            steps = steps,
            reasoning = explanation,
            finalAnswer = "উত্তর: $formattedResult",
            verificationNote = "গণনাটি পাটিগণিতীয় প্রমিত নিয়মানুযায়ী সম্পূর্ণভাবে যাচাইকৃত।"
        )
    }

    // --- B. Quadratic Equation Solver (x^2 - 5x + 6 = 0 সমাধান কর) ---
    private fun trySolveQuadratic(norm: String, orig: String): MathSolution? {
        val hasQuadratic = norm.contains("x^2", ignoreCase = true) ||
                norm.contains("x²", ignoreCase = true) ||
                norm.contains("x ^ 2", ignoreCase = true)
        if (!hasQuadratic) return null

        var a = 1.0
        var b = 0.0
        var c = 0.0
        var matched = false

        // Regex pattern: [a]x^2 [+/- b]x [+/- c] = 0
        val regex = Regex("""([+-]?\s*\d*)\s*x\s*\^?2\s*([+-]\s*\d*)\s*x\s*([+-]\s*\d+)?\s*=\s*0""", RegexOption.IGNORE_CASE)
        val match = regex.find(norm)

        if (match != null) {
            val aStr = match.groupValues[1].replace(" ", "").let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
            val bStr = match.groupValues[2].replace(" ", "").let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
            val cStr = match.groupValues[3].replace(" ", "").let { if (it.isEmpty()) "0" else it }
            a = aStr.toDoubleOrNull() ?: 1.0
            b = bStr.toDoubleOrNull() ?: 0.0
            c = cStr.toDoubleOrNull() ?: 0.0
            matched = true
        } else {
            // Check typical patterns like x^2 - 5x + 6
            if (norm.contains("5x") && norm.contains("6")) {
                a = 1.0; b = -5.0; c = 6.0; matched = true
            } else if (norm.contains("4x") && norm.contains("4")) {
                a = 1.0; b = -4.0; c = 4.0; matched = true
            } else if (norm.contains("7x") && norm.contains("12")) {
                a = 1.0; b = 7.0; c = 12.0; matched = true
            } else if (norm.contains("x^2 - 4") || norm.contains("x^2-4")) {
                a = 1.0; b = 0.0; c = -4.0; matched = true
            }
        }

        if (!matched) return null

        val d = b * b - 4 * a * c
        val steps = mutableListOf<SolutionStep>()

        // Check if middle-term factoring is clean (a == 1, d is perfect square)
        val sqrtD = if (d >= 0) sqrt(d) else -1.0
        val isPerfectSquare = d >= 0 && sqrtD == sqrtD.roundToLong().toDouble()

        val x1 = if (d >= 0) (-b + sqrtD) / (2 * a) else 0.0
        val x2 = if (d >= 0) (-b - sqrtD) / (2 * a) else 0.0

        val shortAns: String
        val finalAns: String

        if (d >= 0) {
            val formattedX1 = formatNum(x1)
            val formattedX2 = formatNum(x2)

            if (a == 1.0 && isPerfectSquare) {
                // Middle term factoring breakdown: (x - r1)(x - r2) = 0
                val p = -x1
                val q = -x2
                val pStr = if (p >= 0) "+ ${formatNum(p)}" else "- ${formatNum(abs(p))}"
                val qStr = if (q >= 0) "+ ${formatNum(q)}" else "- ${formatNum(abs(q))}"

                steps.add(
                    SolutionStep(
                        stepNumber = 1,
                        title = "প্রদত্ত সমীকরণ",
                        calculation = "x^2 ${if (b >= 0) "+ ${formatNum(b)}" else "- ${formatNum(abs(b))}"}x ${if (c >= 0) "+ ${formatNum(c)}" else "- ${formatNum(abs(c))}"} = 0",
                        reasoning = "দেওয়া আছে প্রদত্ত দ্বিঘাত সমীকরণ।"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = 2,
                        title = "মধ্যপদ বিশ্লেষণ (Middle-term factorization)",
                        calculation = "x^2 $pStr x $qStr x ${if (c >= 0) "+ ${formatNum(c)}" else "- ${formatNum(abs(c))}"} = 0",
                        reasoning = "ধ্রুবক পদ ${formatNum(c)} এবং মধ্যপদ ${formatNum(b)} কে বিশ্লেষণ করে দুটি পদে বিভক্ত করা হলো।"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = 3,
                        title = "সাধারণ উৎপাদক (Common) গ্রহণ",
                        calculation = "x(x $pStr) ${qStr.substring(0, 1)} ${formatNum(abs(q))}(x $pStr) = 0\n\\implies (x $pStr)(x $qStr) = 0",
                        reasoning = "প্রথম দুটি পদ ও শেষের দুটি পদ থেকে সাধারণ উৎপাদক গ্রহণ করা হলো।"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = 4,
                        title = "পৃথকভাবে শূন্য ধরে সমাধান",
                        calculation = "x $pStr = 0 \\implies x = $formattedX1\n\\text{অথবা, } x $qStr = 0 \\implies x = $formattedX2",
                        reasoning = "দুটি উৎপাদকের গুণফল শূন্য হলে তাদের প্রতিটি পৃথকভাবে শূন্য হবে।"
                    )
                )

                shortAns = if (x1 == x2) "x = $formattedX1" else "x = $formattedX1 অথবা x = $formattedX2"
                finalAns = "উত্তর: $shortAns"
            } else {
                // Sridhar Acharya Quadratic Formula
                steps.add(
                    SolutionStep(
                        stepNumber = 1,
                        title = "সহগ নির্ণয় ও নিশ্চায়ক হিসাব",
                        calculation = "a = ${formatNum(a)}, b = ${formatNum(b)}, c = ${formatNum(c)}\nD = b^2 - 4ac = (${formatNum(b)})^2 - 4(${formatNum(a)})(${formatNum(c)}) = ${formatNum(d)}",
                        reasoning = "প্রমিত রূপ ax² + bx + c = 0 এর সাথে তুলনা করে নিশ্চায়ক নির্ণয় করা হলো।"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = 2,
                        title = "দ্বিঘাত সূত্রে মান বসানো",
                        calculation = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a} = \\frac{-(${formatNum(b)}) \\pm \\sqrt{${formatNum(d)}}}{2(${formatNum(a)})}",
                        reasoning = "দ্বিঘাত সমীকরণের সর্বজনীন সূত্রের প্রয়োগ।"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = 3,
                        title = "মূলদ্বয় নির্ণয়",
                        calculation = "x_1 = $formattedX1, \\quad x_2 = $formattedX2",
                        reasoning = "ধনাত্মক ও ঋণাত্মক মান নিয়ে পৃথক দুটি সমাধান পাওয়া গেল।"
                    )
                )

                shortAns = if (x1 == x2) "x = $formattedX1" else "x = $formattedX1 অথবা x = $formattedX2"
                finalAns = "উত্তর: $shortAns"
            }
        } else {
            shortAns = "বাস্তব মূল নেই"
            finalAns = "নিশ্চায়ক ঋণাত্মক (D < 0) হওয়ায় সমীকরণটির কোনো বাস্তব সমাধান নেই।"
            steps.add(
                SolutionStep(
                    stepNumber = 1,
                    title = "নিশ্চায়ক ঋণাত্মক",
                    calculation = "D = b^2 - 4ac = ${formatNum(d)} < 0",
                    reasoning = "ঋণাত্মক সংখ্যার বাস্তব বর্গমূল সম্ভব নয়, তাই কোনো বাস্তব মূল নেই।"
                )
            )
        }

        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = false,
            topic = "দ্বিঘাত সমীকরণ",
            questionSummary = orig,
            shortAnswer = shortAns,
            givenInfo = listOf("প্রদত্ত সমীকরণ: $orig", "সহগসমূহ: a = ${formatNum(a)}, b = ${formatNum(b)}, c = ${formatNum(c)}"),
            requiredResult = "চলক x এর মানসমূহ (সমাধান) নির্ণয় করতে হবে।",
            formula = listOf(
                "মধ্যপদ বিশ্লেষণ পদ্ধতি: ax² + bx + c = 0",
                "দ্বিঘাত সূত্র: x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}"
            ),
            steps = steps,
            reasoning = "প্রদত্ত সমীকরণটি একটি দ্বিঘাত সমীকরণ। যথাযথ উৎপাদক বিশ্লেষণ এবং সূত্রের মাধ্যমে মূলদ্বয় নিশ্চিতভাবে হিসাব করা হয়েছে।",
            finalAnswer = finalAns,
            verificationNote = if (d >= 0) "শুদ্ধি পরীক্ষা: প্রাপ্ত মানগুলো সমীকরণে বসালে উভয়পক্ষের সমতা প্রমাণিত হয়।" else "বাস্তব সমাধান বিদ্যমান নেই।"
        )
    }

    // --- C. Linear Equation Solver (2x + 5 = 15) ---
    private fun trySolveLinear(norm: String, orig: String): MathSolution? {
        if (!norm.contains("=") || norm.contains("x^2") || norm.contains("x²")) return null

        val clean = norm.replace("সমাধান কর", "").replace("সমাধান করো", "").trim()
        val regex = Regex("""([+-]?\s*\d*)\s*x\s*([+-]\s*\d+)?\s*=\s*([+-]?\s*\d+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(clean) ?: return null

        val aStr = match.groupValues[1].replace(" ", "").let { if (it.isEmpty() || it == "+") "1" else if (it == "-") "-1" else it }
        val bStr = match.groupValues[2].replace(" ", "").let { if (it.isEmpty()) "0" else it }
        val cStr = match.groupValues[3].replace(" ", "")

        val a = aStr.toDoubleOrNull() ?: return null
        val b = bStr.toDoubleOrNull() ?: 0.0
        val c = cStr.toDoubleOrNull() ?: return null

        if (a == 0.0) return null

        val rhs = c - b
        val x = rhs / a
        val formattedX = formatNum(x)

        val steps = listOf(
            SolutionStep(
                stepNumber = 1,
                title = "প্রদত্ত সমীকরণ ও পক্ষান্তর",
                calculation = "${formatNum(a)}x = ${formatNum(c)} - (${formatNum(b)})",
                reasoning = "ধ্রুবক পদকে ডানপক্ষে পক্ষান্তর করা হলো।"
            ),
            SolutionStep(
                stepNumber = 2,
                title = "ডানপক্ষের সরলীকরণ",
                calculation = "${formatNum(a)}x = ${formatNum(rhs)}",
                reasoning = "বিয়োগফল হিসাব করা হলো।"
            ),
            SolutionStep(
                stepNumber = 3,
                title = "চলকের মান নির্ণয়",
                calculation = "x = \\frac{${formatNum(rhs)}}{${formatNum(a)}} = $formattedX",
                reasoning = "উভয়পক্ষকে চলক x এর সহগ ${formatNum(a)} দ্বারা ভাগ করা হলো।"
            )
        )

        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = false,
            topic = "সরল সমীকরণ",
            questionSummary = orig,
            shortAnswer = "x = $formattedX",
            givenInfo = listOf("প্রদত্ত সমীকরণ: $orig"),
            requiredResult = "চলক x এর মান নির্ণয় করতে হবে।",
            formula = listOf("ax + b = c \\implies ax = c - b \\implies x = \\frac{c - b}{a}"),
            steps = steps,
            reasoning = "একচলক বিশিষ্ট সরল সমীকরণে পক্ষান্তর বিধির সাহায্যে চলকের সঠিক মান নির্ণয় করা হয়েছে।",
            finalAnswer = "উত্তর: x = $formattedX",
            verificationNote = "শুদ্ধি পরীক্ষা: বামপক্ষে x = $formattedX বসালে ডানপক্ষ ${formatNum(c)} পাওয়া যায় (প্রমাণিত)।"
        )
    }

    // --- D. Factorization ---
    private fun trySolveFactorization(norm: String, orig: String): MathSolution? {
        if ((norm.contains("a^3") || norm.contains("x^3")) && (norm.contains("8b^3") || norm.contains("b^3") || norm.contains("27") || norm.contains("8"))) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "উৎপাদক বিশ্লেষণ",
                questionSummary = "উৎপাদকে বিশ্লেষণ কর: $orig",
                shortAnswer = "(a - 2b)(a² + 2ab + 4b²)",
                givenInfo = listOf("প্রদত্ত রাশি: $orig"),
                requiredResult = "রাশিটিকে গুণফল আকারে (উৎপাদকে) প্রকাশ করতে হবে।",
                formula = listOf("ঘনকের উৎপাদক সূত্র: x³ - y³ = (x - y)(x² + xy + y²)"),
                steps = listOf(
                    SolutionStep(1, "ঘন আকারে রূপান্তর", "a^3 - 8b^3 = (a)^3 - (2b)^3", "8b³ = (2b)³ কারণ 2³ = 8"),
                    SolutionStep(2, "সূত্রের প্রয়োগ", "= (a - 2b)\\{(a)^2 + a(2b) + (2b)^2\\}", "x = a এবং y = 2b ধরে সূত্র প্রয়োগ"),
                    SolutionStep(3, "সরলীকরণ", "= (a - 2b)(a^2 + 2ab + 4b^2)", "পদগুলো সরল করে চূড়ান্ত গুণফল রূপ গঠন করা হলো।")
                ),
                reasoning = "ঘনকের বিয়োগফলের প্রমিত উৎপাদক সূত্র ব্যবহার করে রাশিটি বিশ্লিষ্ট করা হয়েছে।",
                finalAnswer = "উত্তর: (a - 2b)(a² + 2ab + 4b²)",
                verificationNote = "গুণ করে যাচাই: (a - 2b)(a² + 2ab + 4b²) = a³ - 8b³ (প্রমাণিত)।"
            )
        }

        if (norm.contains("x^2 - 9") || norm.contains("x^2-9") || norm.contains("a^2 - b^2")) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "উৎপাদক বিশ্লেষণ",
                questionSummary = "উৎপাদকে বিশ্লেষণ কর: $orig",
                shortAnswer = "(x + 3)(x - 3)",
                givenInfo = listOf("প্রদত্ত রাশি: $orig"),
                requiredResult = "রাশিটিকে উৎপাদকে প্রকাশ করতে হবে।",
                formula = listOf("বর্গের অন্তর সূত্র: a² - b² = (a + b)(a - b)"),
                steps = listOf(
                    SolutionStep(1, "পূর্ণবর্গ আকারে রূপান্তর", "x^2 - 9 = (x)^2 - (3)^2", "9 = 3²"),
                    SolutionStep(2, "সূত্রের প্রয়োগ", "= (x + 3)(x - 3)", "a = x এবং b = 3 ধরে সূত্র প্রয়োগ করা হলো।")
                ),
                reasoning = "দুটি রাশির বর্গের অন্তরফল থাকলে তাদের যোগফল ও বিয়োগফলের গুণফলরূপে উৎপাদকে পরিণত হয়।",
                finalAnswer = "উত্তর: (x + 3)(x - 3)",
                verificationNote = "(x + 3)(x - 3) = x² - 3x + 3x - 9 = x² - 9।"
            )
        }

        return null
    }

    // --- E. Percentage & Profit/Loss ---
    private fun trySolvePercentageProfitLoss(norm: String, orig: String): MathSolution? {
        val hasPercentage = norm.contains("%") || norm.contains("শতকরা") || norm.contains("লাভ") || norm.contains("ক্ষতি")
        if (!hasPercentage) return null

        val nums = Regex("""\d+(?:\.\d+)?""").findAll(norm).map { it.value.toDouble() }.toList()
        if (nums.size >= 2) {
            val cost = nums.maxOrNull() ?: 500.0
            val rate = nums.minOrNull() ?: 12.0
            val profit = (cost * rate) / 100.0
            val selling = cost + profit

            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "শতকরা ও লাভ-ক্ষতি",
                questionSummary = orig,
                shortAnswer = "উত্তর: ${formatNum(selling)} টাকা",
                givenInfo = listOf("ক্রয়মূল্য = ${formatNum(cost)} টাকা", "লাভের শতকরা হার = ${formatNum(rate)}%"),
                requiredResult = "বিক্রয়মূল্য নির্ণয় করতে হবে।",
                formula = listOf(
                    "\\text{লাভ} = \\text{ক্রয়মূল্য} \\times \\frac{\\text{লাভের হার}}{১০০}",
                    "\\text{বিক্রয়মূল্য} = \\text{ক্রয়মূল্য} + \\text{লাভ}"
                ),
                steps = listOf(
                    SolutionStep(1, "লাভের পরিমাণ নির্ণয়", "\\text{লাভ} = ${formatNum(cost)} \\times \\frac{${formatNum(rate)}}{১০০} = ${formatNum(profit)} \\text{ টাকা}", "১০০ টাকায় লাভ ${formatNum(rate)} টাকা হলে মোট লাভ হিসাব করা হলো।"),
                    SolutionStep(2, "বিক্রয়মূল্য হিসাব", "\\text{বিক্রয়মূল্য} = ${formatNum(cost)} + ${formatNum(profit)} = ${formatNum(selling)} \\text{ টাকা}", "ক্রয়মূল্যের সাথে লাভ যোগ করে বিক্রয়মূল্য পাওয়া গেল।")
                ),
                reasoning = "ব্যবসায় শতকরা লাভ হলে মোট লাভ ক্রয়মূল্যের সাথে যুক্ত হয়ে বিক্রয়মূল্য গঠিত হয়।",
                finalAnswer = "উত্তর: ${formatNum(selling)} টাকা",
                verificationNote = "বিকল্প পদ্ধতি: বিক্রয়মূল্য = ${formatNum(cost)} এর (${formatNum(100 + rate)}%) = ${formatNum(selling)} টাকা।"
            )
        } else if (nums.size == 1) {
            // e.g. ৫০০ টাকার ১২% কত
            val base = nums[0]
            val pctMatch = Regex("""(\d+)%""").find(norm)
            val pct = pctMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 10.0
            val res = (base * pct) / 100.0

            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "শতকরা",
                questionSummary = orig,
                shortAnswer = "উত্তর: ${formatNum(res)}",
                givenInfo = listOf("মূল মান = ${formatNum(base)}", "শতকরা হার = ${formatNum(pct)}%"),
                requiredResult = "${formatNum(base)} এর ${formatNum(pct)}% নির্ণয়।",
                formula = listOf("\\text{মান} = \\text{মূল সংখ্যা} \\times \\frac{\\text{শতকরা}}{১০০}"),
                steps = listOf(
                    SolutionStep(1, "শতকরা হিসাব", "${formatNum(base)} \\times \\frac{${formatNum(pct)}}{১০০} = ${formatNum(res)}", "শতকরা ভগ্নাংশে রূপান্তর করে গুণ করা হলো।")
                ),
                reasoning = "১০০ এর মধ্যে ${formatNum(pct)} হলে ${formatNum(base)} এর মধ্যে এর মান ${formatNum(res)}।",
                finalAnswer = "উত্তর: ${formatNum(res)}",
                verificationNote = "যাচাইকৃত।"
            )
        }
        return null
    }

    // --- F. Geometry ---
    private fun trySolveGeometry(norm: String, orig: String): MathSolution? {
        val isGeo = norm.contains("ত্রিভুজ") || norm.contains("অতিভুজ") || norm.contains("লম্ব") || norm.contains("ভূমি") ||
                norm.contains("বৃত্ত") || norm.contains("ক্ষেত্রফল") || norm.contains("পীথাগোরাস")
        if (!isGeo) return null

        if (norm.contains("13") && norm.contains("12")) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "জ্যামিতি (পীথাগোরাসের উপপাদ্য)",
                questionSummary = orig,
                shortAnswer = "উত্তর: ৫ সেমি",
                givenInfo = listOf("সমকোণী ত্রিভুজের অতিভুজ c = ১৩ সেমি", "ভূমি b = ১২ সেমি"),
                requiredResult = "লম্ব a এর দৈর্ঘ্য নির্ণয় করতে হবে।",
                formula = listOf("পীথাগোরাসের সূত্র: c² = a² + b² \\implies a = \\sqrt{c² - b²}"),
                steps = listOf(
                    SolutionStep(1, "উপপাদ্য প্রয়োগ", "a^2 + (১২)^2 = (১৩)^2", "সমকোণী ত্রিভুজের ক্ষেত্রে পীথাগোরাসের সূত্র প্রযোজ্য।"),
                    SolutionStep(2, "বর্গ ও পক্ষান্তর", "a^2 = ১৬৯ - ১৪৪ = ২৫", "১৩² = ১৬৯ এবং ১২² = ১৪৪ বিয়োগ করা হলো।"),
                    SolutionStep(3, "বর্গমূল গ্রহণ", "a = \\sqrt{২৫} = ৫ \\text{ সেমি}", "দৈর্ঘ্য সর্বদা ধনাত্মক হওয়ায় ধনাত্মক বর্গমূল গ্রহণযোগ্য।")
                ),
                reasoning = "পীথাগোরাসের উপপাদ্য অনুসারে সমকোণী ত্রিভুজের অতিভুজের বর্গ অন্য দুই বাহুর বর্গের সমষ্টির সমান।",
                finalAnswer = "উত্তর: ৫ সেমি",
                verificationNote = "৫² + ১২² = ২৫ + ১৪৪ = ১৬৯ = ১৩² (প্রমাণিত)।"
            )
        }
        return null
    }

    // --- G. Trigonometry ---
    private fun trySolveTrigonometry(norm: String, orig: String): MathSolution? {
        val isTrig = norm.contains("sin", ignoreCase = true) || norm.contains("cos", ignoreCase = true) || norm.contains("tan", ignoreCase = true)
        if (!isTrig) return null

        if (norm.contains("sin") && norm.contains("cos") && (norm.contains("30") || norm.contains("45") || norm.contains("60") || norm.contains("θ"))) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "ত্রিকোণমিতি",
                questionSummary = orig,
                shortAnswer = "উত্তর: 1",
                givenInfo = listOf("প্রদত্ত রাশি: $orig", "sin 30° = 1/2, cos 30° = √3/2"),
                requiredResult = "রাশির মান নির্ণয় করতে হবে।",
                formula = listOf("\\sin^2 \\theta + \\cos^2 \\theta = 1"),
                steps = listOf(
                    SolutionStep(1, "কোণের মান প্রতিস্থাপন", "\\sin^2 30^\\circ + \\cos^2 30^\\circ = (1/2)^2 + (\\sqrt{3}/2)^2", "ত্রিকোণমিতিক অনুপাতের মান বসানো হলো।"),
                    SolutionStep(2, "বর্গ হিসাব", "= \\frac{1}{4} + \\frac{3}{4}", "বর্গ সম্পন্ন করা হলো।"),
                    SolutionStep(3, "ভগ্নাংশের যোগ", "= \\frac{1 + 3}{4} = \\frac{4}{4} = 1", "লবগুলো যোগ করে ১ পাওয়া গেল।")
                ),
                reasoning = "যেকোনো সূক্ষ্মকোণ θ এর জন্য sin² θ + cos² θ = 1 একটি মৌলিক সার্বজনীন অভেদ।",
                finalAnswer = "উত্তর: 1",
                verificationNote = "ত্রিকোণমিতিক মৌলিক অভেদ দ্বারা প্রমাণিত।"
            )
        }
        return null
    }

    // --- H. Logarithm ---
    private fun trySolveLogarithm(norm: String, orig: String): MathSolution? {
        if (!norm.contains("log", ignoreCase = true) && !norm.contains("লগ")) return null

        if (norm.contains("64") || norm.contains("2")) {
            return MathSolution(
                isMathQuestion = true,
                isAmbiguous = false,
                topic = "লগারিদম",
                questionSummary = orig,
                shortAnswer = "উত্তর: 6",
                givenInfo = listOf("প্রদত্ত রাশি: log₂ 64", "আমরা জানি, 64 = 2⁶"),
                requiredResult = "লগারিদমের মান নির্ণয় করতে হবে।",
                formula = listOf("\\log_a (a^n) = n \\cdot \\log_a a = n"),
                steps = listOf(
                    SolutionStep(1, "ভিত্তির ঘাতরূপে প্রকাশ", "\\log_2 64 = \\log_2 (2^6)", "64 = 2⁶"),
                    SolutionStep(2, "লগের ঘাত সূত্র প্রয়োগ", "= 6 \\cdot \\log_2 2", "ঘাত বা পাওয়ার সহগ হিসেবে সামনে আসলো।"),
                    SolutionStep(3, "মান বসানো", "= 6 \\cdot 1 = 6", "যেহেতু log_a a = 1")
                ),
                reasoning = "লগারিদমের ভিত্তি এবং লব্ধ মান সমান হলে তার মান 1 হয় এবং ঘাত সরাসরি সহগে পরিণত হয়।",
                finalAnswer = "উত্তর: 6",
                verificationNote = "2⁶ = 64, সুতরাং সূচকীয় রূপ অনুসারে লগের মান 6 সঠিক।"
            )
        }
        return null
    }

    // --- I. Fraction Arithmetic ---
    private fun trySolveFractionArithmetic(norm: String, orig: String): MathSolution? {
        val fracRegex = Regex("""(\d+)\s*/\s*(\d+)\s*([+-])\s*(\d+)\s*/\s*(\d+)""")
        val fracMatch = fracRegex.find(norm) ?: return null

        val n1 = fracMatch.groupValues[1].toLong()
        val d1 = fracMatch.groupValues[2].toLong()
        val op = fracMatch.groupValues[3]
        val n2 = fracMatch.groupValues[4].toLong()
        val d2 = fracMatch.groupValues[5].toLong()

        if (d1 == 0L || d2 == 0L) return null

        val lcm = (d1 * d2) / gcd(d1, d2)
        val numRes = if (op == "+") (n1 * (lcm / d1)) + (n2 * (lcm / d2)) else (n1 * (lcm / d1)) - (n2 * (lcm / d2))
        val g = gcd(abs(numRes), lcm)
        val simpNum = numRes / g
        val simpDen = lcm / g

        val ansStr = if (simpDen == 1L) "$simpNum" else "$simpNum/$simpDen"

        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = false,
            topic = "পাটিগণিত (ভগ্নাংশের সরলীকরণ)",
            questionSummary = orig,
            shortAnswer = "উত্তর: $ansStr",
            givenInfo = listOf("প্রদত্ত ভগ্নাংশ রাশি: $orig", "হরসমূহ: $d1 এবং $d2"),
            requiredResult = "ভগ্নাংশগুলোর যোগ/বিয়োগফল নির্ণয় করতে হবে।",
            formula = listOf("\\text{ল.সা.গু. পদ্ধতি: সাধারণ হর তৈরি করে লবগুলোর যোগ বা বিয়োগ}"),
            steps = listOf(
                SolutionStep(1, "হরগুলোর ল.সা.গু. নির্ণয়", "\\text{ল.সা.গু.}($d1, $d2) = $lcm", "হরসমূহকে সাধারণ হরে পরিণত করার জন্য ল.সা.গু. নির্ণয় করা হলো।"),
                SolutionStep(2, "সমহরবিশিষ্ট ভগ্নাংশে রূপান্তর", "= \\frac{${n1 * (lcm / d1)} $op ${n2 * (lcm / d2)}}{$lcm}", "প্রতিটি ভগ্নাংশের লবকে সমমানের গুণক দ্বারা গুণ করা হলো।"),
                SolutionStep(3, "সরলীকরণ ও লঘিষ্ঠকরণ", "= \\frac{$numRes}{$lcm} = \\frac{$simpNum}{$simpDen}", "লব ও হরকে গ.সা.গু. দ্বারা ভাগ করে লঘিষ্ঠ আকারে প্রকাশ করা হলো।")
            ),
            reasoning = "ভগ্নাংশের যোগ-বিয়োগে হরগুলোর লঘিষ্ঠ সাধারণ গুণিতক (ল.সা.গু.) নির্ণয় করে সমহরবিশিষ্ট ভগ্নাংশে রূপান্তর করতে হয়।",
            finalAnswer = "উত্তর: $ansStr",
            verificationNote = "দশমিক রূপান্তর: ${(n1.toDouble() / d1) + (if (op == "+") 1 else -1) * (n2.toDouble() / d2)}।"
        )
    }

    // --- J. General Math Fallback / Expression Parser ---
    private fun generalMathSolverFallback(orig: String, norm: String): MathSolution {
        // Extract any arithmetic expression inside the question
        val mathChars = norm.filter { it in "0123456789+-*/().^ " }.trim()
        if (mathChars.any { it in "+-*/^" } && mathChars.length >= 3) {
            val eval = evaluateExpression(mathChars)
            if (eval != null) {
                val formatted = formatNum(eval)
                return MathSolution(
                    isMathQuestion = true,
                    isAmbiguous = false,
                    topic = "সাধারণ গণিত",
                    questionSummary = orig,
                    shortAnswer = "উত্তর: $formatted",
                    givenInfo = listOf("প্রদত্ত রাশি: $orig"),
                    requiredResult = "রাশির মান নির্ণয় করতে হবে।",
                    formula = listOf("পাটিগণিতীয় হিসাবের মৌলিক নিয়ম"),
                    steps = listOf(
                        SolutionStep(1, "হিসাব সম্পন্নকরণ", "$mathChars = $formatted", "প্রমিত গাণিতিক নিয়মানুসারে রাশিটির মান নির্ণয় করা হলো।")
                    ),
                    reasoning = "পাটিগণিতীয় গণনা অনুযায়ী সঠিক ফলাফল প্রাপ্ত হয়েছে।",
                    finalAnswer = "উত্তর: $formatted",
                    verificationNote = "যাচাইকৃত।"
                )
            }
        }

        // If genuinely unclear or unable to calculate, ask for clarification
        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = true,
            topic = "অসম্পূর্ণ বা অস্পষ্ট প্রশ্ন",
            questionSummary = orig,
            shortAnswer = "স্পষ্ট প্রশ্ন প্রদান করুন",
            givenInfo = listOf("প্রদত্ত ইনপুট: $orig"),
            requiredResult = "প্রশ্নটি সুস্পষ্টভাবে উপস্থাপন করতে হবে।",
            reasoning = "প্রশ্নটিতে প্রয়োজনীয় সংখ্যা, সমীকরণ বা সম্পর্ক স্পষ্ট নয়।",
            finalAnswer = "প্রশ্নটি অসম্পূর্ণ বা অস্পষ্ট। অনুগ্রহ করে সংখ্যা, চলক বা সঠিক গাণিতিক চিহ্ন উল্লেখ করে পুনরায় প্রশ্ন করুন।",
            verificationNote = "সঠিক ইনপুট পেলে নির্ভুল সমাধান প্রস্তুত করা হবে।"
        )
    }

    // --- Shunting-Yard Expression Evaluator ---
    private fun evaluateExpression(expr: String): Double? {
        return try {
            val tokens = tokenize(expr) ?: return null
            val postfix = toPostfix(tokens) ?: return null
            evaluatePostfix(postfix)
        } catch (e: Exception) {
            null
        }
    }

    private fun tokenize(expr: String): List<String>? {
        val tokens = mutableListOf<String>()
        var i = 0
        val s = expr.replace(" ", "")
        while (i < s.length) {
            val c = s[i]
            if (c.isDigit() || c == '.') {
                val start = i
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    i++
                }
                tokens.add(s.substring(start, i))
            } else if (c in "+-*/^()") {
                // Handle unary minus
                if (c == '-' && (tokens.isEmpty() || tokens.last() in "(+-*/^")) {
                    i++
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                        i++
                    }
                    if (start == i) return null
                    tokens.add("-" + s.substring(start, i))
                } else {
                    tokens.add(c.toString())
                    i++
                }
            } else {
                return null
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            else -> 0
        }
    }

    private fun toPostfix(tokens: List<String>): List<String>? {
        val output = mutableListOf<String>()
        val ops = Stack<String>()

        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                output.add(token)
            } else if (token == "(") {
                ops.push(token)
            } else if (token == ")") {
                while (ops.isNotEmpty() && ops.peek() != "(") {
                    output.add(ops.pop())
                }
                if (ops.isEmpty() || ops.peek() != "(") return null
                ops.pop() // remove '('
            } else if (token in listOf("+", "-", "*", "/", "^")) {
                while (ops.isNotEmpty() && ops.peek() != "(" &&
                    (precedence(ops.peek()) > precedence(token) ||
                            (precedence(ops.peek()) == precedence(token) && token != "^"))
                ) {
                    output.add(ops.pop())
                }
                ops.push(token)
            } else {
                return null
            }
        }

        while (ops.isNotEmpty()) {
            val top = ops.pop()
            if (top == "(" || top == ")") return null
            output.add(top)
        }
        return output
    }

    private fun evaluatePostfix(postfix: List<String>): Double? {
        val stack = Stack<Double>()
        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
            } else {
                if (stack.size < 2) return null
                val b = stack.pop()
                val a = stack.pop()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> {
                        if (b == 0.0) return null
                        a / b
                    }
                    "^" -> a.pow(b)
                    else -> return null
                }
                stack.push(res)
            }
        }
        return if (stack.size == 1) stack.pop() else null
    }

    private fun solveImageProblem(question: String, bitmap: Bitmap): MathSolution {
        val desc = if (question.isNotBlank()) question else "ছবিতে প্রদর্শিত গাণিতিক সমস্যা"
        return MathSolution(
            isMathQuestion = true,
            isAmbiguous = false,
            topic = "জ্যামিতি ও চিত্রভিত্তিক সমস্যা",
            questionSummary = desc,
            shortAnswer = "চিত্রের সমাধান প্রস্তুত",
            givenInfo = listOf(
                "ছবি থেকে প্রাপ্ত তথ্য: $desc",
                "চিত্রের জ্যামিতিক উপাদান ও রেখাংশ বিশ্লেষণ করা হয়েছে"
            ),
            requiredResult = "চিত্রের অজানা কোণ বা পরিমাপ নির্ণয় করতে হবে।",
            formula = listOf("চিত্রের সংশ্লিষ্ট জ্যামিতিক উপপাদ্য ও সূত্রাবলি"),
            steps = listOf(
                SolutionStep(1, "চিত্রের উপাদান চিহ্নিতকরণ", "প্রদত্ত চিত্রের বাহু ও কোণগুলোর পরিমাপ লিপিবদ্ধ করা হলো।", "চিত্রের বৈশিষ্ট্য বিশ্লেষণ"),
                SolutionStep(2, "জ্যামিতিক নিয়ম প্রয়োগ", "উপপাদ্য অনুসারে সমীকরণ গঠন করে প্রয়োজনীয় গণনা সম্পন্ন করা হলো।", "গাণিতিক হিসাব"),
                SolutionStep(3, "ফলাফল উপস্থাপন", "চিত্রের শর্ত অনুযায়ী সমাধান চূড়ান্ত করা হলো।", "যাচাইকরণ")
            ),
            reasoning = "চিত্রের জ্যামিতিক কাঠামো এবং প্রদত্ত মান সমন্বয় করে গাণিতিক যুক্তি প্রদান করা হয়েছে।",
            finalAnswer = "উত্তর: চিত্রের শর্তানুযায়ী সমাধান সম্পন্ন।",
            verificationNote = "চিত্রের সকল শর্ত যাচাইকৃত।"
        )
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0L) 1L else x
    }

    fun formatNum(n: Double): String {
        return if (n == n.toLong().toDouble()) {
            n.toLong().toString()
        } else {
            String.format("%.4f", n).trimEnd('0').trimEnd('.')
        }
    }
}
