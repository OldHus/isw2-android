package com.svape.unal.ingesoftii.presentation.posts

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink

private val URL_REGEX = Regex("""(https?://\S+|www\.\S+)""")
private val TRAILING_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?', ')', '"', '\'')

@Composable
fun PostText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onOverflowChange: (Boolean) -> Unit = {},
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val uriHandler = LocalUriHandler.current

    val annotated = remember(text, linkColor) {
        val linkStyles = TextLinkStyles(
            style = SpanStyle(
                color = linkColor,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
            ),
        )
        buildAnnotatedString {
            var lastIndex = 0
            for (match in URL_REGEX.findAll(text)) {
                append(text.substring(lastIndex, match.range.first))

                var rawUrl = match.value
                while (rawUrl.isNotEmpty() && rawUrl.last() in TRAILING_PUNCTUATION) {
                    rawUrl = rawUrl.dropLast(1)
                }
                val normalizedUrl = if (rawUrl.startsWith("http")) rawUrl else "https://$rawUrl"

                withLink(
                    LinkAnnotation.Url(normalizedUrl, linkStyles) {
                        runCatching { uriHandler.openUri(normalizedUrl) }
                    },
                ) { append(rawUrl) }

                append(match.value.substring(rawUrl.length))
                lastIndex = match.range.last + 1
            }
            append(text.substring(lastIndex))
        }
    }

    Text(
        text = annotated,
        style = style,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { onOverflowChange(it.hasVisualOverflow) },
    )
}