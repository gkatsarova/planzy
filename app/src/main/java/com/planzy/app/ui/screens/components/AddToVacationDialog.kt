package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.planzy.app.domain.model.Vacation
import com.planzy.app.ui.theme.*
import com.planzy.app.R

@Composable
fun AddToVacationDialog(
    vacations: List<Vacation>,
    isLoading: Boolean,
    isCreating: Boolean,
    isAdding: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onDismiss: () -> Unit,
    onCreateVacation: (String) -> Unit,
    onSelectVacation: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            color = Lavender
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.vacations),
                        fontFamily = Raleway,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmericanBlue
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.close),
                            tint = AmericanBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = ErrorColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (successMessage != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = successMessage,
                            color = AmericanBlue,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MediumBluePurple,
                        contentColor = Lavender
                    ),
                    enabled = !isCreating && !isAdding
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_new_vacation),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.create_new_vacation))
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = AmericanBlue)

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AmaranthPurple)
                        }
                    }

                    vacations.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_vacations_yet),
                                color = AmericanBlue,
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(vacations) { vacation ->
                                VacationItem(
                                    vacation = vacation,
                                    onClick = { onSelectVacation(vacation.id) },
                                    enabled = !isAdding
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateVacationDialog(
            isCreating = isCreating,
            onDismiss = { showCreateDialog = false },
            onCreate = { title ->
                onCreateVacation(title)
                showCreateDialog = false
            }
        )
    }
}