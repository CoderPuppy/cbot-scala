package cpup.cbot.plugin.commandPlugin

import cpup.cbot.{Context, CBot}
import cpup.cbot.users.{IRCUser, User}
import cpup.lib.data.ETupleBuilder
import scala.language.implicitConversions
import cpup.cbot.plugin.{Plugin, PluginType}
import cpup.cbot.channels.Channel

case class ArgDef[T](name: String, parse: (CBot, Context, IRCUser, String) => ArgResult[T], default: Option[(CBot, Context, IRCUser) => ArgResult[_ <: T]]  = None) {
	self =>

	override def toString = if(default.isDefined) {
		s"[$name]"
	} else {
		s"<$name>"
	}

	def withName(name: String) = ArgDef[T](name, parse, default)

	def required = ArgDef[T](name, parse, None)
	def default(default: (CBot, Context, IRCUser) => ArgResult[_ <: T]) = ArgDef[T](name, parse, Some(default))

	def res[R](opt: Option[R]) = ArgResult.fromOption(opt)
	def res[R](v: R) = ArgResult.from(v)

	def map[R](f: (CBot, Context, IRCUser, T) => R) = new ArgDef[R](name, (bot, context, ircUser, arg) => {
		self.parse(bot, context, ircUser, arg).map(f.curried(bot)(context)(ircUser))
	})
	def map[R](f: T => R): ArgDef[R] = map((bot: CBot, context: Context, ircUser: IRCUser, arg: T) => f(arg))

	def flatMap[R](f: T => ArgDef[R]) = new ArgDef[R](name, (bot, context, ircUser, arg) => {
		self.parse(bot, context, ircUser, arg).flatMap((v) => {
			f(v).parse(bot, context, ircUser, arg)
		})
	})
}
object ArgDef {
	lazy val string = new ArgDef[String]("string", (bot, context, ircUser, arg) => ArgResult.from(arg))
	lazy val str = string

	lazy val user = new ArgDef[User]("user", (bot, context, ircUser, arg) => {
		ArgResult.from(bot.users.fromNick(arg).user)//.flatOr(ArgResult.fromOption(bot.users.fromUsername(arg)))
	}, Some((bot, context, ircUser) => {
		ArgResult.from(ircUser.user)
	}))

	lazy val ircUser = new ArgDef[IRCUser]("nickname", (bot, context, ircUser, arg) => {
		ArgResult.from(bot.users.fromNick(arg))
	}, Some((bot, context, ircUser) => {
		ArgResult.from(ircUser)
	}))

	lazy val context = new ArgDef[Context]("context", (bot, context, ircUser, arg) => {
		ArgResult.from(bot.getContext(arg))
	}, Some((bot, context, ircUser) => {
		ArgResult.from(context)
	}))

	lazy val channel = new ArgDef[Channel]("channel", (bot, context, ircUser, arg) => {
		ArgResult.from(bot.channels(arg))
	})

	lazy val channels = ArgDef.commas(ArgDef.channel).default((bot, context, ircUser) => {
		ArgResult.from(context match {
			case chan: Channel =>
				List(chan)
			case _ =>
				List()
		})
	})

	def commas[T](argDef: ArgDef[T]) = new ArgDef[List[T]](argDef.name + ",", (bot, context, ircUser, arg) => {
		val res = arg.split(",").map(argDef.parse(bot, context, ircUser, _))
		res.find(_.isInstanceOf[ArgError]) match {
			case Some(error) =>
				error.asInstanceOf[ArgError]
			case None =>
				ArgResult.from(res.map(_.get).toList)
		}
	}, Some((bot, context, ircUser) => {
		ArgResult.from(List())
	}))

	def pluginType(pluginTypes: Map[String, PluginType[_ <: Plugin]]) = {
		new ArgDef[PluginType[_ <: Plugin]]("pluginType", (bot, context, ircUser, arg) => {
			ArgResult.fromOption(pluginTypes.get(arg))
		})
	}

//	object syntax {
//		implicit val eTupleBuilder = new ETupleBuilder[ArgDef]
//		implicit def eTuple1[T](w: ArgDef[T]) = eTupleBuilder(w)
//	}
}