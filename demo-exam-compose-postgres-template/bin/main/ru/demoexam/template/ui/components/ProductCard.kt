package ru.demoexam.template.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.util.toCurrencyText
import ru.demoexam.template.util.toPercentText
import java.math.BigDecimal

private val DiscountHighlightColor = Color(0xFF2E8B57)
private val OutOfStockHighlightColor = Color(0xFFADD8E6)

@Composable
fun ProductCard(
    product: ProductListItem,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val backgroundColor = when {
        product.stockQuantity == 0 -> OutOfStockHighlightColor
        product.discountPercent > BigDecimal("15") -> DiscountHighlightColor
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        product.stockQuantity == 0 -> Color(0xFF1A1A1A)
        product.discountPercent > BigDecimal("15") -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable(enabled = canManage, onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ProductImage(
                imagePath = product.imagePath,
                contentDescription = product.name,
                modifier = Modifier.width(150.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Артикул: ${product.article}",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.85f),
                )
                Text("Категория: ${product.categoryName}", color = contentColor)
                Text("Производитель: ${product.manufacturerName}", color = contentColor)
                Text("Поставщик: ${product.supplierName}", color = contentColor)
                Text("Единица измерения: ${product.unitName}", color = contentColor)
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier.width(160.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Остаток: ${product.stockQuantity}", color = contentColor)
                Text("Скидка: ${product.discountPercent.toPercentText()}", color = contentColor)

                if (product.discountPercent > BigDecimal.ZERO) {
                    Text(
                        text = product.price.toCurrencyText(),
                        color = MaterialTheme.colorScheme.error,
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Text(
                        text = product.finalPrice.toCurrencyText(),
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = product.price.toCurrencyText(),
                        color = contentColor,
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
