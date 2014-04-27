package cpup.cbot.plugin

import scala.util.Random
import cpup.cbot.{CBot, Context}
import cpup.cbot.events.Replyable
import cpup.cbot.events.plugin.SetConfigOptionEvent


trait Plugin {
	def pluginType: PluginType[_ <: Plugin]
	var id = Array.fill[Char](6)(0.toChar).map((char) => {
		val int = Random.nextInt(16)
		(int + (if(int <= 9) { 48 } else { 65 - 10 })).toChar
	}).mkString("")

	override def toString = s"${pluginType.name}@$id"

	def configOptions: Set[ConfigOption] = Set()
	def setConfigOption(bot: CBot, reply: Replyable, key: String, value: String) = {
		configOptions.find(_.name == key) match {
			case Some(configOption) =>
				configOption.set(reply, value)
				bot.bus.post(new SetConfigOptionEvent(bot, this, reply, key, value))

			case None =>
				throw new UnknownConfigOptionException(s"Unknown Config Option: $key")
		}

		this
	}
	def getConfigOption(key: String) = {
		configOptions.find(_.name == key) match {
			case Some(configOption) =>
				configOption.get

			case None =>
				throw new UnknownConfigOptionException(s"Unknown Config Option: $key")
		}
	}

	protected var _managers = Set[Context]()
	def managers = _managers

	def enable(manager: Context) {
		_managers += manager
		manager.bus.register(this)
	}

	def disable(manager: Context) {
		_managers -= manager
		manager.bus.unregister(this)
	}
}

class UnknownConfigOptionException(msg: String) extends Exception(msg)