package cpup.cbot.events

import cpup.cbot.CBot
import org.pircbotx.hooks.events.ConnectEvent
import org.pircbotx.PircBotX

class ConnectedEvent(val bot: CBot) extends Event {
	def this(bot: CBot, event: ConnectEvent[PircBotX]) {
		this(bot)
	}

	override def context = bot
}