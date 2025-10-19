package com.example.network

import com.example.model.BaseResponse
import com.example.model.Counselor
import com.example.model.Message
import com.example.model.QuickConsultation
import com.example.model.SearchCounselorsRequest
import com.example.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    
    @GET("hello")
    suspend fun getHello(): String
    
    @POST("api/counselors/search")
    suspend fun searchCounselors(@Body request: SearchCounselorsRequest): List<Counselor>
    
    /**
     * 获取指定ID的咨询师详细信息
     */
    @GET("api/counselors/{id}")
    suspend fun getCounselorDetail(@Path("id") counselorId: Int): Counselor
    
    /**
     * 获取所有擅长领域（去重）
     */
    @GET("api/counselors/specializations")
    suspend fun getAllSpecializations(): List<String>
    
    /**
     * 获取所有治疗流派（去重）
     */
    @GET("api/counselors/approaches")
    suspend fun getAllTherapeuticApproaches(): List<String>
    
    /**
     * 创建新用户，支持上传头像文件
     */
    @Multipart
    @POST("api/users")
    suspend fun createUser(
        @Part("user") user: RequestBody,
        @Part avatar: MultipartBody.Part? = null
    ): BaseResponse<User>
    
    /**
     * 修改现有用户信息，支持上传新的头像文件
     */
    @Multipart
    @PUT("api/users")
    suspend fun updateUser(
        @Query("phone") phone: String,
        @Part("user") user: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): BaseResponse<User>
    
    /**
     * 提交快速咨询申请
     */
    @Multipart
    @POST("api/quick-consultation")
    suspend fun submitQuickConsultation(
        @Part("userId") userId: RequestBody,
        @Part("problemDescription") problemDescription: RequestBody,
        @Part("problemDuration") problemDuration: RequestBody,
        @Part("preferredMethod") preferredMethod: RequestBody,
        @Part files: List<MultipartBody.Part>? = null,
        @Part("matchedCounselorId") matchedCounselorId: RequestBody? = null
    ): BaseResponse<QuickConsultation>
    
    /**
     * 根据用户ID查询已匹配的咨询师列表
     */
    @GET("api/quick-consultation/matched-counselors")
    suspend fun getMatchedCounselors(@Query("useId") userId: Long): BaseResponse<List<Counselor>>
    
    /**
     * 获取指定用户和咨询师之间的对话记录
     */
    @GET("api/consultation/messages/conversation")
    suspend fun getConversationMessages(
        @Query("userId") userId: Long,
        @Query("counselorId") counselorId: Long,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<Message>
    

}