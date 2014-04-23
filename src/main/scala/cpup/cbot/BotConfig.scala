package cpup.cbot

import org.pircbotx.{PircBotX, Configuration}

case class BotConfig(realName: String,
                     username: String,
                     server: String,
                     port: Int,
                     autoReconnect: Boolean) {
	def isValid = realName != null &&
	              username != null &&
	              server != null

	def pConfig(pConfig: Configuration.Builder[PircBotX]) {
		pConfig.setServer(server, port)
		pConfig.setName(username)
		pConfig.setRealName(realName)
	}
}

object BotConfig {
	class Builder {
		var realName: String = null
		var username: String = null
		var server: String = null
		var port: Int = 6667
		var autoReconnect = true

		def setRealName(newVal: String) = {
			realName = newVal
			this
		}

		def setUsername(newVal: String) = {
			username = newVal
			this
		}

		def setServer(newVal: String) = {
			server = newVal
			this
		}

		def setPort(newVal: Int) = {
			port = newVal
			this
		}

		def setAutoReconnect(newVal: Boolean) = {
			autoReconnect = newVal
			this
		}

		def this(_server: String, _port: Int) {
			this()
			server = _server
			port = _port
		}

		def this(_server: String) {
			this()
			server = _server
		}

		def this(_server: String, _port: Int, name: String) {
			this()
			server = _server
			port = _port
			realName = name
			username = name
		}

		def this(_server: String, name: String) {
			this()
			server = _server
			realName = name
			username = name
		}

		def finish = BotConfig(realName, username, server, port, autoReconnect)
	}
}