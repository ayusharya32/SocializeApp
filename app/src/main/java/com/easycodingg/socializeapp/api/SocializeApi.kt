package com.easycodingg.socializeapp.api

import com.easycodingg.socializeapp.api.requests.AddCommentRequest
import com.easycodingg.socializeapp.api.requests.LoginRequest
import com.easycodingg.socializeapp.api.requests.RegisterRequest
import com.easycodingg.socializeapp.api.requests.UserUpdateRequest
import com.easycodingg.socializeapp.api.responses.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SocializeApi {

    @POST("auth/register")
    suspend fun registerUser(@Body registerRequestBody: RegisterRequest): Response<Token>

    @POST("auth/login")
    suspend fun loginUser(@Body loginRequestBody: LoginRequest): Response<Token>

    @GET("user")
    suspend fun getCurrentUser(@Header("Authorization") accessToken: String): Response<User>

    @Multipart
    @PUT("user")
    suspend fun updateUser(
            @Header("Authorization") accessToken: String,
            @Part profileImage: MultipartBody.Part,
            @Part("name") name: RequestBody
    ): Response<MessageResponse>

    @Multipart
    @PUT("user")
    suspend fun updateName(
            @Header("Authorization") accessToken: String,
            @Part("name") name: RequestBody
    ): Response<MessageResponse>

    @GET("posts")
    suspend fun getCurrentUserPosts(@Header("Authorization") accessToken: String): Response<List<Post>>

    @GET("posts/{userId}")
    suspend fun getUserPosts(
            @Header("Authorization") accessToken: String,
            @Path("userId") userId: String
    ): Response<List<Post>>

    @Multipart
    @POST("posts/add")
    suspend fun addPost(
            @Header("Authorization") accessToken: String,
            @Part postImage: MultipartBody.Part,
            @Part("caption") caption: RequestBody
    ): Response<MessageResponse>

    @DELETE("posts/remove")
    suspend fun removePost(
            @Header("Authorization") accessToken: String,
            @Query("postId") postId: String
    ): Response<MessageResponse>

    @GET("posts/followed")
    suspend fun getFeedPosts(@Header("Authorization") accessToken: String): Response<List<Post>>

    @PUT("posts/likes/add")
    suspend fun addLikeToPost(
            @Header("Authorization") accessToken: String,
            @Query("postId") postId: String
    ): Response<MessageResponse>

    @PUT("posts/likes/remove")
    suspend fun removeLikeFromPost(
            @Header("Authorization") accessToken: String,
            @Query("postId") postId: String
    ): Response<MessageResponse>

    @GET("search")
    suspend fun searchUsers(
            @Header("Authorization") accessToken: String,
            @Query("searchQuery") searchQuery: String
    ): Response<List<User>>

    @GET("following")
    suspend fun getCurrentUserFollowing(
            @Header("Authorization") accessToken: String
    ): Response<List<User>>

    @GET("following/{userId}")
    suspend fun getFollowingByUserId(
            @Header("Authorization") accessToken: String,
            @Path("userId") userId: String
    ): Response<List<User>>

    @GET("followers")
    suspend fun getCurrentUserFollowers(
            @Header("Authorization") accessToken: String
    ): Response<List<User>>

    @GET("followers/{userId}")
    suspend fun getFollowersByUserId(
            @Header("Authorization") accessToken: String,
            @Path("userId") userId: String
    ): Response<List<User>>

    @PUT("following/add")
    suspend fun followUser(
            @Header("Authorization") accessToken: String,
            @Query("followingId") followingId: String
    ): Response<MessageResponse>

    @PUT("following/remove")
    suspend fun unFollowUser(
            @Header("Authorization") accessToken: String,
            @Query("followingId") followingId: String
    ): Response<MessageResponse>

    @GET("comments")
    suspend fun getPostComments(
            @Header("Authorization") accessToken: String,
            @Query("postId") postId: String
    ): Response<List<Comment>>

    @POST("comments/add")
    suspend fun addCommentToPost(
            @Header("Authorization") accessToken: String,
            @Body addCommentRequest: AddCommentRequest
    ): Response<MessageResponse>

    @DELETE("comments/remove")
    suspend fun removeCommentFromPost(
            @Header("Authorization") accessToken: String,
            @Query("commentId") commentId: String
    ): Response<MessageResponse>
}