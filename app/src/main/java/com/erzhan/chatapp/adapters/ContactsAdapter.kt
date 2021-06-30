package com.erzhan.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth

class ContactsAdapter(
    context: Context,
    users: List<User>,
    onItemCLickListener: OnItemClickListener
) : RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {

    private val userList: List<User>
    private val inflater: LayoutInflater
    private var onItemClickListener: OnItemClickListener

    init {
        this.userList = users
        inflater = LayoutInflater.from(context)
        this.onItemClickListener = onItemCLickListener
    }

    class MyViewHolder(itemView: View, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextViewId)
        private var onItemClickListener: OnItemClickListener

        init {
            this.onItemClickListener = onItemClickListener
            itemView.setOnClickListener { v: View ->
                onClick(
                    v
                )
            }
        }

        fun bind(user: User) {
            usernameTextView.text = user.name
        }

        override fun onClick(v: View) {
            onItemClickListener.onItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_user, parent, false)

        return MyViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}