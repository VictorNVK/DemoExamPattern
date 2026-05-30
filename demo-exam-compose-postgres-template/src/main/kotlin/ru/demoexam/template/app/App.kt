package ru.demoexam.template.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.demoexam.template.navigation.AppScreen
import ru.demoexam.template.ui.components.AppTopBar
import ru.demoexam.template.ui.components.MessageDialog
import ru.demoexam.template.ui.screens.LoginScreen
import ru.demoexam.template.ui.screens.OrderListScreen
import ru.demoexam.template.ui.screens.ProductEditorScreen
import ru.demoexam.template.ui.screens.ProductListScreen
import ru.demoexam.template.ui.theme.AppTheme

fun launchTemplateApplication() = application {
    val appState = remember { AppState() }
    val windowState = rememberWindowState(width = 1280.dp, height = 800.dp)
    val iconPainter = painterResource("assets/icon.png")

    LaunchedEffect(Unit) {
        appState.initialize()
    }

    DisposableEffect(Unit) {
        onDispose {
            appState.shutdown()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = appState.windowTitle,
        state = windowState,
        icon = iconPainter,
    ) {
        AppTheme {
            appState.uiMessage?.let { message ->
                MessageDialog(
                    message = message,
                    onDismiss = appState::dismissMessage,
                )
            }

            when {
                appState.startupError != null -> {
                    StartupErrorScreen(
                        message = appState.startupError.orEmpty(),
                        onRetry = appState::retryInitialization,
                    )
                }

                !appState.isInitialized -> {
                    LoadingScreen()
                }

                else -> {
                    Scaffold(
                        topBar = {
                            if (appState.currentScreen != AppScreen.Login) {
                                AppTopBar(
                                    title = appState.screenTitle,
                                    session = appState.currentSession,
                                    canGoBack = appState.canGoBack,
                                    onBack = appState::goBack,
                                    onExit = appState::returnToLogin,
                                )
                            }
                        },
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(paddingValues)
                                .padding(24.dp),
                        ) {
                            when (appState.currentScreen) {
                                AppScreen.Login -> {
                                    LoginScreen(
                                        onLogin = appState::login,
                                        onGuest = appState::continueAsGuest,
                                    )
                                }

                                AppScreen.Products -> {
                                    ProductListScreen(
                                        session = appState.currentSession,
                                        products = appState.products,
                                        filter = appState.productFilter,
                                        onFilterChange = appState::updateProductFilter,
                                        onRefresh = appState::refreshProducts,
                                        onOpenOrders = appState::openOrders,
                                        onAddProduct = { appState.openProductEditor(productId = null) },
                                        onEditProduct = { appState.openProductEditor(it) },
                                        onDeleteProduct = appState::deleteProduct,
                                    )
                                }

                                AppScreen.Orders -> {
                                    OrderListScreen(
                                        orders = appState.orders,
                                    )
                                }

                                is AppScreen.ProductEditor -> {
                                    val payload = appState.editorPayload
                                    if (payload != null) {
                                        ProductEditorScreen(
                                            payload = payload,
                                            onSave = appState::saveProduct,
                                            onCancel = appState::goBack,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text("Подключение к Spring backend...")
        }
    }
}

@Composable
private fun StartupErrorScreen(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Не удалось запустить приложение.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

