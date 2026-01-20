package com.planzy.app.ui.screens.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.planzy.app.domain.model.Place
import com.planzy.app.ui.theme.*
import com.planzy.app.R

@Composable
fun PlaceDetailsCard(
    place: Place,
    onAddToVacation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AmaranthPurple),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MediumBluePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!place.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = place.photoUrl,
                                contentDescription = place.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Lavender
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        InfoRow(rememberVectorPainter(Icons.Default.LocationOn),
                            place.location.address
                        ) {
                            val uri = "geo:0,0?q=${android.net.Uri.encode(place.location.address)}".toUri()
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }

                        place.contact?.phone?.let { phone ->
                            if (phone.isNotBlank()) {
                                InfoRow(rememberVectorPainter(Icons.Default.Phone),
                                    phone
                                ) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
                                }
                            }
                        }

                        place.contact?.website?.let { website ->
                            if (website.isNotBlank()) {
                                InfoRow(
                                    rememberVectorPainter(Icons.Default.Language),
                                    stringResource(id = R.string.visit_website)
                                ) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, website.toUri()))
                                }
                            }
                        }

                        InfoRow(
                            painter = painterResource(id = R.drawable.ic_add),
                            text = stringResource(id = R.string.add_to_vacation),
                        ) {
                            onAddToVacation()
                        }
                    }
                }

                place.description?.let { desc ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = desc,
                        fontFamily = Raleway,
                        fontSize = 16.sp,
                        color = Lavender
                    )
                }
            }
        }
    }
}