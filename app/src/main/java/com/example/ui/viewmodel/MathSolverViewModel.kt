package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MathDatabase
import com.example.data.local.SavedSolutionEntity
import com.example.data.model.MathSolution
import com.example.data.remote.FirebaseAiMathService
import com.example.data.repository.MathRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MathSolverViewModel"

sealed interface SolverUiState {
    data object Idle : SolverUiState
    data class Loading(val progressMessage: String = "সমস্যাটি বিশ্লেষণ করা হচ্ছে...") : SolverUiState
    data class Success(
        val solution: MathSolution,
        val isSaved: Boolean = false,
        val savedId: Long? = null
    ) : SolverUiState
    data class Error(val message: String) : SolverUiState
}

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class MathSolverViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MathRepository

    init {
        val database = MathDatabase.getDatabase(application)
        val aiService = FirebaseAiMathService()
        repository = MathRepository(aiService, database.savedSolutionDao())
    }

    // Input States
    private val _questionText = MutableStateFlow("")
    val questionText: StateFlow<String> = _questionText.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private var selectedImageBitmap: Bitmap? = null

    // UI State
    private val _uiState = MutableStateFlow<SolverUiState>(SolverUiState.Idle)
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    // Answer Tab Selection (0 = Short Answer, 1 = Long Guidebook Answer)
    private val _selectedAnswerTab = MutableStateFlow(0)
    val selectedAnswerTab: StateFlow<Int> = _selectedAnswerTab.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // History and Saved Solutions from Room
    val historyItems: StateFlow<List<SavedSolutionEntity>> = repository.historySolutions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQuestionTextChanged(newText: String) {
        _questionText.value = newText
    }

    fun onImageSelected(uri: Uri?) {
        _selectedImageUri.value = uri
        if (uri != null) {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load selected image", e)
                selectedImageBitmap = null
            }
        } else {
            selectedImageBitmap = null
        }
    }

    fun clearInput() {
        _questionText.value = ""
        _selectedImageUri.value = null
        selectedImageBitmap = null
        _uiState.value = SolverUiState.Idle
    }

    fun selectAnswerTab(index: Int) {
        _selectedAnswerTab.value = index
    }

    fun toggleTheme() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
    }

    fun loadSample(sample: MathRepository.SampleMathItem) {
        _questionText.value = sample.question
        _selectedImageUri.value = null
        selectedImageBitmap = null
        _uiState.value = SolverUiState.Success(sample.solution)
        _selectedAnswerTab.value = 1 // Show detailed guidebook solution by default
    }

    fun solve() {
        val text = _questionText.value.trim()
        val bitmap = selectedImageBitmap

        if (text.isEmpty() && bitmap == null) {
            _uiState.value = SolverUiState.Error("অনুগ্রহ করে একটি গণিত প্রশ্ন লিখুন অথবা সমস্যার ছবি নির্বাচন করুন।")
            return
        }

        viewModelScope.launch {
            _uiState.value = SolverUiState.Loading("সমস্যাটি বিশ্লেষণ করা হচ্ছে...")
            try {
                val solution = repository.solveMath(text, bitmap)
                if (!solution.isMathQuestion) {
                    _uiState.value = SolverUiState.Error(
                        solution.nonMathExplanation.ifBlank {
                            "SANI MATH GUIDE শুধুমাত্র গণিত বিষয়ক সমস্যার সমাধানের জন্য তৈরি। দয়া করে কোনো গাণিতিক প্রশ্ন জিজ্ঞাসা করুন।"
                        }
                    )
                } else {
                    // Automatically record to history in Room
                    val id = repository.saveToHistory(
                        question = text.ifBlank { "ছবি দ্বারা সমাধানকৃত সমস্যা (${solution.topic})" },
                        solution = solution,
                        isFavorite = false
                    )
                    _uiState.value = SolverUiState.Success(solution = solution, isSaved = true, savedId = id)
                    // If complex or guidebook format, show long answer or short answer based on preference
                    _selectedAnswerTab.value = 0
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Solving failed", e)
                _uiState.value = SolverUiState.Error(
                    e.localizedMessage ?: "গণিত সমস্যা সমাধানে সমস্যা হয়েছে। অনুগ্রহ করে ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় চেষ্টা করুন।"
                )
            }
        }
    }

    fun toggleFavorite(entity: SavedSolutionEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(entity.id, !entity.isFavorite)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSolution(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun loadHistorySolution(entity: SavedSolutionEntity) {
        val solution = repository.deserializeSolution(entity.fullSolutionJson)
        _questionText.value = entity.questionText
        _uiState.value = SolverUiState.Success(solution = solution, isSaved = true, savedId = entity.id)
    }

    fun copyToClipboard(context: Context, textToCopy: String, label: String = "Math Solution") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, textToCopy)
        clipboard.setPrimaryClip(clip)
    }

    fun getFormattedGuidebookText(solution: MathSolution): String {
        return buildString {
            appendLine("═══════════════════════════════════")
            appendLine("📐 SANI MATH GUIDE • সমাধান")
            appendLine("👤 প্রস্তুতকারক: SANI")
            appendLine("📚 বিষয়: ${solution.topic}")
            appendLine("═══════════════════════════════════\n")

            if (solution.questionSummary.isNotBlank()) {
                appendLine("❓ প্রশ্ন:")
                appendLine(solution.questionSummary)
                appendLine()
            }

            if (solution.givenInfo.isNotEmpty()) {
                appendLine("📌 যা দেওয়া আছে:")
                solution.givenInfo.forEach { appendLine(" • $it") }
                appendLine()
            }

            if (solution.requiredResult.isNotBlank()) {
                appendLine("🎯 নির্ণয় করতে হবে:")
                appendLine(" ${solution.requiredResult}")
                appendLine()
            }

            if (solution.formula.isNotEmpty()) {
                appendLine("📐 প্রয়োজনীয় সূত্র বা নিয়ম:")
                solution.formula.forEach { appendLine(" • $it") }
                appendLine()
            }

            if (solution.steps.isNotEmpty()) {
                appendLine("🔢 সমাধান ধাপসমূহ:")
                solution.steps.forEach { step ->
                    appendLine(" [ধাপ ${step.stepNumber}: ${step.title}]")
                    appendLine("  ${step.calculation}")
                    if (step.reasoning.isNotBlank()) {
                        appendLine("  (যুক্তি: ${step.reasoning})")
                    }
                    appendLine()
                }
            }

            if (solution.reasoning.isNotBlank()) {
                appendLine("💡 গাণিতিক ব্যাখ্যা ও যুক্তি:")
                appendLine(" ${solution.reasoning}")
                appendLine()
            }

            appendLine("═══════════════════════════════════")
            appendLine("✅ নির্ণেয় উত্তর:")
            appendLine(" ${solution.finalAnswer.ifBlank { solution.shortAnswer }}")
            appendLine("═══════════════════════════════════")

            if (solution.verificationNote.isNotBlank()) {
                appendLine("\n🔍 শুদ্ধি পরীক্ষা / যাচাইকরণ:")
                appendLine(" ${solution.verificationNote}")
            }
        }
    }
}
