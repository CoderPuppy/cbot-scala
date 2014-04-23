package cpup.cbot.events

import cpup.cbot.channels.Channel

trait ChannelEvent extends Event {
	def channel: Channel
}