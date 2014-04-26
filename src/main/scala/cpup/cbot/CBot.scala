package cpup.cbot

import org.pircbotx.{Configuration, PircBotX, hooks}
import cpup.cbot.channels.ChannelManager
import org.pircbotx.hooks.{WaitForQueue, Listener}
import com.google.common.eventbus.{Subscribe, EventBus}
import cpup.cbot.events.{ConnectedEvent, EventWrapper}
import cpup.cbot.users.{User, IRCUser, UserManager}
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.PluginManager
import cpup.cbot.events.user.UserEvent

class CBot(val config: BotConfig) extends Listener[PircBotX] with Context {
	def this(config: BotConfig.Builder) {
		this(config.finish)
	}

	override def bot = this

	override def toString = s"CBot(${config.server}, ${user.username})"

	bus.register(this)

	val pConfig = new Configuration.Builder[PircBotX]
	config.pConfig(pConfig)
	pConfig.addListener(this)

	val pBot = new PircBotX(pConfig.buildConfiguration)
	val pEventQueue = new WaitForQueue(pBot)

	val channels = new ChannelManager(this)
	val users = new UserManager(this)

	protected var _ircUser: IRCUser = null
	def ircUser = _ircUser
	val user = new CBotUser(this)

	def connect {
		pBot.startBot
	}

	def getContext(name: String) = if(name == "@") {
		this
	} else if(name.startsWith("@")) {
		users.fromNick(name.substring(1)).user
	} else if(name.startsWith("#")) {
		channels(name.substring(1))
	} else {
		null
	}

	override def getPermissions(user: User) = user.permissions.toSet
	override protected def _grantPermission(user: User, permission: Symbol) {
		user.permissions += permission
	}
	override protected def _takePermission(user: User, permission: Symbol) {
		user.permissions -= permission
	}

	def onEvent(pEvent: hooks.Event[PircBotX]) {
		println(pEvent)
		bus.post(pEvent)
		if(EventWrapper.canWrap(pEvent)) {
			val e = EventWrapper.wrap(this, pEvent)
			println(e)
			bus.post(e)
		}
	}

	@Subscribe
	def repostChannelEvent(e: ChannelEvent) {
		e.channel.bus.post(e)
	}

	@Subscribe
	def repostUserEvent(e: UserEvent) {
		e.user.bus.post(e)
	}

	def isConnected = pBot.isConnected

	@Subscribe
	def connected(e: ConnectedEvent) {
		_ircUser = users.fromNick(pBot.getNick)
		_ircUser.user = user
	}
}

class CBotUser(bot: CBot) extends User(bot, bot.config.username, null) {
	override def toString = bot.toString
}