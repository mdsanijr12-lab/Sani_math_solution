package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SavedSolutionEntity
import com.example.data.model.MathSolution
import com.example.data.model.SolutionStep
import com.example.data.repository.MathRepository
import com.example.ui.components.FormattedMathText
import com.example.ui.components.KaTeXMathView
import com.example.ui.viewmodel.MathSolverViewModel
import com.example.ui.viewmodel.SolverUiState
import com.example.ui.viewmodel.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathSolverScreen(
    viewModel: MathSolverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val questionText by viewModel.questionText.collectAsState()
    val imageUri by viewModel.selectedImageUri.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedAnswerTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val historyItems by viewModel.historyItems.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SANI MATH GUIDE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "গণিত",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Created by SANI • বিশুদ্ধ গণিত সহায়িকা",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // History Button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recent Questions History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK -> Icons.Default.LightMode
                                else -> Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Dark/Light Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Creator Info
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App and Creator Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrExpanded = maxWidth >= 760.dp

            if (isTabletOrExpanded) {
                // Side-by-side layout on large screens / tablets
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Question Input & Controls
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                CreatorBanner()
                            }
                            item {
                                QuestionInputSection(
                                    questionText = questionText,
                                    onTextChanged = viewModel::onQuestionTextChanged,
                                    imageUri = imageUri,
                                    onPickImage = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onRemoveImage = { viewModel.onImageSelected(null) },
                                    onClear = viewModel::clearInput,
                                    onSolve = viewModel::solve,
                                    isLoading = uiState is SolverUiState.Loading
                                )
                            }
                            item {
                                SampleProblemsSection(onSelectSample = viewModel::loadSample)
                            }
                        }
                    }

                    // Right Column: Dual Answer Sections Side-by-Side
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AnswerDisplayContainer(
                            uiState = uiState,
                            selectedTab = selectedTab,
                            onTabSelected = viewModel::selectAnswerTab,
                            isTabletMode = true,
                            onCopySolution = { solution ->
                                viewModel.copyToClipboard(
                                    context,
                                    viewModel.getFormattedGuidebookText(solution),
                                    "SANI MATH GUIDE Solution"
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("সম্পূর্ণ সমাধান ক্লিপবোর্ডে কপি করা হয়েছে!")
                                }
                            },
                            onCopyShortAnswer = { answer ->
                                viewModel.copyToClipboard(context, answer, "Math Short Answer")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("সংক্ষিপ্ত উত্তর কপি করা হয়েছে!")
                                }
                            }
                        )
                    }
                }
            } else {
                // Mobile-first scrollable vertical layout
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CreatorBanner()
                    }

                    item {
                        QuestionInputSection(
                            questionText = questionText,
                            onTextChanged = viewModel::onQuestionTextChanged,
                            imageUri = imageUri,
                            onPickImage = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemoveImage = { viewModel.onImageSelected(null) },
                            onClear = viewModel::clearInput,
                            onSolve = viewModel::solve,
                            isLoading = uiState is SolverUiState.Loading
                        )
                    }

                    item {
                        SampleProblemsSection(onSelectSample = viewModel::loadSample)
                    }

                    item {
                        AnswerDisplayContainer(
                            uiState = uiState,
                            selectedTab = selectedTab,
                            onTabSelected = viewModel::selectAnswerTab,
                            isTabletMode = false,
                            onCopySolution = { solution ->
                                viewModel.copyToClipboard(
                                    context,
                                    viewModel.getFormattedGuidebookText(solution),
                                    "SANI MATH GUIDE Solution"
                                )
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("সম্পূর্ণ সমাধান ক্লিপবোর্ডে কপি করা হয়েছে!")
                                }
                            },
                            onCopyShortAnswer = { answer ->
                                viewModel.copyToClipboard(context, answer, "Math Short Answer")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("সংক্ষিপ্ত উত্তর কপি করা হয়েছে!")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // History Bottom Sheet
    if (showHistorySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState
        ) {
            HistorySheetContent(
                historyItems = historyItems,
                onSelectHistory = { item ->
                    viewModel.loadHistorySolution(item)
                    showHistorySheet = false
                },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearAll = { viewModel.clearAllHistory() },
                onClose = { showHistorySheet = false }
            )
        }
    }

    // Info & Creator Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SANI MATH GUIDE",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "👤 প্রস্তুতকারক ও স্বত্বাধিকারী:",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "SANI (সানী)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "🎯 অ্যাপের উদ্দেশ্য:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "এই অ্যাপটি বিশেষভাবে শুধুমাত্র গণিত (Mathematics) বিষয়ের সমাধান ও গাইডবুক বিশ্লেষণের জন্য প্রস্তুত করা হয়েছে। এখানে বাংলা, ইংরেজি বা ব্যাকরণ নয়—বরং পাটিগণিত, বীজগণিত, জ্যামিতি, ত্রিকোণমিতি ও উচ্চতর গণিতের নিখুঁত সমাধান প্রদান করা হয়।",
                        fontSize = 14.sp
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "📖 গাইডবুক ফরম্যাট:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "বাংলাদেশি পাঠ্যবই ও পাঞ্জেরী/অনুপম গাইডের মতো 'যা দেওয়া আছে', 'নির্ণয় করতে হবে', 'প্রয়োজনীয় সূত্র', 'ধাপভিত্তিক হিসাব' ও 'নির্ণেয় উত্তর' সুন্দরভাবে সাজিয়ে উপস্থাপন করে।",
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

@Composable
fun CreatorBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SANI MATH GUIDE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Owner: SANI • Created by SANI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "গণিত স্পেশাল",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun QuestionInputSection(
    questionText: String,
    onTextChanged: (String) -> Unit,
    imageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "গণিত প্রশ্ন বা সমীকরণ দিন:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (questionText.isNotEmpty() || imageUri != null) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.testTag("clear_input_button")
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "মুছুন", fontSize = 12.sp)
                    }
                }
            }

            OutlinedTextField(
                value = questionText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = "যেমন: x² - 5x + 6 = 0 সমাধান কর, অথবা একটি ত্রিভুজের ক্ষেত্রফল নির্ণয় কর...",
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_text_input"),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Image Thumbnail if uploaded
            if (imageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Math Problem Photo",
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "গণিত প্রশ্নের ছবি সংযুক্ত",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "AI দ্বারা ছবি ও হস্তলিপি বিশ্লেষণ হবে",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onRemoveImage) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove photo", tint = Color.Red)
                        }
                    }
                }
            }

            // Buttons: Photo Upload and Solve
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upload_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (imageUri == null) "ছবি তুলুন/দিন" else "ছবি পরিবর্তন", fontSize = 13.sp)
                }

                Button(
                    onClick = onSolve,
                    enabled = !isLoading && (questionText.isNotBlank() || imageUri != null),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("solve_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "সমাধান হচ্ছে...", fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "সমাধান করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SampleProblemsSection(
    onSelectSample: (MathRepository.SampleMathItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "নমুনা সমস্যা (দ্রুত পরীক্ষার জন্য চাপুন):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(MathRepository.SAMPLE_PROBLEMS) { sample ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.clickable { onSelectSample(sample) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${sample.category}]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sample.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerDisplayContainer(
    uiState: SolverUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isTabletMode: Boolean,
    onCopySolution: (MathSolution) -> Unit,
    onCopyShortAnswer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is SolverUiState.Idle -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "যেকোনো গণিত সমস্যা লিখুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "বীজগণিত, পাটিগণিত, জ্যামিতি, উৎপাদক বা ত্রিকোণমিতির সমস্যা টাইপ করুন বা ছবি দিয়ে 'সমাধান করুন' চাপুন।",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is SolverUiState.Loading -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SANI MATH GUIDE সমাধান করছে...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.progressMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is SolverUiState.Error -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বিজ্ঞপ্তি / ত্রুটি",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.message,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        is SolverUiState.Success -> {
            val solution = uiState.solution

            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Topic & Copy Action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "বিষয়: ${solution.topic}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "SANI MATH GUIDE • সঠিক সমাধান",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            OutlinedButton(
                                onClick = { onCopySolution(solution) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("copy_solution_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "কপি", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Ambiguity alert if applicable
                if (solution.isAmbiguous) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সতর্কতা: প্রশ্নটিতে কিছু প্রয়োজনীয় মান অনুপস্থিত বা অস্পষ্ট। সাধারণ শর্তে সমাধান নিচে দেওয়া হয়েছে।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (isTabletMode) {
                    // On wide screens / tablet: display side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section A: Short Answer
                        Column(modifier = Modifier.weight(0.8f)) {
                            ShortAnswerCard(
                                shortAnswer = solution.shortAnswer,
                                finalAnswer = solution.finalAnswer,
                                onCopy = { onCopyShortAnswer(solution.shortAnswer) }
                            )
                        }

                        // Section B: Long Guidebook Solution
                        Column(modifier = Modifier.weight(1.2f)) {
                            LongAnswerCard(solution = solution)
                        }
                    }
                } else {
                    // Mobile: easy-to-switch tabs
                    SecondaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { onTabSelected(0) },
                            text = {
                                Text(
                                    text = "A. সংক্ষিপ্ত উত্তর",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_short_answer")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { onTabSelected(1) },
                            text = {
                                Text(
                                    text = "B. বিস্তারিত গাইড সমাধান",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_long_answer")
                        )
                    }

                    if (selectedTab == 0) {
                        ShortAnswerCard(
                            shortAnswer = solution.shortAnswer,
                            finalAnswer = solution.finalAnswer,
                            onCopy = { onCopyShortAnswer(solution.shortAnswer) }
                        )
                    } else {
                        LongAnswerCard(solution = solution)
                    }
                }
            }
        }
    }
}

@Composable
fun ShortAnswerCard(
    shortAnswer: String,
    finalAnswer: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সংক্ষিপ্ত উত্তর (Short Answer)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Short Answer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "নির্ণেয় মান / সমাধান:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    KaTeXMathView(
                        latex = shortAnswer.ifBlank { finalAnswer },
                        fontSizeSp = 20
                    )
                }
            }

            if (finalAnswer.isNotBlank() && finalAnswer != shortAnswer) {
                Text(
                    text = "উত্তর: $finalAnswer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun LongAnswerCard(
    solution: MathSolution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "বিস্তারিত গাইড সমাধান (Bangladeshi Guidebook)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // 1. Given Information (যা দেওয়া আছে)
            if (solution.givenInfo.isNotEmpty()) {
                GuidebookSectionItem(
                    icon = "📌",
                    title = "যা দেওয়া আছে (Given Information):",
                    titleColor = MaterialTheme.colorScheme.primary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        solution.givenInfo.forEach { info ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = "• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                FormattedMathText(text = info)
                            }
                        }
                    }
                }
            }

            // 2. Required Result (নির্ণয় করতে হবে)
            if (solution.requiredResult.isNotBlank()) {
                GuidebookSectionItem(
                    icon = "🎯",
                    title = "নির্ণয় করতে হবে (Required Result):",
                    titleColor = MaterialTheme.colorScheme.secondary
                ) {
                    FormattedMathText(text = solution.requiredResult)
                }
            }

            // 3. Relevant Formulas or Rules (প্রয়োজনীয় সূত্র বা নিয়ম)
            if (solution.formula.isNotEmpty()) {
                GuidebookSectionItem(
                    icon = "📐",
                    title = "প্রয়োজনীয় সূত্র বা গাণিতিক নিয়মাবলী:",
                    titleColor = MaterialTheme.colorScheme.primary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        solution.formula.forEach { form ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    KaTeXMathView(latex = form, fontSizeSp = 16)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Step-by-Step Calculations (ধাপ অনুযায়ী সমাধান)
            if (solution.steps.isNotEmpty()) {
                GuidebookSectionItem(
                    icon = "🔢",
                    title = "সমাধান ধাপসমূহ (Step-by-step Calculations):",
                    titleColor = MaterialTheme.colorScheme.primary
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        solution.steps.forEach { step ->
                            StepItemView(step = step)
                        }
                    }
                }
            }

            // 5. Mathematical Reasoning (গাণিতিক যুক্তি ও বিশ্লেষণ)
            if (solution.reasoning.isNotBlank()) {
                GuidebookSectionItem(
                    icon = "💡",
                    title = "গাণিতিক যুক্তি ও ব্যাখ্যা:",
                    titleColor = MaterialTheme.colorScheme.tertiary
                ) {
                    FormattedMathText(text = solution.reasoning)
                }
            }

            // 6. Final Answer (নির্ণেয় উত্তর)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "নির্ণেয় উত্তর (Final Answer):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    KaTeXMathView(
                        latex = solution.finalAnswer.ifBlank { solution.shortAnswer },
                        fontSizeSp = 18
                    )
                }
            }

            // 7. Verification Note (শুদ্ধি পরীক্ষা / বিকল্প পদ্ধতি)
            if (solution.verificationNote.isNotBlank()) {
                GuidebookSectionItem(
                    icon = "🔍",
                    title = "শুদ্ধি পরীক্ষা ও যাচাইকরণ:",
                    titleColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    FormattedMathText(text = solution.verificationNote)
                }
            }
        }
    }
}

@Composable
fun GuidebookSectionItem(
    icon: String,
    title: String,
    titleColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = titleColor
            )
        }
        Box(modifier = Modifier.padding(start = 6.dp)) {
            content()
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StepItemView(step: SolutionStep, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = step.title.ifBlank { "ধাপ ${step.stepNumber}" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (step.calculation.isNotBlank()) {
                KaTeXMathView(latex = step.calculation, fontSizeSp = 16)
            }

            if (step.reasoning.isNotBlank()) {
                Text(
                    text = "যুক্তি: ${step.reasoning}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun HistorySheetContent(
    historyItems: List<SavedSolutionEntity>,
    onSelectHistory: (SavedSolutionEntity) -> Unit,
    onToggleFavorite: (SavedSolutionEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সমাধানের ইতিহাস (${historyItems.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (historyItems.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("সব মুছুন", color = Color.Red, fontSize = 12.sp)
                }
            }
        }

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো সংরক্ষিত ইতিহাস নেই। নতুন গণিত সমস্যা সমাধান করুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectHistory(item) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.questionText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row {
                                    Text(
                                        text = item.topic,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• উত্তর: ${item.shortAnswer}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onToggleFavorite(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Favorite",
                                        tint = if (item.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
