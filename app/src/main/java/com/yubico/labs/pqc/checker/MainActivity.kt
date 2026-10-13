package com.yubico.labs.pqc.checker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.yubico.labs.pqc.checker.ui.ApkCheckerView
import com.yubico.labs.pqc.checker.ui.ApkCheckerViewModel
import com.yubico.labs.pqc.checker.ui.PkiCheckerView
import com.yubico.labs.pqc.checker.ui.PkiCheckerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pkiVm: PkiCheckerViewModel by viewModels()
    private val apkVm: ApkCheckerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pkiStatus by pkiVm.status.collectAsState()

            val apkIsScanning by apkVm.isScanning.collectAsState()
            val apkStatus by apkVm.status.collectAsState()

            val pagerState = rememberPagerState(initialPage = 1) { 2 }

            PostQuantumCryptographyCheckerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        PqcTopBar(
                            pagerState
                        )
                    },
                ) { innerPadding ->
                    HorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        state = pagerState,
                    ) { page ->
                        when (page) {
                            0 -> PkiCheckerView(
                                innerPadding = innerPadding,
                                status = pkiStatus,
                                onCheckPki = { toSign ->
                                    pkiVm.checkPqc(toSign)
                                },
                            )

                            1 -> ApkCheckerView(
                                innerPadding = innerPadding,
                                isScanning = apkIsScanning,
                                apks = apkStatus,
                                onCheckApks = apkVm::checkApks,
                                launchApp = apkVm::launchApp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun PqcTopBarPreview() {
    PqcTopBar(
        PagerState { 2 },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PqcTopBar(
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pki),
                    contentDescription = null,
                    tint = if (pagerState.currentPage == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apk),
                    contentDescription = null,
                    tint = if (pagerState.currentPage == 1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
        }
    )
}
