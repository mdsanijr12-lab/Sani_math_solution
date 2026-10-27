package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KaTeXMathView(
    latex: String,
    modifier: Modifier = Modifier,
    isDisplayMode: Boolean = true,
    fontSizeSp: Int = 18
) {
    val isDark = isSystemInDarkTheme()
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    val textColorHex = remember(textColor) {
        String.format("#%06X", (0xFFFFFF and textColor.toArgb()))
    }
    val primaryColorHex = remember(primaryColor) {
        String.format("#%06X", (0xFFFFFF and primaryColor.toArgb()))
    }

    val cleanLatex = remember(latex) {
        latex.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", " ")
            .trim()
    }

    val htmlContent = remember(cleanLatex, isDark, textColorHex, fontSizeSp) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
            <style>
                body {
                    margin: 0;
                    padding: 4px 6px;
                    background-color: transparent;
                    color: $textColorHex;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Kalpurush", sans-serif;
                    font-size: ${fontSizeSp}px;
                    overflow: hidden;
                    word-wrap: break-word;
                }
                .katex-display {
                    margin: 4px 0 !important;
                }
                .math-container {
                    display: inline-block;
                    max-width: 100%;
                    overflow-x: auto;
                    -webkit-overflow-scrolling: touch;
                }
            </style>
        </head>
        <body>
            <div id="math" class="math-container">$cleanLatex</div>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    const el = document.getElementById("math");
                    if (window.katex) {
                        try {
                            katex.render('$cleanLatex', el, {
                                displayMode: $isDisplayMode,
                                throwOnError: false
                            });
                        } catch (e) {
                            el.innerText = '$cleanLatex';
                        }
                    }
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(0) // Transparent background
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webViewClient = object : WebViewClient() {}
                    loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 36.dp)
        )
    }
}

@Composable
fun FormattedMathText(
    text: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    fontSizeSp: Int = if (isHighlight) 18 else 15
) {
    val hasBengali = text.any { it in '\u0980'..'\u09FF' }
    val hasLatex = (text.contains("\\") || text.contains("^") || text.contains("_") || text.contains("{")) && !hasBengali

    if (hasLatex && text.length < 250) {
        KaTeXMathView(latex = text, modifier = modifier, fontSizeSp = fontSizeSp)
    } else {
        // Native Compose math text with crisp typography
        Text(
            text = text,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = fontSizeSp.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = (fontSizeSp + 6).sp,
            modifier = modifier.padding(vertical = 2.dp)
        )
    }
}
