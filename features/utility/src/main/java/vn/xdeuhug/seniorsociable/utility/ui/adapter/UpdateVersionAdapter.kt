package vn.xdeuhug.seniorsociable.utility.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.utility.databinding.ItemVersionUpdateBinding
import vn.xdeuhug.seniorsociable.utility.model.UpdateVersion
import vn.xdeuhug.seniorsociable.utils.DateUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 04 / 12 / 2023
 */
class UpdateVersionAdapter(context: Context) : AppAdapter<UpdateVersion>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemVersionUpdateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemVersionUpdateBinding) : AppViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBindView(position: Int) {
            val item = getItem(position)
            binding.tvHeadVersion.text = "${getString(R.string.version)} ${item.appVersion}"
            binding.tvTimeCreated.text = DateUtils.getDateByFormatDateTime6(item.createdAt)
            binding.tvDescription.text = item.message
        }

    }
}