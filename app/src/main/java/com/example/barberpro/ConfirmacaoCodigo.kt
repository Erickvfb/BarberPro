package com.example.barberpro.ui.auth

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barberpro.R
import com.example.barberpro.data.api.ResendOtpRequest
import com.example.barberpro.data.api.RetrofitClient
import com.example.barberpro.data.api.VerifyOtpRequest
import com.example.barberpro.model.com.example.barberpro.adapter.OtpAdapter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ConfirmacaoCodigo : AppCompatActivity() {

    private lateinit var otpFields: List<EditText>
    private lateinit var btnVerify: MaterialButton
    private lateinit var txtResend: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtChangeEmail: TextView
    private lateinit var btnBack: ImageView

    private val authService = RetrofitClient.apiService
    private var timer: CountDownTimer? = null
    private val resendSeconds = 45L
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacao_codigo)

        initializeViews()
        setupListeners()
        startCountdown()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.backButton)
        btnVerify = findViewById(R.id.btnVerify)
        txtResend = findViewById(R.id.txtResend)
        txtEmail = findViewById(R.id.txtEmail)
        txtChangeEmail = findViewById(R.id.txtChangeEmail)

        otpFields = listOf(
            findViewById(R.id.otp1),
            findViewById(R.id.otp2),
            findViewById(R.id.otp3),
            findViewById(R.id.otp4),
            findViewById(R.id.otp5),
            findViewById(R.id.otp6)
        )

        //Obter email da intent
        userEmail = intent.getStringExtra("email") ?: "usuario@email.com"
        txtEmail.text = userEmail

        setupOtpInputs()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        txtChangeEmail.setOnClickListener {
            finish()
        }

        //Reenviar OTP
        txtResend.setOnClickListener {
            if (txtResend.isEnabled) {
                resendOtpCode()
            }
        }

        //Validar OTP
        btnVerify.setOnClickListener {
            val otp = getOtpCode()

            if (otp.length != 6) {
                Toast.makeText(this, "Digite o código completo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyOtpCode(otp)
        }
    }
    private fun resendOtpCode() {
        lifecycleScope.launch {
            btnVerify.isEnabled = false
            txtResend.isEnabled = false

            try {
                val request = ResendOtpRequest(email = userEmail)
                val response = authService.resendOtp(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("OTP", "Código reenviado para $userEmail")
                    Toast.makeText(
                        this@ConfirmacaoCodigo,
                        "Novo código enviado para seu e-mail",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpar campos OTP
                    otpFields.forEach { it.text.clear() }
                    otpFields[0].requestFocus()

                    // Reiniciar contador
                    startCountdown()
                } else {
                    val errorMsg = response.body()?.message ?: "Erro ao reenviar código"
                    Toast.makeText(this@ConfirmacaoCodigo, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("OTP_ERROR", errorMsg)
                }
            } catch (e: Exception) {
                Log.e("OTP_ERROR", "Erro ao reenviar: ${e.message}", e)
                Toast.makeText(
                    this@ConfirmacaoCodigo,
                    "Erro ao reenviar código: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                btnVerify.isEnabled = true
            }
        }
    }

    private fun verifyOtpCode(otp: String) {
        lifecycleScope.launch {
            btnVerify.isEnabled = false

            try {
                val request = VerifyOtpRequest(email = userEmail, otp = otp)
                val response = authService.verifyOtp(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("OTP", "Código verificado com sucesso!")

                    Toast.makeText(
                        this@ConfirmacaoCodigo,
                        "E-mail verificado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()

                    //Cancelar timer
                    timer?.cancel()

                    startActivity(
                        intent.putExtra("email_verified", true)
                    )
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: "Código inválido"
                    Toast.makeText(this@ConfirmacaoCodigo, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("OTP_ERROR", errorMsg)

                    //Limpar campo OTP para o usuário tentar novamente
                    otpFields.forEach { it.text.clear() }
                    otpFields[0].requestFocus()
                }
            } catch (e: Exception) {
                Log.e("OTP_ERROR", "Erro ao validar: ${e.message}", e)
                Toast.makeText(
                    this@ConfirmacaoCodigo,
                    "Erro ao validar código: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                btnVerify.isEnabled = true
            }
        }
    }

    private fun setupOtpInputs() {
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(OtpAdapter {
                if (editText.text.length == 1 && index < otpFields.lastIndex) {
                    otpFields[index + 1].requestFocus()
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    editText.text.isEmpty() &&
                    index > 0
                ) {
                    otpFields[index - 1].requestFocus()
                }
                false
            }
        }
    }

    private fun getOtpCode(): String {
        return otpFields.joinToString("") { it.text.toString() }
    }

    private fun startCountdown() {
        txtResend.isEnabled = false

        timer?.cancel()

        timer = object : CountDownTimer(resendSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                txtResend.text = "Reenviar código (00:${String.format("%02d", seconds)})"
            }

            override fun onFinish() {
                txtResend.text = "Reenviar código"
                txtResend.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}