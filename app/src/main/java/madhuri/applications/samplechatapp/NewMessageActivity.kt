package madhuri.applications.samplechatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.layout_new_message_row.view.*
import madhuri.applications.samplechatapp.data.User

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title= "Select User"

        new_message_recycler_view.layoutManager = LinearLayoutManager(this)

        getUsers()

    }

    companion object {
        const val USER_KEY = "USER_KEY"
    }
    private fun getUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                p0.children.forEach{
                    val user = it.getValue(User::class.java)
                    if (user != null)
                        adapter.add(UserItem(user, this@NewMessageActivity))
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }
                new_message_recycler_view.adapter = adapter
            }

        })
    }
}

class UserItem(val user: User, private val context: Context): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.layout_new_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.user_name_text_view_row.text = user.username
        val media = user.imageUri
        Glide.with(context)
            .load(media)
            .into(viewHolder.itemView.user_image_view_row)
    }

}