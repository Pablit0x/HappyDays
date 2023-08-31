package com.ps.dimensional_feels.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Long.toLocalDate(): LocalDate {
    val instant = Instant.ofEpochMilli(this)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun RealmInstant.toInstant(): Instant {
    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0) {
        Instant.ofEpochSecond(sec, nano.toLong())
    } else {
        Instant.ofEpochSecond(sec - 1, 1_000_000 + nano.toLong())
    }
}


//https://www.mongodb.com/docs/realm/sdk/kotlin/realm-database/schemas/supported-types/
fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSecond
    val nano: Int = this.nano
    return if (sec >= 0) {
        RealmInstant.from(sec, nano)
    } else {
        RealmInstant.from(sec + 1, -1_000_000 + nano)
    }
}
fun fetchImagesFromFirebase(
    remoteImagePaths: List<String>,
    onImageDownload: (Uri) -> Unit,
    onImageDownloadFailed: (Exception) -> Unit = {},
    onReadyToDisplay: () -> Unit = {}
) {
    if (remoteImagePaths.isNotEmpty()) {
        remoteImagePaths.forEachIndexed { index, remoteImagePath ->
            val trimmedRemoteImagePath = remoteImagePath.trim()
            if (trimmedRemoteImagePath.isNotEmpty()) {
                FirebaseStorage.getInstance().reference.child(trimmedRemoteImagePath).downloadUrl.addOnSuccessListener {
                    onImageDownload(it)
                    if (remoteImagePaths.lastIndexOf(remoteImagePaths.last()) == index) {
                        onReadyToDisplay()
                    }
                }.addOnFailureListener {
                    onImageDownloadFailed(it)
                }
            }
        }
    }
}


