package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

class ChannelManagementPlugin extends Plugin {
	@Subscribe
	def channels(e: TCommandCheckEvent) {
		e.command(
			name = "channels",
			usages = List(
				"list",
				"join <channel>",
				"leave <channel>"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "list" =>
							e.reply(s"Current Channels: ${e.bot.channels.current.map("#" + _.name).mkString(", ")}")

						case "join" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								if(!e.bot.channels(e.args(1)).checkPermission(e.user.user, 'channels)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								e.bot.channels.join(e.args(1))
							}

						case "leave" =>
							if(e.args.length < 2) {
									printUsage()
							} else {
								if(!e.bot.channels(e.args(1)).checkPermission(e.user.user, 'channels)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								e.bot.channels.leave(e.args(1))
							}

						case _ =>
							printUsage()
					}
				}
			}
		)
	}
}