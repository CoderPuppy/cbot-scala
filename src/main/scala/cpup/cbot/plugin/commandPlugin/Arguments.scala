package cpup.cbot.plugin.commandPlugin

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class Arguments(named: Map[String, Either[String, Boolean]], positional: List[String]) {
	def slice(start: Int, end: Int = positional.length) = Arguments(named, positional.slice(start, end))

	def apply(i: Int) = if(i < positional.length) { Some(positional(i)) } else { None }
	def apply(name: String) = named.get(name)
	def isSet(name: String) = named.getOrElse(name, Right(false)).right.getOrElse(false)
}
object Arguments {
	def parse(args: Seq[String]) = {
		val named = new mutable.HashMap[String, Either[String, Boolean]]()
		val positional = new ListBuffer[String]

		for(arg <- args) {
			if(arg.startsWith("--")) {
				val remaining = arg.substring(2)
				if(remaining.contains("=")) {
					val (key, value) = remaining.splitAt(arg.indexOf('='))
					named(key) = Left(value)
				} else {
					if(remaining.startsWith("no-")) {
						named(remaining.substring(3)) = Right(false)
					} else if(remaining.startsWith("not-")) {
						named(remaining.substring(4)) = Right(false)
					} else {
						named(remaining) = Right(true)
					}
				}
			} else if(arg.startsWith("-")) {
				val remaining = arg.substring(1)

				if(remaining.substring(1, 2) == "=") {
					named(remaining.substring(0, 1)) = Left(remaining.substring(2))
				} else {
					for(short <- remaining) {
						named(short.toString) = Right(true)
					}
				}
			} else {
				positional += arg
			}
		}

		Arguments(named.toMap, positional.toList)
	}
}
class InvalidUsageException(msg: String) extends Exception(msg)