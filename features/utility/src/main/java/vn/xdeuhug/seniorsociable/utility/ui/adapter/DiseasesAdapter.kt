package vn.xdeuhug.seniorsociable.utility.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.utility.databinding.ItemDiseasesBinding
import vn.xdeuhug.seniorsociable.utility.model.Diseases
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class DiseasesAdapter(context: Context) : AppAdapter<Diseases>(context) {
    var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemDiseasesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(private val binding: ItemDiseasesBinding) : AppViewHolder(binding.root) {

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION)
                {
                    onClickListener!!.onClick(position)
                }
            }
        }
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            //
            val item = getItem(position)
            binding.tvName.text = item.name
            binding.tvText.text = item.overview
            if(item.image == null)
            {
                PhotoShowUtils.loadPhotoRound("/gfh/ghg",binding.imvLogo)
            }else{
                Timber.tag("log ")
                item.image?.firstNotNullOf {
                    PhotoShowUtils.loadPhotoRound(it,binding.imvLogo)
                }
            }
        }

    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

}