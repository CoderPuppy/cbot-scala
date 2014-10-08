package cpup.cbot.plugin.commandPlugin

import cpup.lib.data.ETupleBuilder
import scala.language.implicitConversions

trait ArgResult[+T] {
	def isDefined: Boolean
	def isEmpty: Boolean

	def get: T

	def map[R](f: (T) => R): ArgResult[R]
	def flatMap[R](f: (T) => ArgResult[R]): ArgResult[R]
	def or[R >: T](f: => R): ArgResult[R]
	def flatOr[R >: T](f: => ArgResult[R]): ArgResult[R]
}
object ArgResult {
	def from[T](v: T): ArgResult[T] = if(v == null) {
		ArgError("is null")
	} else {
		Argument(v)
	}

	def fromOption[T](opt: Option[T]): ArgResult[T] = if(opt == None) {
		ArgError("is None")
	} else {
		Argument(opt.get)
	}

	object syntax {
		implicit val eTupleBuilder = new ETupleBuilder[ArgResult]
		implicit def eTuple1[T](w: ArgResult[T]) = eTupleBuilder(w)
	}
}
case class Argument[T](value: T) extends ArgResult[T] {
	override def isDefined = true

	override def isEmpty = false

	override def get = value

	override def map[R](f: (T) => R) = Argument(f(value))
	override def flatMap[R](f: (T) => ArgResult[R]) = f(value)

	override def or[R >: T](f: => R) = this
	override def flatOr[R >: T](f: => ArgResult[R]) = this
}
case class ArgError(msg: String) extends Exception(msg) with ArgResult[Nothing] {
	override def isDefined = false
	override def isEmpty = true

	override def get = throw new NullPointerException("ArgError")

	override def map[R](f: (Nothing) => R) = this
	override def flatMap[R](f: (Nothing) => ArgResult[R]) = this

	override def or[R >: Nothing](f: => R) = ArgResult.from(f)
	override def flatOr[R >: Nothing](f: => ArgResult[R]) = f
}