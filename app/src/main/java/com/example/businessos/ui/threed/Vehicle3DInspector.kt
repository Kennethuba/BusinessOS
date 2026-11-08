package com.example.businessos.ui.threed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.ui.theme.*

enum class VehicleZone(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val desc: String,
    val swahiliName: String,
    val commonRepairs: List<String>
) {
    BODYWORK_PANEL(
        title = "Bodywork & Panels",
        icon = Icons.Filled.Build,
        color = Color(0xFFEF4444),
        desc = "Panel beating, bumper repair, dents & plastic welding",
        swahiliName = "Bodi, Dents & Bumper",
        commonRepairs = listOf("Bumper Dent & Plastic Welding", "Panel Beating Fender/Door", "Scratch Removal", "Rust Repair")
    ),
    PAINT_JOB(
        title = "Paint & Respray",
        icon = Icons.Filled.FormatPaint,
        color = Color(0xFFF59E0B),
        desc = "Full respray, touchups, metallic paint & buffing",
        swahiliName = "Kupulizia Rangi & Buffing",
        commonRepairs = listOf("Full Bumper Respray", "Panel Touchup", "Scratch Buffing & Polish", "Full Vehicle Spray")
    ),
    INTERIOR_LEATHER(
        title = "Interior & Upholstery",
        icon = Icons.Filled.AirlineSeatReclineNormal,
        color = Color(0xFF8B5CF6),
        desc = "Torn leather/fabric seats, sagging roof lining & dashboard",
        swahiliName = "Viti vya Ngozi & Dari",
        commonRepairs = listOf("Torn Leather Seat Stitching", "Roof Lining Repair (Dari)", "Dashboard Crack Restoration", "Full Seat Reupholstery")
    ),
    TYRES_WHEELS(
        title = "Tyres, Rims & Wheels",
        icon = Icons.Filled.Album,
        color = Color(0xFF06B6D4),
        desc = "Tyre burst, puncture, rim bend straightening & alignment",
        swahiliName = "Matairi Burst & Rims",
        commonRepairs = listOf("Tyre Burst / Puncture Repair", "Rim Bend Straightening", "Wheel Alignment & Balancing", "New Tyre Replacement")
    ),
    ENGINE_COOLING(
        title = "Engine & Cooling",
        icon = Icons.Filled.ElectricBolt,
        color = Color(0xFF10B981),
        desc = "Overheating, radiator leak, oil service & head gasket",
        swahiliName = "Injini & Maji ya Radiator",
        commonRepairs = listOf("Radiator Leak Repair & Flush", "Engine Oil & Filter Service", "Clutch Plate Replacement", "Timing Belt")
    ),
    SUSPENSION_STEERING(
        title = "Suspension & Shocks",
        icon = Icons.Filled.DirectionsCar,
        color = Color(0xFF3B82F6),
        desc = "Shock absorbers, ball joints, bushings & tie rods",
        swahiliName = "Shock Absorbers & Bushing",
        commonRepairs = listOf("Front/Rear Shock Absorbers", "Tie Rod Ends & Rack Ends", "Control Arm Bushings", "Wheel Bearings")
    ),
    ELECTRICAL_BRAKES(
        title = "Brakes & Electrical",
        icon = Icons.Filled.FlashOn,
        color = Color(0xFFA855F7),
        desc = "Brake pads, disc skimming, alternator & battery drain",
        swahiliName = "Breki, Alternator & Umeme",
        commonRepairs = listOf("Front/Rear Brake Pads", "Brake Disc Skimming", "Alternator / Starter Rewind", "Battery Drainage")
    )
}

/**
 * 2D Practical Vehicle Defect & Job Scope Matrix
 * Allows mechanics to quickly inspect all major vehicle areas (Body, Paint, Torn Leather, Tyres, Engine, Electrical)
 */
@Composable
fun Vehicle3DInspector(
    modifier: Modifier = Modifier,
    onZoneSelected: (VehicleZone) -> Unit = {}
) {
    var selectedZone by remember { mutableStateOf(VehicleZone.BODYWORK_PANEL) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AmberAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ukaguzi wa Sehemu za Gari",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bodi, Rangi, Viti vya Ngozi, Matairi, Injini & Breki",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    color = selectedZone.color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectedZone.color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Grid of Vehicle Zones
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val zones = VehicleZone.values().toList()
                for (i in zones.indices step 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VehicleZoneCard(
                            zone = zones[i],
                            isSelected = selectedZone == zones[i],
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedZone = zones[i]
                                onZoneSelected(zones[i])
                            }
                        )
                        if (i + 1 < zones.size) {
                            VehicleZoneCard(
                                zone = zones[i + 1],
                                isSelected = selectedZone == zones[i + 1],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedZone = zones[i + 1]
                                    onZoneSelected(zones[i + 1])
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Details and Quick Repairs Banner for the selected zone
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = selectedZone.color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, selectedZone.color.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            selectedZone.icon,
                            contentDescription = null,
                            tint = selectedZone.color,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedZone.title} • ${selectedZone.swahiliName}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = selectedZone.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Matatizo ya Kawaida & Ufundi (Common Scope):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedZone.color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    selectedZone.commonRepairs.forEach { repair ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(selectedZone.color)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(text = repair, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onZoneSelected(selectedZone) },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedZone.color),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add ${selectedZone.title} Part/Cost", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleZoneCard(
    zone: VehicleZone,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) zone.color.copy(alpha = 0.22f) else DarkSurfaceElevated
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) zone.color else DarkBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                zone.icon,
                contentDescription = null,
                tint = if (isSelected) zone.color else TextMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = zone.title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = zone.swahiliName,
                    fontSize = 10.sp,
                    color = if (isSelected) zone.color else TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}
