package com.example.mobile_programming_project.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SafetyReportScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val scrollState = rememberScrollState()
    var reporterType by remember { mutableStateOf("Anonymous") }
    var incidentType by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val incidentOptions = listOf(
        "Suspicious Activity",
        "Theft",
        "Harassment",
        "Vandalism",
        "Assault",
        "Other"
    )

    val locationOptions = listOf(
        "Library",
        "Parking Lot",
        "Dorms",
        "Cafeteria",
        "Gym",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Campus Safety and Crime Reporting",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            "Help keep our campus safe. Reports can be anonymous or identified.",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Reporter type
        Text("Reporter Type", fontSize = 18.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            RadioButton(
                selected = reporterType == "Anonymous",
                onClick = { reporterType = "Anonymous" }
            )
            Text("Anonymous")

            Spacer(modifier = Modifier.width(24.dp))

            RadioButton(
                selected = reporterType == "Identified",
                onClick = { reporterType = "Identified" }
            )
            Text("Identified")
        }

        Spacer(Modifier.height(16.dp))

        // Incident type
        Text("Incident Type", fontSize = 18.sp)
        DropdownMenuBox(
            selectedOption = incidentType,
            options = incidentOptions,
            label = "Select incident type",
            onOptionSelected = { incidentType = it }
        )

        Spacer(Modifier.height(16.dp))

        // Date and time (text field placeholder)
        Text("Date and Time", fontSize = 18.sp)
        OutlinedTextField(
            value = dateTime,
            onValueChange = { dateTime = it },
            placeholder = { Text("Select date and time") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Location
        Text("Location", fontSize = 18.sp)
        DropdownMenuBox(
            selectedOption = location,
            options = locationOptions,
            label = "Select location",
            onOptionSelected = { location = it }
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text("Description", fontSize = 18.sp)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Describe what happened...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            maxLines = 5
        )

        Spacer(Modifier.height(24.dp))

        // Submit button
        Button(
            onClick = {
                onSubmit(reporterType, incidentType, dateTime, location, description)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Submit Report", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DropdownMenuBox(
    selectedOption: String,
    options: List<String>,
    label: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}