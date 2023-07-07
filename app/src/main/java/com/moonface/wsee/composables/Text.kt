package com.moonface.wsee.composables

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.moonface.wsee.R

val fontFamily = FontFamily(
    Font(R.font.inter_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.inter_thin, weight = FontWeight.Thin),
    Font(R.font.inter_light, weight = FontWeight.Light),
    Font(R.font.inter_regular, weight = FontWeight.Normal),
    Font(R.font.inter_medium, weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
    Font(R.font.inter_bold, weight = FontWeight.Bold),
    Font(R.font.inter_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.inter_black, weight = FontWeight.Black)
)

@Composable
fun Text(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    return androidx.compose.material3.Text(
        text = text,
        fontFamily = fontFamily,
        color = color,
        style = style
    )
}