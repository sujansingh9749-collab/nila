package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ScreenElement(
    val text: String,
    val contentDescription: String,
    val className: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val bounds: Rect,
    val viewId: String? = null
)

class ScreenAutomationService : AccessibilityService() {

    companion object {
        private const val TAG = "ScreenAutomationService"
        private var instance: ScreenAutomationService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _currentScreenSummary = MutableStateFlow("")
        val currentScreenSummary: StateFlow<String> = _currentScreenSummary.asStateFlow()

        private val _lastDetectedApp = MutableStateFlow("")
        val lastDetectedApp: StateFlow<String> = _lastDetectedApp.asStateFlow()

        private val securityBlacklistPackages = setOf(
            "com.android.settings.password",
            "com.eg.android.AlipayGphone",
            "com.bkash.app",
            "com.dbbl.nexus.pay",
            "com.dutchbangla.rocket",
            "com.nagad.customer"
        )

        fun getInstance(): ScreenAutomationService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "ScreenAutomationService connected successfully.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        event.packageName?.let { pkg ->
            val pkgStr = pkg.toString()
            _lastDetectedApp.value = pkgStr
            if (securityBlacklistPackages.contains(pkgStr)) {
                _currentScreenSummary.value = "[Protected Security/Financial App]"
                return
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            updateScreenContext()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "ScreenAutomationService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
    }

    // 1. Read & Analyze all visible text and interactive elements on the current screen
    fun readScreenContent(): List<ScreenElement> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val elements = mutableListOf<ScreenElement>()
        traverseNode(rootNode, elements)
        
        // Update summary for AI reasoning
        val visibleTexts = elements.mapNotNull { 
            if (it.text.isNotBlank()) it.text else if (it.contentDescription.isNotBlank()) it.contentDescription else null 
        }.distinct().take(30)
        _currentScreenSummary.value = visibleTexts.joinToString(" | ")

        return elements
    }

    private fun traverseNode(node: AccessibilityNodeInfo?, list: MutableList<ScreenElement>) {
        if (node == null) return

        // Skip password / sensitive entry fields for security
        if (node.isPassword) return

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val className = node.className?.toString() ?: ""
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        if (text.isNotBlank() || desc.isNotBlank() || isClickable || isEditable) {
            list.add(
                ScreenElement(
                    text = text,
                    contentDescription = desc,
                    className = className,
                    isClickable = isClickable,
                    isEditable = isEditable,
                    bounds = bounds,
                    viewId = node.viewIdResourceName
                )
            )
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), list)
        }
    }

    // 2. Click on a specific element matching text or description (Bengali or English)
    fun clickByText(query: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val targetQuery = query.trim().lowercase(Locale.getDefault())

        // Try direct find
        val directNodes = rootNode.findAccessibilityNodeInfosByText(query)
        for (node in directNodes) {
            if (performClickOnNodeOrParent(node)) {
                return true
            }
        }

        // Fuzzy tree search
        return searchAndClickNode(rootNode, targetQuery)
    }

    private fun searchAndClickNode(node: AccessibilityNodeInfo?, target: String): Boolean {
        if (node == null) return false

        val text = node.text?.toString()?.lowercase(Locale.getDefault()) ?: ""
        val desc = node.contentDescription?.toString()?.lowercase(Locale.getDefault()) ?: ""

        if (text.contains(target) || desc.contains(target)) {
            if (performClickOnNodeOrParent(node)) return true
        }

        for (i in 0 until node.childCount) {
            if (searchAndClickNode(node.getChild(i), target)) return true
        }
        return false
    }

    private fun performClickOnNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                val success = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) return true
            }
            current = current.parent
        }
        // If not clickable, simulate coordinate tap
        node?.let {
            val rect = Rect()
            it.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                tapAt(rect.centerX().toFloat(), rect.centerY().toFloat())
                return true
            }
        }
        return false
    }

    // 3. Type text into an active or focused input field
    fun typeText(textToType: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        if (focusedNode != null && (focusedNode.isEditable || focusedNode.isFocused)) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            }
            return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }

        // Find first editable element
        val editable = findFirstEditable(rootNode)
        if (editable != null) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            }
            return editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    private fun findFirstEditable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val res = findFirstEditable(node.getChild(i))
            if (res != null) return res
        }
        return null
    }

    // 4. Global System Gestures & Actions
    fun performGlobalBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performGlobalHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performGlobalRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun performGlobalNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun performGlobalQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

    // 5. Scroll Gestures
    fun scrollDown(): Boolean {
        val metrics = resources.displayMetrics
        val centerX = metrics.widthPixels / 2f
        val startY = metrics.heightPixels * 0.75f
        val endY = metrics.heightPixels * 0.25f
        return performSwipe(centerX, startY, centerX, endY, 300)
    }

    fun scrollUp(): Boolean {
        val metrics = resources.displayMetrics
        val centerX = metrics.widthPixels / 2f
        val startY = metrics.heightPixels * 0.25f
        val endY = metrics.heightPixels * 0.75f
        return performSwipe(centerX, startY, centerX, endY, 300)
    }

    // 6. Touch & Swipe Simulation via GestureDescription
    fun tapAt(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    private fun updateScreenContext() {
        try {
            readScreenContent()
        } catch (e: Exception) {
            Log.e(TAG, "Screen update error: ${e.message}")
        }
    }
}
