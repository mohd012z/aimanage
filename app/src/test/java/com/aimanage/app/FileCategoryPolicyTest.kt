package com.aimanage.app

import org.junit.Assert.assertEquals
import org.junit.Test

class FileCategoryPolicyTest {
 @Test fun handlesKnownExtensionsCaseInsensitively() {
  assertEquals("Images",FileCategoryPolicy.folder("photo.HEIC"))
  assertEquals("Documents",FileCategoryPolicy.folder("report.PDF"))
  assertEquals("APK_Installers",FileCategoryPolicy.folder("app.apk"))
  assertEquals("Archives",FileCategoryPolicy.folder("backup.zip"))
 }
 @Test fun unknownAndHiddenFilesAreNotClassifiedAsExecutable() {
  assertEquals("Other",FileCategoryPolicy.folder(".nomedia"))
  assertEquals("Other",FileCategoryPolicy.folder("no_extension"))
  assertEquals("Other",FileCategoryPolicy.folder("something.sh"))
 }
}
