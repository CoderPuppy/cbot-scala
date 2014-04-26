package cpup.cbot.events.channel

import cpup.cbot.CBot
import cpup.cbot.events.{MessageEvent, Event}
import cpup.cbot.users.IRCUser
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX
import cpup.cbot.channels.Channel
import cpup.cbot.events.user.IRCUserEvent

case class KickEvent(val bot: CBot, val channel: Channel, val ircUser: IRCUser, val kicked: IRCUser, val reason: String) extends Event with ChannelEvent with IRCUserEvent with MessageEvent {
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