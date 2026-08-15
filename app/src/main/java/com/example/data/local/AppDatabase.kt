package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AssistantDao
import com.example.data.local.entities.ConversationMessage
import com.example.data.local.entities.ExpenseEntry
import com.example.data.local.entities.VoiceMacro
import com.example.data.local.entities.VoiceNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationMessage::class,
        VoiceMacro::class,
        VoiceNote::class,
        ExpenseEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assistantDao(): AssistantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_voice_assistant.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with default voice macros
                        scope.launch(Dispatchers.IO) {
                            INSTANCE?.assistantDao()?.let { dao ->
                                dao.insertMacro(
                                    VoiceMacro(
                                        name = "Good Morning Routine",
                                        triggerPhrase = "Good morning",
                                        description = "Turns on torch briefly, speaks greeting and sets audio volume",
                                        stepsJson = """["TORCH_ON", "SPEAK:Good morning! Have an energetic and productive day ahead.", "SET_VOLUME:70"]""",
                                        iconName = "wb_sunny"
                                    )
                                )
                                dao.insertMacro(
                                    VoiceMacro(
                                        name = "Night Focus Routine",
                                        triggerPhrase = "Good night",
                                        description = "Sets silent mode, turns off flashlight, and plays soft confirmation",
                                        stepsJson = """["TORCH_OFF", "SET_VOLUME:10", "SPEAK:Good night! Setting your phone to calm night mode."]""",
                                        iconName = "bedtime"
                                    )
                                )
                                dao.insertMacro(
                                    VoiceMacro(
                                        name = "Study & Work Mode",
                                        triggerPhrase = "Focus mode",
                                        description = "Mutes ringtone and speaks focus reminder",
                                        stepsJson = """["SET_VOLUME:0", "SPEAK:Focus mode activated. Do your best work!"]""",
                                        iconName = "psychology"
                                    )
                                )
                                dao.insertMacro(
                                    VoiceMacro(
                                        name = "শুভ সকাল রুটিন",
                                        triggerPhrase = "শুভ সকাল",
                                        description = "বাংলায় সম্ভাষণ ও ভলিউম সেট করা",
                                        stepsJson = """["SET_VOLUME:75", "SPEAK:শুভ সকাল! আপনার আজকের দিনটি সুন্দর ও সাফল্যমণ্ডিত হোক।"]""",
                                        iconName = "wb_sunny"
                                    )
                                )
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
