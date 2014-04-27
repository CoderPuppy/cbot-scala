package cpup.cbot.events.channel

import cpup.cbot.{Context, CBot}
import cpup.cbot.channels.Channel
import cpup.cbot.events.Event

case class JoinEvent(bot: CBot, channel: Channel) extends Event {
	def context: Context = channel
}