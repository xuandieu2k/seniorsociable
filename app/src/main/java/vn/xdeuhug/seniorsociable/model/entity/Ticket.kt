package vn.xdeuhug.seniorsociable.model.entity


import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class Ticket {
    @SerializedName("amount")
    var amount = 0.0

    @SerializedName("code")
    var code = ""

    @SerializedName("created_at")
    var createdAt = ""

    @SerializedName("delivery_date")
    var deliveryDate = ""

    @SerializedName("discount_amount")
    var discountAmount = 0.0

    @SerializedName("discount_percent")
    var discountPercent = 0.0

    @SerializedName("discount_type")
    var discountType = 0

    @SerializedName("id")
    var id = 0L

    @SerializedName("is_deleted")
    var isDeleted = 0

    @SerializedName("is_include_vat")
    var isIncludeVat = 0

    @SerializedName("note")
    var note = ""

    @SerializedName("number")
    var number = 0

    @SerializedName("payment_date")
    var paymentDate = ""

    @SerializedName("payment_status")
    var paymentStatus = 0

    @SerializedName("source_of_supply_name")
    var sourceOfSupplyName = ""

    @SerializedName("status")
    var status = 0

    @SerializedName("supplier_employee_id")
    var supplierEmployeeId = 0

    @SerializedName("supplier_employee_name")
    var supplierEmployeeName = ""

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("supplier_order_id")
    var supplierOrderId = 0L

    @SerializedName("total_amount")
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @SerializedName("total_material")
    var totalMaterial = 0

    @SerializedName("type")
    var type = 0

    @SerializedName("updated_at")
    var updatedAt = ""

    @SerializedName("vat")
    var vat = 0.0

    @SerializedName("vat_amount")
    var vatAmount = 0.0



}