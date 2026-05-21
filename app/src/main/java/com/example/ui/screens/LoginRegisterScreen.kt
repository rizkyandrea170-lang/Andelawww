package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkshopViewModel

@Composable
fun LoginScreen(
    viewModel: WorkshopViewModel,
    onLoginSuccess: (UserEntity) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var nik by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var passwordVisible by remember { mutableStateFlowOf(false) }
    
    val loginError by viewModel.loginError.collectAsState()
    val registerStatus by viewModel.registerStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon / Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF3B4454))
                    .border(2.dp, ProfessionalAccent, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Workshop Logo",
                    tint = ProfessionalAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "WORKSHOP MR SYSTEM",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Material Request & Activity Tracker",
                fontSize = 13.sp,
                color = ProfessionalSubtle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Form card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Masuk ke Akun",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = nik,
                        onValueChange = { nik = it },
                        label = { Text("NIK (Nomor Induk Karyawan)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_nik_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, contentDescription = null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Error indicator
                    if (loginError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = loginError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (registerStatus != null) {
                        Surface(
                            color = Color(0xFF1B5E20),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = registerStatus ?: "",
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.login(nik, password, onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switch to Register
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Belum punya akun?", color = ProfessionalSubtle, fontSize = 13.sp)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Register Karyawan", color = ProfessionalAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Dummy profiles tip for reviewer testing
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 Mode Demo (Gunakan Akun Ini):", fontWeight = FontWeight.Bold, color = ColorApproved, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Mekanik: mech1 / mech1", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    Text("• PIC Foreman: foreman1 / foreman1", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    Text("• Logistik: log1 / log1", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    Text("• Admin Plant: plant1 / plant1", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    Text("• Admin All Akses: adminall / adminall", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    Text("• Supervisor Admin: admin / admin", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: WorkshopViewModel,
    onNavigateToLogin: () -> Unit
) {
    var nik by remember { mutableStateFlowOf("") }
    var nama by remember { mutableStateFlowOf("") }
    var jabatan by remember { mutableStateFlowOf("") }
    var departemen by remember { mutableStateFlowOf("") }
    var noHp by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var selectedRole by remember { mutableStateFlowOf("MEKANIK") } // "ADMIN", "PIC", "MEKANIK", "LOGISTIK", "ADMIN_PLANT"
    var isExpanded by remember { mutableStateFlowOf(false) }

    val registerStatus by viewModel.registerStatus.collectAsState()

    val roles = listOf(
        "MEKANIK" to "Mekanik Lapangan",
        "PIC" to "PIC / Foreman",
        "LOGISTIK" to "Logistik / Warehouse",
        "ADMIN_PLANT" to "Admin Plant",
        "ADMIN" to "Admin Utama / Supervisor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "REGISTRASI KARYAWAN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Isi data dengan lengkap & benar",
                fontSize = 13.sp,
                color = ProfessionalSubtle
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = nik,
                        onValueChange = { nik = it },
                        label = { Text("NIK (Nomor Induk Karyawan)") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_nik"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_nama"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = jabatan,
                        onValueChange = { jabatan = it },
                        label = { Text("Jabatan") },
                        placeholder = { Text("Contoh: Mechanic Senior / Helper") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_jabatan"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = departemen,
                        onValueChange = { departemen = it },
                        label = { Text("Departemen / Site") },
                        placeholder = { Text("Contoh: Workshop A / Site Batu") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_departemen"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = noHp,
                        onValueChange = { noHp = it },
                        label = { Text("No. Handphone / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password Akun") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("reg_password"),
                        singleLine = true
                    )

                    // Role Selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isExpanded,
                            onExpandedChange = { isExpanded = !isExpanded }
                        ) {
                            OutlinedTextField(
                                value = roles.find { it.first == selectedRole }?.second ?: selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Role Kerja") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false }
                            ) {
                                roles.forEach { (roleKey, roleLabel) ->
                                    DropdownMenuItem(
                                        text = { Text(roleLabel) },
                                        onClick = {
                                            selectedRole = roleKey
                                            isExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (registerStatus != null) {
                        Text(
                            text = registerStatus ?: "",
                            color = ColorApproved,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            if (nik.isNotEmpty() && nama.isNotEmpty() && password.isNotEmpty() && noHp.isNotEmpty()) {
                                val user = UserEntity(
                                    nik = nik,
                                    nama = nama,
                                    jabatan = jabatan,
                                    departemen = departemen,
                                    noHp = noHp,
                                    password = password,
                                    role = selectedRole,
                                    isApproved = false // must wait for admin
                                )
                                viewModel.register(user) {
                                    // reset status
                                    nik = ""
                                    nama = ""
                                    jabatan = ""
                                    departemen = ""
                                    noHp = ""
                                    password = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_submit"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("DAFTAR SEKARANG", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("Sudah punya akun? Login di sini", color = ProfessionalAccent)
            }
        }
    }
}

// Minimalist fallback for mutable state tracking
private fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
