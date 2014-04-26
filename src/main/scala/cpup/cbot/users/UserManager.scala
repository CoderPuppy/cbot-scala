package cpup.cbot.users

import cpup.cbot.CBot
import scala.collection.mutable
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.user.{RegisterNickServEvent, NickChangeEvent, RegisterEvent}

class UserManager(val bot: CBot) {
	bot.bus.register(this)

	val onlineUsers = new mutable.WeakHashMap[String, IRCUser]
	val registeredUsers = new mutable.HashMap[String, User]
	val nickServUsers = new mutable.HashMap[String, User]

	def fromNick(nick: String) = onlineUsers.getOrElseUpdate(nick, new IRCUser(bot, nick))
	def fromNickServ(nickserv: String) = nickServUsers.getOrElse(nickserv, null)
	def fromUsername(username: String) = registeredUsers.getOrElse(username, null)

	def apply(username: String) = fromUsername(username)

	def register(username: String, password: String = null) = {
		if(registeredUsers.contains(username)) {
			throw new AlreadyRegisteredException("Username is already registered")
		}

		val user = new User(
			bot,
			username,
			User.hash(password)
		)
		registeredUsers(username) = user

		bot.bus.post(new RegisterEvent(bot, user))

		user
	}

	def registerNickServ(nickserv: String, user: User) = {
		if(user.isInstanceOf[GuestUser]) {
			throw new GuestUserException("Cannot register nickserv registration with this user")
		}

		if(nickServUsers.contains(nickserv)) {
			throw new AlreadyRegisteredException("NickServ account is already registered to an account")
		}

		bot.bus.post(new RegisterNickServEvent(bot, user, nickserv))
		nickServUsers(nickserv) = user

		this
	}

	@Subscribe
	def onNickChange(e: NickChangeEvent) {
		e.ircUser.nick = e.newNick
		onlineUsers.remove(e.oldNick)
		onlineUsers(e.newNick) = e.ircUser
	}
}

class AlreadyRegisteredException(msg: String) extends Exception(msg)