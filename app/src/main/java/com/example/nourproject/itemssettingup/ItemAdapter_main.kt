package com.example.nourproject.itemssettingup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.nourproject.R
import java.util.ArrayList


class ItemAdapter_main(var context: Context, var arrayList: ArrayList<ItemList_main>) : BaseAdapter() {
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.card_view_item_layout_list_main, null)

        var title_g: TextView = view.findViewById(R.id.title_gaslevel)
        var title_h: TextView = view.findViewById(R.id.title_hum)
        var title_t: TextView = view.findViewById(R.id.title_temp)
        var detail_g: TextView = view.findViewById(R.id.detail_gas)
        var detail_t: TextView = view.findViewById(R.id.detail_temp)
        var detail_h: TextView = view.findViewById(R.id.detail_hum)
        var time: TextView = view.findViewById(R.id.time)

        var items : ItemList_main = arrayList.get(position)

        title_g.text = items.title_g
        title_t.text = items.title_t
        title_h.text = items.title_h
        detail_g.text = items.detail_g
        detail_t.text = items.detail_t
        detail_h.text = items.detail_h
        time.text = items.time

        return view!!
    }

}