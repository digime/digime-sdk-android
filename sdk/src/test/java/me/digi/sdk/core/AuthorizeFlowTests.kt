/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */
package me.digi.sdk.core

import android.app.Activity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import me.digi.sdk.core.internal.ipc.DigiMeDirectResolver
import me.digi.sdk.core.internal.ipc.DigiMeFirstInstallResolver
import me.digi.sdk.core.session.SessionResult
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthorizeFlowTests {
    @MockK(relaxed = true)
    private lateinit var activity: Activity

    private lateinit var digiMeClient: DigiMeClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(DigiMeClient::class)
        every { DigiMeClient.checkClientInitialized() } returns Unit

        digiMeClient = spyk(DigiMeClient.getInstance())
        every { digiMeClient.createSession(any<SDKCallback<SessionResult>>(), any()) } returns Unit

        mockkConstructor(DigiMeDirectResolver::class)
        every { anyConstructed<DigiMeDirectResolver>().resolveAuthFlow(any(), any(), any<SDKCallback<SessionResult>>()) } returns Unit

        mockkConstructor(DigiMeFirstInstallResolver::class)
        every { anyConstructed<DigiMeFirstInstallResolver>().resolveAuthFlow(any(), any(), any<SDKCallback<SessionResult>>()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkStatic(DigiMeClient::class)
        unmockkConstructor(DigiMeDirectResolver::class)
        unmockkConstructor(DigiMeFirstInstallResolver::class)
    }

    @Test
    fun `authorize for guest always uses direct resolver`() {
        val authManager = spyk(DigiMeGuestCAAuthManager())
        digiMeClient.caAuthManager = authManager

        every { digiMeClient.createCAAuthManagerForGuest() } returns authManager
        every { authManager.nativeClientAvailable(any()) } returns true

        digiMeClient.authorizeGuest(activity, null)
        assertTrue(digiMeClient.caAuthManager.resolver is DigiMeDirectResolver)

        every { authManager.nativeClientAvailable(any()) } returns false
        digiMeClient.authorizeGuest(activity, null)
        assertTrue(digiMeClient.caAuthManager.resolver is DigiMeDirectResolver)
    }

    @Test
    fun `authorize use direct resolver when app available`() {
        val authManager = spyk(DigiMeConsentAccessAuthManager())
        digiMeClient.caAuthManager = authManager

        every { digiMeClient.createCAAuthManager() } returns authManager
        every { authManager.nativeClientAvailable(any()) } returns true
        digiMeClient.authorize(activity, null)
        assertTrue(digiMeClient.caAuthManager.resolver is DigiMeDirectResolver)
    }

    @Test
    fun `authorize use first install resolver when app unavailable`() {
        val authManager = spyk(DigiMeConsentAccessAuthManager())
        every { authManager.startInstallDigiMeFlow(any()) } returns Unit
        digiMeClient.caAuthManager = authManager

        every { digiMeClient.createCAAuthManager() } returns authManager
        every { authManager.nativeClientAvailable(any()) } returns false
        digiMeClient.authorize(activity, null)
        assertTrue(digiMeClient.caAuthManager.resolver is DigiMeFirstInstallResolver)
    }
}