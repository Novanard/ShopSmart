package fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.shopsmart.MainActivity
import com.example.shopsmart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        findViews(view)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }

        registerText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null) // Allows going back to LoginFragment
                .commit()
        }
    }

    private fun findViews(view: View) {
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        registerText = view.findViewById(R.id.registerText)
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                   onLoginSuccess()
                else
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun onLoginSuccess() {
        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
        // Redirect to CartFragment after successful login
        (activity as? MainActivity)?.loadFragment(CartFragment())
    }

}
