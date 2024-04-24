package me.digi.sdk.entities

import org.json.JSONObject

data class WriteDataPayload(
    val metadata: WriteMetadata,
    val content: ByteArray,
)

data class WriteMetadata(
    var accounts: List<WriteAccount>? = null,
    var mimeType: String? = null,
    var objectTypes: List<ObjectType>? = null,
    var providerName: List<String>? = null,
    var reference: List<String>? = null,
    var serviceGroups: List<MetadataServiceGroup>? = null,
    var tags: List<String>? = null,
    var appId: String? = null,
    var contractId: String? = null
)

data class MetadataServiceGroup(
    val serviceGroupId: String?
)

data class ObjectType(
    val name: String?,
    val references: List<String>?,
    val typeDef: JSONObject,
)


data class WriteAccount(
    var accountId: String? = null,
    var references: List<String>? = null,
    var username: String? = null
)