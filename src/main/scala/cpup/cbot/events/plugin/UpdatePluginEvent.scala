package cpup.cbot.events.plugin

import cpup.cbot.events.Event
import cpup.cbot.plugin.Plugin

trait UpdatePluginEvent extends Event {
	def plugin: Plugin
}