package cpup.cbot.plugin

import cpup.cbot.events.{Replyable, MessageEvent, Event}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.events.user.IRCUserEvent
import play.api.libs.json._
import cpup.cbot.plugin.CommandPlugin.ChannelCommandEvent
import java.io.File
import cpup.cbot.{Context, CBot}

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
						parts.slice(1, parts.length + 1)
					)
					ev match {
						case ev: ChannelEvent =>
							e.bot.bus.post(new ChannelCommandEvent(event, ev))

						case _ =>
							e.bot.bus.post(event)
					}
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
		def command(name: String, handle: (TCommandEvent, () => Unit) => Any) {
			command(name, List(), handle)
		}

		def command(name: String, usage: String, handle: (TCommandEvent, () => Unit) => Any) {
			command(name, List(usage), handle)
		}

		def command(name: String, usages: List[String], handle: (TCommandEvent, () => Unit) => Any): Unit
	}

	trait TCommandEvent extends Event with IRCUserEvent with Replyable with TCommandCheckEvent {
		def cmd: String
		def args: Seq[String]

		override def command(name: String, usages: List[String], handle: (TCommandEvent, () => Unit) => Any) {
			if(name == cmd) {
				handle(this, () => {
					genericReply("Usage: ")
					for(usage <- usages) {
						genericReply(s" - $cmd $usage")
					}
				})
			}
		}
	}

	case class CommandEvent[MSG <: MessageEvent with IRCUserEvent with Replyable](msgEvent: MSG, cmd: String, args: Seq[String]) extends TCommandEvent {
		override def bot = msgEvent.bot
		override def ircUser = msgEvent.ircUser
		override def reply(msg: String) { msgEvent.reply(msg) }
		override def genericReply(msg: String) { msgEvent.genericReply(msg) }
		override def privateReply(msg: String) { msgEvent.privateReply(msg) }
		override def context = msgEvent.context
	}

	case class ChannelCommandEvent[MSG <: MessageEvent with IRCUserEvent with Replyable with ChannelEvent](commandEvent: CommandEvent[_ <: MessageEvent with IRCUserEvent with Replyable], msgEvent: MSG) extends TCommandEvent with ChannelEvent {
		override def bot = commandEvent.bot
		override def ircUser = commandEvent.ircUser
		override def reply(msg: String) { commandEvent.reply(msg) }
		override def genericReply(msg: String) { commandEvent.genericReply(msg) }
		override def privateReply(msg: String) { commandEvent.privateReply(msg) }
		override def channel = msgEvent.channel
		override def cmd = commandEvent.cmd
		override def args = commandEvent.args
		override def context = commandEvent.context
	}
}