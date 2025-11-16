package com.example.network

import com.example.model.Child
import com.example.model.CreateChildRequest
import com.example.model.UpdateChildRequest
import retrofit2.http.*

interface ChildApiService {
    
    /**
     * 新增孩子信息
     * POST /api/children
     */
    @POST("/api/children")
    suspend fun createChild(@Body request: CreateChildRequest): ApiResponse<Child>
    
    /**
     * 修改孩子信息
     * PUT /api/children
     */
    @PUT("/api/children")
    suspend fun updateChild(@Body request: UpdateChildRequest): ApiResponse<Child>
    
    /**
     * 设置当前操作孩子
     * PUT /api/children/current?userId={userId}&childId={childId}
     */
    @PUT("/api/children/current")
    suspend fun setCurrentChild(
        @Query("userId") userId: Long,
        @Query("childId") childId: Long
    ): ApiResponse<Unit>
    
    /**
     * 获取当前操作孩子
     * GET /api/children/current?userId={userId}
     */
    @GET("/api/children/current")
    suspend fun getCurrentChild(@Query("userId") userId: Long): ApiResponse<Child>
    
    /**
     * 根据用户ID获取所有孩子信息
     * GET /api/children/list?userId={userId}
     */
    @GET("/api/children/list")
    suspend fun getChildrenByUserId(@Query("userId") userId: Long): ApiResponse<List<Child>>
}