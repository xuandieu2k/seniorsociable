package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 6/30/2023
 */
class MaterialUnitSpecifications {
    @SerializedName("id")
    var id = 0L

    @SerializedName("name")
    var name = ""

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("exchange_value")
    var exchangeValue = 0

    @SerializedName("material_unit_specification_exchange_name_id")
    var materialUnitSpecificationExchangeNameId = 0L

    @SerializedName("material_unit_specification_exchange_name")
    var materialUnitSpecificationExchangeName = ""

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