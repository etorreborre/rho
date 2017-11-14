package org.http4s
package rho.bits

import cats.Monad
import org.http4s.rho.bits.ResponseGeneratorInstances.BadRequest

import scala.reflect.runtime.universe.TypeTag

/** Parse values from a `String`
  *
  * @tparam T The result type generated by the parser.
  */
trait StringParser[F[_], T] {

  /** Attempt to parse the `String`. */
  def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, T]): ResultResponse[F, T]

  /** TypeTag of the type T */
  def typeTag: Option[TypeTag[T]]
}

class BooleanParser[F[_]] extends StringParser[F, Boolean] {
  override val typeTag: Some[TypeTag[Boolean]] = Some(implicitly[TypeTag[Boolean]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Boolean]): ResultResponse[F, Boolean] = s match {
    case "true"  => SuccessResponse(true)
    case "false" => SuccessResponse(false)
    case _       => FailureResponse.pure[F] { BadRequest[F].pure(s"Invalid boolean format: '$s'") }
  }
}

class DoubleParser[F[_]] extends StringParser[F, Double] {
  override val typeTag: Some[TypeTag[Double]] = Some(implicitly[TypeTag[Double]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Double]): ResultResponse[F, Double] =
    try SuccessResponse(s.toDouble)
    catch { case e: NumberFormatException => StringParser.invalidNumberFormat[F, Double](s) }
}

class FloatParser[F[_]] extends StringParser[F, Float] {
  override val typeTag: Some[TypeTag[Float]] = Some(implicitly[TypeTag[Float]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Float]): ResultResponse[F, Float] =
    try SuccessResponse(s.toFloat)
    catch { case e: NumberFormatException => StringParser.invalidNumberFormat[F, Float](s) }
}

class IntParser[F[_]] extends StringParser[F, Int] {
  override val typeTag: Some[TypeTag[Int]] = Some(implicitly[TypeTag[Int]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Int]): ResultResponse[F, Int] =
    try SuccessResponse(s.toInt)
    catch { case e: NumberFormatException => StringParser.invalidNumberFormat[F, Int](s) }
}

class LongParser[F[_]] extends StringParser[F, Long] {
  override val typeTag: Some[TypeTag[Long]] = Some(implicitly[TypeTag[Long]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Long]): ResultResponse[F, Long] =
    try SuccessResponse(s.toLong)
    catch { case e: NumberFormatException => StringParser.invalidNumberFormat[F, Long](s) }
}

class ShortParser[F[_]] extends StringParser[F, Short] {
  override val typeTag: Some[TypeTag[Short]] = Some(implicitly[TypeTag[Short]])

  override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, Short]): ResultResponse[F, Short] =
    try SuccessResponse(s.toShort)
    catch { case e: NumberFormatException => StringParser.invalidNumberFormat[F, Short](s) }
}

object StringParser {

  ////////////////////// Default parsers //////////////////////////////

  implicit def booleanParser[F[_]]: BooleanParser[F] = new BooleanParser[F]()
  implicit def doubleParser[F[_]]: DoubleParser[F] = new DoubleParser[F]()
  implicit def floatParser[F[_]]: FloatParser[F] = new FloatParser[F]()
  implicit def intParser[F[_]]: IntParser[F] = new IntParser[F]()
  implicit def longParser[F[_]]: LongParser[F] = new LongParser[F]()
  implicit def shortParser[F[_]]: ShortParser[F] = new ShortParser[F]()

  implicit def strParser[F[_]]: StringParser[F, String] = new StringParser[F, String] {

    override val typeTag: Some[TypeTag[String]] = Some(implicitly[TypeTag[String]])

    override def parse(s: String)(implicit F: Monad[F], w: EntityEncoder[F, String]): ResultResponse[F, String] =
      SuccessResponse(s)
  }

  def invalidNumberFormat[F[_], A](n : String)(implicit F: Monad[F], w: EntityEncoder[F, A]): FailureResponse[F] = FailureResponse.pure[F] {
    BadRequest[F].pure(s"Invalid number format: '$n'")
  }
}
