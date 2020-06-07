package madhuri.applications.samplechatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import madhuri.applications.samplechatapp.data.User
import java.util.*


class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        user_register_button.setOnClickListener {
            onRegisterClick()
        }
        already_have_account_text_view.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        user_image_view_register.setOnClickListener { onSelectPhotoClick() }
    }

    private fun onRegisterClick(){
        val email =user_email_edit_text_register.text.toString()
        val password = user_password_edit_text_register.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                saveImageToFirebase()
            }
            .addOnFailureListener {
                Log.d("Register", "Registration failed due to: ${it.message}")
                Toast.makeText(this, "Registration failed due to: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageToFirebase() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDB(it.toString())
                    Log.d("Register", "Image uploaded at: ${it.path}")
                }
            }
            .addOnFailureListener{

            }
    }

    private fun saveUserToFirebaseDB(imageUri: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, user_name_edit_text_register.text.toString(), imageUri)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register", "User added successfully")

                val intent = Intent(this, MessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Log.d("Register", "User not added: ${exception.message}")
            }
    }

    private fun onSelectPhotoClick(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data

                Glide.with(this)
                    .load(selectedPhotoUri)
                    .into(user_image_view_register)

        }
    }

}
