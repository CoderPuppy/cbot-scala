package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.users._

object UsersPlugin extends SingletonPlugin {
	def name = "users"

	@Subscribe
	def users(e: TCommandCheckEvent) {
		e.command(
			name = "users",
			usages = List(
				"whois [user]",
				"login <username> <password>",
				"logout",
				"setpass[word] <new password>",
				"register <nick> <username>",
				"unregister",
				"register-nickserv",
				"unregister-nickserv"
			),
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				if(e.args.length < 1) {
					printUsage()
				} else {
					e.args(0) match {
						case "login" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								try {
									e.ircUser.login(e.args(1), e.args(2))
									e.reply(s"Logged in as ${e.args(1)}")
								} catch {
									case ex: UnknownUserException =>
										e.reply(s"Unknown User: ${e.args(1)}")

									case ex: IncorrectPasswordException =>
										e.reply(s"Incorrect Password")
								}
							}

						case "logout" =>
							e.ircUser.logout
							e.reply("Logged out")

						case "setpass" | "setpassword" =>
							if(e.args.length < 2) {
								printUsage()
							} else {
								e.ircUser.user.password = User.hash(e.args(1))
								e.reply(s"Set password for ${e.ircUser.user.username}")
							}

						case "whois" =>
							var user = e.ircUser

							if(e.args.length >= 2) {
								user = e.bot.users.fromNick(e.args(1))
							}

							e.genericReply(s"--[===[${user.nick}]===]--")
							e.genericReply(s"Logged in as ${user.user.username}")

						case "register" =>
							if(e.args.length < 3) {
								printUsage()
							} else {
								if(!e.bot.checkPermission(e.ircUser.user, 'register)) {
									e.reply("Insufficient Permissions")
									return ()
								}

								try {
									val user = e.bot.users.fromNick(e.args(1))
									if(user.user.isInstanceOf[GuestUser]) {
										user.user = e.bot.users.register(e.args(2))
										e.genericReply(s"Created account: ${e.args(2)}")
										e.genericReply(s"${user.nick} logged into ${e.args(2)}")
									} else {
										e.reply(s"${user.nick} has already registered")
									}
								} catch {
									case ex: AlreadyRegisteredException =>
										e.reply("Username is already in use")
								}
							}

						case "unregister" =>
							e.bot.users.unregister(e.user)
							e.reply(s"Unregistered @${e.user}")

						case "register-nickserv" =>
							try {
								e.ircUser.registerNickServ
								e.reply(s"Registered NickServ account: ${e.ircUser.nickserv} to user: ${e.ircUser.user.username}")
							} catch {
								case ex: GuestUserException =>
									e.reply("Cannot register NickServ account to the user because you aren't logged in")

								case ex: AlreadyRegisteredException =>
									e.reply("Your NickServ account is already registered to a user")
							}

						case "unregister-nickserv" =>
							e.ircUser.unregisterNickServ
							e.reply(s"Unregistered NickServ account: ${e.ircUser.nickserv}")

						case _ =>
							printUsage()
					}

					()
				}
			}
		)
	}
}