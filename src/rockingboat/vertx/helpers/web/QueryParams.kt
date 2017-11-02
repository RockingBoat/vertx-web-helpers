package rockingboat.vertx.helpers.web

import io.vertx.ext.web.RoutingContext

data class Paging(val limit: Int, val page: Int, val sort: String)

private var currentPageLimit: Int = 20

@Suppress("unused")
fun RoutingContext.pageLimit(limit: Int) {
    currentPageLimit = limit
}

@Suppress("unused")
fun RoutingContext.paging() = Paging(
        queryParam("limit").firstOrNull()?.toInt() ?: currentPageLimit,
        queryParam("page").firstOrNull()?.toInt() ?: 0,
        queryParam("sort").firstOrNull() ?: ""
)