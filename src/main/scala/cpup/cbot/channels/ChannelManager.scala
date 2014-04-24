package cpup.cbot.channels

import scala.collection.mutable
import cpup.cbot.CBot
import cpup.cbot.events.ConnectedEvent
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.KickEvent

class ChannelManager(val bot: CBot) {
	bot.bus.register(this)

	val channels = new mutable.HashMap[String, Channel]

	def join(_name: String) = {
		val name = Channel.unifyName(_name)
		channels.getOrElseUpdate(name, get(_name))
	}
	def join(chan: Channel): Channel = join(chan.name)

	val channelCache = new mutable.WeakHashMap[String, Channel]

	def get(_name: String) = {
		val name = Channel.unifyName(_name)
		channelCache.getOrElseUpdate(name, new Channel(bot, name))
	}
	def apply(name: String) = get(name)

	def joinChannels {
		for((name, channel) <- channels) {
			if(channel.key == null) {
				bot.pBot.sendIRC.joinChannel(s"#${channel.name}")
			} else {
				bot.pBot.sendIRC.joinChannel(s"#${channel.name}", channel.key)
			}
		}
	}

	@Subscribe
	def connected(e: ConnectedEvent) {
		joinChannels
	}

	@Subscribe
	def kicked(e: KickEvent) {
		if(e.kicked == bot.user && channels(e.channel.name) == e.channel) {
			if(e.channel.rejoin) {
				bot.pBot.sendIRC.joinChannel(s"#${e.channel.name}")
			} else {
				channels(e.channel.name) = null
			}
		}
	}
}