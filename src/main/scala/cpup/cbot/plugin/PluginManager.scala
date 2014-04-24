package cpup.cbot.plugin

import com.google.common.eventbus.EventBus

trait PluginManager {
	val bus = new EventBus

	protected var _plugins = Set[Plugin]()
	def plugins = _plugins
	
	def enablePlugin(plugin: Plugin) = {
		if(!_plugins.contains(plugin)) {
			_plugins += plugin
			plugin.enable(this)
		}
		this
	}

	def disablePlugin(plugin: Plugin) = {
		if(_plugins.contains(plugin)) {
			_plugins -= plugin
			plugin.disable(this)
		}
		this
	}
}