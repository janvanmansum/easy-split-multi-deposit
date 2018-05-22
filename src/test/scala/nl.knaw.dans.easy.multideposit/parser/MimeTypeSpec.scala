/**
 * Copyright (C) 2016 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.multideposit.parser

import java.nio.file.NoSuchFileException

import better.files.File
import nl.knaw.dans.easy.multideposit.TestSupportFixture
import org.scalatest.BeforeAndAfterEach

import scala.util.{ Failure, Success }

class MimeTypeSpec extends TestSupportFixture with BeforeAndAfterEach {

  private val mimetypesDir = testDir / "mimetypes"

  override def beforeEach(): Unit = {
    super.beforeEach()

    if (mimetypesDir.exists) mimetypesDir.delete()
    File(getClass.getResource("/mimetypes").toURI).copyTo(mimetypesDir)
  }

  "getMimeType" should "produce the correct doc mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-ms-doc.doc")) {
      case Success(mimetype) => mimetype shouldBe "application/msword"
    }
  }

  it should "produce the correct docx mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-ms-docx.docx")) {
      case Success(mimetype) => mimetype shouldBe "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
  }

  it should "produce the correct xlsx mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-ms-excel.xlsx")) {
      case Success(mimetype) => mimetype shouldBe "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }
  }

  it should "produce the correct pdf mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-pdf.pdf")) {
      case Success(mimetype) => mimetype shouldBe "application/pdf"
    }
  }

  it should "produce the correct plain text mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-plain-text.txt")) {
      case Success(mimetype) => mimetype shouldBe "text/plain"
    }
  }

  it should "produce the correct json mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-json.json")) {
      case Success(mimetype) => mimetype shouldBe "application/json"
    }
  }

  it should "produce the correct xml mimetype" in {
    inside(MimeType.get(mimetypesDir / "file-xml.xml")) {
      case Success(mimetype) => mimetype shouldBe "application/xml"
    }
  }

  it should "give the correct mimetype if the file is plain text and has no extension" in {
    inside(MimeType.get(mimetypesDir / "file-unknown")) {
      case Success(mimetype) => mimetype shouldBe "text/plain"
    }
  }

  it should "give the correct mimetype if the file has no extension" in {
    inside(MimeType.get(mimetypesDir / "file-unknown-pdf")) {
      case Success(mimetype) => mimetype shouldBe "application/pdf"
    }
  }

  it should "give the correct mimetype if the file is hidden" in {
    inside(MimeType.get(mimetypesDir / ".file-hidden-pdf")) {
      case Success(mimetype) => mimetype shouldBe "application/pdf"
    }
  }

  it should "fail if the file does not exist" in {
    inside(MimeType.get(mimetypesDir / "file-does-not-exist.doc")) {
      case Failure(e: NoSuchFileException) => e.getMessage should include("mimetypes/file-does-not-exist.doc")
    }
  }
}
