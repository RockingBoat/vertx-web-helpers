package rockingboat.vertx.helpers.web

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.Json as KJson

/**
 * Created by s.suslov on 12.06.17.
 */

@Suppress("unused")
fun RoutingContext.jsonResponse(data: Any?, statusCode: Int = 200, errorCode: Int = 0) {
    response().apply {
        jsonOutput()

        if (statusCode != 200) {
            setStatusCode(statusCode)
        }

        end(Json.encode(JsonObject().also {
            it.putObject("data", data)
            it.put("code", errorCode)
        }))
    }
}

@Suppress("unused")
fun RoutingContext.jsonResponseFail(data: Any?, errorCode: Int = 0) = jsonResponse(data, 400, errorCode)

@Suppress("unused")
fun RoutingContext.jsonResponseError(data: Any?, errorCode: Int = 0) = jsonResponse(data, 500, errorCode)

fun HttpServerResponse.jsonOutput() {
    putHeader("Content-Type", "application/json; charset=utf-8")
}

fun JsonObject.putObject(key: String, data: Any?) {
    when (data) {
        null -> putNull(key)
        is String -> put(key, data)
        is List<*> -> this.put(key, KJson.array(data))
        else -> this.put(key, JsonObject.mapFrom(data))
    }
}