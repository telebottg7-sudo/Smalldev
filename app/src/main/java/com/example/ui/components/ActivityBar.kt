package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeCrust
import com.example.ui.theme.IdePrimary
import com.example.ui.theme.IdeSecondary
import com.example.ui.theme.IdeText
import com.example.ui.theme.IdeTextMuted
import com.example.viewmodel.IdeActiveTab

data class ActivityItem(
    val tab: IdeActiveTab,
    val icon: ImageVector,
    val label: String,
    val testTag: String
)

@Composable
fun ActivityBar(
    activeTab: IdeActiveTab,
    onTabSelected: (IdeActiveTab) -> Unit,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        ActivityItem(IdeActiveTab.PROJECTS, Icons.Default.Home, "Projects", "activity_tab_projects"),
        ActivityItem(IdeActiveTab.EXPLORER, Icons.Default.Folder, "Files", "activity_tab_explorer"),
        ActivityItem(IdeActiveTab.EDITOR, Icons.Default.Code, "Editor", "activity_tab_editor"),
        ActivityItem(IdeActiveTab.TERMINAL, Icons.Default.Terminal, "Terminal", "activity_tab_terminal"),
        ActivityItem(IdeActiveTab.AI_AGENT, Icons.Default.AutoAwesome, "AI Agent", "activity_tab_ai_agent"),
        ActivityItem(IdeActiveTab.PACKAGES, Icons.Default.Inventory2, "Packages", "activity_tab_packages"),
        ActivityItem(IdeActiveTab.PREVIEW, Icons.Default.Language, "Preview", "activity_tab_preview"),
        ActivityItem(IdeActiveTab.SETTINGS, Icons.Default.Settings, "Distro", "activity_tab_settings")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(IdeCrust)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (activeTab != IdeActiveTab.PROJECTS) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("toggle_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Workspace Sidebar",
                    tint = IdeText,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        items.forEach { item ->
            val isSelected = activeTab == item.tab
            val tintColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (item.tab == IdeActiveTab.AI_AGENT) IdeSecondary else IdePrimary
                } else IdeTextMuted,
                label = "tabTint"
            )
            val bgColor = if (isSelected) IdePrimary.copy(alpha = 0.15f) else Color.Transparent

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(item.tab) }
                    .padding(horizontal = 9.dp, vertical = 7.dp)
                    .testTag(item.testTag),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tintColor,
                        modifier = Modifier.size(17.dp)
                    )
                    if (isSelected) {
                        Text(
                            text = item.label,
                            color = tintColor,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
