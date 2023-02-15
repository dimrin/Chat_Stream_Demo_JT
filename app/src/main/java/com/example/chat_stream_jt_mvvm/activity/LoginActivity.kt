package com.example.chat_stream_jt_mvvm.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chat_stream_jt_mvvm.R
import com.example.chat_stream_jt_mvvm.ui.theme.Chat_Stream_JT_MVVMTheme
import com.example.chat_stream_jt_mvvm.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvents()

        setContent {
            Chat_Stream_JT_MVVMTheme {
                LoginScreen()
            }
        }
    }


    @Composable
    fun LoginScreen() {
        var username by remember {
            mutableStateOf(TextFieldValue(""))
        }

        var showProgress by remember {
            mutableStateOf(false)
        }

        viewModel.loadingState.observe(this){uiLoginState ->
            showProgress = when(uiLoginState){
                is LoginViewModel.UILoadingState.Loading -> {
                    true
                }

                is LoginViewModel.UILoadingState.NotLoading -> {
                    false
                }
            }

        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 35.dp, end = 35.dp, top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {


            Image(
                painter = painterResource(id = R.drawable.chat_logo),
                contentDescription = "chat logo",
                modifier = Modifier
                    .height(120.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { newValue -> username = newValue },
                label = { Text(text = "Enter username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Button(
                onClick = {
                    viewModel.loginUser(
                        username = username.text,
                        token = getString(R.string.jwt_token)
                    )
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Login as User")
            }
            Button(
                onClick = { viewModel.loginUser(username.text) }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Login as Guest")
            }

            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    private fun subscribeToEvents() {

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginEvent.collect { event ->
                    when (event) {
                        is LoginViewModel.LogInEvent.ErrorInputTooShort -> {
                            showToast("Invalid! Enter more than 3 characters")
                        }

                        is LoginViewModel.LogInEvent.ErrorLogIn -> {
                            val errorMassage = event.error
                            showToast("Error: $errorMassage")
                        }

                        is LoginViewModel.LogInEvent.Success -> {
                            showToast("Login Successful!")
                            startActivity(Intent(this@LoginActivity, ChannelListActivity::class.java))
                            finish()
                        }

                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @Preview(showBackground = true)
    @Composable
    fun Preview() {
        LoginScreen()
    }
}


