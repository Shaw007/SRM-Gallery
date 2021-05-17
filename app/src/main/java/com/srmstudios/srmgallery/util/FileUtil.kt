package com.srmstudios.srmgallery.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class FileUtil @Throws(IllegalAccessException::class)
private constructor() {

    init {
        throw IllegalAccessException("Can not instantiate FileUtil")
    }

    companion object {
        private const val PRIMARY_DOCUMENT = "primary"
        private const val FILE_IMAGE_TYPE = "image"
        private const val FILE_AUDIO_TYPE = "audio"
        private const val FILE_VIDEO_TYPE = "video"

        private const val DOWNLOAD_FOLDER_NAME = "Download"
        private const val QUERY_ID_PARAMETER = "_id=?"
        private const val URI_SCHEME_CONTENT = "content"
        private const val URI_SCHEME_FILE = "file"
        private const val FILE_COLUMN_NAME = "_data"
        private const val JPGE_EXTENSION = "jpg"

        private const val CONTENT_PUBLIC_DOWNLOAD = "content://downloads/public_downloads"
        private const val GOOGLE_STORAGE_DOCUMENT_PROVIDER =
            "com.google.android.apps.photos.content"
        private const val EXTERNAL_STORAGE_DOCUMENT_PROVIDER =
            "com.android.externalstorage.documents"
        private const val DOWNLOAD_DOCUMENT_PROVIDER = "com.android.providers.downloads.documents"
        private const val MEDIA_DOCUMENT_PROVIDER = "com.android.providers.media.documents"

        //region Helper method for Downloading File from URI
        /***
         * Retrieves a File path represented by given Uri
         *
         * @param context Calling context
         * @param uri     File uri which needs to be used for getting file object
         * @return [File] object which would be returned from uri
         */
        suspend fun getFileFromUri(context: Context, uri: Uri?): File? = withContext(Dispatchers.IO) {
            val path = getPathFromUri(context, uri)
            if (TextUtils.isEmpty(path)) {
                uri?.let {
                    getFileFromRemoteUri(context, it)
                }
            } else
                File(path)
        }

        /**
         * Retrieves a String path represented by given Uri
         *
         * @param context Calling context
         * @param uri     File uri which needs to be used for getting file object
         * @return retrieves file path from provided URI.
         */
        private fun getPathFromUri(context: Context, uri: Uri?): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val type = split[0]

                    if (PRIMARY_DOCUMENT.equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + File.separator + split[1]
                    }
                    // DownloadsProvider
                } else if (isDownloadsDocument(uri)) {
                    val fileName = getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory().toString() + File.separator + DOWNLOAD_FOLDER_NAME +
                                File.separator + fileName
                    }

                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(CONTENT_PUBLIC_DOWNLOAD), java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                    // MediaProvider
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    when {
                        FILE_IMAGE_TYPE == type -> contentUri =
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        FILE_VIDEO_TYPE == type -> contentUri =
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        FILE_AUDIO_TYPE == type -> contentUri =
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(
                        context, contentUri, QUERY_ID_PARAMETER,
                        selectionArgs
                    )
                }
                // MediaStore (and general)
            } else if (URI_SCHEME_CONTENT.equals(uri?.scheme ?: "", ignoreCase = true)) {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri?.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
                // File
            } else if (URI_SCHEME_FILE.equals(uri?.scheme ?: "", ignoreCase = true)) {
                return uri?.path
            }
            return null
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The mContext.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?
        ): String? {

            var cursor: Cursor? = null
            val projection = arrayOf(FILE_COLUMN_NAME)

            try {
                cursor =
                    context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(FILE_COLUMN_NAME)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        private fun getFilePath(context: Context, uri: Uri?): String? {
            var cursor: Cursor? = null
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            try {
                cursor =
                    uri?.let { context.contentResolver.query(it, projection, null, null, null) }
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri?): Boolean {
            return EXTERNAL_STORAGE_DOCUMENT_PROVIDER == uri?.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri?): Boolean {
            return DOWNLOAD_DOCUMENT_PROVIDER == uri?.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri?): Boolean {
            return MEDIA_DOCUMENT_PROVIDER == uri?.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        private fun isGooglePhotosUri(uri: Uri?): Boolean {
            return GOOGLE_STORAGE_DOCUMENT_PROVIDER == uri?.authority
        }


        /**
         * Download the remote file in cache directory
         *
         * @param context Calling context
         * @param uri remote file URI
         *
         * @return remote file object.
         */
        private fun getFileFromRemoteUri(context: Context, uri: Uri): File? {
            var file: File? = null
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            var success = false
            try {
                val extension = getImageExtension(uri)
                inputStream = context.contentResolver.openInputStream(uri)
                file = File.createTempFile("image_picker", extension, context.cacheDir)
                outputStream = FileOutputStream(file)
                if (inputStream != null) {
                    file.copyInputStream(inputStream)
                    success = true
                }
            } catch (ignored: IOException) {
            } finally {
                try {
                    inputStream?.close()
                } catch (ignored: IOException) {
                }

                try {
                    outputStream?.close()
                } catch (ignored: IOException) {
                    success = false
                }

            }
            return if (success) file else null
        }

        /***
         *  Get extension from source image.
         * @param uriImage Source Image URI
         * @return extension of image with dot, or default .jpg if it none.
         *
         */
        private fun getImageExtension(uriImage: Uri): String {
            var extension: String? = null

            try {
                val imagePath = uriImage.path
                if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
                    extension = imagePath.substring(imagePath.lastIndexOf(".") + 1)
                }
            } catch (e: Exception) {
                extension = null
            }

            if (extension == null || extension.isEmpty()) {
                //default extension for matches the previous behavior of the plugin
                extension = JPGE_EXTENSION
            }

            return ".$extension"
        }

        //endregion
    }
}