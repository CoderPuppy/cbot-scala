package cpup.cbot.users

import cpup.cbot.CBot

class IRCUser(val bot: CBot, var nick: String, var nickserv: String) {
	var realName: String = null
	var username: String = null
	var user: User = User.newGuest(this)

	def this(bot: CBot, nick: String) { this(bot, nick, null) }

	def login(username: String, password: String) = {
		val user = bot.users(username)

		if(user == null) {
			throw new UnknownUserException(username)
		}

		if(User.hash(password) != user.password) {
			throw new IncorrectPasswordException(user, password)
		}

		this
	}

	val send = IRCUserSend(this)
}

case class IRCUserSend(user: IRCUser) {
	def msg(msg: String) {
		user.bot.pBot.sendIRC.message(user.nick, msg)
	}
}