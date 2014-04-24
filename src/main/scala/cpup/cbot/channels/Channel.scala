package cpup.cbot.channels

import cpup.cbot.CBot
import cpup.cbot.plugin.PluginManager

case class Channel(bot: CBot, name: String, key: String) extends PluginManager {
	def this(bot: CBot, name: String) {
		this(bot, name, null)
	}

	var rejoin = false
	def setRejoin(newVal: Boolean) = {
		rejoin = newVal
		this
	}

	val send = new ChannelSend(this)
}

object Channel {
	def unifyName(name: String) = if(name.charAt(0) == '#') {
		name.substring(1)
	} else { name }
}

case class ChannelSend(chan: Channel) {
	def msg(msg: String) = {
		chan.bot.pBot.sendIRC.message(s"#${chan.name}", msg)
		this
	}
}