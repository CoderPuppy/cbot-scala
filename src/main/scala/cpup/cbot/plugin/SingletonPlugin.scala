package cpup.cbot.plugin

import play.api.libs.json._
import scala.Some
import java.io.File
import cpup.cbot.{Context, CBot}

trait SingletonPlugin extends Plugin with PluginType[SingletonPlugin] {
	override def pluginType = this
	override def create(context: Context, pluginTypes: Map[String, PluginType[Plugin]]) = this

	override def reads(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]], file: File) = Some(new Reads[SingletonPlugin] {
		override def reads(json: JsValue) = JsSuccess(pluginType)
	})
	override def writes(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]]) = Some(new Writes[SingletonPlugin] {
		override def writes(pl: SingletonPlugin) = JsNull
	})
}