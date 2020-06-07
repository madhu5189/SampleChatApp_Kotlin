package madhuri.applications.samplechatapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.layout_from_chat_row.view.*
import kotlinx.android.synthetic.main.layout_to_chat_row.view.*
import madhuri.applications.samplechatapp.data.ChatMessage
import madhuri.applications.samplechatapp.data.User

class ChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var context: Context
    lateinit var toUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser.username

        context = this

        recycler_view_message_chat.adapter = adapter
        recycler_view_message_chat.layoutManager = LinearLayoutManager(this)

        getMessagesFromFirebase()
        button_send_chat.setOnClickListener {
            onSendClick()
        }
    }

    private fun getMessagesFromFirebase(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser.uid
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId")

        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d("ChatActivity", "chat message saved to db: ${chatMessage.text}")

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = MessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser, context))
                        Log.d("ChatActivity", "current user: ${currentUser.username}")
                    }
                    else {
                        adapter.add(ChatToItem(chatMessage.text, toUser, context))
                        Log.d("ChatActivity", "current user: ${toUser.username}")
                    }
                }
                recycler_view_message_chat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun onSendClick() {
        val message = edit_text_message_chat.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = toUser.uid
//        val dbReference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val dbReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$toId/$fromId").push()


        if (fromId == null) return
        val chatMessage = ChatMessage(dbReference.key!!, message, fromId, toId, System.currentTimeMillis()/1000)
        dbReference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatActivity", "chat message saved to db")
                edit_text_message_chat.text.clear()
                recycler_view_message_chat.scrollToPosition(adapter.itemCount-1)
            }
        toReference.setValue(chatMessage)

        val latestMessageFromRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageFromRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}



class ChatFromItem(private val message: String, private val user: User, private val context: Context): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.layout_from_chat_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.from_chat_text_view.text = message
        val media = user.imageUri
        Glide.with(context)
            .load(media)
            .into(viewHolder.itemView.from_chat_image_view)
    }

}
class ChatToItem(private val message: String, private val user: User, private val context: Context): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.layout_to_chat_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.to_chat_text_view.text = message
        val media = user.imageUri
        Glide.with(context)
            .load(media)
            .into(viewHolder.itemView.to_chat_image_view)
    }

}