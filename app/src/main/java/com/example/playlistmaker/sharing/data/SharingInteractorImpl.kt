package com.example.playlistmaker.sharing.data


import android.content.Context
import com.example.playlistmaker.R
import com.example.playlistmaker.sharing.domain.ExternalNavigator
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.model.EmailData

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val context: Context
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareLink(getShareAppLink())
    }

    override fun openTerms() {
        externalNavigator.openLink(getTermsLink())
    }

    override fun openSupport() {
        externalNavigator.openEmail(getSupportEmailData())
    }

    private fun getShareAppLink(): String {
        return context.getString(R.string.url_of_Practicum)
    }

    private fun getTermsLink(): String {
        return context.getString(R.string.terms_of_use)
    }

    private fun getSupportEmailData(): EmailData {
        return EmailData(
            emailAddress = context.getString(R.string.myEmail),
            subject = context.getString(R.string.theme_support),
            message = context.getString(R.string.message_support)
        )
    }
}