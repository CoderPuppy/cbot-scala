package cpup.cbot.plugin


trait Plugin {
	protected var _managers = Set[PluginManager]()
	def managers = _managers

	def enable(manager: PluginManager) {
		_managers += manager
		manager.bus.register(this)
	}

	def disable(manager: PluginManager) {
		_managers -= manager
		manager.bus.unregister(this)
	}
}