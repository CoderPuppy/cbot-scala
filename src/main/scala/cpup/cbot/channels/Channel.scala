package cpup.cbot.channels

import cpup.cbot.CBot
import cpup.cbot.plugin.{AlreadyRegisteredPluginException, Plugin}

class Channel(val bot: CBot, val name: String, var key: String) {
	def this(bot: CBot, name: String) {
		this(bot, name, null)
	}

	var rejoin = false
	def setRejoin(newVal: Boolean) = {
		rejoin = newVal
		this
	}

	var plugins = List[Plugin]()
	def plugin(plugin: Plugin) {
		if(plugin.channel != null) {
			throw new AlreadyRegisteredPluginException(plugin.toString)
		}

		plugins ::= plugin
		plugin.init(this)
	}
}

object Channel {
	def unifyName(name: String) = if(name.charAt(0) == '#') {
		name.substring(1)
	} else { name }
}