package cpup.cbot.users

import cpup.cbot.CBot
import scala.collection.mutable

class UserManager(val bot: CBot) {
	val onlineUsers = new mutable.HashMap[String, IRCUser]
	val registeredUsers = new mutable.HashMap[String, User]
	val nickservUsers = new mutable.HashMap[String, User]

	def fromNick(nick: String) = onlineUsers.getOrElseUpdate(nick, new IRCUser(bot, nick))
	def fromUsername(username: String) = registeredUsers.getOrElse(username, null)

	def apply(username: String) = fromUsername(username)
}