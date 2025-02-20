package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 21/07/2023
 */
class SupplierOrderDetail {
    @SerializedName("id")
    var id = 0L

    @SerializedName("quantity")
    var quantity = 0.0

    @SerializedName("supplier_material_id")
    var supplierMaterialId = 0L

    @SerializedName("supplier_material_name")
    var supplierMaterialName = ""

    @SerializedName("supplier_order_id")
    var supplierOrderId = 0L

    @SerializedName("supplier_unit_name")
    var supplierUnitName = ""

}