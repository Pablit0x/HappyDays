package com.ps.dimensional_feels.presentation.screens.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ps.dimensional_feels.R
import com.ps.dimensional_feels.alarm.AlarmScheduler
import com.ps.dimensional_feels.presentation.components.DailyReminderAlarmCard
import com.ps.dimensional_feels.presentation.components.SettingsCardItem
import com.ps.dimensional_feels.presentation.components.TimePickerDialog
import com.ps.dimensional_feels.util.Constants
import com.ps.dimensional_feels.util.PreferencesManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    onSignOutClicked: () -> Unit,
    onClearDiaryClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onAlarmCanceled: () -> Unit,
    onAlarmScheduled: (LocalTime) -> Unit,
    onUpdateReminderStatusPrefs : (Boolean) -> Unit,
    onUpdateReminderTimePrefs : (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    isDailyReminderEnabled: Boolean,
    dailyReminderTime: LocalTime
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            mutableStateOf(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val formattedTime = remember(dailyReminderTime) {
        val localDateTime = LocalTime.of(dailyReminderTime.hour, dailyReminderTime.minute)
        DateTimeFormatter.ofPattern(Constants.TIME_PATTERN).format(localDateTime).uppercase()
    }

    Column(modifier = modifier) {

        Text(
            text = stringResource(id = R.string.reminder_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        DailyReminderAlarmCard(alarmTime = formattedTime,
            onDailyReminderSwitchChange = { isEnabled ->
                if(isEnabled){
                    if(hasNotificationPermission){
                        onUpdateReminderStatusPrefs(true)
                        onUpdateReminderTimePrefs(dailyReminderTime)
                        onAlarmScheduled(dailyReminderTime)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    onUpdateReminderStatusPrefs(false)
                    onAlarmCanceled()
                }
            },
            isDailyReminderEnabled = isDailyReminderEnabled,
            onClick = { if (isDailyReminderEnabled) showTimePickerDialog = true })

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.account_settings),
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(modifier = Modifier.height(8.dp))

        SettingsCardItem(
            optionText = stringResource(id = R.string.google_sign_out),
            optionIcon = Icons.Outlined.ExitToApp,
            onClick = onSignOutClicked
        )

        Spacer(modifier = Modifier.height(6.dp))

        SettingsCardItem(
            optionText = stringResource(id = R.string.clear_diary),
            optionIcon = Icons.Outlined.DeleteOutline,
            onClick = onClearDiaryClicked,
            backgroundColor = MaterialTheme.colorScheme.error,
            onBackgroundColor = MaterialTheme.colorScheme.onError
        )

        Spacer(modifier = Modifier.height(6.dp))

        SettingsCardItem(
            optionText = stringResource(id = R.string.delete_account),
            optionIcon = Icons.Outlined.Close,
            onClick = onDeleteAccountClicked,
            backgroundColor = MaterialTheme.colorScheme.error,
            onBackgroundColor = MaterialTheme.colorScheme.onError
        )
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = dailyReminderTime.hour, initialMinute = dailyReminderTime.minute
        )
        TimePickerDialog(onCancel = { showTimePickerDialog = false }, onConfirm = {
            val updatedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
            onUpdateReminderTimePrefs(updatedTime)
            onAlarmScheduled(updatedTime)
            showTimePickerDialog = false
        }) {
            TimePicker(state = timePickerState)
        }
    }
}