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
				"list [context]",
				"enable [context] <plugin>",
				"disable [context] <plugin>"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "list" => {
							var context = e.context

							if(e.args.length >= 2) {
								val argContext = e.bot.getContext(e.args(1))
								if(argContext != null) {
									context = argContext
								} else {
									e.reply(s"Unknown context: ${e.args(1)}")
									return ()
								}
							}

							val enabledPlugins = context.plugins.map(convertToName)
							e.reply(s"Enabled Plugins: ${enabledPlugins.mkString(", ")}")
							e.reply(s"Available Plugins: ${(plugins.keySet -- enabledPlugins).mkString(", ")}")
						}

						case "enable" => {
							if(e.args.length < 2) {
								printUsage()
							} else {
								var context = e.context
								var pluginsArg = e.args(1)

								if(e.args.length >= 3) {
									if(e.args(1) == "@") {
										context = e.bot
									} else {
										context = e.bot.channels(e.args(1))
									}
									pluginsArg = e.args(2)
								}

								if(!context.checkPermission(e.ircUser.user, 'plugins)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								for(arg <- pluginsArg.split(",")) {
									plugins.get(arg) match {
										case Some(plugin) =>
											e.genericReply(s"Enabling plugin: $arg in $context")
											context.enablePlugin(plugin)

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
								var context = e.context
								var pluginsArg = e.args(1)

								if(e.args.length >= 3) {
									val argContext = e.bot.getContext(e.args(1))
									if(argContext != null) {
										context = argContext
									} else {
										e.reply(s"Unknown context: ${e.args(1)}")
									}
									pluginsArg = e.args(2)
								}

								if(!context.checkPermission(e.ircUser.user, 'plugins)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								val enabledPlugins = context.plugins.map((pl) => {
									(convertToName(pl), pl)
								}).toMap

								for(arg <- pluginsArg.split(",")) {
									enabledPlugins.get(arg) match {
										case Some(plugin) =>
											e.genericReply(s"Disabling plugin: $arg in $context")
											context.disablePlugin(plugin)

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