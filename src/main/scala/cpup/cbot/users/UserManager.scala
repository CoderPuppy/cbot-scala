package cpup.cbot.users

import cpup.cbot.CBot
import scala.collection.mutable
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.NickChangeEvent

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
		user
	}

	@Subscribe
	def onNickChange(e: NickChangeEvent) {
		e.ircUser.nick = e.newNick
		onlineUsers.remove(e.oldNick)
		onlineUsers(e.newNick) = e.ircUser
	}
}

class AlreadyRegisteredException(msg: String) extends Exception(msg)