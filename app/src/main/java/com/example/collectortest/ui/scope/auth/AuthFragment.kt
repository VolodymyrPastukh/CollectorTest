package com.example.collectortest.ui.scope.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.collectortest.R
import com.example.collectortest.databinding.FragmentAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AuthFragment"
private const val RC_SIGN_IN = 9001

@AndroidEntryPoint
class AuthFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            } else {
                checkAllPermissions()
            }
        }



    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAuthBinding.inflate(inflater).apply {
            _binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){
        super.onViewCreated(view, savedInstanceState)
        binding.signInButton.setOnClickListener {
            signIn()
        }
        checkAllPermissions()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = Firebase.auth

        nextBtn.setOnClickListener {
            updateUI(auth.currentUser)
        }
    }

    private fun checkAllPermissions(){
        val permissions = arrayListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )
        permissions.forEach { checkPermissions(it) }
    }

    private fun checkPermissions(permission: String){
        val isGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if(isGranted) return
        requestPermissionLauncher.launch(permission)
    }

    override fun onStart() = with(binding) {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) logsTv.text = "Authorize by SignIn button below"
        else {
            signInButton.isEnabled = true
            nextBtn.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = with(binding){
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "firebaseAuthWithGoogle:" + account.id)
                Log.i(TAG, "firebaseAuthWithGoogle:" + account.displayName)
                Log.i(TAG, "firebaseAuthWithGoogle:" + account.email)
                logsTv.text = "Authentication success: " +
                        "\n UserId[${account.id}] " +
                        "\n Username[${account.displayName}] \n" +
                        " Username[${account.email}] \n" +
                        " Press next to continue work..."
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                logsTv.text = "Authentication failed ${e.message} | ${e.cause}"
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) = with(binding){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    nextBtn.isEnabled = true
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    logsTv.text = "Authentication failed ${task.exception}"
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToNavigationFragment())
        } else {
            Log.w(TAG, "Null user")
        }
    }

}