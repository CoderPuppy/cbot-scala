package cpup.cbot.events

import cpup.cbot.CBot
import cpup.cbot.plugin.PluginManager

trait Event {
	def bot: CBot
	def pluginManager: PluginManager
}