package com.innioasis.launcher3.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import androidx.annotation.NonNull
import androidx.annotation.Nullable

class LauncherProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.innioasis.launcher3.settings"
        const val SETTINGS_URI = "content://$AUTHORITY/settings"

        private const val SETTINGS = 1
        private const val SETTING_ID = 2

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "settings", SETTINGS)
            addURI(AUTHORITY, "settings/#", SETTING_ID)
        }
    }

    private lateinit var mContext: Context

    override fun onCreate(): Boolean {
        mContext = context!!
        return true
    }

    @Nullable
    override fun query(
        @NonNull uri: Uri,
        @Nullable projection: Array<out String>?,
        @Nullable selection: String?,
        @Nullable selectionArgs: Array<out String>?,
        @Nullable sortOrder: String?
    ): Cursor? {
        val match = sUriMatcher.match(uri)
        return when (match) {
            SETTINGS -> querySettings(projection)
            SETTING_ID -> querySetting(uri.lastPathSegment, projection)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Nullable
    override fun getType(@NonNull uri: Uri): String? {
        val match = sUriMatcher.match(uri)
        return when (match) {
            SETTINGS -> "vnd.android.cursor.dir/vnd.innioasis.launcher3.settings"
            SETTING_ID -> "vnd.android.cursor.item/vnd.innioasis.launcher3.settings"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Nullable
    override fun insert(@NonNull uri: Uri, @Nullable values: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        return when (match) {
            SETTINGS -> insertSetting(values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(
        @NonNull uri: Uri,
        @Nullable values: ContentValues?,
        @Nullable selection: String?,
        @Nullable selectionArgs: Array<out String>?
    ): Int {
        val match = sUriMatcher.match(uri)
        return when (match) {
            SETTING_ID -> updateSetting(uri.lastPathSegment, values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(
        @NonNull uri: Uri,
        @Nullable selection: String?,
        @Nullable selectionArgs: Array<out String>?
    ): Int {
        val match = sUriMatcher.match(uri)
        return when (match) {
            SETTING_ID -> deleteSetting(uri.lastPathSegment)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Nullable
    override fun call(
        @NonNull method: String,
        @Nullable request: String?,
        @Nullable args: Bundle?
    ): Bundle? {
        return when (method) {
            "get_all_settings" -> {
                val bundle = Bundle()
                bundle.putString("settings", "all")
                bundle
            }
            else -> super.call(method, request, args)
        }
    }

    private fun querySettings(projection: Array<out String>?): MatrixCursor {
        val columns = projection ?: arrayOf("_id", "key", "value", "type")
        val cursor = MatrixCursor(columns)
        // Add default settings
        cursor.addRow(arrayOf(1, "workspace_page", "0", "int"))
        cursor.addRow(arrayOf(2, "grid_size", "4x5", "string"))
        cursor.addRow(arrayOf(3, "icon_size", "normal", "string"))
        cursor.addRow(arrayOf(4, "show_labels", "true", "boolean"))
        cursor.addRow(arrayOf(5, "animation_speed", "1.0", "float"))
        return cursor
    }

    private fun querySetting(key: String?, projection: Array<out String>?): MatrixCursor? {
        key?.let {
            val columns = projection ?: arrayOf("_id", "key", "value", "type")
            val cursor = MatrixCursor(columns)
            // Return default value for key
            when (key) {
                "workspace_page" -> cursor.addRow(arrayOf(1, key, "0", "int"))
                "grid_size" -> cursor.addRow(arrayOf(2, key, "4x5", "string"))
                "icon_size" -> cursor.addRow(arrayOf(3, key, "normal", "string"))
                "show_labels" -> cursor.addRow(arrayOf(4, key, "true", "boolean"))
                "animation_speed" -> cursor.addRow(arrayOf(5, key, "1.0", "float"))
            }
            return cursor
        }
        return null
    }

    private fun insertSetting(values: ContentValues?): Uri? {
        values?.getAsString("key")?.let { key ->
            // Store in SharedPreferences or similar
            return Uri.parse("$SETTINGS_URI/$key")
        }
        return null
    }

    private fun updateSetting(key: String?, values: ContentValues?): Int {
        return if (key != null && values != null) 1 else 0
    }

    private fun deleteSetting(key: String?): Int {
        return if (key != null) 1 else 0
    }
}