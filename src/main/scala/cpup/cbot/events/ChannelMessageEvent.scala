package cpup.cbot.events

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import cpup.cbot.channels.Channel
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX

class ChannelMessageEvent(val bot: CBot, val channel: Channel, val user: IRCUser, val message: String) extends Event with ChannelEvent with UserEvent with MessageEvent {
	def this(bot: CBot, e: events.MessageEvent[PircBotX]) {
		this(bot, bot.channels(e.getChannel.getName), bot.users.fromNick(e.getUser.getNick), e.getMessage)
	}
}