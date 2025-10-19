package com.example.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mental_health.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USER = "user"
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
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_USER_TABLE = (
            "CREATE TABLE $TABLE_USER ("
            + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
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
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 简单处理：删除旧表并创建新表
        // 实际应用中可能需要更复杂的数据迁移策略
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // 添加或更新用户
    fun addOrUpdateUser(user: User): Long {
        val db = this.writableDatabase
        
        // 开始事务
        db.beginTransaction()
        try {
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
            contentValues.put(COLUMN_CREATED_TIME, user.createdTime ?: "")
            contentValues.put(COLUMN_UPDATED_TIME, user.updatedTime ?: "")
            contentValues.put(COLUMN_IS_LOGIN, if (user.isLogin) 1 else 0)
            
            // 同步远程数据库的ID到本地数据库，但只有当远程ID不为0时才更新
            if (user.id > 0) {
                contentValues.put(COLUMN_ID, user.id)
            }

            // 先检查手机号是否已存在
            val cursor = db.query(TABLE_USER, arrayOf(COLUMN_ID), "$COLUMN_PHONE = ?", arrayOf(user.phone), null, null, null)
            val userId = if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            } else {
                0
            }
            cursor.close()

            val result = if (userId > 0) {
                // 更新现有用户
                db.update(TABLE_USER, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString())).toLong()
            } else {
                // 添加新用户
                db.insert(TABLE_USER, null, contentValues)
            }
            
            // 提交事务
            db.setTransactionSuccessful()
            return result
        } finally {
            // 结束事务
            db.endTransaction()
        }
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
}