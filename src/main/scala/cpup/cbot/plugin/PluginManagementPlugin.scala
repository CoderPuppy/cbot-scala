package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.Context

class PluginManagementPlugin(val pluginTypes: Map[String, PluginType[Plugin]]) extends Plugin {
	def pluginType = PluginManagementPlugin

	val reversePluginTypes = pluginTypes.map(_.swap)

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

							val enabledPlugins = context.plugins
							val availablePlugins = (pluginTypes.values.toSet -- enabledPlugins.map(_.pluginType)).toSet
							e.reply(s"Enabled Plugins: ${enabledPlugins.mkString(", ")}")
							e.reply(s"Available Plugins: ${availablePlugins.map(_.name).mkString(", ")}")
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
									pluginTypes.get(arg) match {
										case Some(pluginType) =>
											e.genericReply(s"Enabling plugin: $arg in $context")
											context.enablePlugin(pluginType.create(context, pluginTypes))

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
									(pl.toString, pl)
								}).toMap

								def disable(plugin: Plugin) {
									e.genericReply(s"Disabling plugin: $plugin in $context")
									context.disablePlugin(plugin)
								}

								for(arg <- pluginsArg.split(",")) {
									enabledPlugins.get(arg) match {
										case Some(plugin) =>
											disable(plugin)

										case None =>
											pluginTypes.get(arg) match {
												case Some(pluginType) =>
													context.plugins.filter(_.pluginType == pluginType).foreach(disable)

												case None =>
													e.reply(s"Unknown plugin: $arg")
											}
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

object PluginManagementPlugin extends TransientPluginType {
	def name = "plugin-management"
	def create(context: Context, pluginTypes: Map[String, PluginType[Plugin]]) = new PluginManagementPlugin(pluginTypes)
}