package vn.xdeuhug.seniorsociable.model.entity.modelReport

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class Reason {
    var id = 0
    var name = ""
    var isHint = false

    constructor(id: Int, name: String, isHint: Boolean) {
        this.id = id
        this.name = name
        this.isHint = isHint
    }

    override fun toString(): String {
        return name
    }

}