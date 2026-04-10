package ba.fluxor.fetchapi.network.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GraphQlRequest(
    val query: String,
    val variables: Map<String, String>? = null,
    val operationName: String? = null,
)
