package cpup.cbot.plugin.commandPlugin

import cpup.cbot.events.{Replyable, MessageEvent, Event}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.user.IRCUserEvent
import play.api.libs.json._
import java.io.File
import cpup.cbot.{Context, CBot}
import scala.collection.mutable
import cpup.cbot.plugin.{PluginType, ConfigOption, Plugin}

case class CommandPlugin(var prefix: String) extends Plugin {
	def pluginType = CommandPlugin

	override def configOptions = Set(
		new ConfigOption {
			override def name = "prefix"
			override def usage = "Prefix used for commands"

			override def get = prefix
			override def set(reply: Replyable, newVal: String) {
				if(newVal.length > 0) {
					prefix = newVal
				} else {
					reply.reply(s"$name must not be empty")
				}
			}
		}
	)

	@Subscribe
	def commandMessage(e: MessageEvent) {
		e match {
			case ev: IRCUserEvent with Replyable if e.msg.startsWith(prefix) =>
				val parts = e.msg.substring(prefix.length).split(' ')
				if(parts.length >= 1) {
					val event = CommandPlugin.CommandEvent(
						ev,
						parts(0),
						Arguments.parse(parts.view(1, parts.length))
					)
					e.bot.bus.post(event)
				}
			case _ =>
		}
	}
}

object CommandPlugin extends PluginType[CommandPlugin] {
	override def name = "command"
	override def create(context: Context, pluginTypes: Map[String, PluginType[Plugin]]) = new CommandPlugin("!")

	override def reads(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]], file: File) = Some(new Reads[CommandPlugin] {
		override def reads(json: JsValue) = (json \ "prefix").validate[String].map(CommandPlugin(_))
	})
	override def writes(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]]) = Some(new Writes[CommandPlugin] {
		override def writes(pl: CommandPlugin) = Json.obj(
			"prefix" -> pl.prefix
		)
	})

	trait TCommandCheckEvent extends Event {
		def command(command: TCommand[_])
	}

	case class CommandQueryEvent(bot: CBot) extends TCommandCheckEvent {
		def context = bot

		val commands = new mutable.HashMap[String, TCommand[_]]()

		override def command(command: TCommand[_]) {
			commands(command.name) = command
		}
	}

	trait TCommandEvent extends Event with IRCUserEvent with Replyable with TCommandCheckEvent {
		if(context.output.isEmpty) {
			throw new NoSuchMethodException("Cannot output to context")
		}

		def cmd: String
		def args: Arguments

		override def command(command: TCommand[_]) {
			def printUsage() {
				genericReply("Usage: ")
				for(usage <- command.usages) {
					genericReply(s" - $cmd $usage")
				}
			}

			if(command.name == cmd) {
				try
					command.asInstanceOf[TCommand[Any]].handle(this, command.parse(this))
				catch {
					case ex: InvalidUsageException =>
						genericReply(ex.getMessage)
//						ex.getStackTrace.map(_.toString).foreach(genericReply)
						printUsage
				}
			}
		}
	}

	case class CommandEvent[E <: IRCUserEvent with Replyable](event: E, cmd: String, args: Arguments) extends TCommandEvent {
		override def bot = event.bot
		override def ircUser = event.ircUser
		override def reply(msg: String) { event.reply(msg) }
		override def genericReply(msg: String) { event.genericReply(msg) }
		override def privateReply(msg: String) { event.privateReply(msg) }
		override def context = event.context
	}
}