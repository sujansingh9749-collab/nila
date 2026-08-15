package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ExpenseEntry
import com.example.data.local.entities.VoiceNote
import com.example.ui.AssistantUiState
import com.example.ui.theme.AmbientMint
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateBorderDark
import com.example.ui.theme.SlateCardDark
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoiceToolsScreen(
    uiState: AssistantUiState,
    voiceNotes: List<VoiceNote>,
    expenses: List<ExpenseEntry>,
    totalExpense: Double?,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddExpense: (String, Double, String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onTranslate: (String, String) -> Unit,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var toolTab by remember { mutableIntStateOf(0) } // 0: Notes, 1: Expenses, 2: Translator

    val isBn = uiState.selectedLanguage.startsWith("bn")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = toolTab,
            containerColor = ObsidianDark,
            contentColor = CyberCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[toolTab]),
                    color = CyberCyan
                )
            },
            divider = { Box(modifier = Modifier.height(1.dp).background(SlateBorderDark)) }
        ) {
            Tab(
                selected = toolTab == 0,
                onClick = { toolTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "ভয়েস নোট" else "Notes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = toolTab == 1,
                onClick = { toolTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "হিসাব-নিকাশ" else "Expenses", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = toolTab == 2,
                onClick = { toolTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "অনুবাদ" else "Translate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        when (toolTab) {
            0 -> NotesSubTab(
                voiceNotes = voiceNotes,
                onAddNote = onAddNote,
                onDeleteNote = onDeleteNote,
                onSpeak = onSpeak,
                isBn = isBn
            )
            1 -> ExpensesSubTab(
                expenses = expenses,
                totalExpense = totalExpense ?: 0.0,
                onAddExpense = onAddExpense,
                onDeleteExpense = onDeleteExpense,
                isBn = isBn
            )
            2 -> TranslatorSubTab(
                uiState = uiState,
                onTranslate = onTranslate,
                onSpeak = onSpeak,
                isBn = isBn
            )
        }
    }
}

@Composable
fun NotesSubTab(
    voiceNotes: List<VoiceNote>,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onSpeak: (String) -> Unit,
    isBn: Boolean
) {
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Add Note Input Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "নতুন নোট তৈরি করুন" else "Quick Voice Note",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Note Title", color = TextSecondaryDark) },
                        placeholder = { Text("Meeting Discussion", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Content / Voice Memo", color = TextSecondaryDark) },
                        placeholder = { Text("Key points discussed with the team...", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newNoteContent.isNotBlank()) {
                                onAddNote(
                                    newNoteTitle.ifBlank { "Voice Note ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}" },
                                    newNoteContent
                                )
                                newNoteTitle = ""
                                newNoteContent = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = SpaceBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "নোট সংরক্ষণ করুন" else "Save Note", color = SpaceBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Notes List
        if (voiceNotes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "কোনো নোট নেই। বলুন \"নোট লেখো বাজারে যেতে হবে\"" else "No notes saved yet. Say \"Note down buy groceries\"",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                }
            }
        } else {
            items(voiceNotes, key = { it.id }) { note ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(note.timestamp)),
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = note.content, fontSize = 13.sp, color = TextPrimaryDark, lineHeight = 18.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onSpeak(note.content) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Read Note", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDeleteNote(note.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesSubTab(
    expenses: List<ExpenseEntry>,
    totalExpense: Double,
    onAddExpense: (String, Double, String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    isBn: Boolean
) {
    var expenseItem by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("Food") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Total Expense Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AmbientMint.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "মোট রেকর্ডকৃত খরচ" else "Total Voice Logged Expenses",
                            fontSize = 13.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "৳ ${String.format(Locale.US, "%.2f", totalExpense)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmbientMint
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AmbientMint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = AmbientMint, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        // Add Expense Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "নতুন খরচের এন্ট্রি" else "Log Quick Expense",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expenseItem,
                            onValueChange = { expenseItem = it },
                            label = { Text("Item / Purpose", color = TextSecondaryDark) },
                            placeholder = { Text("Groceries", color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Amount (৳)", color = TextSecondaryDark) },
                            placeholder = { Text("350", color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SlateBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                            if (expenseItem.isNotBlank() && amount > 0) {
                                onAddExpense(expenseItem, amount, expenseCategory)
                                expenseItem = ""
                                expenseAmount = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmbientMint),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (isBn) "খরচ যুক্ত করুন" else "Log Expense", color = SpaceBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expenses List
        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "কোনো হিসাব নেই। বলুন \"খরচ ২০০ টাকা খাবার\"" else "No expenses logged. Say \"Spent 250 on food\"",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                }
            }
        } else {
            items(expenses, key = { it.id }) { exp ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderDark, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = exp.item, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(
                                text = "${exp.category} • ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(exp.date))}",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "৳ ${String.format(Locale.US, "%.2f", exp.amount)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmbientMint
                            )
                            IconButton(onClick = { onDeleteExpense(exp.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranslatorSubTab(
    uiState: AssistantUiState,
    onTranslate: (String, String) -> Unit,
    onSpeak: (String) -> Unit,
    isBn: Boolean
) {
    var sourceText by remember { mutableStateOf("") }
    var targetLanguage by remember { mutableStateOf("bn-BD") } // Translate to bn or en

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (targetLanguage.startsWith("bn")) "English ➔ Bengali (বাংলা)" else "Bengali (বাংলা) ➔ English",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )

                        IconButton(
                            onClick = {
                                targetLanguage = if (targetLanguage.startsWith("bn")) "en-US" else "bn-BD"
                            }
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Swap", tint = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("Enter phrase to translate", color = TextSecondaryDark) },
                        placeholder = { Text("How can I help you today?", color = TextMutedDark) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorderDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (sourceText.isNotBlank()) {
                                onTranslate(sourceText, targetLanguage)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isGeminiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SpaceBlack, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = SpaceBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "অনুবাদ ও উচ্চারণ শুনুন" else "Translate & Speak", color = SpaceBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Translation Result Card
        if (uiState.lastResponse.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Translation Output",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                            IconButton(onClick = { onSpeak(uiState.lastResponse) }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Play", tint = CyberCyan, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = uiState.lastResponse, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    }
                }
            }
        }
    }
}
