package com.fitworkup.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var isLoginMode by remember { mutableStateOf(true) }

    var loginIdentifier by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    var erroLoginIdentifier by remember { mutableStateOf(false) }
    var erroNome by remember { mutableStateOf(false) }
    var erroUsername by remember { mutableStateOf(false) }
    var erroEmail by remember { mutableStateOf(false) }
    var erroSenha by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loginIdentifier, email, senha, nome, username) {
        erroLoginIdentifier = false
        erroNome = false
        erroUsername = false
        erroEmail = false
        erroSenha = false
        mensagemErro = null
        viewModel.clearError()
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateToHome()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { mensagemErro = it }
    }

    fun validar(): Boolean {
        if (isLoginMode) {
            if (loginIdentifier.isBlank()) {
                erroLoginIdentifier = true
                mensagemErro = "Digite seu e-mail ou nome de usuário."
                return false
            }
        } else {
            if (nome.isBlank()) {
                erroNome = true
                mensagemErro = "Por favor, digite seu nome."
                return false
            }
            if (username.isBlank() || username.contains(" ")) {
                erroUsername = true
                mensagemErro = "O nickname não pode ter espaços ou estar vazio."
                return false
            }
            if (!email.contains("@") || email.isBlank()) {
                erroEmail = true
                mensagemErro = "Digite um e-mail válido para o cadastro."
                return false
            }
        }

        if (senha.length < 6) {
            erroSenha = true
            mensagemErro = "A senha deve conter no mínimo 6 caracteres."
            return false
        }
        return true
    }

    fun onConfirmar() {
        focusManager.clearFocus()
        if (!validar()) return
        if (isLoginMode) {
            viewModel.login(loginIdentifier, senha)
        } else {
            viewModel.register(username, email, senha)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FitWorkUp",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isLoginMode) "Entre e continue sua evolução" else "Crie sua conta de atleta",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ModeToggle(
            isLoginMode = isLoginMode,
            onToggle = {
                isLoginMode = it
                mensagemErro = null
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoginMode) {
            FitTextField(
                value = loginIdentifier,
                onValueChange = { loginIdentifier = it.trim() },
                label = "E-mail ou @nickname",
                isError = erroLoginIdentifier,
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
        } else {
            FitTextField(
                value = nome,
                onValueChange = { nome = it },
                label = "Nome",
                isError = erroNome,
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(14.dp))

            FitTextField(
                value = username,
                onValueChange = { username = it.trim().lowercase() },
                label = "Nickname único (ex: atleta_fit)",
                isError = erroUsername,
                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(14.dp))

            FitTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = "E-mail",
                isError = erroEmail,
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        FitTextField(
            value = senha,
            onValueChange = { senha = it },
            label = "Senha",
            isError = erroSenha,
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirmar() })
        )

        mensagemErro?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        if (isLoginMode) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Esqueci minha senha",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onForgotPassword)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onConfirmar() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text(text = if (isLoginMode) "Entrar" else "Criar Conta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
            Text(text = "  ou  ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { /* TODO: Firebase/Google Auth */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Continuar com o Google", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = if (isLoginMode) "Novo no FitWorkUp?" else "Já possui uma conta?", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Text(
                text = if (isLoginMode) "Cadastre-se" else "Faça Login",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isLoginMode = !isLoginMode }
            )
        }
    }
}
