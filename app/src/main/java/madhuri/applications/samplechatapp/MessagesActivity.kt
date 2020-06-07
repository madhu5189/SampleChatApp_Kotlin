package madhuri.applications.samplechatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_messages.*
import kotlinx.android.synthetic.main.layout_latest_message_row.view.*
import madhuri.applications.samplechatapp.data.ChatMessage
import madhuri.applications.samplechatapp.data.User

class MessagesActivity : AppCompatActivity() {

    companion object {
        lateinit var currentUser: User
    }

    var adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        context = this
        adapter.setOnItemClickListener { item, view ->
            val messageRow = item as LatestMessageRow
            val intent = Intent(view.context, ChatActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_KEY, messageRow.chatPartnerUser)
            startActivity(intent)
        }
        recycler_view_latest_message.adapter = adapter
        recycler_view_latest_message
            .addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        checkIfUserIsLoggedIn()
    }

    var latestMessageMap = HashMap<String, ChatMessage> ()

    private fun checkForLatestMessages() {
        val uid = FirebaseAuth.getInstance().uid
        val dbRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$uid")
        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[p0.key!!] = chatMessage
                refreshMessageList()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[p0.key!!] = chatMessage
                refreshMessageList()
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    private fun refreshMessageList(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it, context))
        }
    }

    private fun getCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val dbRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)!!
            }

        })
        checkForLatestMessages()
    }

    private fun checkIfUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            getCurrentUser()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_message_menu -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.log_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class LatestMessageRow(
        private val message: ChatMessage,
        private val context: Context
    ) : Item<GroupieViewHolder>() {
        var chatPartnerUser:User ?= null
        override fun getLayout(): Int {
            return R.layout.layout_latest_message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            val chatPartnerId: String = if (message.fromId == FirebaseAuth.getInstance().uid){
                message.toId
            } else {
                message.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent( object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    viewHolder.itemView.text_view_user_name_message.text = chatPartnerUser?.username
                    val media = chatPartnerUser?.imageUri
                    Glide.with(context)
                        .load(media)
                        .into(viewHolder.itemView.image_view_user_message)
                    viewHolder.itemView.text_view_text_message.text = message.text
                    viewHolder.itemView.image_view_user_message

                }

            })

        }

    }
}