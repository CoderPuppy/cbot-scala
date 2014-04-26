package cpup.cbot.plugin

import cpup.cbot.events.{Replyable, MessageEvent, Event}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.CommandPlugin.ChannelCommandEvent
import cpup.cbot.events.user.IRCUserEvent

case class CommandPlugin(commandSymbol: String) extends Plugin {
	@Subscribe
	def commandMessage(e: MessageEvent) {
		e match {
			case ev: IRCUserEvent with Replyable if e.msg.startsWith(commandSymbol) =>
				val parts = e.msg.substring(commandSymbol.length).split(' ')
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

object CommandPlugin {
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