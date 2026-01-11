package com.planzy.app.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.planzy.app.R
import com.planzy.app.ui.theme.AmaranthPurple
import com.planzy.app.ui.theme.Lavender
import com.planzy.app.ui.theme.Raleway

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(id = R.string.search)
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = Raleway,
            color = Lavender,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        placeholder = { Text(
            text = placeholder,
            fontFamily = Raleway,
            color = Lavender,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        ) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(id = R.string.search),
                modifier = Modifier.height(30.dp),
                tint = Lavender
            )
        },
        trailingIcon = null,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Lavender,
            unfocusedBorderColor = Lavender,
            focusedContainerColor = AmaranthPurple,
            cursorColor = Lavender,
            focusedTextColor = Lavender,
            unfocusedTextColor = Lavender
        )
    )
}