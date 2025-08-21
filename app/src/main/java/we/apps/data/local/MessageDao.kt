package we.apps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import we.apps.chat.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): List<Message>

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}

