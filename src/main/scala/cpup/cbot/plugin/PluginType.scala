package cpup.cbot.plugin

import play.api.libs.json.{Reads, Writes}
import java.io.File
import cpup.cbot.{Context, CBot}

trait PluginType[PL <: Plugin] {
	def name: String
	def create(context: Context, pluginTypes: Map[String, PluginType[Plugin]]): PL

	def reads(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]], file: File): Option[Reads[PL]]
	def writes(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]]): Option[Writes[PL]]
}

object PluginType {
	def pluginTypes(types: PluginType[_ <: Plugin]*) = {
		types.map((pluginType) => {
			(pluginType.name, pluginType.asInstanceOf[PluginType[Plugin]])
		}).toMap
	}
}