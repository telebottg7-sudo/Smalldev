package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DistroStatus
import com.example.model.LinuxDistro
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCrust
import com.example.ui.theme.IdePrimary
import com.example.ui.theme.IdeSecondary
import com.example.ui.theme.IdeSuccess
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeSurfaceElevated
import com.example.ui.theme.IdeText
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextSubtle

@Composable
fun DistroSettingsView(
    distros: List<LinuxDistro>,
    activeDistro: LinuxDistro?,
    onSwitchDistro: (LinuxDistro) -> Unit,
    modifier: Modifier = Modifier
) {
    val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"
    val deviceModel = Build.MODEL

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IdeCrust)
            .padding(14.dp)
            .testTag("distro_settings_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = IdePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Linux PRoot Distro Environment",
                        color = IdeText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rootless Linux container subsystem for AI coding agents",
                        color = IdeTextSubtle,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Host & Hardware Specs Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = IdeSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IdeBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HARDWARE & SUBSYSTEM TELEMETRY",
                        color = IdeTextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CPU Architecture", color = IdeTextMuted, fontSize = 11.sp)
                            Text(arch, color = IdePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Host Device", color = IdeTextMuted, fontSize = 11.sp)
                            Text(deviceModel, color = IdeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("PRoot Version", color = IdeTextMuted, fontSize = 11.sp)
                            Text("v5.4.0 (arm64)", color = IdeSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Divider(color = IdeBorder, thickness = 0.5.dp)

                    Text(
                        text = "PRoot Launch Flags:\nproot -0 -r /distro/rootfs -b /dev -b /proc -b /sys -b /storage -w /root /bin/bash",
                        color = IdeTextSubtle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "AVAILABLE LINUX DISTRIBUTIONS",
                color = IdeTextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Distro Cards
        items(distros, key = { it.id }) { distro ->
            val isActive = distro.id == activeDistro?.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("distro_card_${distro.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) IdeSurfaceElevated else IdeSurface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) IdePrimary else IdeBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = distro.type.displayName,
                                color = IdeText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IdeCrust)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(distro.type.defaultPkgManager.uppercase(), color = IdePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isActive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(IdeSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(IdeSuccess)
                                )
                                Text("ACTIVE CONTAINER", color = IdeSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = distro.type.description,
                        color = IdeTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RootFS: ~${distro.type.rootfsSizeMb} MB",
                            color = IdeTextSubtle,
                            fontSize = 11.sp
                        )

                        if (!isActive) {
                            Button(
                                onClick = { onSwitchDistro(distro) },
                                colors = ButtonDefaults.buttonColors(containerColor = IdePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp).testTag("activate_distro_${distro.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF11111B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Activate Distro", color = Color(0xFF11111B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // AI Agent Guide in PRoot
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = IdeSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IdeSecondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = IdeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Why PRoot for AI Coding Agents?",
                            color = IdeSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "AI Coding Agents like Claude Code, Aider, and OpenDevin require full Linux userspace environments (glibc, Python 3.12, Node.js 20+, and git).\n\nPRoot uses ptrace to simulate a root Linux environment inside Android's unprivileged app sandbox, allowing seamless package installation without voiding warranty or needing root access.",
                            color = IdeTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
