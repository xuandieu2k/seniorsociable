package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.ItemUserSearchBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 09 / 11 / 2023
 */
class SearchUserAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerCLick: OnListenerCLick? = null
    private var listFriend = ArrayList<User>()
    private var listIdsFriend = ArrayList<String>()
    init {
        FriendManagerFSDB.getAllFriendsByIdUser(UserCache.getUser().id,object :
            FriendManagerFSDB.Companion.FireStoreCallback<ArrayList<User>>{
            override fun onSuccess(result: ArrayList<User>) {
                listFriend.clear()
                listFriend.addAll(result)
                listIdsFriend = listFriend.map { it.id }.toArrayList()
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }

        })
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemUserSearchBinding) :
        AppViewHolder(binding.root) {
        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(position)
                }
            }
        }
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            PhotoShowUtils.loadAvatarImage(
                UploadFireStorageUtils.getRootURLAvatarById(item.id), item.avatar, binding.imvAvatar
            )
            binding.tvUsername.text = item.name
            if(item.id in listIdsFriend)
            {
                binding.tvStatus.show()
                binding.tvStatus.text = getString(R.string.friend)
            }else{
                binding.tvStatus.hide()
                binding.tvStatus.text = ""
            }
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}