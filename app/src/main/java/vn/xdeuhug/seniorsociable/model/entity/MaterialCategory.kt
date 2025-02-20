package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

class MaterialCategory {
    @SerializedName("code")
    var code = ""

    @SerializedName("description")
    var description = ""

    @SerializedName("id")
    var id = 0L

    @SerializedName("material_category_type_parent_id")
    var materialCategoryTypeParentId = 0L

    @SerializedName("material_category_type_parent_name")
    var materialCategoryTypeParentName = ""

    @SerializedName("name")
    var name = ""

    @SerializedName("normalize_name")
    var normalizeName = ""


    @SerializedName("prefix")
    var prefix = ""

    @SerializedName("status")
    var status = 0

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