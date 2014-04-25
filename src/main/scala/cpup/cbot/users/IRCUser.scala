package cpup.cbot.users

import cpup.cbot.CBot
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.WhoisEvent

class IRCUser(val bot: CBot, var nick: String, var nickserv: String) {
	def ircUsers = List(this)

	var realName: String = null
	var username: String = null

	var user: User = new GuestUser(this)

	def this(bot: CBot, nick: String) { this(bot, nick, null) }

	def login(username: String, password: String) = {
		if(this.user != null && this.user.password != null) {
			// TODO: logging out
		}

		val user = bot.users(username)

		if(user == null) {
			throw new UnknownUserException(username)
		}

		if(User.hash(password) != user.password) {
			throw new IncorrectPasswordException(user, password)
		}

		this.user = user
		// TODO: transfer data?

		this
	}

	def updateWhoIs {
		bot.pBot.sendRaw.rawLine(s"WHOIS $nick")
		val event = bot.pEventQueue.waitFor(classOf[WhoisEvent[PircBotX]])
		nickserv = event.getRegisteredAs
		realName = event.getRealname
		username = event.getLogin

		if(bot.users.nickServUsers.contains(nickserv)) {
			user = bot.users.fromNickServ(nickserv)
		}
	}

	updateWhoIs

	def registerNickServ {
		updateWhoIs
		if(user.isInstanceOf[GuestUser]) {
			throw new GuestUserException("Cannot register nickserv registration with this user")
		}

		if(bot.users.nickServUsers.contains(nickserv)) {
			throw new AlreadyRegisteredException("NickServ account is already registered to an account")
		}

		bot.users.nickServUsers(nickserv) = user
	}

	val send = IRCUserSend(this)
}

case class IRCUserSend(user: IRCUser) {
	def msg(msg: String) {
		user.bot.pBot.sendIRC.message(user.nick, msg)
	}
}