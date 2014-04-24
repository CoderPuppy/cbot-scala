package cpup.cbot.events.channel

import cpup.cbot.channels.Channel
import cpup.cbot.events.Event
import cpup.cbot.plugin.PluginManager

trait ChannelEvent extends Event {
	def channel: Channel
	def pluginManager: PluginManager = channel
}