package vn.xdeuhug.seniorsociable.utility.model

import com.google.gson.annotations.SerializedName

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class Diseases {
    var id = 0
    var name: String? = null // tên bệnh

    var image: ArrayList<String>? = null
    var overview: String? = null // Tổng quan

    var reason: String? = null // nguyên nhân

    var symptom: String? = null // triệu chứng

    @SerializedName("transmission_route")
    var transmissionRoute: String? = null // đường lây truyền

    @SerializedName("subjects_at_risk")
    var subjectsAtRisk: String? = null // đối tượng nguy cơ
    var prevent: String? = null // phòng ngừa

    @SerializedName("diagnostic_measures")
    var diagnosticMeasures: String? = null // biện pháp chuẩn đoán

    @SerializedName("treatment_measures")
    var treatmentMeasures: String? = null // biện pháp điều trị


    constructor()

    constructor(
        id: Int,
        name: String?,
        image: ArrayList<String>?,
        overview: String?,
        reason: String?,
        symptom: String?,
        transmissionRoute: String?,
        subjectsAtRisk: String?,
        prevent: String?,
        diagnosticMeasures: String?,
        treatmentMeasures: String?
    ) {
        this.id = id
        this.name = name
        this.image = image
        this.overview = overview
        this.reason = reason
        this.symptom = symptom
        this.transmissionRoute = transmissionRoute
        this.subjectsAtRisk = subjectsAtRisk
        this.prevent = prevent
        this.diagnosticMeasures = diagnosticMeasures
        this.treatmentMeasures = treatmentMeasures
    }
}