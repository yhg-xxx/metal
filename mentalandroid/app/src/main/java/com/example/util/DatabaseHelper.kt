package com.example.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.Task
import com.example.model.User
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mental_health.db"
        private const val DATABASE_VERSION = 4 // 升级数据库版本以去掉id自增限制
        private const val TABLE_USER = "user"
        private const val TABLE_TASK = "task"
        
        // 用户表列名
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_NICKNAME = "nickname"
        private const val COLUMN_AVATAR_URL = "avatar_url"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_CREATED_TIME = "created_time"
        private const val COLUMN_UPDATED_TIME = "updated_time"
        private const val COLUMN_IS_LOGIN = "is_login"
        
        // 任务表列名
        private const val TASK_COLUMN_ID = "id"
        private const val TASK_COLUMN_TITLE = "title"
        private const val TASK_COLUMN_DESCRIPTION = "description"
        private const val TASK_COLUMN_IS_COMPLETED = "is_completed"
        private const val TASK_COLUMN_PRIORITY = "priority"
        private const val TASK_COLUMN_CREATE_TIME = "create_time"
        private const val TASK_COLUMN_REMINDER_COUNT = "reminder_count"
        private const val TASK_COLUMN_LAST_REMINDER_DATE = "last_reminder_date"
        
        // 日期格式化
        @SuppressLint("ConstantLocale")
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // 创建用户表
        val CREATE_USER_TABLE = (
            "CREATE TABLE $TABLE_USER ("
            + "$COLUMN_ID INTEGER PRIMARY KEY,"
            + "$COLUMN_USERNAME TEXT NOT NULL,"
            + "$COLUMN_PHONE TEXT NOT NULL,"
            + "$COLUMN_PASSWORD TEXT NOT NULL,"
            + "$COLUMN_EMAIL TEXT,"
            + "$COLUMN_NICKNAME TEXT,"
            + "$COLUMN_AVATAR_URL TEXT,"
            + "$COLUMN_GENDER TEXT DEFAULT 'UNKNOWN',"
            + "$COLUMN_AGE INTEGER,"
            + "$COLUMN_STATUS TEXT DEFAULT 'ACTIVE',"
            + "$COLUMN_CREATED_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
            + "$COLUMN_UPDATED_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
            + "$COLUMN_IS_LOGIN INTEGER DEFAULT 0,"
            + "UNIQUE($COLUMN_USERNAME),"
            + "UNIQUE($COLUMN_PHONE),"
            + "UNIQUE($COLUMN_EMAIL))"
        )
        db?.execSQL(CREATE_USER_TABLE)
        
        // 创建任务表
        val CREATE_TASK_TABLE = (
            "CREATE TABLE $TABLE_TASK ("
            + "$TASK_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "$TASK_COLUMN_TITLE TEXT NOT NULL,"
            + "$TASK_COLUMN_DESCRIPTION TEXT,"
            + "$TASK_COLUMN_IS_COMPLETED INTEGER DEFAULT 0,"
            + "$TASK_COLUMN_PRIORITY INTEGER DEFAULT 1,"
            + "$TASK_COLUMN_CREATE_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
            + "$TASK_COLUMN_REMINDER_COUNT INTEGER DEFAULT 0,"
            + "$TASK_COLUMN_LAST_REMINDER_DATE TEXT"
            + ")"
        )
        db?.execSQL(CREATE_TASK_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 如果是从版本2升级到版本3，只添加任务表而不删除用户表
        if (oldVersion < 3) {
            val CREATE_TASK_TABLE = (
                "CREATE TABLE $TABLE_TASK ("
                + "$TASK_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$TASK_COLUMN_TITLE TEXT NOT NULL,"
                + "$TASK_COLUMN_DESCRIPTION TEXT,"
                + "$TASK_COLUMN_IS_COMPLETED INTEGER DEFAULT 0,"
                + "$TASK_COLUMN_PRIORITY INTEGER DEFAULT 1,"
                + "$TASK_COLUMN_CREATE_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "$TASK_COLUMN_REMINDER_COUNT INTEGER DEFAULT 0,"
                + "$TASK_COLUMN_LAST_REMINDER_DATE TEXT"
                + ")"
            )
            db?.execSQL(CREATE_TASK_TABLE)
        }
        
        // 如果是从版本3升级到版本4，修改用户表去掉id自增限制
        if (oldVersion < 4) {
            // 创建临时表，去掉AUTOINCREMENT限制
            val CREATE_TEMP_USER_TABLE = (
                "CREATE TABLE " + TABLE_USER + "_temp ("
                + "$COLUMN_ID INTEGER PRIMARY KEY,"
                + "$COLUMN_USERNAME TEXT NOT NULL,"
                + "$COLUMN_PHONE TEXT NOT NULL,"
                + "$COLUMN_PASSWORD TEXT NOT NULL,"
                + "$COLUMN_EMAIL TEXT,"
                + "$COLUMN_NICKNAME TEXT,"
                + "$COLUMN_AVATAR_URL TEXT,"
                + "$COLUMN_GENDER TEXT DEFAULT 'UNKNOWN',"
                + "$COLUMN_AGE INTEGER,"
                + "$COLUMN_STATUS TEXT DEFAULT 'ACTIVE',"
                + "$COLUMN_CREATED_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "$COLUMN_UPDATED_TIME TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "$COLUMN_IS_LOGIN INTEGER DEFAULT 0,"
                + "UNIQUE($COLUMN_USERNAME),"
                + "UNIQUE($COLUMN_PHONE),"
                + "UNIQUE($COLUMN_EMAIL))"
            )
            db?.execSQL(CREATE_TEMP_USER_TABLE)
            
            // 复制数据到临时表
            db?.execSQL("INSERT INTO " + TABLE_USER + "_temp SELECT * FROM " + TABLE_USER)
            
            // 删除旧表
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
            
            // 重命名临时表为原表名
            db?.execSQL("ALTER TABLE " + TABLE_USER + "_temp RENAME TO " + TABLE_USER)
        }
        
        if (oldVersion >= 3 && oldVersion < 4) {
            // 版本3到4的升级已经处理，无需额外操作
        } else if (oldVersion >= 4) {
            // 简单处理：删除旧表并创建新表
            // 实际应用中可能需要更复杂的数据迁移策略
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")
            onCreate(db)
        }
    }

    // 添加或更新用户
    fun addOrUpdateUser(user: User): Long {
        val db = this.writableDatabase
        var result: Long = -1

        try {
            db.beginTransaction()

            // 如果用户要登录，先将所有其他用户的is_login设置为0
            if (user.isLogin) {
                val logoutContentValues = ContentValues()
                logoutContentValues.put(COLUMN_IS_LOGIN, 0)
                db.update(TABLE_USER, logoutContentValues, null, null)
            }

            val contentValues = ContentValues()
            contentValues.put(COLUMN_USERNAME, user.username)
            contentValues.put(COLUMN_PHONE, user.phone)
            contentValues.put(COLUMN_PASSWORD, user.password)
            contentValues.put(COLUMN_EMAIL, user.email ?: "")
            contentValues.put(COLUMN_NICKNAME, user.nickname ?: "")
            contentValues.put(COLUMN_AVATAR_URL, user.avatarUrl ?: "")
            contentValues.put(COLUMN_GENDER, user.gender)
            contentValues.put(COLUMN_AGE, user.age)
            contentValues.put(COLUMN_STATUS, user.status)
            contentValues.put(COLUMN_IS_LOGIN, if (user.isLogin) 1 else 0)

            // 处理时间字段
            val currentTime = DATE_FORMAT.format(Date())
            if (user.createdTime.isNullOrEmpty()) {
                contentValues.put(COLUMN_CREATED_TIME, currentTime)
            } else {
                contentValues.put(COLUMN_CREATED_TIME, user.createdTime)
            }
            contentValues.put(COLUMN_UPDATED_TIME, currentTime)

            // 直接根据电话号码查找用户，确保本地与远程用户数据正确匹配
            var existingUser = getUserByPhone(user.phone)

            result = if (existingUser != null) {
                // 更新现有用户
                // 始终使用手机号作为更新条件，确保本地与远程用户数据正确匹配
                val whereClause = "$COLUMN_PHONE = ?"
                val whereArgs = arrayOf(user.phone)

                // 重要：在更新时不要包含ID字段，避免主键冲突
                if (contentValues.containsKey(COLUMN_ID)) {
                    contentValues.remove(COLUMN_ID)
                }

                val updateResult = db.update(TABLE_USER, contentValues, whereClause, whereArgs)
                println("更新用户结果: $updateResult, where: $whereClause, args: ${whereArgs.joinToString()}")
                updateResult.toLong()
            } else {
                // 添加新用户
                // 如果有远程ID，使用远程ID
                if (user.id > 0) {
                    contentValues.put(COLUMN_ID, user.id)
                }
                val insertResult = db.insert(TABLE_USER, null, contentValues)
                println("插入用户结果: $insertResult")
                insertResult
            }

            db.setTransactionSuccessful()
            println("数据库事务提交成功")
        } catch (e: Exception) {
            e.printStackTrace()
            result = -1
            println("数据库事务失败: ${e.message}")
        } finally {
            db.endTransaction()
        }

        return result
    }





    // 检查用户是否存在
    fun checkUser(phone: String, password: String): User? {
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_PHONE = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(phone, password)
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
                val userPhone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE))
                val userPassword = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val email = if (it.getColumnIndex(COLUMN_EMAIL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) else null
                val nickname = if (it.getColumnIndex(COLUMN_NICKNAME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_NICKNAME)) else null
                val avatarUrl = if (it.getColumnIndex(COLUMN_AVATAR_URL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_AVATAR_URL)) else null
                val gender = if (it.getColumnIndex(COLUMN_GENDER) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_GENDER)) else "UNKNOWN"
                val ageColumnIndex = it.getColumnIndex(COLUMN_AGE)
                val age = if (ageColumnIndex != -1 && !it.isNull(ageColumnIndex)) it.getInt(ageColumnIndex) else null
                val status = if (it.getColumnIndex(COLUMN_STATUS) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS)) else "ACTIVE"
                val createdTime = if (it.getColumnIndex(COLUMN_CREATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_TIME)) else null
                val updatedTime = if (it.getColumnIndex(COLUMN_UPDATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_UPDATED_TIME)) else null
                val isLogin = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOGIN)) == 1
                
                return User(
                    id = id,
                    username = username,
                    phone = userPhone,
                    password = userPassword,
                    email = email,
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    gender = gender,
                    age = age,
                    status = status,
                    createdTime = createdTime,
                    updatedTime = updatedTime,
                    isLogin = isLogin
                )
            }
        }
        return null
    }

    // 获取当前登录的用户
    fun getLoggedInUser(): User? {
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_IS_LOGIN = 1",
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
                val phone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE))
                val password = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val email = if (it.getColumnIndex(COLUMN_EMAIL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) else null
                val nickname = if (it.getColumnIndex(COLUMN_NICKNAME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_NICKNAME)) else null
                val avatarUrl = if (it.getColumnIndex(COLUMN_AVATAR_URL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_AVATAR_URL)) else null
                val gender = if (it.getColumnIndex(COLUMN_GENDER) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_GENDER)) else "UNKNOWN"
                val ageColumnIndex = it.getColumnIndex(COLUMN_AGE)
                val age = if (ageColumnIndex != -1 && !it.isNull(ageColumnIndex)) it.getInt(ageColumnIndex) else null
                val status = if (it.getColumnIndex(COLUMN_STATUS) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS)) else "ACTIVE"
                val createdTime = if (it.getColumnIndex(COLUMN_CREATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_TIME)) else null
                val updatedTime = if (it.getColumnIndex(COLUMN_UPDATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_UPDATED_TIME)) else null
                val isLogin = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOGIN)) == 1
                
                return User(
                    id = id,
                    username = username,
                    phone = phone,
                    password = password,
                    email = email,
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    gender = gender,
                    age = age,
                    status = status,
                    createdTime = createdTime,
                    updatedTime = updatedTime,
                    isLogin = isLogin
                )
            }
        }
        return null
    }

    // 注销用户
    fun logout() {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_IS_LOGIN, 0)
        db.update(TABLE_USER, contentValues, "$COLUMN_IS_LOGIN = 1", null)
    }
    
    // 获取所有已登录过的用户手机号
    fun getAllUserPhones(): List<String> {
        val phoneList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(
            "SELECT DISTINCT $COLUMN_PHONE FROM $TABLE_USER",
            null
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val phone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE))
                phoneList.add(phone)
            }
        }
        return phoneList
    }
    
    // 根据手机号获取用户信息
    fun getUserByPhone(phone: String): User? {
        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_PHONE = ?",
            arrayOf(phone)
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME))
                val userPhone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE))
                val userPassword = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val email = if (it.getColumnIndex(COLUMN_EMAIL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)) else null
                val nickname = if (it.getColumnIndex(COLUMN_NICKNAME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_NICKNAME)) else null
                val avatarUrl = if (it.getColumnIndex(COLUMN_AVATAR_URL) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_AVATAR_URL)) else null
                val gender = if (it.getColumnIndex(COLUMN_GENDER) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_GENDER)) else "UNKNOWN"
                val ageColumnIndex = it.getColumnIndex(COLUMN_AGE)
                val age = if (ageColumnIndex != -1 && !it.isNull(ageColumnIndex)) it.getInt(ageColumnIndex) else null
                val status = if (it.getColumnIndex(COLUMN_STATUS) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS)) else "ACTIVE"
                val createdTime = if (it.getColumnIndex(COLUMN_CREATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_CREATED_TIME)) else null
                val updatedTime = if (it.getColumnIndex(COLUMN_UPDATED_TIME) != -1) it.getString(it.getColumnIndexOrThrow(COLUMN_UPDATED_TIME)) else null
                val isLogin = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_LOGIN)) == 1
                
                return User(
                    id = id,
                    username = username,
                    phone = userPhone,
                    password = userPassword,
                    email = email,
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    gender = gender,
                    age = age,
                    status = status,
                    createdTime = createdTime,
                    updatedTime = updatedTime,
                    isLogin = isLogin
                )
            }
        }
        return null
    }
    
    // ===== 任务相关操作方法 =====
    
    // 添加任务
    fun addTask(task: Task): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        
        contentValues.put(TASK_COLUMN_TITLE, task.title)
        contentValues.put(TASK_COLUMN_DESCRIPTION, task.description)
        contentValues.put(TASK_COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
        contentValues.put(TASK_COLUMN_PRIORITY, task.priority)
        contentValues.put(TASK_COLUMN_CREATE_TIME, DATE_FORMAT.format(task.createTime))
        contentValues.put(TASK_COLUMN_REMINDER_COUNT, task.reminderCount)
        task.lastReminderDate?.let {
            contentValues.put(TASK_COLUMN_LAST_REMINDER_DATE, DATE_FORMAT.format(it))
        }
        
        return db.insert(TABLE_TASK, null, contentValues)
    }
    
    // 更新任务
    fun updateTask(task: Task): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        
        contentValues.put(TASK_COLUMN_TITLE, task.title)
        contentValues.put(TASK_COLUMN_DESCRIPTION, task.description)
        contentValues.put(TASK_COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
        contentValues.put(TASK_COLUMN_PRIORITY, task.priority)
        contentValues.put(TASK_COLUMN_REMINDER_COUNT, task.reminderCount)
        task.lastReminderDate?.let {
            contentValues.put(TASK_COLUMN_LAST_REMINDER_DATE, DATE_FORMAT.format(it))
        }
        
        return db.update(TABLE_TASK, contentValues, "$TASK_COLUMN_ID = ?", arrayOf(task.id.toString()))
    }
    
    // 删除任务
    fun deleteTask(taskId: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_TASK, "$TASK_COLUMN_ID = ?", arrayOf(taskId.toString()))
    }
    
    // 获取所有任务
    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TASK ORDER BY $TASK_COLUMN_CREATE_TIME DESC", null)
        
        cursor.use {
            while (it.moveToNext()) {
                tasks.add(cursorToTask(it))
            }
        }
        return tasks
    }
    
    // 根据ID获取任务
    fun getTaskById(taskId: Int): Task? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TASK WHERE $TASK_COLUMN_ID = ?",
            arrayOf(taskId.toString())
        )
        
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToTask(it)
            }
        }
        return null
    }

    
    // 将游标转换为Task对象
    private fun cursorToTask(cursor: Cursor): Task {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(TASK_COLUMN_TITLE))
        val description = cursor.getString(cursor.getColumnIndexOrThrow(TASK_COLUMN_DESCRIPTION))
        val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_COLUMN_IS_COMPLETED)) == 1
        val priority = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_COLUMN_PRIORITY))
        val createTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(TASK_COLUMN_CREATE_TIME))
        val reminderCount = cursor.getInt(cursor.getColumnIndexOrThrow(TASK_COLUMN_REMINDER_COUNT))
        
        val createTime = try {
            DATE_FORMAT.parse(createTimeStr) ?: Date()
        } catch (_: Exception) {
            Date()
        }
        
        val lastReminderDateStr = cursor.getString(cursor.getColumnIndexOrThrow(TASK_COLUMN_LAST_REMINDER_DATE))
        val lastReminderDate = if (lastReminderDateStr != null && lastReminderDateStr.isNotEmpty()) {
            try {
                DATE_FORMAT.parse(lastReminderDateStr)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
        
        return Task(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            priority = priority,
            createTime = createTime,
            reminderCount = reminderCount,
            lastReminderDate = lastReminderDate
        )
    }
}