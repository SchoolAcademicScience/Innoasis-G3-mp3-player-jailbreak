package com.innioasis.launcher3.model

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val activityInfo: android.content.pm.ActivityInfo?,
    val isSystemApp: Boolean = false,
    val isDisabled: Boolean = false,
    val installTime: Long = 0,
    val lastUsedTime: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Drawable::class.java.classLoader),
        parcel.readParcelable(android.content.pm.ActivityInfo::class.java.classLoader),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(label)
        parcel.writeParcelable(icon, flags)
        parcel.writeParcelable(activityInfo, flags)
        parcel.writeByte(if (isSystemApp) 1 else 0)
        parcel.writeByte(if (isDisabled) 1 else 0)
        parcel.writeLong(installTime)
        parcel.writeLong(lastUsedTime)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppInfo> {
        override fun createFromParcel(parcel: Parcel): AppInfo = AppInfo(parcel)
        override fun newArray(size: Int): Array<AppInfo?> = arrayOfNulls(size)
    }

    fun toShortString(): String = "$packageName ($label)"
}