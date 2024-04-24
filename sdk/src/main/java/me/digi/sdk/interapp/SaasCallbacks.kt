package me.digi.sdk.interapp

import me.digi.sdk.Error
import me.digi.sdk.entities.response.SaasCallbackResponse
typealias AuthorizationCallback = (consentAuthResponse: SaasCallbackResponse?, error: Error?) -> Unit

typealias ServiceOnboardCallback= (onboardAuthResponse: SaasCallbackResponse?, error: Error?) -> Unit

typealias ServiceReAuthCallback = (reAuthResponse: SaasCallbackResponse?, error: Error?) -> Unit