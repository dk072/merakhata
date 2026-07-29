package com.merakhata.app.domain.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.domain.accounting.AccountingEngine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateCustomerStatementPdf(
        context: Context,
        businessName: String,
        customer: CustomerEntity,
        transactions: List<TransactionEntity>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1B5E20") // Dark Green accent
            textSize = 22f
            isFakeBoldText = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val gavePaint = Paint().apply {
            color = Color.parseColor("#D32F2F")
            textSize = 10f
        }

        val gotPaint = Paint().apply {
            color = Color.parseColor("#388E3C")
            textSize = 10f
        }

        var y = 40f

        // Header
        canvas.drawText("MERA KHATA", 40f, y, titlePaint)
        y += 20f
        val displayBusiness = if (businessName.isBlank()) "Digital Udhar Khata" else businessName
        canvas.drawText(displayBusiness, 40f, y, subTitlePaint)
        y += 30f

        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Customer Info
        canvas.drawText("Statement For: ${customer.name}", 40f, y, boldTextPaint)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        canvas.drawText("Date: $todayStr", 400f, y, subTitlePaint)
        y += 15f
        if (!customer.phone.isNull_or_blank()) {
            canvas.drawText("Phone: ${customer.phone}", 40f, y, textPaint)
            y += 15f
        }

        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Table Header
        canvas.drawText("Date", 40f, y, boldTextPaint)
        canvas.drawText("Description", 130f, y, boldTextPaint)
        canvas.drawText("You Gave", 300f, y, boldTextPaint)
        canvas.drawText("You Got", 390f, y, boldTextPaint)
        canvas.drawText("Balance", 480f, y, boldTextPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Transactions
        var runningBalance = 0L
        var totalGave = 0L
        var totalGot = 0L

        val sortedTxs = transactions.sortedBy { it.transactionDate }

        for (tx in sortedTxs) {
            if (y > 780f) {
                // Page overflow protection
                break
            }

            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(tx.transactionDate))
            val desc = (tx.description ?: "-").take(22)
            val amtStr = AccountingEngine.formatCurrency(tx.amountMinor)

            canvas.drawText(dateStr, 40f, y, textPaint)
            canvas.drawText(desc, 130f, y, textPaint)

            if (tx.type == TransactionType.YOU_GAVE) {
                totalGave += tx.amountMinor
                runningBalance += tx.amountMinor
                canvas.drawText(amtStr, 300f, y, gavePaint)
                canvas.drawText("-", 390f, y, textPaint)
            } else {
                totalGot += tx.amountMinor
                runningBalance -= tx.amountMinor
                canvas.drawText("-", 300f, y, textPaint)
                canvas.drawText(amtStr, 390f, y, gotPaint)
            }

            val balStr = AccountingEngine.formatCurrency(runningBalance)
            canvas.drawText(balStr, 480f, y, textPaint)

            y += 18f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Summary Totals
        canvas.drawText("Total Gave: ${AccountingEngine.formatCurrency(totalGave)}", 40f, y, gavePaint)
        canvas.drawText("Total Got: ${AccountingEngine.formatCurrency(totalGot)}", 220f, y, gotPaint)

        val netSummary = AccountingEngine.calculateCustomerSummary(customer, transactions)
        val finalStatusStr = when (netSummary.status) {
            com.merakhata.app.domain.accounting.LedgerStatus.YOU_WILL_RECEIVE -> "Pending: ${AccountingEngine.formatCurrency(netSummary.netBalanceMinor)}"
            com.merakhata.app.domain.accounting.LedgerStatus.YOU_WILL_PAY -> "You Owe: ${AccountingEngine.formatCurrency(kotlin.math.abs(netSummary.netBalanceMinor))}"
            com.merakhata.app.domain.accounting.LedgerStatus.SETTLED -> "SETTLED (₹0)"
        }
        canvas.drawText(finalStatusStr, 400f, y, boldTextPaint)

        pdfDocument.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Statement_${customer.name.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    fun sharePdfFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.merakhata.app.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Customer Statement PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
