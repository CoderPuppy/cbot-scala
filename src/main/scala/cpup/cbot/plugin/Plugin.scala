package cpup.cbot.plugin

import cpup.cbot.channels.Channel

trait Plugin {
	protected var _channel: Channel = null
	def channel = _channel
	def channel_=(newVal: Channel) = {
		if(_channel == null) {
			_channel = newVal
			newVal
		} else {
			throw new AlreadyRegisteredPluginException(this.toString)
		}
	}

	def init(_channel: Channel) {
		channel = _channel
	}
}

class AlreadyRegisteredPluginException(plugin: String) extends Exception(plugin + " is already registered to a channel")