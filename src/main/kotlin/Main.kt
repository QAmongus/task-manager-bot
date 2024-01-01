import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main() {
    val bot = TaskBot()
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    try{
        botsApi.registerBot(bot)
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}