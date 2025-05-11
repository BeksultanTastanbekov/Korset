package com.example.korset.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.korset.R
import com.example.korset.ui.main.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // INITIALIZE FIREBASE AUTH
        auth = FirebaseAuth.getInstance()

        // Get the current user's email and display it
        val userEmailTextView = view.findViewById<TextView>(R.id.user_email)
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email != null) {
            userEmailTextView.text = currentUser.email
        } else {
            userEmailTextView.text = "Не удалось загрузить email"
        }

        // Set up the logout button
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Sign out from Firebase
            Log.d("ProfileFragment", "Signing out user")
            auth.signOut()

            // Log the user state after sign out
            Log.d("ProfileFragment", "User after sign out: ${auth.currentUser?.email ?: "null"}")

            // Show a toast message
            Toast.makeText(context, "Вы успешно вышли из аккаунта", Toast.LENGTH_SHORT).show()

            // Redirect to LoginActivity
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d("ProfileFragment", "Redirecting to LoginActivity")
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}