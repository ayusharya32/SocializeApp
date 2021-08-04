package com.easycodingg.socializeapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter(
    private var postList: List<Post>,
    private val onCommentButtonClickedListener: (Post) -> Unit,
    private val onLikeButtonClickedListener: (Post) -> Unit
): RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private var isDeleteIconVisible = false

    fun submitList(list: List<Post>) {
        postList = list
    }

    fun setDeleteIconVisibility(state: Boolean) {
        isDeleteIconVisible = state
    }

    fun currentList() = postList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.bind(currentPost)
    }

    override fun getItemCount() = postList.size

    inner class PostViewHolder(private val binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvUserEmail.setOnClickListener {
                onProfileItemClickedListener?.let {
                    it(postList[adapterPosition].postUser)
                }
            }

            binding.ivProfileImage.setOnClickListener {
                onProfileItemClickedListener?.let {
                    it(postList[adapterPosition].postUser)
                }
            }

            binding.ivComment.setOnClickListener {
                onCommentButtonClickedListener(postList[adapterPosition])
            }

            binding.ivLike.setOnClickListener {
                onLikeButtonClickedListener(postList[adapterPosition])
            }

            binding.ivDelete.setOnClickListener {
                onDeleteButtonClickedListener?.let {
                    it(postList[adapterPosition])
                }
            }
        }

        fun bind(post: Post) {
            binding.apply {
                tvUserEmail.text = post.postUser.email

                val createdDate = SimpleDateFormat("MMM dd, yyyy", Locale.UK).format(post.createdAt)
                tvCreatedAt.text = createdDate

                val likesString = "${post.likedBy.size} Likes"
                tvLikes.text = likesString

                tvCaption.text = post.caption

                ivDelete.isVisible = isDeleteIconVisible

                if(post.isLiked) {
                    ivLike.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.red))
                } else {
                    ivLike.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.light_grey))
                }

                Glide.with(binding.root)
                        .load(post.postUser.profilePhotoUrl)
                        .placeholder(R.drawable.ic_user)
                        .centerCrop()
                        .into(ivProfileImage)

                Glide.with(binding.root)
                        .load(post.postImageUrl)
                        .placeholder(R.drawable.dot_placeholder)
                        .centerCrop()
                        .into(ivPostImage)
            }
        }
    }

    private var onProfileItemClickedListener: ((User) -> Unit)? = null
    private var onDeleteButtonClickedListener: ((Post) -> Unit)? = null

    fun setOnProfileItemClickedListener(listener: (User) -> Unit) {
        onProfileItemClickedListener = listener
    }

    fun setOnDeleteButtonClickedListener(listener: (Post) -> Unit) {
        onDeleteButtonClickedListener = listener
    }

}