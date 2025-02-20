package vn.xdeuhug.seniorsociable.chat.utils

import vn.xdeuhug.seniorsociable.chat.entity.Message
import java.util.Calendar
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 26 / 11 / 2023
 */
object ChatUtils {
    fun sortMessagesByDate(messages: ArrayList<Message>): Map<Date, List<Message>> {
        return messages
            .groupBy { truncateTime(it.timeSend) }
            .toSortedMap(compareByDescending { it.time })
    }

    fun truncateTime(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }


    fun sortMessagesByDate(messages: List<Message>): ArrayList<ArrayList<Message>> {
        val sortedMessages: ArrayList<ArrayList<Message>> = ArrayList()

        messages.forEach { message ->
            val truncatedDate = truncateTimes(message.timeSend)
            val existingGroup = sortedMessages.find { it.isNotEmpty() && truncateTime(it[0].timeSend) == truncatedDate }

            if (existingGroup != null) {
                existingGroup.add(message)
            } else {
                val newGroup = ArrayList<Message>()
                newGroup.add(message)
                sortedMessages.add(newGroup)
            }
        }

        return sortedMessages
    }

    fun truncateTimes(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}