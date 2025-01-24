package com.example.bai1

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bai1.databinding.RvItemBinding
import kotlin.math.cbrt

class ItemAdapter(
    var list: MutableList<Note>,
    private val onItemClickListener: OnItemClickListener,
    var selectedItems: MutableList<Note>,
    var tvSelected: TextView,
    var showCheckbox: Boolean
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        val binding = RvItemBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            // gán dữ liệu cho các giá trị
            val item = list[position]
            tvTitle.text = item.title
            tvContent.text = item.content
            tvEditTime.text = item.editTime

            //Kiểm tra checkBox
            checkBox.visibility = if (showCheckbox) View.VISIBLE else View.GONE

            //Kiểm tra xem item này đã được chọn hay chưa
            checkBox.isChecked = selectedItems.contains(item)

            //Xử lí checkBox được chọn hoặc là bỏ
            checkBox.setOnCheckedChangeListener{_, isChecked ->
                if (isChecked) {
                    if (!selectedItems.contains(item)) {
                        selectedItems.add(item)
                    }
                } else {
                    selectedItems.remove(item)
                }

                // Cập nhật số lượng mục đã chọn và hiển thị lên TextView
                val selectedCount = selectedItems.size
                tvSelected.text = "Đã chọn $selectedCount" // Cập nhật số lượng
            }
        }
        val note = list[position]
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(note)
        }

        holder.itemView.setOnLongClickListener {
            onItemClickListener.onItemLongClick()
            true
        }

    }


    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(private val binding: RvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var tvTitle: TextView
        var tvContent: TextView
        var tvEditTime: TextView
        var checkBox: CheckBox

        init {
            tvTitle = binding.tvTitle
            tvContent = binding.tvContent
            tvEditTime = binding.tvEditTime
            checkBox = binding.cbDelete
        }
    }

}