package cpup.cbot

import org.pircbotx.{Configuration, PircBotX, hooks}
import cpup.cbot.channels.ChannelManager
import org.pircbotx.hooks.{WaitForQueue, Listener}
import com.google.common.eventbus.{Subscribe, EventBus}
import cpup.cbot.events.{ConnectedEvent, EventWrapper}
import cpup.cbot.users.{User, IRCUser, UserManager}
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.PluginManager

class CBot(val config: BotConfig) extends Listener[PircBotX] with Context {
	def this(config: BotConfig.Builder) {
		this(config.finish)
	}

	override def toString = s"CBot(${config.server}, ${user.nick})"

	bus.register(this)

	val pConfig = new Configuration.Builder[PircBotX]
	config.pConfig(pConfig)
	pConfig.addListener(this)

	val pBot = new PircBotX(pConfig.buildConfiguration)
	val pEventQueue = new WaitForQueue(pBot)

	val channels = new ChannelManager(this)
	val users = new UserManager(this)

	protected var _user: IRCUser = null
	def user = _user

	def connect {
		pBot.startBot
	}

	override def getPermissions(user: User) = user.permissions.toSet
	override def grantPermission(user: User, permission: Symbol) = {
		user.permissions += permission
		this
	}
	override def takePermission(user: User, permission: Symbol) = {
		user.permissions -= permission
		this
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

	def isConnected = pBot.isConnected

	@Subscribe
	def connected(e: ConnectedEvent) {
		_user = users.fromNick(pBot.getNick)
	}
}