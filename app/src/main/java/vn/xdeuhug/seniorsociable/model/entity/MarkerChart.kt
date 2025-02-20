package vn.xdeuhug.seniorsociable.model.entity

import java.math.BigDecimal

class MarkerChart {

    var total = BigDecimal.ZERO
    var amount = 0

    constructor( total : BigDecimal, amount: Int){
        this.total = total
        this.amount = amount
    }
    constructor()
}