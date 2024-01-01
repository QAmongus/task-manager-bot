import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Suppress("IMPLICIT_CAST_TO_ANY")
class TaskBot : TelegramLongPollingBot("YOUR_BOT_TOKEN") {

    private val tasksToDo: MutableMap<Long, MutableList<String>> = HashMap()
    private val tasksDone: MutableMap<Long, MutableList<String>> = HashMap()

    override fun getBotUsername(): String {
        return "YOUR_BOT_NAME"
    }

    override fun getBotToken(): String {
        return "YOUR_BOT_TOKEN"
    }

    override fun onUpdateReceived(update: Update){
        if (update.hasMessage() && update.message.hasText()) {

            val userId = update.message.from.id
            val messageText = update.message.text
            val chatId = update.message.chatId.toString()
            val toDo = tasksToDo[userId]?.mapIndexed { index, s -> "${index + 1}. $s"}?.joinToString("\n") ?: "No tasks today."
            val done = tasksDone[userId]?.mapIndexed { index, s -> "${index + 1}. $s"}?.joinToString("\n") ?: "No completed tasks"

            val fullMessage = "Active tasks:\n$toDo\n\nDone today: \n$done"


            val commands = when{
                messageText == "/list" -> {
                    sendMessage(chatId, fullMessage)
                }
                messageText.startsWith("/done") -> {
                        val numbers = messageText.removePrefix("/done")
                            .trim()
                            .split("\\s+".toRegex())
                            .mapNotNull { it.toIntOrNull() }
                            .sortedDescending() // Sort in reverse order to avoid index shifting

                        val messagesMarkedDone = mutableListOf<String>()
                        for (number in numbers) {
                            val activeMessages = tasksToDo[userId]
                            if (activeMessages != null && number > 0 && number <= activeMessages.size) {
                                val doneMessage = activeMessages.removeAt(number - 1)
                                tasksDone.computeIfAbsent(userId) { mutableListOf() }.add(doneMessage)
                                messagesMarkedDone.add("Task number $number")
                                sendMessage(chatId, text = "Completed task is recorded")
                            } else {
                                sendMessage(chatId, "Invalid task number: $number")
                                return // Break out of the loop and method if any number is invalid
                            }
                        }
                    }
                messageText == "/clear" -> {
                    tasksToDo.computeIfAbsent(userId) { mutableListOf() }.clear()
                    sendMessage(chatId, text = "List restored")
                }
                messageText == "/start" ->{
                    sendMessage(chatId, text = "Send any task which you plan to do")
                }
                else -> tasksToDo.computeIfAbsent(userId) { mutableListOf() }.add(messageText)
            }
        }
    }

    private fun sendMessage(chatId: String, text: String) {
        val message = SendMessage(chatId, text)
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}