package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 25/08/2023
 */
class ItemChooseDialog {
    // Category
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
    // Unit

    @SerializedName("created_at")
    var createdAt = 0L

    @SerializedName("updated_at")
    var updatedAt = ""

    @SerializedName("material_unit_specifications")
    var materialUnitSpecifications = ArrayList<MaterialUnitSpecifications>()

    // Unit Specials

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("exchange_value")
    var exchangeValue = 0

    @SerializedName("material_unit_specification_exchange_name_id")
    var materialUnitSpecificationExchangeNameId = 0L

    @SerializedName("material_unit_specification_exchange_name")
    var materialUnitSpecificationExchangeName = ""

    var isChecked = false

    fun copyMaterialCategory(materialCategory: MaterialCategory)
    {
        this.code = materialCategory.code
        this.description = materialCategory.description
        this.id = materialCategory.id
        this.materialCategoryTypeParentId = materialCategory.materialCategoryTypeParentId
        this.materialCategoryTypeParentName = materialCategory.materialCategoryTypeParentName
        this.name = materialCategory.name
        this.normalizeName = materialCategory.normalizeName
        this.prefix = materialCategory.prefix
        this.status = materialCategory.status
    }
    fun copyMaterialUnit(materialUnit: MaterialUnit)
    {
        this.id = materialUnit.id
        this.code = materialUnit.code
        this.name = materialUnit.name
        this.description = materialUnit.description
        this.supplierId = materialUnit.supplierId
        this.createdAt = materialUnit.createdAt
        this.updatedAt = materialUnit.updatedAt
        this.normalizeName = materialUnit.normalizeName
        this.prefix = materialUnit.prefix
        this.status = materialUnit.status
        this.materialUnitSpecifications = materialUnit.materialUnitSpecifications
    }

    fun copyMaterialUnitSpecifications(materialUnitSpecifications: MaterialUnitSpecifications)
    {
        this.id = materialUnitSpecifications.id
        this.name = materialUnitSpecifications.name
        this.supplierId = materialUnitSpecifications.supplierId
        this.exchangeValue = materialUnitSpecifications.exchangeValue
        this.materialUnitSpecificationExchangeNameId = materialUnitSpecifications.materialUnitSpecificationExchangeNameId
        this.materialUnitSpecificationExchangeName = materialUnitSpecifications.materialUnitSpecificationExchangeName
    }
}