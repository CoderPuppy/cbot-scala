package cpup.cbot.plugin

import com.google.common.eventbus.EventBus
import cpup.cbot.CBot
import cpup.cbot.events.plugin.{EnablePluginEvent, DisablePluginEvent}

trait PluginManager {
	def bot: CBot

	val bus = new EventBus

	protected var _plugins = Set[Plugin]()
	def plugins = _plugins
	
	def enablePlugin(plugin: Plugin) = {
		if(!_plugins.contains(plugin)) {
			_plugins += plugin
			plugin.enable(this)
			bot.bus.post(new EnablePluginEvent(bot, this, plugin))
		}
		this
	}

	def disablePlugin(plugin: Plugin) = {
		if(_plugins.contains(plugin)) {
			_plugins -= plugin
			plugin.disable(this)
			bot.bus.post(new DisablePluginEvent(bot, this, plugin))
		}
		this
	}
}