package nl.sytac.slack.reachout

import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.users.UsersListRequest
import com.slack.api.model.User
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.slf4j.LoggerFactory
import java.util.*

@SpringBootApplication
class ReachOutApplication : CommandLineRunner {

		//start here: https://api.slack.com/authentication/oauth-v2

		private val logger = LoggerFactory.getLogger(ReachOutApplication::class.java)
		private val userTokenOrBetter = "xoxp-YOUR-TOKEN-HERE"
		private val messageText = """Your message here
			|Kind regards, Your Name.
			|""".trimMargin()
		val userEmails: List<String> = listOf() // list of emails here to filter against (optional)
		val userIds: List<String> = listOf() // list of user ids here to send message to (if you have already the list of user ids)

		override fun run(vararg args: String?) {
			logger.info("This app will send slack messages to the users in the list.")
			val mClient = Slack.getInstance().methods(userTokenOrBetter)
			val users = getActiveUsers(mClient, userEmails)
			sendMessage(mClient, users.map { it.id} )
			//or directly:	sendMessage(userIds)
		}

	private fun getActiveUsers(mClient: MethodsClient, userEmails: List<String> = emptyList()): List<User> {
		val usersResponse = mClient.usersList(UsersListRequest.builder().build())
		if (!usersResponse.isOk) {
			println("Error fetching users: ${usersResponse.error}")
		}
		val filteredUsers = usersResponse.members.filter {
				!it.isBot &&
				!it.isDeleted &&
				!it.isStranger &&
				it.profile.email != null
		}
		if(userEmails.isEmpty()) {
			return filteredUsers
		}
		return filteredUsers.filter {
			it.profile.email.lowercase(Locale.getDefault()) in userEmails.map { email -> email.lowercase(Locale.getDefault()) }
		}
	}

	fun sendMessage(mClient: MethodsClient, usersIdToTarget : List<String>) {
		usersIdToTarget.forEach {
			println("Sending message to $it")
				val messageResponse = mClient.chatPostMessage(
					ChatPostMessageRequest.builder()
						.channel(it)
						.text(messageText)
						.asUser(true) // Send as the authenticated user
						.build()
				)
			  Thread.sleep(2500)
				if (!messageResponse.isOk) {
					println("Error sending message to ${it}: ${messageResponse.error}")
				}
		}
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(ReachOutApplication::class.java, *args)
}




