package cpup.cbot.users

import cpup.cbot.CBot
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events.WhoisEvent

class IRCUser(val bot: CBot, var nick: String, var nickserv: String) {
	def ircUsers = List(this)

	var realName: String = null
	var username: String = null

	val guestUser = new GuestUser(this)
	protected var _user: User = null

	def user = _user
	def user_=(newVal: User) = {
		if(_user != null) {
			_user.ircUsers -= this
		}

		if(newVal == null) {
			_user = guestUser
		} else {
			_user = newVal
		}

		_user.ircUsers += this

		newVal
	}

	user = guestUser

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

	def logout {
		user = guestUser
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
		bot.users.registerNickServ(nickserv, user)
	}

	def unregisterNickServ {
		updateWhoIs
		bot.users.unregisterNickServ(nickserv)
	}

	val send = IRCUserOutput(this)
}

case class IRCUserOutput(user: IRCUser) {
	def msg(msg: String) {
		user.bot.pBot.sendIRC.message(user.nick, msg)
	}
}