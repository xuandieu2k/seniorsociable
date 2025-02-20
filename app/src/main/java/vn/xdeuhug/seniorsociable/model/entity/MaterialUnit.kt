package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: NguyenXuanDieu
 * @Date: 6/30/2023
 */
class MaterialUnit {
    @SerializedName("id")
    var id = 0L

    @SerializedName("code")
    var code = ""

    @SerializedName("name")
    var name = ""

    @SerializedName("description")
    var description = ""

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("created_at")
    var createdAt = 0L
    @SerializedName("updated_at")
    var updatedAt = ""

    @SerializedName("normalize_name")
    var normalizeName = ""


    @SerializedName("prefix")
    var prefix = ""

    @SerializedName("status")
    var status = 0

    @SerializedName("material_unit_specifications")
    var materialUnitSpecifications = ArrayList<MaterialUnitSpecifications>()

    constructor(id: Long, name: String) {
        this.id = id
        this.name = name
    }

    constructor()


    override fun toString(): String {
        return name
    }

    var isHint = false




}