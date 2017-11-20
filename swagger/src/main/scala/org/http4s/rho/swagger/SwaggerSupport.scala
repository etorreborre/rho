package org.http4s
package rho
package swagger

import cats.Monad
import io.swagger.util.Json
import headers.`Content-Type`
import org.http4s.rho.bits.HListToFunc
import org.http4s.rho.bits.PathAST.TypedPath
import org.http4s.rho.swagger.models.{Info, Swagger}
import shapeless._

object SwaggerSupport {

  /**
   * Create a RhoMiddleware adding a route to get the Swagger json file
   * representing the full API
   */
  def apply[F[_]: Monad](
    swaggerFormats: SwaggerFormats = DefaultSwaggerFormats,
    apiPath: TypedPath[F, HNil] = "swagger.json",
    apiInfo: Info = Info(title = "My API", version = "1.0.0"),
    swaggerRoutesInSwagger: Boolean = false): RhoMiddleware[F] = { routes =>

    lazy val swaggerSpec: Swagger =
      createSwagger(swaggerFormats, apiPath, apiInfo)(
        routes ++ (if(swaggerRoutesInSwagger) swaggerRoute else Seq.empty )
      )

    lazy val swaggerRoute: Seq[RhoRoute[F, _ <: HList]] =
      createSwaggerRoute(swaggerSpec, apiPath).getRoutes

    routes ++ swaggerRoute
  }

  /**
   * Create the swagger model for a set of routes
   */
  def createSwagger[F[_]](
             swaggerFormats: SwaggerFormats = DefaultSwaggerFormats,
             apiPath: TypedPath[F, HNil] = "swagger.json",
             apiInfo: Info = Info(title = "My API", version = "1.0.0"))(routes: Seq[RhoRoute[F, _]]): Swagger = {
    val sb = new SwaggerModelsBuilder(swaggerFormats)
    routes.foldLeft(Swagger())((s, r) => sb.mkSwagger(apiInfo, r)(s))
  }

  /**
   * Create a RhoService with the route to the Swagger json
   * for the given Swagger Specification
   */
  def createSwaggerRoute[F[_]](
    swagger: => Swagger,
    apiPath: TypedPath[F, HNil] = "swagger.json"
  )(implicit F: Monad[F]): RhoService[F] = new RhoService[F] {

    lazy val response: F[OK[F, String]] = {
      val fOk = Ok[F].apply(
        Json.mapper()
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(swagger.toJModel)
      )

      F.map(fOk) { ok =>
        ok.copy(resp = ok.resp.putHeaders(`Content-Type`(MediaType.`application/json`)))
      }
    }

    "Swagger documentation" ** GET / apiPath |>> (() => response)
  }
}
