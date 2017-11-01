package rockingboat.vertx.helpers.web

/**
 * Created by s.suslov on 12.06.17.
 */

import io.vertx.core.http.HttpMethod

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Controller(val path: String = "/")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Route(val method: HttpMethod, val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Get(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Post(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Put(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Options(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Head(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Delete(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trace(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Connect(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Patch(val path: String = "", val enableCors: Boolean = false)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class All(val path: String = "", val enableCors: Boolean = false)
