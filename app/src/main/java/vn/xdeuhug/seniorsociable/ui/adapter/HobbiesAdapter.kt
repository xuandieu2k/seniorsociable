package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.databinding.ItemHobbiesBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 11 / 2023
 */
class HobbiesAdapter(context: Context) : AppAdapter<Hobby>(context) {
    var onListenerSelected: OnListenerSelected? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemHobbiesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemHobbiesBinding) : AppViewHolder(binding.root) {
        init {
            binding.cbSelected.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected!!.onSelected(position)
                }
            }
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerSelected!!.onSelected(position)
                }
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            binding.imvHobbies.setImageResource(item.imageResource)
            binding.tvHobbies.text = item.name
            binding.cbSelected.isChecked = item.isChecked
        }

    }

    interface OnListenerSelected {
        fun onSelected(position: Int)
    }
}