package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
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
import com.example.ui.theme.SmartBottomNavBg
import com.example.ui.theme.SmartPillActive
import com.example.ui.theme.SmartPrimary
import com.example.ui.theme.SmartText
import com.example.ui.theme.SmartTextSubtle

enum class SmartNavTab {
    PROJECTS,
    PLUGINS,
    AI_AGENT
}

@Composable
fun SmartBottomNav(
    activeTab: SmartNavTab,
    onTabSelected: (SmartNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SmartBottomNavBg)
            .navigationBarsPadding()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Code,
            label = "Projects",
            isSelected = activeTab == SmartNavTab.PROJECTS,
            onClick = { onTabSelected(SmartNavTab.PROJECTS) },
            testTag = "bottom_nav_projects"
        )

        BottomNavItem(
            icon = Icons.Default.Extension,
            label = "Plugins",
            isSelected = activeTab == SmartNavTab.PLUGINS,
            onClick = { onTabSelected(SmartNavTab.PLUGINS) },
            testTag = "bottom_nav_plugins"
        )

        BottomNavItem(
            icon = Icons.Default.AutoAwesome,
            label = "AI Agent",
            isSelected = activeTab == SmartNavTab.AI_AGENT,
            onClick = { onTabSelected(SmartNavTab.AI_AGENT) },
            testTag = "bottom_nav_ai_agent"
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val pillBg = if (isSelected) SmartPillActive else Color.Transparent
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) SmartPrimary else SmartTextSubtle,
        label = "navTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(pillBg)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            color = if (isSelected) SmartText else SmartTextSubtle,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
