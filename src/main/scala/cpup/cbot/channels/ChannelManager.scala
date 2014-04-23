package cpup.cbot.channels

import scala.collection.mutable
import cpup.cbot.CBot
import cpup.cbot.events.ConnectedEvent
import com.google.common.eventbus.Subscribe

class ChannelManager(val bot: CBot) {
	bot.bus.register(this)

	val channels = new mutable.HashMap[String, Channel]

	def add(_name: String) = {
		val name = Channel.unifyName(_name)
		channels.getOrElseUpdate(name, new Channel(bot, name))
	}

	val channelCache = new mutable.WeakHashMap[String, Channel]

	def get(_name: String) = {
		val name = Channel.unifyName(_name)
		channelCache.getOrElseUpdate(name, new Channel(bot, name))
	}
	def apply(name: String) = get(name)

	@Subscribe
	def connected(e: ConnectedEvent) {
		for((name, channel) <- channels) {
			if(channel.key == null) {
				bot.pBot.sendIRC.joinChannel("#" + channel.name)
			} else {
				bot.pBot.sendIRC.joinChannel("#" + channel.name, channel.key)
			}
		}
	}
}