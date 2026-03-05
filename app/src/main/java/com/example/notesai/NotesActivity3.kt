package com.example.notesai

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.createBitmap

class NotesActivity3 : AppCompatActivity() {



    private lateinit var pageContainer: LinearLayout

    private val drawingViews = mutableListOf<DrawingView>()
    private val pageLabels = mutableListOf<TextView>()
    private val pageCards = mutableListOf<CardView>()

    private var pageCount = 0
    private var activePageIndex = 0

    private fun viewToBitmap(view: View): Bitmap {
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveNotesAsPdf(): android.net.Uri? {
        val pdfDocument = android.graphics.pdf.PdfDocument()

        drawingViews.forEachIndexed { index, drawingView ->
            val bitmap = viewToBitmap(drawingView)

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                index + 1
            ).create()

            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }

        val fileName = "Notes_${System.currentTimeMillis()}.pdf"

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
        }

        val uri = contentResolver.insert(
            android.provider.MediaStore.Files.getContentUri("external"),
            contentValues
        )

        try {
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use {
                    pdfDocument.writeTo(it)
                }
                Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notes3)

        pageContainer = findViewById(R.id.pageContainer)

        // Back
        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to go back?")
                .setPositiveButton("Yes") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        }

        addNewPage()

        findViewById<ImageView>(R.id.btnNewPage).setOnClickListener { addNewPage() }

        findViewById<ImageView>(R.id.btnUndo).setOnClickListener {
            drawingViews.getOrNull(activePageIndex)?.undo()
        }

        findViewById<ImageView>(R.id.btnRedo).setOnClickListener {
            drawingViews.getOrNull(activePageIndex)?.redo()
        }

        findViewById<ImageView>(R.id.btnDeletePage).setOnClickListener {
            deleteCurrentPage()
        }

        // ✏️ PEN POPUP
        // ✏️ PEN POPUP
        findViewById<ImageView>(R.id.btnPen).setOnClickListener {

            val view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_pen_size, null)

            val txtSize = view.findViewById<TextView>(R.id.txtSize)
            val seekBar = view.findViewById<SeekBar>(R.id.seekPenSize)

            val page = drawingViews[activePageIndex]
            page.disableEraser()

            seekBar.progress = page.getPenSize().toInt()
            txtSize.text = "Pen Size: ${seekBar.progress}"

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val size = progress.coerceAtLeast(2).toFloat()
                    page.setPenSize(size)
                    txtSize.text = "Pen Size: $size"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            AlertDialog.Builder(this)
                .setTitle("Pen Settings")
                .setView(view)
                .setPositiveButton("Done", null)
                .show()
        }

        //Btn Eraser
        findViewById<ImageView>(R.id.btnErase).setOnClickListener {

            val view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_eraser_size, null)

            val txtSize = view.findViewById<TextView>(R.id.txtSize)
            val seekBar = view.findViewById<SeekBar>(R.id.seekEraserSize)

            val page = drawingViews[activePageIndex]

            seekBar.progress = 20
            txtSize.text = "Eraser Size: 20"

            page.enableEraser(20f)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val size = progress.coerceAtLeast(5).toFloat()
                    page.enableEraser(size)
                    txtSize.text = "Eraser Size: $size"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            AlertDialog.Builder(this)
                .setTitle("Eraser Settings")
                .setView(view)
                .setPositiveButton("Done", null)
                .show()
        }



        // 🎨 COLOR POPUP
        findViewById<ImageView>(R.id.btnColor).setOnClickListener { anchor ->
            val view = LayoutInflater.from(this)
                .inflate(R.layout.popup_color_palette, null)

            val popup = PopupWindow(view,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            popup.elevation = 12f
            popup.showAsDropDown(anchor, 0, -anchor.height * 3)

            val page = drawingViews[activePageIndex]
            val grid = view as GridLayout

            for (i in 0 until grid.childCount) {
                val colorView = grid.getChildAt(i)
                colorView.setOnClickListener {
                    val color = (it.background as ColorDrawable).color
                    page.disableEraser()
                    page.setPenColor(color)
                    popup.dismiss()
                }
            }
        }

        findViewById<ImageView>(R.id.downloadBtn).setOnClickListener {
            val pdfUri = saveNotesAsPdf()

            if (pdfUri != null) {
                val intent = Intent(this@NotesActivity3, AfterSavedActivity::class.java)
                intent.putExtra("pdf_uri", pdfUri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "PDF not saved", Toast.LENGTH_SHORT).show()
            }
        }
        onBackPressedDispatcher.addCallback(this) {

            AlertDialog.Builder(this@NotesActivity3)
                .setTitle("Exit")
                .setMessage("Are you sure you want to go back?")
                .setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun addNewPage() {
        pageCount++

        val label = TextView(this).apply {
            text = "Page $pageCount"
            setTextColor(Color.DKGRAY)
        }

        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                900.dp()
            ).apply { bottomMargin = 24.dp() }
            radius = 12f
            cardElevation = 6f
        }

        val drawingView = DrawingView(this).apply {
            setBackgroundColor(Color.WHITE)
            onPageTouched = {
                activePageIndex = drawingViews.indexOf(this)
            }
        }

        drawingViews.add(drawingView)
        pageLabels.add(label)
        pageCards.add(card)
        activePageIndex = drawingViews.lastIndex

        card.addView(drawingView)
        pageContainer.addView(label)
        pageContainer.addView(card)
    }

    private fun deleteCurrentPage() {
        if (drawingViews.size == 1) {
            Toast.makeText(this, "At least one page must remain", Toast.LENGTH_SHORT).show()
            return
        }
        val i = activePageIndex
        pageContainer.removeView(pageLabels[i])
        pageContainer.removeView(pageCards[i])
        drawingViews.removeAt(i)
        pageLabels.removeAt(i)
        pageCards.removeAt(i)
        activePageIndex = if (i > 0) i - 1 else 0
        updatePageNumbers()
    }

    private fun updatePageNumbers() {
        for (i in pageLabels.indices) {
            pageLabels[i].text = "Page ${i + 1}"
        }
        pageCount = pageLabels.size
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}
