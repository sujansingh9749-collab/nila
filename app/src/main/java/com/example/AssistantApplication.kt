package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.AssistantRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AssistantApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AssistantRepository(database.assistantDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
