package vertx.helpers.web

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.Json as KJson

/**
 * Created by s.suslov on 12.06.17.
 */


fun HttpServerResponse.endWithJson(obj: Any) {
    putHeader("Content-Type", "application/json; charset=utf-8").end(Json.encode(obj))
}

@Suppress("unused")
fun HttpServerResponse.endJSend(data: Any?, code: Int = 0) {
    putHeader("Content-Type", "application/json; charset=utf-8")
        .end(Json.encode(JsonObject().also {
            it.putObject("data", data)
            it.put("code", code)
        }))
}

fun JsonObject.putObject(key: String, data: Any?) {
    when (data) {
        null       -> this.putNull(key)
        is List<*> -> this.put(key, KJson.array(data))
        else       -> this.put(key, JsonObject.mapFrom(data))
    }
}