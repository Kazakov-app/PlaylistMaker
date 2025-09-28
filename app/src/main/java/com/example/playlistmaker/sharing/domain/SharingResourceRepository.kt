package com.example.playlistmaker.sharing.domain

import com.example.playlistmaker.sharing.domain.model.EmailData

interface SharingResourceRepository {
    fun getShareAppLink(): String
    fun getTermsLink(): String
    fun getSupportEmailData(): EmailData
}