package com.francekim.empreintedelame.ui.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

// ─── Partage PDF ────────────────────────────────────────────────────────────
// Pagine le texte fourni sur autant de pages A4 (72 dpi) que nécessaire.
// La 1ère ligne est traitée comme titre, les lignes tout en majuscules
// (titres de section générés en .uppercase()) sont mises en gras.
// Une image optionnelle (ex. le schéma bodygraph) est insérée à l'endroit où
// la ligne PDF_IMAGE_MARKER apparaît dans le texte (l'appelant l'ajoute où il
// veut qu'elle sorte). "pageBreakBeforeLine" force une nouvelle page juste
// avant une ligne donnée (ex. le titre de section qui précède l'image), pour
// que le titre, sa légende et l'image restent groupés en haut de la même page.

private const val PDF_PAGE_WIDTH  = 595
private const val PDF_PAGE_HEIGHT = 842
private const val PDF_MARGIN      = 40f
const val PDF_IMAGE_MARKER = "@@PDF_IMAGE@@"
// Préfixe qui marque un titre de sous-section plus grand que les en-têtes tout-majuscules habituels.
const val PDF_SECTION_MARKER = "@@PDF_SECTION@@"

fun sharePdf(
    context             : Context,
    fileNamePrefix      : String,
    text                : String,
    image               : Bitmap? = null,
    maxImageHeight      : Float = 380f,
    pageBreakBeforeLine : String? = null
) {
    val contentWidth = (PDF_PAGE_WIDTH - 2 * PDF_MARGIN).toInt()

    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.BLACK
        textSize  = 20f
        typeface  = Typeface.DEFAULT_BOLD
    }
    val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.BLACK
        textSize  = 13f
        typeface  = Typeface.DEFAULT_BOLD
    }
    val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.BLACK
        textSize  = 16f
        typeface  = Typeface.DEFAULT_BOLD
    }
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.DKGRAY
        textSize  = 11f
        typeface  = Typeface.DEFAULT
    }

    val document = PdfDocument()
    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, pageNumber).create())
    var canvas = page.canvas
    var y = PDF_MARGIN

    fun startNewPage() {
        document.finishPage(page)
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, pageNumber).create())
        canvas = page.canvas
        y = PDF_MARGIN
    }

    val lines     = text.lines()
    val titleLine = lines.firstOrNull { it.isNotBlank() } ?: ""
    val bodyLines = lines.dropWhile { it.isBlank() }.drop(1)

    val titleLayout = StaticLayout.Builder
        .obtain(titleLine, 0, titleLine.length, titlePaint, contentWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .build()
    canvas.save()
    canvas.translate(PDF_MARGIN, y)
    titleLayout.draw(canvas)
    canvas.restore()
    y += titleLayout.height + 16f

    // Le canvas du PDF est logiciel : un bitmap "hardware" (issu de la capture
    // GraphicsLayer, accélérée GPU) doit être converti avant de pouvoir y être dessiné.
    // Config.HARDWARE n'existe qu'à partir d'Android 8, où ces bitmaps sont apparus :
    // sans ce garde-fou, la simple lecture de la constante plante sur Android 7.
    val softwareImage = image?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && it.config == Bitmap.Config.HARDWARE) {
            it.copy(Bitmap.Config.ARGB_8888, false)
        } else it
    }

    fun drawImage() {
        val img = softwareImage ?: return
        val scale      = minOf(contentWidth / img.width.toFloat(), maxImageHeight / img.height.toFloat())
        val drawWidth  = img.width * scale
        val drawHeight = img.height * scale
        if (y + drawHeight > PDF_PAGE_HEIGHT - PDF_MARGIN) startNewPage()
        val xOffset = PDF_MARGIN + (contentWidth - drawWidth) / 2f
        val destRect = android.graphics.RectF(xOffset, y, xOffset + drawWidth, y + drawHeight)
        canvas.drawBitmap(img, null, destRect, null)
        y += drawHeight + 16f
    }

    var pageBroken = pageBreakBeforeLine == null

    bodyLines.forEach { line ->
        if (!pageBroken && line == pageBreakBeforeLine) {
            startNewPage()
            pageBroken = true
        }

        if (line == PDF_IMAGE_MARKER) {
            drawImage()
            return@forEach
        }

        if (line.isBlank()) {
            y += 8f
            return@forEach
        }

        val isSectionTitle = line.startsWith(PDF_SECTION_MARKER)
        val displayLine = if (isSectionTitle) line.removePrefix(PDF_SECTION_MARKER) else line

        val paint = when {
            isSectionTitle                       -> sectionPaint
            displayLine == displayLine.uppercase() -> headerPaint
            else                                   -> bodyPaint
        }
        if (paint === headerPaint || isSectionTitle) y += 10f

        val layout = StaticLayout.Builder
            .obtain(displayLine, 0, displayLine.length, paint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

        if (y + layout.height > PDF_PAGE_HEIGHT - PDF_MARGIN) {
            startNewPage()
        }

        canvas.save()
        canvas.translate(PDF_MARGIN, y)
        layout.draw(canvas)
        canvas.restore()

        y += layout.height + 4f
    }
    document.finishPage(page)

    val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
    val file = File(pdfDir, "${fileNamePrefix}_${System.currentTimeMillis()}.pdf")
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun shareImage(context: Context, bitmap: Bitmap, caption: String) {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "share_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, caption)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
