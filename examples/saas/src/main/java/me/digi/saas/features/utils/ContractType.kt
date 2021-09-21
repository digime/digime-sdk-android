package me.digi.saas.features.utils

//sealed class ContractType {
//    object Pull: ContractType()
//    object Push: ContractType()
//    object ReadRaw: ContractType()
//}

object ContractType {
    const val key = "contractType"
    const val pull = "pull"
    const val push = "push"
    const val readRaw = "readRaw"
}