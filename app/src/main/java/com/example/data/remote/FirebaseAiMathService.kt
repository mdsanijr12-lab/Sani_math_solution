package com.example.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.MathSolution
import com.example.data.model.SolutionStep
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "FirebaseAiMathService"

class FirebaseAiMathService {

    private val mathSolutionSchema = Schema.obj(
        properties = mapOf(
            "isMathQuestion" to Schema.boolean(description = "True if the question is strictly about mathematics, false otherwise"),
            "isAmbiguous" to Schema.boolean(description = "True if the question lacks required parameters, is contradictory, or missing values"),
            "nonMathExplanation" to Schema.string(description = "Polite message in Bengali explaining that SANI MATH GUIDE only solves math, if isMathQuestion is false"),
            "topic" to Schema.string(description = "Bangladeshi math syllabus topic name in Bengali, e.g. বীজগণিত, পাটিগণিত, জ্যামিতি, ত্রিকোণমিতি, পরিমিতি, পরিসংখ্যান, লগারিদম"),
            "questionSummary" to Schema.string(description = "A clean standardized statement of the math problem"),
            "shortAnswer" to Schema.string(description = "Direct concise answer with mathematical notation"),
            "givenInfo" to Schema.array(
                items = Schema.string(),
                description = "List of given facts and values (যা দেওয়া আছে)"
            ),
            "requiredResult" to Schema.string(description = "What needs to be found (নির্ণয় করতে হবে)"),
            "formula" to Schema.array(
                items = Schema.string(),
                description = "Relevant math formulas, theorems or identities (প্রয়োজনীয় সূত্র)"
            ),
            "steps" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "stepNumber" to Schema.integer(description = "Step number starting from 1"),
                        "title" to Schema.string(description = "Step heading or description in Bengali"),
                        "calculation" to Schema.string(description = "Mathematical calculation using LaTeX / KaTeX formatting"),
                        "reasoning" to Schema.string(description = "Mathematical reasoning or justification for this step")
                    )
                ),
                description = "Step-by-step detailed calculations"
            ),
            "reasoning" to Schema.string(description = "Overall mathematical logic and explanation in Bengali"),
            "finalAnswer" to Schema.string(description = "Definitive final answer clearly stated with units if applicable (নির্ণেয় উত্তর)"),
            "verificationNote" to Schema.string(description = "Proof check, verification of roots, or alternative method verification")
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mathSolutionSchema
                temperature = 0.2f // Low temperature for high mathematical accuracy
            },
            systemInstruction = content {
                text(
                    """
                    You are the specialized AI engine of 'SANI MATH GUIDE', created by SANI.
                    CRITICAL BRANDING & RULES:
                    1. OWNER & CREATOR: SANI. App Name: SANI MATH GUIDE.
                    2. STRICT MATHEMATICS EXCLUSIVITY:
                       - This app is EXCLUSIVELY for Mathematics.
                       - NEVER solve English, Bangla, grammar, literature, general knowledge, or any non-mathematical subject.
                       - If a user asks a non-math question, set isMathQuestion = false and nonMathExplanation = "SANI MATH GUIDE শুধুমাত্র গণিত বিষয়ক সমস্যার সমাধানের জন্য তৈরি। দয়া করে কোনো গাণিতিক সমস্যা বা প্রশ্ন প্রদান করুন।"
                    3. BANGLADESHI MATHEMATICS GUIDEBOOK FORMAT:
                       - Provide solutions exactly like standard Bangladeshi mathematics guidebooks (NCTB / Panjeree / Lecture / Anupam guide style).
                       - Answer explanations must be in Bengali by default, while supporting Bengali or English input.
                       - Populate:
                         * givenInfo: [যা দেওয়া আছে: ...]
                         * requiredResult: নির্ণয় করতে হবে: ...
                         * formula: [প্রয়োজনীয় সূত্রাবলি বা গাণিতিক নিয়মাবলী]
                         * steps: Sequential calculation steps with LaTeX equations (e.g. x = (-b \pm \sqrt{b^2 - 4ac}) / 2a)
                         * reasoning: স্পষ্ট গাণিতিক যুক্তি ও ব্যাখ্যা
                         * finalAnswer: নির্ণেয় উত্তর (স্পষ্ট ও স্পষ্ট এককে)
                         * verificationNote: শুদ্ধি পরীক্ষা বা বিকল্প প্রমাণ
                    4. RIGOROUS CALCULATION & AMBIGUITY:
                       - Double check all calculations before answering.
                       - If the user's question has missing values or ambiguity, set isAmbiguous = true and clearly explain the missing values. Do NOT invent missing values.
                    5. BEAUTIFUL LATEX MATH NOTATION:
                       - Use KaTeX/LaTeX compatible notation for all equations and expressions, e.g. \frac{a}{b}, x^2, \sqrt{...}, \sin \theta, \log_a b.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun solveMathProblem(questionText: String, imageBitmap: Bitmap? = null): MathSolution {
        return try {
            val response = withTimeout(60_000L) {
                if (imageBitmap != null) {
                    model.generateContent(
                        content {
                            text("Analyze this mathematics problem image and solve it completely following SANI MATH GUIDE format: $questionText")
                            image(imageBitmap)
                        }
                    )
                } else {
                    model.generateContent("Solve this mathematics problem following SANI MATH GUIDE format: $questionText")
                }
            }

            val jsonText = response.text ?: throw IllegalStateException("Model returned empty text.")
            parseMathSolutionJson(jsonText)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Math problem solving timed out", e)
            throw IllegalStateException("গণিত সমস্যা সমাধানের সময় শেষ হয়ে গেছে (Timeout 60s)। দয়া করে আবার চেষ্টা করুন।")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to solve math problem", e)
            throw e
        }
    }

    private fun parseMathSolutionJson(jsonStr: String): MathSolution {
        val root = JSONObject(jsonStr)
        val isMathQuestion = root.optBoolean("isMathQuestion", true)
        val isAmbiguous = root.optBoolean("isAmbiguous", false)
        val nonMathExplanation = root.optString("nonMathExplanation", "")
        val topic = root.optString("topic", "সাধারণ গণিত")
        val questionSummary = root.optString("questionSummary", "")
        val shortAnswer = root.optString("shortAnswer", "")
        val requiredResult = root.optString("requiredResult", "")
        val reasoning = root.optString("reasoning", "")
        val finalAnswer = root.optString("finalAnswer", "")
        val verificationNote = root.optString("verificationNote", "")

        val givenInfoList = mutableListOf<String>()
        val givenArray = root.optJSONArray("givenInfo")
        if (givenArray != null) {
            for (i in 0 until givenArray.length()) {
                givenInfoList.add(givenArray.optString(i))
            }
        }

        val formulaList = mutableListOf<String>()
        val formulaArray = root.optJSONArray("formula")
        if (formulaArray != null) {
            for (i in 0 until formulaArray.length()) {
                formulaList.add(formulaArray.optString(i))
            }
        }

        val stepList = mutableListOf<SolutionStep>()
        val stepsArray = root.optJSONArray("steps")
        if (stepsArray != null) {
            for (i in 0 until stepsArray.length()) {
                val stepObj = stepsArray.optJSONObject(i)
                if (stepObj != null) {
                    stepList.add(
                        SolutionStep(
                            stepNumber = stepObj.optInt("stepNumber", i + 1),
                            title = stepObj.optString("title", "ধাপ ${i + 1}"),
                            calculation = stepObj.optString("calculation", ""),
                            reasoning = stepObj.optString("reasoning", "")
                        )
                    )
                }
            }
        }

        return MathSolution(
            isMathQuestion = isMathQuestion,
            isAmbiguous = isAmbiguous,
            nonMathExplanation = nonMathExplanation,
            topic = topic,
            questionSummary = questionSummary,
            shortAnswer = shortAnswer,
            givenInfo = givenInfoList,
            requiredResult = requiredResult,
            formula = formulaList,
            steps = stepList,
            reasoning = reasoning,
            finalAnswer = finalAnswer,
            verificationNote = verificationNote
        )
    }
}
