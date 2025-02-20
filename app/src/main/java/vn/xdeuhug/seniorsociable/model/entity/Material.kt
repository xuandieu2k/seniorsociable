package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Material : Serializable {
    @SerializedName("avatar")
    var avatar = ""

    @SerializedName("avatar_thumb")
    var avatarThumb = ""

    @SerializedName("code")
    var code = ""

    @SerializedName("created_at")
    var createdAt = ""

    @SerializedName("description")
    var description = ""

    @SerializedName("id")
    var id = 0L

    @SerializedName("is_update")
    var isUpdate = 0

    @SerializedName("material_category_id")
    var materialCategoryId = 0L

    @SerializedName("material_category_name")
    var materialCategoryName = ""

    @SerializedName("material_unit")
    var materialUnit = ArrayList<MaterialUnit>()

    @SerializedName("material_unit_full_name")
    var materialUnitFullName = ""

    @SerializedName("material_unit_id")
    var materialUnitId = 0L

    @SerializedName("material_unit_name")
    var materialUnitName = ""

    @SerializedName("material_unit_specification_exchange_name")
    var materialUnitSpecificationExchangeName = ""

    @SerializedName("material_unit_specification_exchange_value")
    var materialUnitSpecificationExchangeValue = 0

    @SerializedName("material_unit_specification_id")
    var materialUnitSpecificationId = 0L

    @SerializedName("material_unit_specification_name")
    var materialUnitSpecificationName = ""

    @SerializedName("name")
    var name = ""

    @SerializedName("normalize_name")
    var normalizeName = ""

    @SerializedName("out_stock_alert_quantity")
    var outStockAlertQuantity = 0.00

    @SerializedName("prefix")
    var prefix = ""

    @SerializedName("price")
    var price = 0L

    @SerializedName("remain_quantity")
    var remainQuantity = 0.0

    @SerializedName("restaurant_material_copy_id")
    var restaurantMaterialCopyId = 0L

    @SerializedName("retail_price")
    var retailPrice = 0L

    @SerializedName("status")
    var status = 0

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("updated_at")
    var updatedAt = ""

    @SerializedName("wastage_rate")
    var wastageRate = 0.00

    @SerializedName("wholesale_price")
    var wholesalePrice = 0L

    @SerializedName("wholesale_price_quantity")
    var wholesalePriceQuantity = 0.00

    @SerializedName("total_quantity_from_order")
    var totalQuantityFromOrder = 0.00

    @SerializedName("total_import_quantity")
    var totalImportQuantity = 0.00

    @SerializedName("total_amount_from_quantity_import")
    var totalAmountFromQuantityImport = 0L

    var isChecked = false
    var totalImport = 0.00
    var intoMoney = 0L
    var isChanged = false

    constructor()
    constructor(
        code: String,
        name: String,
        materialUnitId: Long,
        restaurantMaterialCopyId: Long,
        materialUnitSpecificationId: Long,
        materialCategoryId: Long,
        avatar: String,
        avatarThumb: String,
        price: Long,
        retailPrice: Long,
        wholesalePriceQuantity: Double,
        outStockAlertQuantity: Double,
        wastageRate: Double,
        status: Int
    ) {
        this.code = code
        this.name = name
        this.materialUnitId = materialUnitId
        this.restaurantMaterialCopyId = restaurantMaterialCopyId
        this.materialUnitSpecificationId = materialUnitSpecificationId
        this.materialCategoryId = materialCategoryId
        this.avatar = avatar
        this.avatarThumb = avatarThumb
        this.price = price
        this.retailPrice = retailPrice
        this.wholesalePriceQuantity = wholesalePriceQuantity
        this.outStockAlertQuantity = outStockAlertQuantity
        this.wastageRate = wastageRate
        this.status = status
    }

    fun copy(): Material {
        val cloneMaterial = Material()
        cloneMaterial.avatar = this.avatar
        cloneMaterial.avatarThumb = this.avatarThumb
        cloneMaterial.code = this.code
        cloneMaterial.createdAt = this.createdAt
        cloneMaterial.description = this.description
        cloneMaterial.id = this.id
        cloneMaterial.isUpdate = this.isUpdate
        cloneMaterial.materialCategoryId = this.materialCategoryId
        cloneMaterial.materialCategoryName = this.materialCategoryName
        cloneMaterial.materialUnit = this.materialUnit
        cloneMaterial.materialUnitFullName = this.materialUnitFullName
        cloneMaterial.materialUnitId = this.materialUnitId
        cloneMaterial.materialUnitName = this.materialUnitName
        cloneMaterial.materialUnitSpecificationExchangeName =
            this.materialUnitSpecificationExchangeName
        cloneMaterial.materialUnitSpecificationExchangeValue =
            this.materialUnitSpecificationExchangeValue
        cloneMaterial.materialUnitSpecificationId = this.materialUnitSpecificationId
        cloneMaterial.materialUnitSpecificationName = this.materialUnitSpecificationName
        cloneMaterial.name = this.name
        cloneMaterial.normalizeName = this.normalizeName
        cloneMaterial.outStockAlertQuantity = this.outStockAlertQuantity
        cloneMaterial.prefix = this.prefix
        cloneMaterial.price = this.price
        cloneMaterial.remainQuantity = this.remainQuantity
        cloneMaterial.restaurantMaterialCopyId = this.restaurantMaterialCopyId
        cloneMaterial.retailPrice = this.retailPrice
        cloneMaterial.status = this.status
        cloneMaterial.supplierId = this.supplierId
        cloneMaterial.updatedAt = this.updatedAt
        cloneMaterial.wastageRate = this.wastageRate
        cloneMaterial.wholesalePrice = this.wholesalePrice
        cloneMaterial.wholesalePriceQuantity = this.wholesalePriceQuantity
        return cloneMaterial
    }

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

        override fun toString(): String {
            return name
        }
    }

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

        override fun toString(): String {
            return name
        }

    }
}