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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.VoiceMacro
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

@Composable
fun MacrosScreen(
    macros: List<VoiceMacro>,
    onRunMacro: (VoiceMacro) -> Unit,
    onToggleMacro: (VoiceMacro) -> Unit,
    onDeleteMacro: (Long) -> Unit,
    onAddMacro: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(SpaceBlack)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Voice Macros & Smart Routines",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Chain multiple phone actions into a single voice trigger phrase",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            if (macros.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom routines yet. Tap + to build your first voice macro!",
                            fontSize = 14.sp,
                            color = TextMutedDark
                        )
                    }
                }
            } else {
                items(macros, key = { it.id }) { macro ->
                    MacroCardItem(
                        macro = macro,
                        onRun = { onRunMacro(macro) },
                        onToggle = { onToggleMacro(macro) },
                        onDelete = { onDeleteMacro(macro.id) }
                    )
                }
            }
        }

        // Floating Action Button to Add Routine
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CyberCyan,
            contentColor = SpaceBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp)
                .testTag("add_macro_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Macro", modifier = Modifier.size(26.dp))
        }

        if (showAddDialog) {
            AddMacroDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, trigger, desc, stepsJson ->
                    onAddMacro(name, trigger, desc, stepsJson)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MacroCardItem(
    macro: VoiceMacro,
    onRun: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderDark, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = macro.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Trigger: \"${macro.triggerPhrase}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberCyan
                        )
                    }
                }

                Switch(
                    checked = macro.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SpaceBlack,
                        checkedTrackColor = CyberCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = macro.description,
                fontSize = 13.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateCardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = AmbientMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute Routine", fontSize = 12.sp, color = AmbientMint, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddMacroDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, trigger: String, desc: String, stepsJson: String) -> Unit
) {
    var routineName by remember { mutableStateOf("") }
    var triggerPhrase by remember { mutableStateOf("") }
    var spokenGreeting by remember { mutableStateOf("") }
    var setVolume100 by remember { mutableStateOf(false) }
    var toggleTorch by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianDark,
        title = {
            Text("Create Custom Voice Routine", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Routine Name", color = TextSecondaryDark) },
                    placeholder = { Text("e.g. Gym Workout Routine", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = triggerPhrase,
                    onValueChange = { triggerPhrase = it },
                    label = { Text("Voice Trigger Phrase", color = TextSecondaryDark) },
                    placeholder = { Text("e.g. Start workout", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = spokenGreeting,
                    onValueChange = { spokenGreeting = it },
                    label = { Text("Spoken Voice Announcement", color = TextSecondaryDark) },
                    placeholder = { Text("e.g. Let's start the workout session!", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateBorderDark,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Checkbox options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Flashlight On", color = TextPrimaryDark, fontSize = 13.sp)
                    Switch(
                        checked = toggleTorch,
                        onCheckedChange = { toggleTorch = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyberCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set Max Media Volume", color = TextPrimaryDark, fontSize = 13.sp)
                    Switch(
                        checked = setVolume100,
                        onCheckedChange = { setVolume100 = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyberCyan)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (routineName.isNotBlank() && triggerPhrase.isNotBlank()) {
                        val steps = mutableListOf<String>()
                        if (toggleTorch) steps.add("TORCH_ON")
                        if (setVolume100) steps.add("SET_VOLUME:100")
                        if (spokenGreeting.isNotBlank()) steps.add("SPEAK:$spokenGreeting")

                        val stepsJson = steps.joinToString(prefix = "[\"", separator = "\", \"", postfix = "\"]")
                        onConfirm(routineName, triggerPhrase, "Triggered by \"$triggerPhrase\"", stepsJson)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Routine", color = SpaceBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}
