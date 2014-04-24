package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

class PluginManagementPlugin(protected var _plugins: Map[String, Plugin]) extends Plugin {
	protected var reversePlugins = plugins.map(_.swap)

	def plugins = _plugins

	def registerPlugin(name: String, plugin: Plugin) = {
		if(_plugins.contains(name)) {

		} else {
			_plugins += name -> plugin
			reversePlugins += plugin -> name
		}
		this
	}

	def convertToName(pl: Plugin) = reversePlugins.getOrElse(pl, s"${pl.getClass.getName}@${pl.hashCode}")

	@Subscribe
	def plugins(e: TCommandCheckEvent) {
		e.command(
			name = "plugins",
			usages = List(
				"list",
				"enable <plugin>",
				"disable <plugin>"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "list" => {
							val enabledPlugins = e.pluginManager.plugins.map(convertToName)
							e.reply(s"Enabled Plugins: ${enabledPlugins.mkString(", ")}")
							e.reply(s"Available Plugins: ${(plugins.keySet -- enabledPlugins).mkString(", ")}")
						}

						case "enable" => {
							if(e.args.length < 2) {
								printUsage()
							} else {
								for(arg <- e.args.view(1, e.args.length)) {
									println(s"'$arg'")
									plugins.get(arg) match {
										case Some(plugin) =>
											e.genericReply(s"Enabling plugin: $arg")
											e.pluginManager.enablePlugin(plugin)

										case None =>
											e.reply(s"Unknown plugin: $arg")
									}
								}
							}
						}

						case "disable" => {
							if(e.args.length < 2) {
								printUsage()
							} else {
								val enabledPlugins = e.pluginManager.plugins.map((pl) => {
									(convertToName(pl), pl)
								}).toMap

								for(arg <- e.args.view(1, e.args.length)) {
									enabledPlugins.get(arg) match {
										case Some(plugin) =>
											e.genericReply(s"Disabling plugin: $arg")
											e.pluginManager.disablePlugin(plugin)

										case None =>
											e.reply(s"Unknown plugin: $arg")
									}
								}
							}
						}

						case _ =>
							printUsage()
					}
				}

				()
			}
		)
	}
}