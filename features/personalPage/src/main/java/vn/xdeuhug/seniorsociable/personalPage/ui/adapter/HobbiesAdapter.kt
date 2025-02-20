package vn.xdeuhug.seniorsociable.personalPage.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.personalPage.databinding.ItemHobbyBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 01 / 12 / 2023
 */
class HobbiesAdapter(context: Context) : AppAdapter<Hobby>(context) {
    private lateinit var binding: ItemHobbyBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemHobbyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemHobbyBinding) : AppViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            binding.imvHobbies.setImageResource(item.imageResource)
            binding.tvHobbies.text = item.name
        }

    }

    interface OnListenerCLick {
        fun onClick(position: Int)
    }
}