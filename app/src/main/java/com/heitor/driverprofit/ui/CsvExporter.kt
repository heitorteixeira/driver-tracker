package com.heitor.driverprofit.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.heitor.driverprofit.data.DayEntry
import com.heitor.driverprofit.domain.Platform
import java.io.File
import java.time.LocalDate
import java.util.Locale

object CsvExporter {

    fun share(context: Context, entries: List<DayEntry>, metric: Boolean, chooserTitle: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "driver-profit.csv")
        file.writeText(buildCsv(entries, metric))

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle))
    }

    fun buildCsv(entries: List<DayEntry>, metric: Boolean): String {
        val distanceUnit = if (metric) "km" else "mi"
        val sb = StringBuilder()
        sb.appendLine("date,platform,gross,distance_$distanceUnit,hours,fuel_expense,other_expense,net")
        entries.sortedBy { it.epochDay }.forEach { e ->
            sb.appendLine(
                listOf(
                    LocalDate.ofEpochDay(e.epochDay).toString(),
                    Platform.fromName(e.platform).brand ?: "Other",
                    num(e.gross),
                    num(e.distance),
                    num(e.hours),
                    num(e.fuelExpense),
                    num(e.otherExpense),
                    num(e.net),
                ).joinToString(","),
            )
        }
        return sb.toString()
    }

    private fun num(v: Double) = String.format(Locale.ROOT, "%.2f", v)
}
