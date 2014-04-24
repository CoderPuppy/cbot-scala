package cpup.cbot

import org.pircbotx.{Configuration, PircBotX, hooks}
import cpup.cbot.channels.ChannelManager
import org.pircbotx.hooks.Listener
import com.google.common.eventbus.{Subscribe, EventBus}
import cpup.cbot.events.{ConnectedEvent, EventWrapper}
import cpup.cbot.users.{IRCUser, UserManager}
import cpup.cbot.events.channel.ChannelEvent
import cpup.cbot.plugin.PluginManager

class CBot(val config: BotConfig) extends Listener[PircBotX] with PluginManager {
	def this(config: BotConfig.Builder) {
		this(config.finish)
	}

	bus.register(this)

	val pConfig = new Configuration.Builder[PircBotX]
	config.pConfig(pConfig)
	pConfig.addListener(this)

	val pBot = new PircBotX(pConfig.buildConfiguration)

	val channels = new ChannelManager(this)
	val users = new UserManager(this)

	protected var _user: IRCUser = null
	def user = _user

	def connect {
		pBot.startBot
	}

	def onEvent(pEvent: hooks.Event[PircBotX]) {
		println(pEvent)
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
	def connected(e: ConnectedEvent) {
		_user = users.fromNick(pBot.getNick)
	}
}