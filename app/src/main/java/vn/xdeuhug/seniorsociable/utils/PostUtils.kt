package vn.xdeuhug.seniorsociable.utils

import android.view.View
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 23 / 10 / 2023
 */
object PostUtils {
     fun setViewTypePost(typePost: Int, typeView: View) {
        when (typePost) {
            PostConstants.TYPE_PUBLIC -> {
                typeView.setBackgroundResource(R.drawable.ic_public)
            }

            PostConstants.TYPE_FRIEND -> {
                typeView.setBackgroundResource(R.drawable.ic_friend)
            }

            PostConstants.TYPE_PRIVATE -> {
                typeView.setBackgroundResource(R.drawable.ic_private)
            }
        }
    }


    fun findMostFrequentInteractions(interacts: ArrayList<Interact>): Pair<Int, Int> {
        val interactionCountMap = mutableMapOf<Int, Int>()

        for (interact in interacts) {
            val type = interact.type
            interactionCountMap[type] = interactionCountMap.getOrDefault(type, 0) + 1
        }

        val sortedInteractions = interactionCountMap.entries.sortedByDescending { it.value }

        if (sortedInteractions.size >= 2) {
            return Pair(sortedInteractions[0].key, sortedInteractions[1].key)
        } else if (sortedInteractions.size == 1) {
            return Pair(sortedInteractions[0].key, sortedInteractions[0].key)
        }

        return Pair(0, 0)
    }

    fun sortInteract(interacts: ArrayList<Interact>): List<MutableMap.MutableEntry<Int, Int>> {
        val interactionCountMap = mutableMapOf<Int, Int>()

        for (interact in interacts) {
            val type = interact.type
            interactionCountMap[type] = interactionCountMap.getOrDefault(type, 0) + 1
        }

        val sortedInteractions = interactionCountMap.entries.sortedByDescending { it.value }
        AppUtils.logJsonFromObject(sortedInteractions.map { it.key })
        return sortedInteractions
    }
}