package vn.xdeuhug.seniorsociable.constants

object NotificationConstants {
    const val TEST = 194 //

    val RESTAURANT_ASSIGN_SUPPLIER = 122 //dashboard gán ncc(api dashboard)
    val RESTAURANT_WAREHOUSE_SESSION_RETURN_MATERIAL_SUPPLIER = 160//api dashboard ( nhà hàng trả hàng)
    val RESTAURANT_SUPPLIER_ORDER_CANCEL = 163 // dashboard hủy đơn hàng ncc (api dashboard)
    val RESTAURANT_SUPPLIER_ORDER_COMPLETE = 164//dashboard nhận đơn hàng ncc (api dashboard)
    val RESTAURANT_SUPPLIER_ORDER_REQUEST_CONFIRM = 166
    val RESTAURANT_UNASSIGN_SUPPLIER = 182//dashboard gỡ gán ncc(api dashboard)
    val SUPPLIER_CANCEL_WAREHOUSE_SESSION = 602//ncc xác nhận phiếu đặt hàng từ nhà hàng
    val SUPPLIER_ADDITIONFEE_CANCEL = 604//supplier hủy phiếu chi  (api/v4//supplier-warehouse-sessions/create-cancel)
    val SUPPLIER_ADDITION_FEE_REASON = 609
    val SUPPLIER_MATERIAL_CATEGORY = 610//(tạo danh mục  nguyên liệu : api/v4/material-categories/create)
    val ADMIN_CREATE_SUPPLIER_MATERIAL = 703//(admin tạo  nguyên liệu cho ncc, api admin)






}