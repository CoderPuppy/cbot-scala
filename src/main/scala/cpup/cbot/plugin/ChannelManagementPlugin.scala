package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.channels.Channel
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.plugin.commandPlugin._
import cpup.cbot.plugin.commandPlugin.BasicArguments
import cpup.cbot.plugin.commandPlugin.SubCommand

object ChannelManagementPlugin extends SingletonPlugin {
	def name = "channel-management"

	@Subscribe
	def channels(e: TCommandCheckEvent) {
		e.command(SubCommand("channels",
			new BasicCommand {
				override def name = "list"
				override def args = List()

				override def handle(e: TCommandEvent, args: BasicArguments) {
					e.reply(s"Current Channels: ${e.bot.channels.current.map("#" + _.name).mkString(", ")}")
				}
			},
			new BasicCommand {
				override def name = "join"

				val channelsA = ArgDef.channels
				override def args = List(channelsA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val chans = args(channelsA)
					chans.foreach((chan) => {
						if(!chan.checkPermission(e.ircUser.user, 'channels)) {
							e.reply(s"Insufficient Permissions to join $chan")
							return
						}

						e.bot.channels.join(chan)
						e.reply(s"Joining $chan")
					})
				}
			},
			new BasicCommand {
				override def name = "leave"

				val channelsA = ArgDef.channels
				override def args = List(channelsA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val chans = args(channelsA)
					chans.foreach((chan) => {
						if(!chan.checkPermission(e.ircUser.user, 'channels)) {
							e.reply(s"Insufficient Permissions to leave $chan")
							return
						}

						e.reply(s"Leaving $chan")
						e.bot.channels.leave(chan)
					})
				}
			}
		))
	}
}