package vn.xdeuhug.seniorsociable.model.entity.modelNewsData

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 07 / 10 / 2023
 */
class Topic {
    var id = ""
    var name = ""
    var englishName = ""
    var isSelected = false

    constructor(id: String, name: String, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.isSelected = isSelected
    }

    constructor(id: String, name: String, isSelected: Boolean, englishName: String) {
        this.id = id
        this.name = name
        this.englishName = englishName
        this.isSelected = isSelected
    }

    constructor()

}