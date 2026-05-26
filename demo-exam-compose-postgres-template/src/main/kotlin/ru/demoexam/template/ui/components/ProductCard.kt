package ru.demoexam.template.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.util.ImageStorage
import ru.demoexam.template.util.toCurrencyText
import ru.demoexam.template.util.toPercentText
import java.math.BigDecimal

@Composable
fun ProductCard(
    product: ProductListItem,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val backgroundColor = when {
        product.stockQuantity == 0 -> Color(0xFFD6D6D6)
        product.discountPercent > BigDecimal("25") -> Color(0xFF23E1EF)
        else -> MaterialTheme.colorScheme.surface
    }

    val imageBitmap = remember(product.imagePath) {
        ImageStorage.loadStoredBitmap(product.imagePath)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable(enabled = canManage, onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(100.dp)
                    .clip(CardDefaults.shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource("assets/picture.svg"),
                        contentDescription = "Нет изображения",
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${product.name}  •  #${product.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Категория: ${product.categoryName}")
                Text("Производитель: ${product.manufacturerName}")
                Text("Поставщик: ${product.supplierName}")
                Text("Единица измерения: ${product.unitName}")
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Остаток: ${product.stockQuantity}")
                Text("Скидка: ${product.discountPercent.toPercentText()}")

                if (product.discountPercent > BigDecimal.ZERO) {
                    Text(
                        text = product.price.toCurrencyText(),
                        color = MaterialTheme.colorScheme.error,
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Text(
                        text = product.finalPrice.toCurrencyText(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = product.price.toCurrencyText(),
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (canManage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать",
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                            )
                        }
                    }
                }
            }
        }
    }
}
