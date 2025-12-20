package com.example.network


import com.example.model.*
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
    ): ApiResponse<User>
    
    /**
     * 修改现有用户信息，支持上传新的头像文件
     */
    @Multipart
    @PUT("api/users")
    suspend fun updateUser(
        @Query("phone") phone: String,
        @Part("user") user: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): ApiResponse<User>
    
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
    ): ApiResponse<QuickConsultation>
    
    /**
     * 根据用户ID查询已匹配的咨询师列表
     */
    @GET("api/quick-consultation/matched-counselors")
    suspend fun getMatchedCounselors(@Query("useId") userId: Long): ApiResponse<List<Counselor>>
    
    /**
     * 获取用户所有进行过对话的咨询师信息
     */
    @GET("api/consultation/messages/user/counselors")
    suspend fun getUserConversatedCounselors(@Query("userId") userId: Long): ApiResponse<List<Counselor>>
    
    /**
     * 获取用户与每个咨询师的最新一条消息
     * 注意：服务器返回直接是消息数组，不是包装在ApiResponse中
     */
    @GET("api/consultation/messages/user/latest")
    suspend fun getUserLatestMessagesWithCounselors(@Query("userId") userId: Long): List<Message>
    
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
    
    /**
     * 获取所有学习包列表
     */
    @GET("api/learning-packages")
    suspend fun getLearningPackages(): ApiResponse<List<LearningPackage>>
    
    /**
     * 根据学习包ID获取视频列表
     */
    @GET("api/learning-videos/package/{learningPackageId}")
    suspend fun getVideosByLearningPackageId(@Path("learningPackageId") learningPackageId: Long): ApiResponse<List<LearningVideo>>
    
    /**
     * 获取用户的咨询记录
     */
    @GET("api/quick-consultation/user/records")
    suspend fun getUserConsultationRecords(@Query("userId") userId: Int): ApiResponse<List<QuickConsultation>>
    
    /**
     * 创建初始对话
     * @param userId 用户ID
     * @param counselorId 咨询师ID
     * @return ApiResponse<Boolean>
     */
    @POST("api/consultation/messages/initial")
    suspend fun createInitialConversation(
        @Query("userId") userId: Long,
        @Query("counselorId") counselorId: Long
    ): ApiResponse<Boolean>

    /**
     * 手机号注册状态检查接口
     * @param phone 待查询的手机号
     * @return ApiResponse<Map<String, Boolean>> 返回包含exists字段的Map
     */
    @GET("api/users/check/{phone}")
    suspend fun checkPhoneRegistration(@Path("phone") phone: String): ApiResponse<Map<String, Boolean>>

    /**
     * 用户注册接口
     * @param user 用户注册信息
     * @return ApiResponse<User> 返回注册成功的用户信息
     */
    @POST("api/users/register")
    suspend fun registerUser(@Body user: User): ApiResponse<User>

    /**
     * 用户登录接口
     * @param loginRequest 包含手机号和密码的登录请求
     * @return ApiResponse<User> 返回登录成功的用户信息
     */
    @POST("api/users/login")
    suspend fun loginUser(@Body loginRequest: Map<String, String>): ApiResponse<User>

    /**
     * 根据学习包ID获取测试题目
     * @param learningPackageId 学习包ID
     * @return ApiResponse<List<TestQuestions>> 测试题目列表
     */
    @GET("api/tests/questions")
    suspend fun getQuestionsByLearningPackageId(@Query("learningPackageId") learningPackageId: Long): ApiResponse<List<TestQuestions>>

    /**
     * 创建测试记录
     * @param userId 用户ID
     * @param learningPackageId 学习包ID
     * @param totalQuestions 总题目数
     * @return ApiResponse<TestRecords> 测试记录
     */
    @POST("api/tests/records")
    suspend fun createTestRecord(@Query("userId") userId: Long, @Query("learningPackageId") learningPackageId: Long, @Query("totalQuestions") totalQuestions: Int): ApiResponse<TestRecords>

    /**
     * 保存用户答案
     * @param testAnswer 用户答案
     * @return ApiResponse<Boolean> 保存结果
     */
    @POST("api/tests/answers")
    suspend fun saveTestAnswer(@Body testAnswer: TestAnswers): ApiResponse<Boolean>

    /**
     * 完成测试
     * @param testRecordId 测试记录ID
     * @param correctAnswers 正确答案数
     * @param score 得分
     * @param timeSpentSeconds 用时（秒）
     * @return ApiResponse<TestRecords> 测试记录
     */
    @PUT("api/tests/records/{testRecordId}/complete")
    suspend fun completeTest(@Path("testRecordId") testRecordId: Long, @Query("correctAnswers") correctAnswers: Int, @Query("score") score: Int, @Query("timeSpentSeconds") timeSpentSeconds: Int): ApiResponse<TestRecords>

    /**
     * 获取用户测试记录
     * @param userId 用户ID
     * @return ApiResponse<List<TestRecords>> 测试记录列表
     */
    @GET("api/tests/records/user/{userId}")
    suspend fun getUserTestRecords(@Path("userId") userId: Long): ApiResponse<List<TestRecords>>

    /**
     * 获取测试记录详情
     * @param testRecordId 测试记录ID
     * @return ApiResponse<TestRecords> 测试记录
     */
    @GET("api/tests/records/{testRecordId}")
    suspend fun getTestRecordDetail(@Path("testRecordId") testRecordId: Long): ApiResponse<TestRecords>

    /**
     * 根据测试记录ID获取答题详情
     * @param testRecordId 测试记录ID
     * @return ApiResponse<List<TestAnswers>> 答题详情列表
     */
    @GET("api/tests/answers/{testRecordId}")
    suspend fun getAnswersByTestRecordId(@Path("testRecordId") testRecordId: Long): ApiResponse<List<TestAnswers>>

    /**
     * 根据测试记录ID获取评估报告
     * @param testRecordId 测试记录ID
     * @return ApiResponse<PsychologicalAssessments> 评估报告
     */
    @GET("api/tests/assessments/{testRecordId}")
    suspend fun getAssessmentByTestRecordId(@Path("testRecordId") testRecordId: Long): ApiResponse<PsychologicalAssessments>

    /**
     * 生成评估报告
     * @param assessment 评估报告信息
     * @return ApiResponse<PsychologicalAssessments> 生成的评估报告
     */
    @POST("api/tests/assessments")
    suspend fun generateAssessment(@Body assessment: PsychologicalAssessments): ApiResponse<PsychologicalAssessments>

}