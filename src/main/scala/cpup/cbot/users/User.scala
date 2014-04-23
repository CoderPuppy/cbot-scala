package cpup.cbot.users

import cpup.cbot.CBot

class User(val bot: CBot, var username: String, var password: String) {
	var ircUsers = Set[IRCUser]()
}

object User {
	def newGuest(ircUser: IRCUser) = new User(ircUser.bot, "guest_" + ircUser.nick, null)
	def hash(password: String) = password
}

class UnknownUserException(val username: String) extends Exception("Unknown user: " + username)
class IncorrectPasswordException(val user: User, val password: String) extends Exception("Incorrect password: \"" + password + "\" for user: " + user.username)