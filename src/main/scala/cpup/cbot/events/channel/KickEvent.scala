package cpup.cbot.events.channel

import cpup.cbot.CBot
import cpup.cbot.events.{MessageEvent, UserEvent, Event}
import cpup.cbot.users.IRCUser
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX
import cpup.cbot.channels.Channel

case class KickEvent(val bot: CBot, val channel: Channel, val user: IRCUser, val kicked: IRCUser, val reason: String) extends Event with ChannelEvent with UserEvent with MessageEvent {
	def msg = reason
	def this(bot: CBot, e: events.KickEvent[PircBotX]) {
		this(
			bot,
			bot.channels(e.getChannel.getName),
			bot.users.fromNick(e.getUser.getNick),
			bot.users.fromNick(e.getRecipient.getNick),
			e.getReason
		)
	}
}