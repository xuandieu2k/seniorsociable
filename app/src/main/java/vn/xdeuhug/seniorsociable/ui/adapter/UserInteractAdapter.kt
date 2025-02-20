package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.databinding.ItemUserInteractBinding
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Interact
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class UserInteractAdapter(context: Context) : AppAdapter<User>(context) {
    var onListenerCLick: OnListenerCLick? = null
    private var id = 0
    private var listInteracts = ArrayList<Interact>()

    constructor(context: Context, listInteracts: ArrayList<Interact>, id: Int) : this(context) {
        this.id = id
        this.listInteracts = listInteracts
    }

    constructor(context: Context, id: Int) : this(context) {
        this.id = id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding =
            ItemUserInteractBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemUserInteractBinding) :
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
            setImageView(item)
            binding.tvName.text = item.name
        }


        private fun setImageView(user:User) {
            when (id) {
                PostConstants.INTERACT_TAB_DEFAULT -> {
                    val interact = listInteracts.firstOrNull { it.id == user.id }
                    val type = interact?.type ?: PostConstants.INTERACT_LIKE
                    setImageInteract(type)
                }

                PostConstants.INTERACT_LIKE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_like_senior_new)
                }

                PostConstants.INTERACT_LOVE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_heart)
                }

                PostConstants.INTERACT_SMILE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_laugh)
                }

                PostConstants.INTERACT_WOW -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_wow)
                }

                PostConstants.INTERACT_SAD -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_sad)
                }

                PostConstants.INTERACT_ANGRY -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_angry)
                }

            }
        }

        private fun setImageInteract(interactType: Int) {
            when (interactType) {
                PostConstants.INTERACT_LIKE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_like_senior_new)
                }

                PostConstants.INTERACT_LOVE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_heart)
                }

                PostConstants.INTERACT_SMILE -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_laugh)
                }

                PostConstants.INTERACT_WOW -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_wow)
                }

                PostConstants.INTERACT_SAD -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_sad)
                }

                PostConstants.INTERACT_ANGRY -> {
                    binding.imvInteract.setImageResource(R.drawable.ic_angry)
                }
                else -> binding.imvInteract.setImageResource(R.drawable.ic_like_senior_new)
            }
        }


    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}