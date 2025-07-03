package io.github.msaggik.settings.presentation.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.github.msaggik.settings.R

class LanguageAdapter(
    private var context: Context,
    private var index: IntArray,
    private var lang: Array<String>
) : BaseAdapter() {

    private var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return index.size
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_language, null)
        val languageName = view.findViewById<TextView>(R.id.language_name)
        languageName.text = lang[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_language_dropdown, null)
        val languageName = view.findViewById<TextView>(R.id.language_name)
        languageName.text = lang[position]
        return view
    }
}