package com.easycodingg.socializeapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.api.responses.Comment
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.ItemCommentBinding

class CommentsAdapter(
    private var commentList: List<Comment>,
    private val onProfileItemClickListener: (User) -> Unit,
    private val onDeleteButtonClickedListener: (Comment) -> Unit
): RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    fun submitList(list: List<Comment>) {
        commentList = list
    }

    fun currentList() = commentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentComment = commentList[position]
        holder.bind(currentComment)
    }

    override fun getItemCount() = commentList.size

    inner class CommentViewHolder(private val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                ivProfileImage.setOnClickListener {
                    onProfileItemClickListener(commentList[adapterPosition].commentUser)
                }

                tvUserEmail.setOnClickListener {
                    onProfileItemClickListener(commentList[adapterPosition].commentUser)
                }

                ivDelete.setOnClickListener {
                    onDeleteButtonClickedListener(commentList[adapterPosition])
                }
            }
        }

        fun bind(comment: Comment) {
            binding.apply {
                tvComment.text = comment.commentText
                tvUserEmail.text = comment.commentUser.email

                Glide.with(binding.root)
                        .load(comment.commentUser.profilePhotoUrl)
                        .placeholder(R.drawable.ic_user)
                        .centerCrop()
                        .into(ivProfileImage)
            }
        }
    }
}