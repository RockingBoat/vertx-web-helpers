package vertx.helpers.web

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf

/**
 * Created by s.suslov on 12.06.17.
 */
val mapper = ObjectMapper().registerKotlinModule()

@Suppress("unused")
fun HttpServer.defaultRouter(): Router {
    val instance = Vertx.currentContext().get<Router>("router")
    if (instance == null) {
        val router = Router.router(Vertx.currentContext().owner())
        Vertx.currentContext().put("router", router)
        return router
    }

    return instance
}

@Suppress("unused")
fun HttpServer.enableCORSGlobal(): HttpServer {
    val router = this.defaultRouter()
    router.route().handler(CorsHandler.create("*")
        .allowedMethod(io.vertx.core.http.HttpMethod.GET)
        .allowedMethod(io.vertx.core.http.HttpMethod.POST)
        .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
        .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
        .allowedMethod(io.vertx.core.http.HttpMethod.CONNECT)
        .allowedMethod(io.vertx.core.http.HttpMethod.TRACE)
        .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
        .allowedMethod(io.vertx.core.http.HttpMethod.HEAD)
        .allowedMethod(io.vertx.core.http.HttpMethod.OTHER)
        .allowCredentials(true)
        .allowedHeader("Access-Control-Allow-Method")
        .allowedHeader("Access-Control-Allow-Origin")
        .allowedHeader("Access-Control-Allow-Credentials")
        .allowedHeader("Content-Type"))
    return this
}

@Suppress("unused")
fun HttpServer.controllers(vararg args: KClass<*>): HttpServer {
    val router = this.defaultRouter()
    router.route().handler(BodyHandler.create())
    args.forEach { kClass ->


        kClass.findAnnotation<Controller>()?.let { ctrlConfig ->

            val instance = kClass.companionObjectInstance ?: kClass.createInstance()
            instance::class.functions.map { function ->
                function.annotations.map {
                    val (method, path) = when (it) {
                        is Get     -> Pair(HttpMethod.GET, it.path)
                        is Post    -> Pair(HttpMethod.POST, it.path)
                        is Put     -> Pair(HttpMethod.PUT, it.path)
                        is Patch   -> Pair(HttpMethod.PATCH, it.path)
                        is Delete  -> Pair(HttpMethod.DELETE, it.path)
                        is Trace   -> Pair(HttpMethod.TRACE, it.path)
                        is Connect -> Pair(HttpMethod.CONNECT, it.path)
                        is Options -> Pair(HttpMethod.OPTIONS, it.path)
                        is Head    -> Pair(HttpMethod.HEAD, it.path)
                        is All     -> Pair(null, it.path)
                        is Route   -> Pair(it.method, it.path)
                        else       -> Pair(null, null)
                    }

                    if (path != null)
                        Triple("${ctrlConfig.path}$path", method, function)
                    else
                        null
                }
            }
                .flatMap { it }
                .filterNotNull()
                .forEach {

                    val method = it.second

                    if (method == null) {
                        router.route(it.first)
                    } else {
                        router.route(method, it.first)
                    }.handler { ctx -> it.third.call(instance, ctx) }
                        .failureHandler { ctx ->
                            val stCode = if (ctx.statusCode() > 0) ctx.statusCode() else 500


                            val fail = ctx.failure()
                            if (fail != null) {
                                ctx.response()
                                    .setStatusCode(stCode)
                                    .endWithJson(JsonObject().also {
                                        it.put("data", fail.message ?: "Unknown Error")
                                        it.put("code", -1)
                                    })
                            } else {
                                ctx.response()
                                    .setStatusCode(stCode)
                                    .endWithJson(JsonObject().also {
                                        it.put("data", "Unknown Error")
                                        it.put("code", -1)
                                    })
                            }

                        }
                }
        }
    }


    return this.requestHandler {
        router.accept(it)
    }
}

@Suppress("unused")
inline fun <reified T : Any> RoutingContext.param(name: String): T? {
    val sValue = this.request().getParam(name.toLowerCase()) ?: return null
    return convertStringValue(sValue)
}

@Suppress("unused")
inline fun <reified T : Any> RoutingContext.header(name: String): T? {
    val sValue = this.request().getHeader(name.toLowerCase()) ?: return null
    return convertStringValue(sValue)
}

inline fun <reified T : Any> convertStringValue(value: String): T? {
    val clazz = T::class
    return when {
        clazz.isSuperclassOf(String::class) -> value as T
        clazz.isSuperclassOf(Long::class)   -> value.toLongOrNull() as T
        clazz.isSuperclassOf(Int::class)    -> value.toIntOrNull() as T
        clazz.isSuperclassOf(Short::class)  -> value.toShortOrNull() as T
        else                                -> null
    }
}

@Suppress("unused")
inline fun <reified T : Any> RoutingContext.body(): T = mapper.readValue(this.bodyAsString)



