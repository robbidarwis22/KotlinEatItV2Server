package com.example.kotlineatitv2server.common

import com.itextpdf.text.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.itextpdf.text.pdf.draw.VerticalPositionMark

object PDFUtils {
    @Throws(DocumentException::class)
    fun addNewItem(
        document: Document,
        text:String,
        align:Int,
        font:Font
    )
    {
        val chunk = Chunk(text,font)
        val p = Paragraph(chunk)
        p.alignment = align
        document.add(p)
    }

    @Throws(DocumentException::class)
    fun addLineSpace(
        document: Document
    )
    {
        document.add(Paragraph(""))
    }

    @Throws(DocumentException::class)
    fun addLineSeparator(
        document: Document
    )
    {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = BaseColor(0,0,0,68)
        addLineSpace(document)
        document.add(Chunk(lineSeparator))
        addLineSpace(document)
    }

    @Throws(DocumentException::class)
    fun addNewItemWithLeftAndRight(
        document: Document,
        leftText:String,
        rightText:String,
        leftFont:Font,
        rightFont: Font
    )
    {
        val chunkTextLeft = Chunk(leftText,leftFont)
        val chunkRightText = Chunk(rightText,rightFont)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkRightText)
        document.add(p)
    }
}