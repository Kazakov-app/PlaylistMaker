package com.example.playlistmaker.sharing.data

import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.SharingResourceRepository
import com.example.playlistmaker.sharing.domain.model.EmailData

class SharingResourceRepositoryImpl(private val context: Context) : SharingResourceRepository {
    override fun getShareAppLink(): String {
        return context.getString(R.string.url_of_Practicum)
    }

    override fun getTermsLink(): String {
        return context.getString(R.string.terms_of_use)
    }

    override fun getSupportEmailData(): EmailData {
        return EmailData(
            emailAddress = context.getString(R.string.myEmail),
            subject = context.getString(R.string.theme_support),
            message = context.getString(R.string.message_support)
        )
    }
}