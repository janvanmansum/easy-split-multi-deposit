package nl.knaw.dans.easy.multiDeposit

import java.io.File

import org.apache.commons.csv.{CSVFormat, CSVParser}
import org.slf4j.LoggerFactory
import rx.lang.scala.Observable
import scala.collection.JavaConversions.{iterableAsScalaIterable}
import scala.collection.mutable

import scala.io.Source
import scala.util.{Failure, Success, Try}

object MultiDepositParser {

  type CsvValues = List[(String, String)]

  /**
    * Transforms a `File` into a stream of `Datasets` by parsing the csv.
    *
    * @param file the csv file to be parsed
    * @return a stream of datasets coming from the file
    */
  def parse(file: File): Observable[Datasets] = {
    val rawContent = Source.fromFile(file).mkString
    val parser = CSVParser.parse(rawContent, CSVFormat.RFC4180)
    val headers :: data = parser.getRecords.map(_.toList)
    validateDatasetHeaders(headers)
      .map(_ => {
        case class IndexDatasets(index: Int, datasets: Datasets)

        Observable.from(data.map(headers zip _))
          .foldLeft(IndexDatasets(1, new Datasets)) {
            case (IndexDatasets(row, datasets), csvValues) => IndexDatasets(row + 1, updateDatasets(datasets, csvValues, row + 1))
          }
          .map(_.datasets)
      })
      .onError(Observable.error(_))
  }

  /**
    * Tests whether the given list of headers is valid. If so, `Success(Unit)` is returned,
    * else a `Failure` with a detailed `ActionException` is returned.
    * @param headers the headers to be validated
    * @return `Success` if the `headers` are valid, `Failure` if the `headers` are invalid
    */
  def validateDatasetHeaders(headers: List[String]): Try[Unit] = {
    val validHeaders = List("DATASET_ID", "FILE_SIP", "FILE_DATASET", "FILE_AUDIO_VIDEO",
      "FILE_STORAGE_SERVICE", "FILE_STORAGE_PATH", "FILE_SUBTITLES",
      "SF_DOMAIN", "SF_USER", "SF_COLLECTION", "SF_PRESENTATION", "SF_SUBTITLES") ++ DDM.allFields
    if (headers.forall(validHeaders.contains)) Success(Unit)
    else Failure(new ActionException("0", "SIP Instructions file contains unknown headers: "
      + headers.filter(!validHeaders.contains(_)).mkString(", ") + ". "
      + "Please, check for spelling errors and consult the documentation for the list of valid headers."))
  }

  /**
    * Updates the `datasets` with the given `values` and `row`.
    *
    * @param datasets
    * @param values
    * @param row
    * @return the same `datasets`
    */
  def updateDatasets(datasets: Datasets, values: CsvValues, row: Int): Datasets = {
    val id = values.find(_._1 equals "DATASET_ID")
      .map(_._2)
      .getOrElse {
        throw new Exception("No dataset ID found")
      }

    datasets.find(_._1 == id)
      .map { case (_, dataset) => updateDataset(dataset, values, row) }
      .getOrElse {
        val newDataset = new Dataset
        datasets += id -> newDataset
        updateDataset(newDataset, values, row)
      }

    datasets
  }

  /**
    * Adds the `values` to the `dataset`, as well as an extra value for the row number.
    *
    * @param dataset the `dataset` to which values need to be added
    * @param values  the `values` to be added
    * @param row     the `row` number that will be added to the `dataset`
    * @return the `dataset`
    */
  private def updateDataset(dataset: Dataset, values: CsvValues, row: Int): Dataset = {
    def addToDataset(dataset: Dataset)(kvPair: (String, String)): Unit = {
      val (key, value) = kvPair
      dataset.put(key, dataset.getOrElseUpdate(key, List()) :+ value)
    }

    values.foreach(addToDataset(dataset))
    addToDataset(dataset)(("ROW", row.toString))

    dataset
  }
}
