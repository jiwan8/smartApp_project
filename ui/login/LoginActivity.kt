package com.mjw.smart.ui.login // ★ 중요: 본인 패키지 이름이 맞는지 꼭 확인하세요! (상단 1번 줄)

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast   //여기서부터
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider     //여기까지 추가됨
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.mjw.smart.MainActivity
import com.mjw.smart.R
import com.mjw.smart.SignUpActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()

        //Google Sign-In 옵션 설정,API Scope 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestScopes(
                Scope("https://www.googleapis.com/auth/calendar"),
                Scope("https://www.googleapis.com/auth/calendar.events")
            )
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        //토글 버튼 (Sign Up 클릭 시 회원가입 화면으로 이동)
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btnToggleSignup) {
                startActivity(Intent(this, SignUpActivity::class.java))
                overridePendingTransition(0, 0)
            }
        }

        //Google 로그인 버튼
        val btnGoogleSignIn = findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogleSignIn)
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        //이메일 로그인
        val loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        //버튼 클릭
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = findViewById<TextInputLayout>(R.id.inputEmail).editText?.text.toString()
            val password = findViewById<TextInputLayout>(R.id.inputPassword).editText?.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 일반 로그인 성공
                        startActivity(Intent(this, MainActivity::class.java))
                        Toast.makeText(this, "일반 로그인 시 구글 캘린더 및 소셜 기능이 제한됩니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("login_type", "email")
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = task.exception
                        when (exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                //계정 없음, 회원가입 유도
                                Toast.makeText(this, "존재하지 않는 계정입니다. 회원가입이 필요합니다.", Toast.LENGTH_SHORT).show()
                            }
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                //비밀번호 틀림
                                Toast.makeText(this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "로그인 실패: ${exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }
    //구글 로그인 처리
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    //구글 로그인 토큰 요청
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, account)
        } catch (e: ApiException) {  //구글 토큰 실패시
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //로그인 성공 시 MainActivity로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("login_type", "google")
                    startActivity(intent)
                    intent.putExtra("google_account", account)
                    startActivity(intent)
                    finish()
                } else {  //로그인 실패(파이어베이스 서버 연결 실패시)
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }

    }
}