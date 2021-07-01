package me.digi.saas.framework.di

import me.digi.saas.data.clients.SaasClients
import me.digi.saas.data.clients.SaasClientsImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val clientsModule: Module = module {
    single<SaasClients> { SaasClientsImpl(get()) }
}