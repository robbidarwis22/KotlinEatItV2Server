package com.example.kotlineatitv2server.adapter

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import com.example.kotlineatitv2server.common.Common
import java.io.*

class PdfDocumentAdapter(var context:Context, var path:String): PrintDocumentAdapter() {
    override fun onLayout(
        p0: PrintAttributes?,
        p1: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        layoutResultCallback: LayoutResultCallback?,
        p4: Bundle?
    ) {
        if (cancellationSignal!!.isCanceled)
            layoutResultCallback!!.onLayoutCancelled()
        else{
            val builder = PrintDocumentInfo.Builder(Common.FILE_PRINT)
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            layoutResultCallback!!.onLayoutFinished(builder.build(),p1 != p0)
        }
    }

    override fun onWrite(
        p0: Array<out PageRange>?,
        parcelFileDescriptor: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        writeResultCallback: WriteResultCallback?
    ) {
        var `in` : InputStream?=null
        var `out` :OutputStream?=null
        try{
            val file = File(path)
            `in` = FileInputStream(file)
            `out` = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

            if (!cancellationSignal!!.isCanceled){
                `in`.copyTo(out)
                writeResultCallback!!.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
            else
                writeResultCallback!!.onWriteCancelled()
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            try {
                `in`!!.close()
                `out`!!.close()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

}