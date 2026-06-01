package com.fitworkup.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Cores ──────────────────────────────────────────────────────────────────
private val FitRed: Color     = Color(0xFFE0271A)
private val ErrorRed: Color   = Color(0xFFC0392B)
private val DividerColor: Color = Color(0xFFDDDDDD)

/**
 * LoginScreen
 *
 * Alterna entre modo Login e Cadastro via [isLoginMode].
 * [onNavigateToHome] chamado após autenticação bem-sucedida.
 *
 * TODO: conectar ao LoginViewModel quando implementar autenticação real.
 */
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // ── Estado local ─────────────────────────────────────────────────────
    var isLoginMode by remember { mutableStateOf<Boolean>(true) }
    var nome        by remember { mutableStateOf<String>("") }
    var email       by remember { mutableStateOf<String>("") }
    var senha       by remember { mutableStateOf<String>("") }
    var senhaVisivel by remember { mutableStateOf<Boolean>(false) }
    var isLoading   by remember { mutableStateOf<Boolean>(false) }
    var erro        by remember { mutableStateOf<String?>(null) }

    // Limpa erro ao digitar
    LaunchedEffect(email, senha, nome) { erro = null }

    // ── Validação simples ────────────────────────────────────────────────
    fun validar(): Boolean {
        if (!isLoginMode && nome.isBlank()) {
            erro = "Informe seu nome"; return false
        }
        if (!email.contains("@")) {
            erro = "E-mail inválido"; return false
        }
        if (senha.length < 6) {
            erro = "Senha deve ter ao menos 6 caracteres"; return false
        }
        return true
    }

    fun onConfirmar() {
        focusManager.clearFocus()
        if (!validar()) return
        isLoading = true
        // TODO: chamar LoginViewModel.login() ou .cadastrar()
        // Por enquanto navega direto (mock)
        onNavigateToHome()
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp)
            .imePadding(),          // sobe layout com teclado aberto
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        // Logo compacto
        Text(
            text = "FitWorkUp",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = FitRed
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isLoginMode) "Entre na sua conta" else "Crie sua conta",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // ── Seletor Login / Cadastro ─────────────────────────────────────
        ModeToggle(
            isLoginMode = isLoginMode,
            onToggle = {
                isLoginMode = it
                erro = null
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Campos ───────────────────────────────────────────────────────

        // Campo nome (só no cadastro)
        if (!isLoginMode) {
            FitTextField(
                value = nome,
                onValueChange = { nome = it },
                label = "Nome",
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Campo e-mail
        FitTextField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail",
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo senha
        FitTextField(
            value = senha,
            onValueChange = { senha = it },
            label = "Senha",
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                    )
                }
            },
            visualTransformation = if (senhaVisivel) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onConfirmar() })
        )

        // Mensagem de erro
        if (erro != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = erro!!,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Esqueci a senha (só no login)
        if (isLoginMode) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Esqueci minha senha",
                fontSize = 12.sp,
                color = FitRed,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO: navegar para recuperação */ }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Botão principal ───────────────────────────────────────────────
        Button(
            onClick = { onConfirmar() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FitRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isLoginMode) "Entrar" else "Criar conta",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Divisor "ou" ──────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
            Text(
                text = "  ou  ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Botão Google ──────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                // TODO: implementar Google Sign-In
                // val signInIntent = googleSignInClient.signInIntent
                // launcher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
        ) {
            // Placeholder Google icon (substituir por asset real)
            Text("G", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = FitRed)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continuar com Google",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Rodapé: alternar modo ─────────────────────────────────────────
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isLoginMode) "Não tem conta?" else "Já tem conta?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            Text(
                text = if (isLoginMode) "Criar agora" else "Entrar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FitRed,
                modifier = Modifier.clickable { isLoginMode = !isLoginMode; erro = null }
            )
        }
    }
}

// ── Componentes reutilizáveis ────────────────────────────────────────────────

@Composable
private fun ModeToggle(
    isLoginMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        listOf(true, false).forEach { loginMode ->
            val label = if (loginMode) "Entrar" else "Cadastrar"
            val selected = isLoginMode == loginMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.background
                        else Color.Transparent
                    )
                    .clickable { onToggle(loginMode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) FitRed
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitRed,
            focusedLabelColor = FitRed,
            cursorColor = FitRed
        )
    )
}
