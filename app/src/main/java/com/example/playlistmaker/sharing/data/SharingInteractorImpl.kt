package com.example.playlistmaker.sharing.data

import com.example.playlistmaker.sharing.domain.ExternalNavigator
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.SharingResourceRepository

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val resourceRepository: SharingResourceRepository
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareLink(resourceRepository.getShareAppLink())
    }

    override fun openTerms() {
        externalNavigator.openLink(resourceRepository.getTermsLink())
    }

    override fun openSupport() {
        externalNavigator.openEmail(resourceRepository.getSupportEmailData())
    }
}