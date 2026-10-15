package com.yubico.labs.pqc.checker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yubico.labs.pqc.checker.R

@Composable
@Preview
private fun KeyStoreCheckerPreviewView() {
    KeyStoreCheckerView(
        innerPadding = PaddingValues(4.dp),
        status = "Previewing",
    ) {}
}

@Composable
fun KeyStoreCheckerView(
    innerPadding: PaddingValues,
    status: String,
    onCheck: (toSign: String) -> Unit,
) {
    val toSign = "Hello World!"

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Row {
                Image(
                    modifier = Modifier
                        .size(130.dp)
                        .clickable {
                            onCheck(toSign)
                        },
                    painter = painterResource(R.drawable.ic_key_store),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        item {
            Button(
                modifier = Modifier
                    .padding(4.dp),
                onClick = { onCheck(toSign) }
            ) {
                Text(stringResource(R.string.nav_check_keystore_title))
            }
        }

        items(status.lines()) { line ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                text = line
            )
        }
    }
}
