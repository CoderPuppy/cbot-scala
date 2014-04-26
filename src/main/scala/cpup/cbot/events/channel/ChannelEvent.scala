package cpup.cbot.events.channel

import cpup.cbot.channels.Channel
import cpup.cbot.events.Event
import cpup.cbot.Context

trait ChannelEvent extends Event {
	def channel: Channel
	override def context: Context = channel
}