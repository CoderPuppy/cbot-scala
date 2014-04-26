package cpup.cbot.plugin

import scala.util.Random
import cpup.cbot.Context


trait Plugin {
	def pluginType: PluginType[_ <: Plugin]
	var id = Array.fill[Char](6)(0.toChar).map((char) => {
		val int = Random.nextInt(16)
		(int + (if(int <= 9) { 48 } else { 65 - 10 })).toChar
	}).mkString("")

	override def toString = s"${pluginType.name}@$id"

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