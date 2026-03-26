package com.diabdata.feature.userProfile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.diabdata.core.database.DataRepository
import com.diabdata.core.model.UserDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: DataRepository,
    application: Application
) : AndroidViewModel(application) {
    val userDetails: StateFlow<UserDetails?> = repository.getUserDetails()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), null)

    fun updateUserDetails(userDetails: UserDetails) {
        viewModelScope.launch {
            repository.updateUserDetails(userDetails)
        }
    }

    fun deleteUserDetails() = viewModelScope.launch {
        repository.deleteUserDetails()
    }

    fun saveProfilePhoto(uri: Uri, onSaved: (String) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(application.filesDir, fileName)

            application.filesDir.listFiles()
                ?.filter { it.name.startsWith("profile_photo_") && it.name != fileName }
                ?.forEach { it.delete() }

            application.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            val path = file.absolutePath
            repository.addProfilePhotoPath(path)

            withContext(Dispatchers.Main) {
                onSaved(path)
            }
        }
    }

    suspend fun updateProfilePhotoPath(path: String) {
        repository.addProfilePhotoPath(path)
    }
}