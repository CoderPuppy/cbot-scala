package cpup.cbot.channels

import scala.collection.mutable
import cpup.cbot.CBot
import cpup.cbot.events.ConnectedEvent
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.channel.{LeaveEvent, JoinEvent, KickEvent}
import org.pircbotx
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX

class ChannelManager(val bot: CBot) {
	bot.bus.register(this)

	protected val channels = new mutable.HashMap[String, (Channel, pircbotx.Channel)]
	def current = channels.values.map(_._1)

	def join(_name: String, key: String = null) = {
		val name = Channel.unifyName(_name)
		val chan = channels.getOrElseUpdate(name, {
			val channel = get(_name, key)
			bot.bus.post(new JoinEvent(bot, channel))
			(channel, null)
		})
		if(bot.isConnected) {
			joinChannel(chan._1)
		}
		chan._1
	}
	def join(chan: Channel): Channel = join(chan.name)

	def leave(_name: String, reason: String = "Leave") = {
		val name = Channel.unifyName(_name)
		if(channels.contains(name)) {
			val chan = channels.remove(name).get
			if(bot.isConnected) {
				chan._2.send.part(reason)
			}
			bot.bus.post(new LeaveEvent(bot, chan._1))

		}
		this
	}
	def leave(chan: Channel): ChannelManager = leave(chan.name)

	protected val channelCache = new mutable.WeakHashMap[String, Channel]

	def get(_name: String, key: String = null) = {
		val name = Channel.unifyName(_name)
		channelCache.getOrElseUpdate(name, new Channel(bot, name, key))
	}
	def apply(name: String) = get(name)

	protected def joinChannel(channel: Channel) {
		if(channel.key == null) {
			bot.pBot.sendIRC.joinChannel(s"#${channel.name}")
		} else {
			bot.pBot.sendIRC.joinChannel(s"#${channel.name}", channel.key)
		}
	}

	def joinChannels {
		for((name, channel) <- channels) {
			joinChannel(channel._1)
		}
	}

	@Subscribe
	def connected(e: ConnectedEvent) {
		joinChannels
	}

	@Subscribe
	def onJoin(e: events.JoinEvent[PircBotX]) {
		val name = Channel.unifyName(e.getChannel.getName)
		channels.get(name) match {
			case Some((chan, null)) =>
				channels(name) = (chan, e.getChannel)

			case _ =>
		}
	}

//	@Subscribe
//	def onLeave(e: events.PartEvent[PircBotX]) {
//		channels.remove(Channel.unifyName(e.getChannel.getName))
//	}

	@Subscribe
	def kicked(e: KickEvent) {
		if(e.kicked == bot.ircUser) {
			if(e.channel.rejoin) {
				bot.pBot.sendIRC.joinChannel(s"#${e.channel.name}")
			} else {
				channels.remove(e.channel.name)
				bot.bus.post(new LeaveEvent(bot, e.channel))
			}
		}
	}
}