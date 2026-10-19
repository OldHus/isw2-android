package com.svape.unal.ingesoftii.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.BuildConfig
import com.svape.unal.ingesoftii.presentation.account.AccountViewModel
import com.svape.unal.ingesoftii.presentation.account.UserProfile
import com.svape.unal.ingesoftii.ui.components.BrandMark
import com.svape.unal.ingesoftii.ui.components.FeatureCard
import com.svape.unal.ingesoftii.ui.components.HeroCard
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.components.ListEntry
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.components.SegmentedList
import com.svape.unal.ingesoftii.ui.components.ThemeToggleButton
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import com.svape.unal.ingesoftii.ui.theme.ThemeMode
import com.svape.unal.ingesoftii.ui.theme.ThemePreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeAction(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val accent: Color? = null,
)

data class HomeHero(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val actionLabel: String,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    accountViewModel: AccountViewModel,
    role: String,
    roleLabel: String,
    hero: HomeHero,
    quickActions: List<HomeAction>,
    moreActions: List<HomeAction>,
    onSignedOut: () -> Unit,
) {
    val profile by accountViewModel.profile.collectAsState()
    val isSigningOut by accountViewModel.isSigningOut.collectAsState()
    var showAccount by rememberSaveable { mutableStateOf(false) }
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = MaterialTheme.colorScheme
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 4 else 2

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(size = 30.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Ingesoft II", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    ThemeToggleButton()
                    IconButton(onClick = { showAccount = true }) {
                        ProfileAvatar(profile.displayName, profile.photoUrl, role, size = 34.dp)
                    }
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                StaggeredEnter(index = 0) {
                    Column {
                        Text(
                            todayLabel(),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${greeting()}, ${profile.firstName}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(10.dp))
                        RoleChip(roleLabel)
                    }
                }

                Spacer(Modifier.height(24.dp))

                StaggeredEnter(index = 1) {
                    HeroCard(
                        title = hero.title,
                        description = hero.description,
                        icon = hero.icon,
                        actionLabel = hero.actionLabel,
                        onClick = hero.onClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(24.dp))

                StaggeredEnter(index = 2) { SectionHeader("Accesos rápidos") }
                quickActions.chunked(columns).forEachIndexed { rowIndex, row ->
                    StaggeredEnter(index = 3 + rowIndex) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEach { action ->
                                FeatureCard(
                                    title = action.title,
                                    subtitle = action.subtitle,
                                    icon = action.icon,
                                    onClick = action.onClick,
                                    accent = action.accent ?: colors.primary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }

                if (moreActions.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    StaggeredEnter(index = 6) {
                        Column {
                            SectionHeader("Más herramientas")
                            SegmentedList(
                                entries = moreActions.map {
                                    ListEntry(
                                        it.key,
                                        it.title,
                                        it.icon,
                                        supporting = it.subtitle
                                    )
                                },
                                onClick = { entry ->
                                    moreActions.first { it.key == entry.key }.onClick()
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showAccount) {
        AccountSheet(
            profile = profile,
            role = role,
            roleLabel = roleLabel,
            onDismiss = { showAccount = false },
            onSignOutClick = { confirmSignOut = true },
        )
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { if (!isSigningOut) confirmSignOut = false },
            icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tendrás que volver a ingresar con tu correo institucional.") },
            confirmButton = {
                TextButton(
                    onClick = { accountViewModel.signOut(onSignedOut) },
                    enabled = !isSigningOut,
                ) {
                    if (isSigningOut) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cerrar sesión", color = colors.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }, enabled = !isSigningOut) {
                    Text("Cancelar")
                }
            },
        )
    }
}

private data class ThemeOption(val mode: ThemeMode, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSheet(
    profile: UserProfile,
    role: String,
    roleLabel: String,
    onDismiss: () -> Unit,
    onSignOutClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val prefs = remember { ThemePreferences.get(context) }
    val themeMode by prefs.themeMode.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(profile.displayName, profile.photoUrl, role, size = 76.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                profile.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
            if (profile.email.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            RoleChip(roleLabel)

            Spacer(Modifier.height(28.dp))

            Text(
                "Apariencia",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            val options = listOf(
                ThemeOption(ThemeMode.System, "Sistema", Icons.Outlined.PhoneAndroid),
                ThemeOption(ThemeMode.Light, "Claro", Icons.Outlined.LightMode),
                ThemeOption(ThemeMode.Dark, "Oscuro", Icons.Outlined.DarkMode),
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = themeMode == option.mode,
                        onClick = { prefs.setThemeMode(option.mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        icon = {
                            Icon(
                                option.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                    ) {
                        Text(option.label)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(8.dp))

            Surface(
                onClick = {
                    onDismiss()
                    onSignOutClick()
                },
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = colors.error
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Cerrar sesión",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Ingesoft II · versión ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun RoleChip(label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            Modifier.padding(PaddingValues(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

private fun todayLabel(): String {
    val formatted =
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-CO")).format(Date())
    return formatted.replaceFirstChar { it.titlecase() }
}