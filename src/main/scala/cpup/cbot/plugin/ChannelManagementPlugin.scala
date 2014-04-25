package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.channels.Channel

class ChannelManagementPlugin extends Plugin {
	@Subscribe
	def channels(e: TCommandCheckEvent) {
		e.command(
			name = "channels",
			usages = List(
				"list",
				"join <channels>",
				"leave <channels>"
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
								for(chan <- e.args(1).split(",")) {
									if(!e.bot.channels(chan).checkPermission(e.user.user, 'channels)) {
										e.reply("Insufficient Permissions")
										return ()
									}

									e.bot.channels.join(chan)
									e.reply(s"Joining #${Channel.unifyName(chan)}")
								}
							}

						case "leave" =>
							if(e.args.length < 2) {
									printUsage()
							} else {
								for(chan <- e.args(1).split(",")) {
									if(!e.bot.channels(chan).checkPermission(e.user.user, 'channels)) {
										e.reply("Insufficient Permissions")
										return ()
									}

									e.bot.channels.leave(chan)
									e.reply(s"Leaving #${Channel.unifyName(chan)}")
								}
							}

						case _ =>
							printUsage()
					}
				}
			}
		)
	}
}