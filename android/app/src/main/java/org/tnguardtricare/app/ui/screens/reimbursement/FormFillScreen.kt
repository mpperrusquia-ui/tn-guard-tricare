package org.tnguardtricare.app.ui.screens.reimbursement

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.model.AppContent
import org.tnguardtricare.app.pdf.PdfFormFiller
import org.tnguardtricare.app.ui.components.InfoCard
import org.tnguardtricare.app.ui.theme.AppRadius
import org.tnguardtricare.app.ui.theme.AppSpacing
import org.tnguardtricare.app.ui.theme.LocalExtendedColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFillScreen(app: TNGuardTricareApplication, formId: String, navController: NavHostController) {
    val content by app.contentRepository.content.collectAsState()
    val c = content ?: return
    val form = c.tnReimbursement.forms.firstOrNull { it.id == formId } ?: return
    val context = LocalContext.current

    val values = remember(formId) { mutableStateMapOf<String, String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingShare by remember { mutableStateOf(false) }

    remember(formId) {
        for (field in form.fields) {
            var value = app.formDraftStore.value(formId, field)
            if (field.type == AppContent.FieldType.DATE && value.isEmpty()) {
                value = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                app.formDraftStore.setValue(value, formId, field)
            }
            values[field.id] = value
        }
        true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingShare) {
                app.progressStore.setFormComplete(form.id, true)
                pendingShare = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun generateAndShare() {
        errorMessage = null
        try {
            val filler = PdfFormFiller(context)
            val file = filler.filledPdf(form, values)
            val uri = FileProvider.getUriForFile(context, "org.tnguardtricare.app.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pendingShare = true
            context.startActivity(Intent.createChooser(intent, "Share ${form.title}"))
        } catch (e: Exception) {
            android.util.Log.e("FormFillScreen", "PDF generation failed for form ${form.id}", e)
            errorMessage = "Couldn't generate the PDF. Try again."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(form.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            if (form.isPlaceholderTemplate == true) {
                Surface(
                    color = LocalExtendedColors.current.warning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(AppRadius.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.md)) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = LocalExtendedColors.current.warning)
                        Text(
                            "This app doesn't yet bundle the official tn.gov PDF for this form, so it generates a labeled summary with the same fields. Cross-check with the official form before sending.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppSpacing.sm),
                        )
                    }
                }
            }

            Text(form.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(
                shape = RoundedCornerShape(AppRadius.md),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LocalExtendedColors.current.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    for (field in form.fields) {
                        FormFieldInput(
                            field = field,
                            value = values[field.id] ?: "",
                            onValueChange = { newValue ->
                                values[field.id] = newValue
                                app.formDraftStore.setValue(newValue, formId, field)
                            },
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = { generateAndShare() }, modifier = Modifier.fillMaxWidth()) {
                Text("Preview & Share PDF")
            }

            InfoCard(Icons.Filled.Email, "Send the completed package to", c.tnReimbursement.email)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormFieldInput(
    field: AppContent.FormField,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        Row {
            Text(field.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (field.sensitive == true) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Sensitive",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = AppSpacing.xs).size(14.dp),
                )
            }
        }
        when (field.type) {
            AppContent.FieldType.CHECKBOX -> {
                Checkbox(
                    checked = value == "true",
                    onCheckedChange = { onValueChange(if (it) "true" else "false") },
                )
            }
            AppContent.FieldType.CHOICE -> {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(field.placeholder ?: "Select…") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        (field.options ?: emptyList()).forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                onValueChange(option)
                                expanded = false
                            })
                        }
                    }
                }
            }
            AppContent.FieldType.DATE -> {
                DateFieldInput(value = value, onValueChange = onValueChange)
            }
            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(field.placeholder ?: field.label) },
                    singleLine = true,
                    textStyle = if (field.type == AppContent.FieldType.SIGNATURE_NAME) {
                        TextStyle(fontStyle = FontStyle.Italic)
                    } else TextStyle.Default,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (field.type) {
                            AppContent.FieldType.NUMBER, AppContent.FieldType.SSN,
                            AppContent.FieldType.BANK_ROUTING, AppContent.FieldType.BANK_ACCOUNT -> KeyboardType.Number
                            else -> KeyboardType.Text
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DateFieldInput(value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val displayText = try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).format(displayFormatter)
    } catch (e: Exception) {
        value
    }
    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val current = try {
                    LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    LocalDate.now()
                }
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val picked = LocalDate.of(year, month + 1, dayOfMonth)
                        onValueChange(picked.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    },
                    current.year,
                    current.monthValue - 1,
                    current.dayOfMonth,
                ).show()
            },
    )
}
