package vn.xdeuhug.seniorsociable.router

@Suppress("FunctionName")
object ApiReportRouters {
    private const val VERSION = "v2"

    fun OVERVIEW_REPORT(): String{
        return "api/${VERSION}/supplier-overview-report"
    }

    fun LIST_ORDER_REPORT(): String{
        return "api/${VERSION}/supplier-order-list-report"
    }

    fun COST_PROFIT_ESTIMATED_REPORT(): String{
        return "api/${VERSION}/supplier-revenue-cost-profit-estimated-report"
    }

    fun REVENUE_REPORT(): String{
        return "api/${VERSION}/supplier-revenue-report-by-time"
    }

    fun WARE_HOUSE_MATERIAL_REPORT(): String{
        return "api/${VERSION}/supplier-warehouse-material-report"
    }

    fun WARE_HOUSE_SESSION_REPORT(): String{
        return "api/${VERSION}/supplier-warehouse-sesssion-report"
    }

    fun REPORT_BY_CUSTOMER(): String{
        return "api/${VERSION}/supplier_order_by_customer"
    }

    fun RETURNS_CANCEL_REPORT(): String{
        return "api/${VERSION}/supplier-material-cancel-return-report"
    }

    fun CATEGORY_REPORT(): String{
        return "api/${VERSION}/supplier-category-detail"
    }

    fun DEBT_REPORT(): String{
        return "api/${VERSION}/supplier-debt-report"
    }

    fun ORDER_SHIP_REPORT(): String{
        return "api/${VERSION}/supplier-order-by-date"
    }

    fun PRICE_FLUCTUATION_REPORT(): String{
        return "api/${VERSION}/supplier-material-price-fluctuation"
    }


}