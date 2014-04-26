package cpup.cbot.plugin

import play.api.libs.json._
import scala.Some
import java.io.File
import cpup.cbot.CBot

trait SingletonPlugin extends Plugin with PluginType[SingletonPlugin] {
	def pluginType = this

	override def format(bot: CBot, pluginManagement: PluginManagementPlugin, pluginTypes: Map[String, PluginType[Plugin]], file: File) = Some(Format(
		new Reads[SingletonPlugin] {
			override def reads(json: JsValue) = JsSuccess(pluginType)
		},
		new Writes[SingletonPlugin] {
			override def writes(pl: SingletonPlugin) = JsNull
		}
	))
}