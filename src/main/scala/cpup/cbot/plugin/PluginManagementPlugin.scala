package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.Context
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.plugin.commandPlugin.{SubCommand, BasicCommand, BasicArguments, ArgDef}

class PluginManagementPlugin(val pluginTypes: Map[String, PluginType[Plugin]]) extends Plugin {
	def pluginType = PluginManagementPlugin

	val reversePluginTypes = pluginTypes.map(_.swap)

	@Subscribe
	def plugins(e: TCommandCheckEvent) {
		e.command(SubCommand("plugins",
			new BasicCommand {
				override def name = "list"

				val contextA = ArgDef.context
				override def args = List(contextA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val enabledPlugins = context.plugins
					val availablePlugins = (pluginTypes.values.toSet -- enabledPlugins.map(_.pluginType)).toSet
					e.reply(s"Enabled Plugins in $context: ${enabledPlugins.mkString(", ")}")
					e.reply(s"Available Plugins: ${availablePlugins.map(_.name).mkString(", ")}")
				}
			},
			new BasicCommand {
				override def name = "enable"

				val contextA = ArgDef.context
				val pluginTypesA = ArgDef.commas(ArgDef.pluginType(pluginTypes)).required
				override def args = List(contextA, pluginTypesA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val enablePluginTypes = args(pluginTypesA)
					for(pluginType <- enablePluginTypes) {
						context.plugins.find(_.pluginType == pluginType) match {
							case Some(plugin) =>
								e.reply(s"${pluginType.name} is already enabled in $context")

							case None =>
								val plugin = pluginType.create(context, pluginTypes)
								e.genericReply(s"Enabling $plugin in $context")
								context.enablePlugin(plugin)
						}
					}
				}
			},
			new BasicCommand {
				override def name = "disable"

				val contextA = ArgDef.context
				val pluginA = ArgDef.str.withName("plugins")
				override def args = List(contextA, pluginA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val pluginsArg = args(pluginA)

					if(!context.checkPermission(e.ircUser.user, 'plugins)) {
						e.reply("Insufficient Permissions")
						return
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
			},
			new BasicCommand {
				override def name = "options"

				val contextA = ArgDef.context
				val pluginA = ArgDef.str.withName("plugin")
				override def args = List(contextA, pluginA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val pluginName = args(pluginA)

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
				}
			},
			new BasicCommand {
				override def name = "get"

				val contextA = ArgDef.context
				val pluginA = ArgDef.str.withName("plugin")
				val keyA = ArgDef.str.withName("key")
				override def args = List(contextA, pluginA, keyA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val pluginName = args(pluginA)
					val key = args(keyA)

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
			},
			new BasicCommand {
				override def name = "set"

				val contextA = ArgDef.context
				val pluginA = ArgDef.str.withName("plugin")
				val keyA = ArgDef.str.withName("key")
				val valueA = ArgDef.str.withName("value")
				override def args = List(contextA, pluginA, keyA, valueA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val pluginName = args(pluginA)
					val key = args(keyA)
					val value = args(valueA)

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
			}
		))
	}
}

object PluginManagementPlugin extends TransientPluginType {
	def name = "plugin-management"
	def create(context: Context, pluginTypes: Map[String, PluginType[Plugin]]) = new PluginManagementPlugin(pluginTypes)
}