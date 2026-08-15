package com.example.data.local

import com.example.data.local.dao.AssistantDao
import com.example.data.local.entities.ConversationMessage
import com.example.data.local.entities.ExpenseEntry
import com.example.data.local.entities.VoiceMacro
import com.example.data.local.entities.VoiceNote
import kotlinx.coroutines.flow.Flow

class AssistantRepository(private val dao: AssistantDao) {

    val conversations: Flow<List<ConversationMessage>> = dao.getAllConversations()
    val macros: Flow<List<VoiceMacro>> = dao.getAllMacros()
    val voiceNotes: Flow<List<VoiceNote>> = dao.getAllVoiceNotes()
    val expenses: Flow<List<ExpenseEntry>> = dao.getAllExpenses()
    val totalExpense: Flow<Double?> = dao.getTotalExpenseAmount()

    suspend fun saveConversation(query: String, response: String, action: String? = null, payload: String? = null, language: String = "en", isSuccess: Boolean = true): Long {
        return dao.insertConversation(
            ConversationMessage(
                query = query,
                response = response,
                intentAction = action,
                actionPayload = payload,
                language = language,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun deleteConversation(id: Long) = dao.deleteConversation(id)

    suspend fun clearHistory() = dao.clearAllConversations()

    suspend fun addMacro(macro: VoiceMacro) = dao.insertMacro(macro)

    suspend fun updateMacro(macro: VoiceMacro) = dao.updateMacro(macro)

    suspend fun deleteMacro(id: Long) = dao.deleteMacro(id)

    suspend fun addVoiceNote(title: String, content: String, summary: String = "", category: String = "General") =
        dao.insertVoiceNote(
            VoiceNote(
                title = title,
                content = content,
                summary = summary,
                category = category
            )
        )

    suspend fun deleteVoiceNote(id: Long) = dao.deleteVoiceNote(id)

    suspend fun addExpense(item: String, amount: Double, category: String = "General", currency: String = "৳") =
        dao.insertExpense(
            ExpenseEntry(
                item = item,
                amount = amount,
                category = category,
                currency = currency
            )
        )

    suspend fun deleteExpense(id: Long) = dao.deleteExpense(id)
}
