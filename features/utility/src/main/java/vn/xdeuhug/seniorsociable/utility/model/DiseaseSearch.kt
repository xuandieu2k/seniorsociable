@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.utility.model

//import org.apache.lucene.analysis.Analyzer
//import org.apache.lucene.analysis.core.KeywordAnalyzer
//import org.apache.lucene.document.Document
//import org.apache.lucene.document.Field
//import org.apache.lucene.document.TextField
//import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexWriter
//import org.apache.lucene.index.IndexWriterConfig
//import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
//import org.apache.lucene.search.IndexSearcher
//import org.apache.lucene.search.Query
//import org.apache.lucene.search.ScoreDoc
//import org.apache.lucene.search.TopDocs
//import org.apache.lucene.store.Directory
//import org.apache.lucene.store.RAMDirectory


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class DiseaseSearch {
//    var index: Directory? = null
//    var analyzer: Analyzer? = null
//    var diseasesList = ArrayList<Diseases>()
//
//
//    constructor(diseasesList: ArrayList<Diseases>) {
//        index = RAMDirectory()
//        analyzer = KeywordAnalyzer()
//        this.diseasesList = diseasesList
//    }
//
//    companion object{
//
//    }
//
//    @Throws(Exception::class)
//    fun createIndex() {
//        val config = IndexWriterConfig(analyzer)
//        IndexWriter(index, config).use { writer ->
//            for (disease in diseasesList) {
//                val doc = Document()
//                doc.add(TextField("id", java.lang.String.valueOf(disease.id), Field.Store.YES))
//                doc.add(TextField("name", disease.name, Field.Store.YES))
//                doc.add(TextField("overview", disease.overview, Field.Store.YES))
//                doc.add(TextField("reason", disease.reason, Field.Store.YES))
//                doc.add(TextField("symptom", disease.symptom, Field.Store.YES))
//                doc.add(
//                    TextField(
//                        "transmission_route",
//                        disease.transmissionRoute,
//                        Field.Store.YES
//                    )
//                )
//                doc.add(
//                    TextField(
//                        "subjects_at_risk",
//                        disease.subjectsAtRisk,
//                        Field.Store.YES
//                    )
//                )
//                doc.add(TextField("prevent", disease.prevent, Field.Store.YES))
//                doc.add(
//                    TextField(
//                        "diagnostic_measures",
//                        disease.diagnosticMeasures,
//                        Field.Store.YES
//                    )
//                )
//                doc.add(
//                    TextField(
//                        "treatment_measures",
//                        disease.treatmentMeasures,
//                        Field.Store.YES
//                    )
//                )
//                writer.addDocument(doc)
//            }
//        }
//    }
//
//    @Throws(Exception::class)
//    fun searchDiseases(searchQuery: String?): List<Diseases> {
//        val parser = MultiFieldQueryParser(
//            arrayOf(
//                "id",
//                "name",
//                "overview",
//                "reason",
//                "symptom",
//                "transmission_route",
//                "subjects_at_risk",
//                "prevent",
//                "diagnostic_measures",
//                "treatment_measures"
//            ),
//            analyzer
//        )
//        val query: Query = parser.parse(searchQuery)
//        val result = ArrayList<Diseases>()
//        DirectoryReader.open(index).use { reader ->
//            val searcher = IndexSearcher(reader)
//            val topDocs: TopDocs = searcher.search(query, 10)
//            val hits: Array<ScoreDoc> = topDocs.scoreDocs
//            for (hit in hits) {
//                val docId: Int = hit.doc
//                val doc: Document = searcher.doc(docId)
//                val id: String = doc["id"]
////                                String name = doc.get("name");
////                String overview = doc.get("overview");
////                String reason = doc.get("reason");
////                String symptom = doc.get("symptom");
////                String transmissionRoute = doc.get("transmission_route");
////                String subjectsAtRisk = doc.get("subjects_at_risk");
////                String prevent = doc.get("prevent");
////                String diagnosticMeasures = doc.get("diagnostic_measures");
////                String treatmentMeasures = doc.get("treatment_measures");
//                for (diseases in diseasesList) {
//                    if (diseases.id == id.toInt()) {
//                        result.add(diseases)
//                    }
//                }
//            }
//        }
//        return result
//    }
}