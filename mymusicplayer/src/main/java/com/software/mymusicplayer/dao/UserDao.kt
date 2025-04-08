package com.software.mymusicplayer.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.software.mymusicplayer.entity.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)
    @Insert() //onConflict = OnConflictStrategy.IGNORE
    suspend fun insertUsers(vararg user: User)

    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updateUsers(vararg user: User)
    @Query("SELECT * FROM users;")
    suspend fun getAllUsers():List<User>
    @Query("DELETE FROM users WHERE user_name = :userName")
    suspend fun deleteUserByUserName(userName:String)
    @Query("DELETE FROM users;")
    suspend fun deleteAllUser()
    @Query("SELECT * FROM users WHERE user_name = :userName;")
    suspend fun queryUserByUserName(userName:String):User?
    @Query("SELECT * FROM users WHERE user_name=:userName AND hash_password=:pwd;")
    suspend fun login(userName: String,pwd:String):User?
}