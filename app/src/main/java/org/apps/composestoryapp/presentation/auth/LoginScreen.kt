package org.apps.composestoryapp.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import org.apps.composestoryapp.UiEvent
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.ui.theme.GreenTertiary
import org.apps.composestoryapp.ui.theme.GreyLight

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
){
    val uiState by authViewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is ViewState.Success) {
            showSuccessDialog = true
        }
    }

    LaunchedEffect(uiState.authState) {
        when (uiState.authState) {
            is ViewState.Success -> {
                showSuccessDialog = true
            }
            is ViewState.Error -> {
                val errorMessage = (uiState.authState as ViewState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                authViewModel.clearForm()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        LoginContent(
            uiState = uiState,
            onEmailChange = authViewModel::onEmailChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onVisibilityChange = authViewModel::togglePasswordVisibility,
            onBackRegister = {
                authViewModel::clearForm
                navController.navigate("register"){
                    popUpTo("login"){
                        inclusive = true
                    }
                }
            },
            onLoginClick = authViewModel::login
        )

        val isActionLoading = uiState.authState is ViewState.Loading

        if (isActionLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (showSuccessDialog) {
            val loginResult = (uiState.authState as ViewState.Success).data
            SuccessDialog(
                title = "Login Berhasil",
                message = "Selamat datang ${loginResult.name}",
                onDismiss = {
                    showSuccessDialog = false
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun LoginContent(
    uiState: AuthState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onVisibilityChange: () -> Unit,
    onBackRegister: () -> Unit,
    onLoginClick: () -> Unit
){
    val SoftDarkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF191A19),
            Color(0xFF1E3A1F)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftDarkGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 50.dp, end = 20.dp, bottom = 50.dp)
        ) {
            Text(
                text = "Sudah siap untuk berbagi cerita?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Masukan datamu dulu yaa",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(Color.White)
                .padding(start = 20.dp, top = 30.dp, end = 20.dp, bottom = 30.dp)
                .weight(1f),
        ) {

            TextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = {
                    Text(
                        text = "Enter your mail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.MailOutline,
                        contentDescription = null,
                        tint = GreenTertiary
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = GreyLight,
                    focusedContainerColor = GreyLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                )
            )
            uiState.emailError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(15.dp))

            TextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = {
                    Text(
                        text = "Enter your password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = GreenTertiary
                    )
                },
                visualTransformation =
                    if (uiState.passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    if (uiState.password.isNotEmpty()){
                        val image = if (uiState.passwordVisible)
                            Icons.Default.Visibility
                        else Icons.Default.VisibilityOff

                        IconButton(onClick = { onVisibilityChange() }) {
                            Icon(
                                imageVector  = image,
                                contentDescription = null,
                                tint = GreenTertiary
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = GreyLight,
                    focusedContainerColor = GreyLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                )
            )
            uiState.passwordError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenTertiary
                )
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Register",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = GreenTertiary,
                modifier = Modifier.clickable(onClick = onBackRegister)
            )
        }

    }
}

