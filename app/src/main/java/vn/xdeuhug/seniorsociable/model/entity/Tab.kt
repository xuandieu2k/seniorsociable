package vn.xdeuhug.seniorsociable.model.entity

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 27 / 11 / 2023
 */
class Tab {
    var id = 0
    var name = ""
    var value = 0
    var isSelected = false

    constructor(id: Int, name: String, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.isSelected = isSelected
    }

    constructor(id: Int, name: String, value: Int, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.value = value
        this.isSelected = isSelected
    }


}