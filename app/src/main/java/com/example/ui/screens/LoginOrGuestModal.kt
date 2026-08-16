package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginOrGuestModal(
    onLoginSuccess: (String, String) -> Unit,
    onGuestSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("Android Developer") }
    var verificationCodeSent by remember { mutableStateOf(false) }
    var verificationCodeInput by remember { mutableStateOf("") }
    var expectedCode by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var showEmailLogin by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SleekBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                color = SleekSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekCardBorder),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Icon / Logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Welcome to AI Studio",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Sign in with your cloud account or continue as a guest to sync projects securely across devices.",
                        fontSize = 13.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!showEmailLogin) {
                        // Google Login
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/google"))
                                try { context.startActivity(intent) } catch (e: Exception) {}
                                onLoginSuccess("user@gmail.com", "Google User")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("google_login_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Google", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Microsoft Login
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/microsoft"))
                                try { context.startActivity(intent) } catch (e: Exception) {}
                                onLoginSuccess("user@outlook.com", "Microsoft User")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("microsoft_login_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A4EF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Microsoft", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Apple ID Login
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/apple"))
                                try { context.startActivity(intent) } catch (e: Exception) {}
                                onLoginSuccess("user@icloud.com", "Apple User")
                            },
                            modifier = Modifier.fillMaxWidth().testTag("apple_login_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Apple", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Email Login Option
                        OutlinedButton(
                            onClick = { showEmailLogin = true },
                            modifier = Modifier.fillMaxWidth().testTag("email_login_option_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SleekPrimary)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Email & Anti-Bot Code", color = SleekPrimary, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                        // Guest Account Button
                        TextButton(
                            onClick = onGuestSelected,
                            modifier = Modifier.fillMaxWidth().testTag("guest_mode_btn")
                        ) {
                            Icon(Icons.Default.PersonOutline, contentDescription = null, tint = SleekTextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue as Guest Account", color = SleekTextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        // Email + Anti-Bot Verification Flow
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekPrimary,
                                unfocusedBorderColor = SleekCardBorder,
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekPrimary,
                                unfocusedBorderColor = SleekCardBorder,
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary
                            )
                        )

                        if (!verificationCodeSent) {
                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank() && emailInput.contains("@")) {
                                        expectedCode = (100000..999999).random().toString()
                                        verificationCodeSent = true
                                        verificationError = null
                                    } else {
                                        verificationError = "Please enter a valid email address."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Anti-Bot Verification Code", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SleekPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Verification code sent to $emailInput",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Simulated Anti-Bot Code: [ $expectedCode ] (Copy & paste)",
                                        fontSize = 11.sp,
                                        color = SleekTextPrimary
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = verificationCodeInput,
                                onValueChange = { verificationCodeInput = it },
                                label = { Text("Enter 6-Digit Verification Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekCardBorder,
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary
                                )
                            )

                            Button(
                                onClick = {
                                    if (verificationCodeInput.trim() == expectedCode.trim()) {
                                        onLoginSuccess(emailInput, nameInput)
                                    } else {
                                        verificationError = "Incorrect verification code. Please check and try again."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Verify & Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (verificationError != null) {
                            Text(
                                text = verificationError!!,
                                fontSize = 11.sp,
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { showEmailLogin = false; verificationCodeSent = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to All Sign-In Options", color = SleekTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
