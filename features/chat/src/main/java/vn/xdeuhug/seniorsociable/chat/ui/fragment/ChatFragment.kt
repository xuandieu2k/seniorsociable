package vn.xdeuhug.seniorsociable.chat.ui.fragment

import android.annotation.SuppressLint
import android.view.View
import com.google.gson.Gson
import org.jetbrains.anko.dimen
import org.jetbrains.anko.support.v4.startActivity
import vn.xdeuhug.seniorsociable.app.AppFragment
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ListUserCache
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.chat.database.ChatManagerFSDB
import vn.xdeuhug.seniorsociable.chat.databinding.FragmentChatBinding
import vn.xdeuhug.seniorsociable.chat.ui.activity.DetailChatActivity
import vn.xdeuhug.seniorsociable.chat.ui.adapter.ChatAdapter
import vn.xdeuhug.seniorsociable.chat.ui.adapter.UserOnlineAdapter
import vn.xdeuhug.seniorsociable.chat.entity.Chat
import vn.xdeuhug.seniorsociable.chat.entity.Message
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.activity.HomeActivity
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.StringUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 05 / 10 / 2023
 */
class ChatFragment : AppFragment<HomeActivity>(), UserOnlineAdapter.OnListenerCLick,
    ChatAdapter.OnListenerCLick {
    private lateinit var binding: FragmentChatBinding

    // set data for rv user online
    private var listUserOnline = ArrayList<User>()
    private var listPair = ArrayList<Pair<Chat,Message>>()
    private lateinit var userOnlineAdapter: UserOnlineAdapter
    // List friend for search
    private var listFriend = ArrayList<User>()

    // set data for rv chat
    private var listChat = ArrayList<Chat>()
    private lateinit var chatAdapter: ChatAdapter
    override fun getLayoutView(): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initData() {
        initUserChat()
        initChat()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initChat() {
        binding.rvUserOnline.measure(
            requireContext().dimen(R.dimen.dp_72), requireContext().dimen(R.dimen.dp_88)
        )
        userOnlineAdapter = UserOnlineAdapter(requireContext())
        userOnlineAdapter.onListenerCLick = this
        userOnlineAdapter.setData(listUserOnline)
        userOnlineAdapter.notifyDataSetChanged()
        AppUtils.initRecyclerViewHorizontal(binding.rvUserOnline, userOnlineAdapter)
        getDataUser()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataUser() {
        FriendManagerFSDB.getAllFriends(object :
            FriendManagerFSDB.Companion.FireStoreCallback<ArrayList<User>> {
            override fun onSuccess(result: ArrayList<User>) {
                listUserOnline.addAll(result)
                listFriend.addAll(result)
                userOnlineAdapter.notifyDataSetChanged()
            }

            override fun onFailure(exception: Exception) {
                getString(R.string.please_try_later)
            }

        })
        val listIdsChat = ArrayList<String>()
        ListUserCache.getList().filter { it.id != UserCache.getUser().id }.map { it.id }.forEach {
            listIdsChat.add(StringUtils.combineAndHashIds(it,UserCache.getUser().id))
        }

        ChatManagerFSDB.getListChatByIdChat(listIdsChat,object :
            ChatManagerFSDB.Companion.FireStoreCallback<ArrayList<Pair<Chat, Message>>>{
            override fun onSuccess(result: ArrayList<Pair<Chat, Message>>) {
                result.sortByDescending { it.second.timeSend }
                val chats = result.map { it.first }
                listChat.clear()
                listChat.addAll(chats)
                listPair.clear()
                listPair.addAll(result)
                chatAdapter.notifyDataSetChanged()
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }

        })
//        listenerDataChatChange(listIdsChat)

    }

    private fun listenerDataChatChange(listIdsChat: ArrayList<String>)
    {
        ChatManagerFSDB.listenerDataChatChange(listIdsChat,object :
            ChatManagerFSDB.Companion.CallBackCRUDChat{
            override fun onAdded(message: Message?, idChat: String) {
                val chat = listChat.firstOrNull { it.id == idChat }
                val position = listChat.indexOf(chat)
                listPair.add(Pair(chat!!,message!!))
                chatAdapter.notifyItemChanged(position,Pair(chat,message))
            }

            override fun onUpdated(message: Message?, idChat: String) {
                //
            }

            override fun onRemoved(message: Message?, idChat: String) {
                //
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initUserChat() {
        chatAdapter = ChatAdapter(requireContext())
        chatAdapter.onListenerCLick = this
        chatAdapter.setData(listPair)
        chatAdapter.notifyDataSetChanged()
        AppUtils.initRecyclerViewVertical(binding.rvChat, chatAdapter)
    }

    override fun onClickItem(position: Int) {
        try {
            startActivity<DetailChatActivity>(
                AppConstants.OBJECT_CHAT to Gson().toJson(
                    listUserOnline[position]
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onClickItemChat(position: Int) {
        try {
            val id = listPair[position].first.members.first { item -> item != UserCache.getUser().id }
            val user = ListUserCache.getList().first { it.id == id }
            startActivity<DetailChatActivity>(
                AppConstants.OBJECT_CHAT to Gson().toJson(
                    user
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}