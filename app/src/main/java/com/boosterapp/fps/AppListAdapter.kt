package com.boosterapp.fps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(private val apps: MutableList<AppInfo>) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iconAppIcon)
        val name: TextView = view.findViewById(R.id.textAppName)
        val tag: TextView = view.findViewById(R.id.textGameTag)
        val protectCheck: CheckBox = view.findViewById(R.id.checkProtect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.appName
        holder.tag.visibility = if (app.isGame) View.VISIBLE else View.GONE
        holder.protectCheck.setOnCheckedChangeListener(null)
        holder.protectCheck.isChecked = app.isProtected
        holder.protectCheck.setOnCheckedChangeListener { _, isChecked ->
            app.isProtected = isChecked
        }
    }

    override fun getItemCount() = apps.size
}
