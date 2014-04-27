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
				"disable [context] <plugin>",
				"listoptions [context] <plugin>",
				"get [context] <plugin> <key>",
				"set [context] <plugin> <key> <value>"
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
							e.reply(s"Enabled Plugins in $context: ${enabledPlugins.mkString(", ")}")
							e.reply(s"Available Plugins: ${availablePlugins.map(_.name).mkString(", ")}")
						}

						case "enable" => {
							if(e.args.length < 2) {
								printUsage()
							} else {
								var context = e.context
								var pluginsArg = e.args(1)

								if(e.args.length >= 3) {
									context = e.bot.getContext(e.args(1))
									pluginsArg = e.args(2)
								}

								if(!context.checkPermission(e.user, 'plugins)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								for(arg <- pluginsArg.split(",")) {
									pluginTypes.get(arg) match {
										case Some(pluginType) =>
											context.plugins.find(_.pluginType == pluginType) match {
												case Some(plugin) =>
													e.genericReply(s"${pluginType.name} is already enabled in $context")

												case None =>
													val plugin = pluginType.create(context, pluginTypes)
													e.genericReply(s"Enabling plugin: $plugin in $context")
													context.enablePlugin(plugin)
											}

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
													val matching = context.plugins.filter(_.pluginType == pluginType)
													matching.foreach(disable)

													if(matching.isEmpty) {
														e.reply(s"No plugins of type: ${pluginType.name} in $context")
													}

												case None =>
													e.reply(s"Unknown plugin: $arg")
											}
									}
								}
							}
						}

						case "listoptions" =>
							val (context, pluginName) = if(e.args.length >= 3) {
								(
									e.bot.getContext(e.args(1)),
									e.args(2)
								)
							} else {
								(
									e.context,
									e.args(1)
								)
							}

							(context.plugins.find(_.toString == pluginName) match {
								case None => context.plugins.find(_.pluginType.name == pluginName)
								case Some(plugin) => Some(plugin)
							}) match {
								case Some(plugin) =>
									e.genericReply(s" -- Options for $plugin")
									plugin.configOptions.foreach((configOption) => {
										e.genericReply(s"  - ${configOption.name} :: ${configOption.usage} = ${configOption.get}")
									})

								case None =>
									e.reply(s"Unknown Plugin: $pluginName")
							}

						case "get" =>
							if(e.args.length < 3) {
								printUsage()
							} else {
								val (context, pluginName, key) = if(e.args.length >= 4) {
									(
										e.bot.getContext(e.args(1)),
										e.args(2),
										e.args(3)
									)
								} else {
									(
										e.context,
										e.args(1),
										e.args(2)
									)
								}

								(context.plugins.find(_.toString == pluginName) match {
									case None => context.plugins.find(_.pluginType.name == pluginName)
									case Some(plugin) => Some(plugin)
								}) match {
									case Some(plugin) =>
										try {
											e.genericReply(s"$plugin $key=${plugin.getConfigOption(key)}")
										} catch {
											case ex: UnknownConfigOptionException =>
												e.reply(s"Unknown Option: $key for $plugin")
										}

									case None =>
										e.reply(s"Unknown Plugin: $pluginName")
								}
							}

						case "set" =>
							if(e.args.length < 4) {
								printUsage()
							} else {
								val (context, pluginName, key, value) = if(e.args.length >= 5) {
									(
										e.bot.getContext(e.args(1)),
										e.args(2),
										e.args(3),
										e.args(4)
									)
								} else {
									(
										e.context,
										e.args(1),
										e.args(2),
										e.args(3)
									)
								}

								(context.plugins.find(_.toString == pluginName) match {
									case None => context.plugins.find(_.pluginType.name == pluginName)
									case Some(plugin) => Some(plugin)
								}) match {
									case Some(plugin) =>
										if(!(
											e.context.checkPermission(e.user, 'plugins) ||
											e.context.checkPermission(e.user, 'pluginsConfig) ||
											e.context.checkPermission(e.user, Symbol(s"pluginsConfig:${plugin.pluginType.name}"))
										)) {
											e.reply("Insufficient Permissions")
											return ()
										}

										try {
											plugin.setConfigOption(e.bot, e, key, value)
											e.genericReply(s"Set $plugin $key to $value")
										} catch {
											case ex: UnknownConfigOptionException =>
												e.reply(s"Unknown Option: $key for $plugin")
										}

									case None =>
										e.reply(s"Unknown Plugin: $pluginName")
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