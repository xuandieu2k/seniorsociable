package vn.xdeuhug.seniorsociable.router

@Suppress("FunctionName")
object ApiApplicationRouters {

    const val VERSION = "v5"

    fun API_GET_WARD(): String {
        return "api/${VERSION}/administrative-units/wards"
    }

    fun API_GET_DISTRICTS(): String {
        return "api/${VERSION}/administrative-units/districts"
    }

    fun API_GET_CITY(): String {
        return "api/${VERSION}/administrative-units/cities"
    }

    // EMPLOYEE

    fun API_GET_LIST_EMPLOYEES(): String {
        return "api/${VERSION}/employees"
    }

    fun API_CHANGE_STATUS_EMPLOYEES(id: Long): String {
        return "api/${VERSION}/employees/$id/change-status"
    }

    fun API_PAGING_EMPLOYEES(): String {
        return "api/${VERSION}/employees"
    }

    fun API_GET_SUPPLIER_ROLES(): String {
        return "api/${VERSION}/supplier-roles"
    }

    fun API_ADD_EMPLOYEE(): String {
        return "api/${VERSION}/employees/create"
    }

    fun API_RESET_PASSWORD_EMPLOYEE(id:Long): String {
        return "api/${VERSION}/employees/${id}/reset-password"
    }

    fun API_GET_LIST_MATERIALS(): String {
        return "api/${VERSION}/materials"
    }

    fun API_CHANGE_STATUS_MATERIAL(id: Long): String {
        return "api/v3/materials/${id}/change-status"
    }

    fun API_GET_LIST_MATERIAL_CATEGORIES(): String {
        return "api/${VERSION}/material-categories"
    }

    fun API_GET_LIST_MATERIAL_UNITS(): String {
        return "api/${VERSION}/material-units"
    }

    fun API_ADD_NEW_MATERIAL(): String {
        return "api/${VERSION}/materials/create"
    }

    fun API_UPDATE_MATERIAL(id: Long): String {
        return "api/${VERSION}/materials/${id}/update"
    }

    fun API_GET_LIST_REGULATION(): String {
        return "api/${VERSION}/supplier-material-unit-specifications"
    }

    fun CHANGES_STATUS_REGULATION(id: Long): String {
        return "api/${VERSION}/supplier-material-unit-specifications/${id}/change-status"
    }

    fun API_ADD_ITEM_REGULATION(): String {
        return "api/${VERSION}/supplier-material-unit-specifications/create"
    }

    fun API_GET_UNIT_REGULATION(): String {
        return "api/${VERSION}/material-unit-specification-exchange-name"
    }

    fun API_CHANGE_REGULATION(id: Long): String {
        return "api/${VERSION}/supplier-material-unit-specifications/${id}"
    }

    fun API_GET_INFORMATION_EMPLOYEE(id: Long): String {
        return "api/${VERSION}/employees/${id}"
    }

    fun API_ADD_UNIT(): String {
        return "api/${VERSION}/material-units/create"
    }

    fun API_UPDATE_UNIT(id: Long): String {
        return "api/${VERSION}/material-units/${id}"
    }

    fun API_CHANGE_STATUS_UNIT(id: Long): String {
        return "api/${VERSION}/material-units/${id}/change-status"
    }

    fun API_LIST_DEBT_PAYMENT_REQUEST(): String {
        return "api/${VERSION}/supplier-restaurant-debt-payment-requests"
    }


    fun API_GET_BILL_REVENUE_EXPENDITURE(): String {
        return "api/${VERSION}/supplier-addition-fees"
    }


    fun API_GET_CATEGORY(): String {
        return "api/${VERSION}/supplier-addition-fee-reasons"
    }

    fun API_GET_CATEGORY_LIST(): String {
        return "api/${VERSION}/supplier-addition-fee-reasons/addition-fee-reason-categories"
    }

    fun API_GET_CATEGORY_UPDATE(id: Long): String {
        return "api/${VERSION}/supplier-addition-fee-reasons/${id}/update"
    }

    fun API_CHANGE_STATUS_BILL(id: Long): String {
        return "api/${VERSION}/supplier-addition-fee-reasons/${id}/change-hidden"
    }

    fun API_DETAILS_PRICE_BILL(id: Long): String {
        return "api/${VERSION}/supplier-addition-fees/${id}"
    }

    fun API_CREATE_CATEGORY(): String {
        return "api/${VERSION}/supplier-addition-fee-reasons/create"
    }

    fun API_CREATE_BILL(): String {
        return "api/${VERSION}/supplier-addition-fees/create"
    }


    fun API_LIST_ORDER_CREATED(): String {
        return "api/${VERSION}/supplier-orders"
    }

    fun API_GET_ORDER_REQUEST_FORM(id: Long): String {
        return "api/${VERSION}/supplier-orders/${id}"
    }

    fun API_GET_LIST_SUPPLIER_WAREHOUSE_SESSION(): String {
        return "api/${VERSION}/supplier-warehouse-sessions"
    }

    fun API_CHANGE_STATUS(id: Long): String {
        return "api/${VERSION}/supplier-addition-fees/${id}/change-status"
    }

    fun API_CREATE_TICKET_IMPORT_WAREHOUSE(): String {
        return "api/${VERSION}/supplier-warehouse-sessions/create"
    }

    fun API_UPDATE_TICKET_IMPORT_WAREHOUSE(id: Long): String {
        return "api/${VERSION}/supplier-warehouse-sessions/${id}/update"
    }

    fun API_SUPPLIER_WAREHOUSE_SESSION_BY_ID(id: Long): String {
        return "api/${VERSION}/supplier-warehouse-sessions/${id}"
    }

    fun API_SUPPLIER_WAREHOUSE_SESSION_DETAILS_BY_ID(): String {
        return "api/${VERSION}/supplier-warehouse-sessions/details?"
    }

    fun API_SUPPLIER_WAREHOUSE_SESSION_DETAILS(id: Long): String {
        return "api/${VERSION}/supplier-warehouse-sessions/details?${id}"
    }

    fun API_GET_INFORMATION_SUPPLIER(): String {
        return "api/${VERSION}/suppliers"
    }

    fun API_CHANGE_STATUS_SUPPLIER_WAREHOUSE_SESSION(id: Long): String {
        return "api/${VERSION}/supplier-warehouse-sessions/${id}/change-status"
    }


    fun API_UPDATE_SUPPLIER(): String {
        return "api/${VERSION}/suppliers/update"
    }
    fun API_GET_RESTAURANTS(): String {
        return "api/${VERSION}/supplier-orders/group-by-restaurant"
    }

    fun API_GET_LIST_ORDER_OF_PAYMENT_REQUEST(): String {
        return "api/${VERSION}/supplier-orders/by-ids?"
    }

    fun API_CREATE_DEVT_PAYMENT_REQUEST(): String {
        return "api/${VERSION}/supplier-restaurant-debt-payment-requests/create"
    }

    fun API_GET_LIST_SUPPLIER_REQUEST_ORDER(): String {
        return "api/${VERSION}/supplier-order-request"
    }

    fun API_CHANGE_STATUS_PAYMENT_REQUEST(id: Long): String {
        return "api/${VERSION}/supplier-restaurant-debt-payment-requests/${id}/change-status"
    }

    fun API_GET_SUPPLIER_REQUEST_ORDER_DETAIL(id: Long): String {
        return "api/${VERSION}/supplier-order-request/${id}/supplier-order-request-detail"
    }

    fun API_CHANGE_STATUS_SUPPLIER_ORDER_REQUEST(id: Long): String {
        return "api/${VERSION}/supplier-order-request/${id}/change-status"
    }
    fun API_CONFIRM_SUPPLIER_ORDER(): String {
        return "api/${VERSION}/supplier-orders/confirm"
    }

    fun API_REVENUE_DETAILS(id:Long): String {
        return "api/${VERSION}/supplier-orders/${id}/supplier-order-detail"
    }

}