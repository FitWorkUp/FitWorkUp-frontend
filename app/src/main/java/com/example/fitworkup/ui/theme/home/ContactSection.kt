package com.example.fitworkup.ui.theme.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fitworkup.ui.theme.FitWorkUpTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContactSection() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    
    // Basic validation states
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding() // Avoid keyboard covering the fields
    ) {
        Text(
            text = "Fale Conosco",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                if (nameError) nameError = it.isBlank()
            },
            label = { Text("Seu Nome") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (nameError) {
            Text(
                text = "O nome é obrigatório",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (emailError) emailError = it.isBlank()
            },
            label = { Text("Seu E-mail") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        if (emailError) {
            Text(
                text = "O e-mail é obrigatório",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { 
                message = it
                if (messageError) messageError = it.isBlank()
            },
            label = { Text("Como podemos te ajudar?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            isError = messageError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        if (messageError) {
            Text(
                text = "A mensagem é obrigatória",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                nameError = name.isBlank()
                emailError = email.isBlank()
                messageError = message.isBlank()

                if (!nameError && !emailError && !messageError) {
                    loading = true
                    scope.launch {
                        val ok = sendFormspree(name, email, message)
                        loading = false
                        if (ok) {
                            name = ""; email = ""; message = ""
                            Toast.makeText(context, "Obrigado! Sua mensagem foi enviada.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Erro ao enviar. Verifique sua conexão.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Enviando...")
            } else {
                Text("Enviar Mensagem")
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                openWhatsApp(context, "5511999999999") // Substitua pelo número real
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chamar no WhatsApp")
        }
    }
}

/**
 * Abre o WhatsApp com um número específico.
 */
private fun openWhatsApp(context: Context, number: String) {
    try {
        val url = "https://api.whatsapp.com/send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp não instalado", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Função placeholder para simular o envio via Formspree.
 * Substitua pela sua lógica real de rede futuramente.
 */
private suspend fun sendFormspree(name: String, email: String, message: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // Logs for demonstration (using parameters)
            println("Enviando mensagem de $name ($email): $message")
            // Simulação de delay de rede
            kotlinx.coroutines.delay(2000)
            // Aqui você implementaria a chamada real (Retrofit, Ktor, etc)
            true
        } catch (_: Exception) {
            false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactSectionPreview() {
    FitWorkUpTheme {
        ContactSection()
    }
}
