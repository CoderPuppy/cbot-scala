package cpup.cbot.plugin

import play.api.libs.json.Format
import java.io.File
import cpup.cbot.CBot

trait PluginType[PL <: Plugin] {
	def name: String
	def format(bot: CBot, pluginManagement: PluginManagementPlugin, pluginTypes: Map[String, PluginType[Plugin]], file: File): Option[Format[PL]]
}

object PluginType {
	def pluginTypes(types: PluginType[_ <: Plugin]*) = {
		types.map((pluginType) => {
			(pluginType.name, pluginType.asInstanceOf[PluginType[Plugin]])
		}).toMap
	}
}