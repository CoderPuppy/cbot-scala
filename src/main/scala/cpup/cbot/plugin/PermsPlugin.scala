package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}

class PermsPlugin extends Plugin {
	@Subscribe
	def perms(e: TCommandCheckEvent) {
		e.command(
			name = "perms",
			usages = List(
				"list [user]",
				"grant [context] [user] <permissions>",
				"take [context] [user] <permissions>"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "list" =>
							var context = e.context
							var user = e.ircUser.user

							for(arg <- e.args.view(1, e.args.length)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							e.reply(s"${user.username}'s permissions in $context: ${context.getPermissions(user).mkString(", ")}")

						case "grant" =>
							var context = e.context
							var user = e.ircUser.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
							val sharePermissions = context.checkPermission(e.ircUser.user, 'sharePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && sharePermissions))) {
									e.reply("Insufficient Permissions")

									return ()
								}

								context.grantPermission(user, perm)
								e.genericReply(s"Granted $perm to ${user.username} in $context")
							}

						case "take" =>
							var context = e.context
							var user = e.ircUser.user
							val permissions = e.args(e.args.length - 1)

							for(arg <- e.args.view(1, e.args.length - 1)) {
								val argContext = e.bot.getContext(arg)
								if(argContext != null) {
									context = argContext
								} else {
									user = e.bot.users.fromNick(arg).user
								}
							}

							val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
							val takeSamePerms = context.checkPermission(e.ircUser.user, 'takePermissions)

							for(permStr <- permissions.split(',')) {
								val perm = Symbol(permStr)

								if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && takeSamePerms))) {
									e.reply("Insufficient Permissions")

									return ()
								}

								context.takePermission(user, perm)
								e.genericReply(s"Took $perm from ${user.username} in $context")
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