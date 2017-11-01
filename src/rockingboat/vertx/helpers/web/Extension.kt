package rockingboat.vertx.helpers.web

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
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
    // TODO: Fix if we call Vertx from main func (Here is NPE)
    val instance = Vertx.currentContext().get<Router>("router")
    if (instance == null) {
        val router = Router.router(Vertx.currentContext().owner())
        Vertx.currentContext().put("router", router)
        return router
    }

    return instance
}

@Suppress("unused")
fun HttpServer.enableCORSGlobal(corsHost: String, allowCredentials: Boolean): HttpServer {
    val router = this.defaultRouter()
    router.route().handler(CorsHandler.create(corsHost)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.CONNECT)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.TRACE)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.DELETE)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.HEAD)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.OTHER)
                                   .allowedMethod(io.vertx.core.http.HttpMethod.PATCH)
                                   .allowCredentials(allowCredentials)
                                   .allowedHeader("Access-Control-Allow-Method")
                                   .allowedHeader("Access-Control-Allow-Origin")
                                   .allowedHeader("Access-Control-Allow-Credentials")
                                   .allowedHeader("Content-Type"))
    return this
}

sealed class ControllerMethod {
    abstract var path: String
    abstract val function: KFunction<*>
    abstract var enableCors: Boolean

    data class Concrete(val method: HttpMethod,
                        override var path: String,
                        override val function: KFunction<*>,
                        override var enableCors: Boolean = false) : ControllerMethod()

    data class All(override var path: String,
                   override val function: KFunction<*>,
                   override var enableCors: Boolean = false) : ControllerMethod()

    fun setControllerPath(cPath: String): ControllerMethod {
        path = (cPath + path).replace("//", "/")
        return this
    }
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
                    val controllerMethod = when (it) {
                        is Get     -> ControllerMethod.Concrete(HttpMethod.GET, it.path, function)
                        is Post    -> ControllerMethod.Concrete(HttpMethod.POST, it.path, function)
                        is Put     -> ControllerMethod.Concrete(HttpMethod.PUT, it.path, function)
                        is Patch   -> ControllerMethod.Concrete(HttpMethod.PATCH, it.path, function)
                        is Delete  -> ControllerMethod.Concrete(HttpMethod.DELETE, it.path, function)
                        is Trace   -> ControllerMethod.Concrete(HttpMethod.TRACE, it.path, function)
                        is Connect -> ControllerMethod.Concrete(HttpMethod.CONNECT, it.path, function)
                        is Options -> ControllerMethod.Concrete(HttpMethod.OPTIONS, it.path, function)
                        is Head    -> ControllerMethod.Concrete(HttpMethod.HEAD, it.path, function)
                        is All     -> ControllerMethod.All(it.path, function)
                        is Route   -> ControllerMethod.Concrete(it.method, it.path, function)
                        else       -> null
                    }
                    controllerMethod?.setControllerPath(ctrlConfig.path)
                }
            }
                    .flatMap { it }
                    .filterNotNull()
                    .forEach {

                        when (it) {
                            is ControllerMethod.Concrete -> router.route(it.method, it.path)
                            is ControllerMethod.All      -> router.route(it.path)
                        }
                                .handler { ctx -> it.function.call(instance, ctx) }
                                .failureHandler { ctx ->
                                    ctx.jsonResponse(ctx.failure()?.message ?: "Unknown Error",
                                                     -1,
                                                     if (ctx.statusCode() > 0) ctx.statusCode() else 500
                                                    )

                                }


                        if (it.enableCors) {
                            router.route(HttpMethod.OPTIONS, it.path)
                                    .handler { ctx -> it.function.call(instance, ctx) }
                                    .failureHandler { ctx ->
                                        ctx.jsonResponse(ctx.failure()?.message ?: "Unknown Error",
                                                         -1,
                                                         if (ctx.statusCode() > 0) ctx.statusCode() else 500
                                                        )

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



