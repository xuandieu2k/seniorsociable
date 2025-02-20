package vn.xdeuhug.seniorsociable.model.entity


import com.google.gson.annotations.SerializedName

/**
 * @Author: Nguyen Xuan Dieu
 * @Date: 21/07/2023
 */
class Order {
    @SerializedName("amount")
    var amount = 0

    @SerializedName("amount_reality")
    var amountReality = 0

    @SerializedName("branch_id")
    var branchId = 0L

    @SerializedName("branch_name")
    var branchName = ""

    @SerializedName("code")
    var code = ""

    @SerializedName("created_at")
    var createdAt = ""

    @SerializedName("delivery_at")
    var deliveryAt = ""

    @SerializedName("discount_amount")
    var discountAmount = 0

    @SerializedName("discount_percent")
    var discountPercent = 0.0

    @SerializedName("employee_cancel_full_name")
    var employeeCancelFullName = ""

    @SerializedName("employee_cancel_id")
    var employeeCancelId = 0L

    @SerializedName("employee_complete_full_name")
    var employeeCompleteFullName = ""

    @SerializedName("employee_complete_id")
    var employeeCompleteId = 0L

    @SerializedName("employee_created_full_name")
    var employeeCreatedFullName = ""

    @SerializedName("employee_created_id")
    var employeeCreatedId = 0L

    @SerializedName("id")
    var id = 0L

    @SerializedName("is_return_all_total_material")
    var isReturnAllTotalMaterial = 0

    @SerializedName("payment_status")
    var paymentStatus = 0

    @SerializedName("reason")
    var reason = ""

    @SerializedName("received_at")
    var receivedAt = ""

    @SerializedName("restaurant_avatar")
    var restaurantAvatar = ""

    @SerializedName("restaurant_brand_id")
    var restaurantBrandId = 0L

    @SerializedName("restaurant_brand_name")
    var restaurantBrandName = ""

    @SerializedName("restaurant_debt_amount")
    var restaurantDebtAmount = 0

    @SerializedName("restaurant_id")
    var restaurantId = 0L

    @SerializedName("restaurant_material_order_request_id")
    var restaurantMaterialOrderRequestId = 0L

    @SerializedName("restaurant_name")
    var restaurantName = ""

    @SerializedName("status")
    var status = 0

    @SerializedName("supplier_avatar")
    var supplierAvatar = ""

    @SerializedName("supplier_employee_cancel_full_name")
    var supplierEmployeeCancelFullName = ""

    @SerializedName("supplier_employee_cancel_id")
    var supplierEmployeeCancelId = 0L

    @SerializedName("supplier_employee_created_full_name")
    var supplierEmployeeCreatedFullName = ""

    @SerializedName("supplier_employee_created_id")
    var supplierEmployeeCreatedId = 0L

    @SerializedName("supplier_employee_delivering_avatar")
    var supplierEmployeeDeliveringAvatar = ""

    @SerializedName("supplier_employee_delivering_name")
    var supplierEmployeeDeliveringName = ""

    @SerializedName("supplier_id")
    var supplierId = 0L

    @SerializedName("supplier_name")
    var supplierName = ""

    @SerializedName("supplier_order_detail")
    var supplierOrderDetail = ArrayList<SupplierOrderDetail>()

    @SerializedName("supplier_order_request_id")
    var supplierOrderRequestId = 0L

    @SerializedName("supplier_phone")
    var supplierPhone = ""

    @SerializedName("supplier_warehouse_session_id")
    var supplierWarehouseSessionId = 0L

    @SerializedName("total_amount")
    var totalAmount = 0

    @SerializedName("total_amount_of_return_material_reality")
    var totalAmountOfReturnMaterialReality = 0

    @SerializedName("total_amount_reality")
    var totalAmountReality = 0

    @SerializedName("total_material")
    var totalMaterial = 0

    @SerializedName("vat")
    var vat = 0.0

    @SerializedName("vat_amount")
    var vatAmount = 0

    var isChecked = false
}