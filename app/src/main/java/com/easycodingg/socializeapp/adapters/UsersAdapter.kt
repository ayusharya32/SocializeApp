package com.easycodingg.socializeapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.ItemSearchUserBinding

class UsersAdapter(
    private var userList: List<User>,
    private val onItemClickedListener: (User) -> Unit
): RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    fun submitList(list: List<User>) {
        userList = list
    }

    fun currentList() = userList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.bind(currentUser)
    }

    override fun getItemCount() = userList.size

    inner class UserViewHolder(private val binding: ItemSearchUserBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClickedListener(userList[adapterPosition])
            }
        }

        fun bind(user: User) {
            binding.apply {
                tvUserName.text = user.name
                tvUserEmail.text = user.email

                Glide.with(binding.root)
                        .load(user.profilePhotoUrl)
                        .placeholder(R.drawable.ic_user)
                        .centerCrop()
                        .into(ivProfileImage)

            }
        }
    }
}