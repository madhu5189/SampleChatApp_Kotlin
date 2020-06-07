package madhuri.applications.samplechatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        user_login_button.setOnClickListener { onLoginClick() }
        back_to_register_text_view.setOnClickListener {
            finish()
        }
    }

    private fun onLoginClick(){
        val emailLogin = user_email_edit_text_login.text.toString()
        val passwordLogin = user_password_edit_text_login.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailLogin, passwordLogin)
            .addOnCompleteListener{
                if (!it.isSuccessful)
                    return@addOnCompleteListener
                val intent = Intent(this, MessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("Login", "Login failed due to: ${it.message}")
                Toast.makeText(this, "Login failed due to: ${it.message}", Toast.LENGTH_SHORT).show()

            }
    }
}