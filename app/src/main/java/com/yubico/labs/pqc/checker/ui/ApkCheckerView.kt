package com.yubico.labs.pqc.checker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yubico.labs.pqc.checker.R

@Composable
@Preview(showBackground = true)
private fun ApkCheckerPreviewView() {
    ApkCheckerView(
        innerPadding = PaddingValues(4.dp),
        isScanning = false,
        apks = listOf(
            AppInfo("checker", "com.yubico.checker", listOf("asdf"), true, null)
        ),
        {}
    ) {}
}

@Composable
@Preview(showBackground = true, showSystemUi = false)
private fun ApkCheckerScanningPreviewWithErrorView() {
    ApkCheckerView(
        innerPadding = PaddingValues(4.dp),
        isScanning = true,
        apks = listOf(
            AppInfo("yolo", "Yolo", listOf("pqc"), true),
            AppInfo("yolo", "Yolo", listOf(), false),
            AppInfo("yolo", "Yolo", listOf("pqcalg"), false, "Error happened"),
            AppInfo("yolo", "Yolo", listOf("pqcalg"), false, "Error happened"),
        ),
        {}
    ) {}
}

@Composable
fun ApkCheckerView(
    innerPadding: PaddingValues,
    isScanning: Boolean,
    apks: List<AppInfo>,
    launchApp: (AppInfo) -> Unit,
    onCheckApks: () -> Unit,
) {
    var filter: String by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Box {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(130.dp)
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Image(
                        modifier = Modifier
                            .size(130.dp)
                            .clickable {
                                onCheckApks()
                            },
                        painter = painterResource(R.drawable.ic_apk),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        item {
            Button(
                enabled = !isScanning,
                modifier = Modifier
                    .padding(4.dp),
                onClick = onCheckApks
            ) {
                Text(stringResource(R.string.nav_check_apps_title))
            }
        }

        if (apks.isNotEmpty()) {
            item {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    value = filter,
                    label = { Text(text = "Filter Apks") },
                    trailingIcon = {
                        IconButton(
                            enabled = filter.isNotEmpty(),
                            onClick = {
                                filter = ""
                            }
                        ) { Text("❌") }
                    },
                    onValueChange = { filter = it }
                )
            }
        }

        val filteredItems = apks.filter { filter in it.appName || filter in it.packageName }

        if (apks.isNotEmpty() && filteredItems.isEmpty()) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(64.dp),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    text = "<nothing found>",
                )
            }
        } else {
            items(filteredItems) { appInfo ->
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            text = appInfo.appName,
                        )
                        if (appInfo.appName != appInfo.packageName) {
                            Text(
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                text = ".. package: ${appInfo.packageName}",
                            )
                        }
                        Text(
                            modifier = Modifier,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            text = ".. signed with ${appInfo.signingAlgorithms.joinToString(", ")}"
                        )

                        val extra = when (val error = appInfo.error) {
                            null -> {
                                val pqc = appInfo.wasSignedWithPqcAlgorithm
                                "${
                                    "${if (pqc) "P" else "Not p"}qc signed".wrap(
                                        if (pqc) "✅" else {
                                            "❌"
                                        }
                                    )
                                } "
                            }

                            else -> error.wrap("⚠️")
                        }

                        Text(
                            modifier = Modifier,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            text = ".. $extra"
                        )
                    }

                    IconButton(
                        onClick = { launchApp(appInfo) }
                    ) {
                        Icon(painterResource(R.drawable.ic_run_app), null)
                    }
                }
            }
        }
    }
}

private fun String.wrap(wrap: String) = "$wrap $this $wrap"
