package cpup.cbot

import org.pircbotx.{Configuration, PircBotX, hooks}
import cpup.cbot.channels.ChannelManager
import org.pircbotx.hooks.Listener
import com.google.common.eventbus.EventBus
import cpup.cbot.events.EventWrapper
import cpup.cbot.users.UserManager

class CBot(val config: BotConfig) extends Listener[PircBotX] {
	def this(config: BotConfig.Builder) {
		this(config.finish)
	}

	val bus = new EventBus()

	val pConfig = new Configuration.Builder[PircBotX]
	config.pConfig(pConfig)
	pConfig.addListener(this)

	val pBot = new PircBotX(pConfig.buildConfiguration)

	val channels = new ChannelManager(this)
	val users = new UserManager(this)

	def connect {
		pBot.startBot
	}

	def onEvent(pEvent: hooks.Event[PircBotX]) {
		println(pEvent)
		if(EventWrapper.canWrap(pEvent)) {
			println(EventWrapper.wrap(this, pEvent))
			bus.post(EventWrapper.wrap(this, pEvent))
		}
	}
}