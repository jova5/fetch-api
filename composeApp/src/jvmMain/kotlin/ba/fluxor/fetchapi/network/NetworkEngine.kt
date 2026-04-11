package ba.fluxor.fetchapi.network

interface NetworkEngine<Req, Resp> {
    suspend fun execute(request: Req): Resp
}
