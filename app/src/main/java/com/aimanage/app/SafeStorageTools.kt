package com.aimanage.app

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.util.Locale

/** Uses app-private cache and explicit SAF folder selection only. No all-files access. */
internal object FileCategoryPolicy {
 fun folder(fileName:String):String {
  val ext=fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
  return when(ext) {
   "jpg","jpeg","png","gif","webp","heic" -> "Images"
   "mp4","mov","mkv","webm","avi" -> "Videos"
   "mp3","wav","m4a","ogg","flac" -> "Audio"
   "pdf","doc","docx","xls","xlsx","ppt","pptx","txt","csv" -> "Documents"
   "zip","7z","rar","tar","gz" -> "Archives"
   "apk" -> "APK_Installers"
   else -> "Other"
  }
 }
}

data class StorageOverview(val privateCacheBytes:Long,val visibleItems:Int,val folders:Int,val files:Int)
data class OrganizeResult(val copied:Int,val skipped:Int,val message:String)
object SafeStorageTools {
 private fun bytes(file:File, depth:Int=0):Long {
  if(depth>12 || !file.exists()) return 0L
  if(file.isFile) return file.length().coerceAtLeast(0L)
  return file.listFiles()?.take(5000)?.sumOf { bytes(it,depth+1) } ?: 0L
 }
 fun cacheBytes(context:Context):Long =
  bytes(context.cacheDir) + (context.externalCacheDir?.let { bytes(it) } ?: 0L)

 private fun clearChildren(directory:File?,depth:Int=0):Int {
  if(directory==null || depth>12) return 0
  var removed=0
  directory.listFiles()?.take(5000)?.forEach { file ->
   if(file.isDirectory) removed+=clearChildren(file,depth+1)
   if(file.delete()) removed++
  }
  return removed
 }
 fun clearOwnCache(context:Context):Int =
  clearChildren(context.cacheDir) + clearChildren(context.externalCacheDir)

 fun preview(context:Context,uri:Uri):StorageOverview {
  val root=DocumentFile.fromTreeUri(context,uri)
  val files=root?.listFiles()?.take(200).orEmpty()
  return StorageOverview(cacheBytes(context),files.size,files.count { it.isDirectory },files.count { it.isFile })
 }

 /** Copy only top-level files, do not delete originals. User explicitly confirms. */
 fun copyToCategoryFolders(context:Context,uri:Uri):OrganizeResult {
  val root=DocumentFile.fromTreeUri(context,uri)
  if(root==null || !root.isDirectory || !root.canWrite()) return OrganizeResult(0,0,"Folder not writable. No files changed.")
  var copied=0
  var skipped=0
  val files=root.listFiles().filter { it.isFile }.take(100)
  for(file in files) {
   try {
    val name=file.name ?: run { skipped++;continue }
    if(file.length()>100L*1024L*1024L) { skipped++;continue }
    val category=FileCategoryPolicy.folder(name)
    val folder=root.findFile(category)?.takeIf { it.isDirectory } ?: root.createDirectory(category)
    if(folder==null || folder.findFile(name)!=null) { skipped++;continue }
    val created=folder.createFile(file.type ?: "application/octet-stream",name)
    if(created==null) { skipped++;continue }
    try {
     val input=context.contentResolver.openInputStream(file.uri)
     val output=context.contentResolver.openOutputStream(created.uri,"w")
     if(input==null || output==null) throw IllegalStateException("Document stream unavailable")
     input.use { from -> output.use { to -> from.copyTo(to,64*1024) } }
     copied++
    } catch (_:Exception) { created.delete();skipped++ }
   } catch (_:Exception) { skipped++ }
  }
  return OrganizeResult(copied,skipped,"Copied $copied files into category folders; $skipped skipped. Originals remain. Review copies before deleting anything.")
 }
}
