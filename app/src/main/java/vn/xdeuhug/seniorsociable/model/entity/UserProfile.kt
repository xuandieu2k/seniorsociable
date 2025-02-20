package vn.xdeuhug.seniorsociable.model.entity

import com.google.gson.annotations.SerializedName

/**
 * @Author: Trần Tuấn Vũ
 * @Date: 21/12/2022
 */
class UserProfile {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("name")
    var name: String = ""

    @SerializedName("username")
    var username: String = ""

    @SerializedName("email")
    var email: String = ""

    @SerializedName("phone")
    var phone: String = ""

    @SerializedName("address")
    var address: String = ""

    @SerializedName("avatar")
    var avatar: String = ""

    @SerializedName("point")
    var point: Double = 0.0

    @SerializedName("status")
    var status: Int = 0

    @SerializedName("branch_id")
    var branchId: Int = 0

    @SerializedName("gender")
    var gender: Int = 0

    @SerializedName("branch_name")
    var branchName: String = ""

    @SerializedName("employee_role_id")
    var employeeRoleId: Int = 0

    @SerializedName("employee_role_type")
    var employeeRoleType: Int = 0

    @SerializedName("role_name")
    var roleName: String = ""

    @SerializedName("passport")
    var passport: String = ""

    @SerializedName("birth_place")
    var birthPlace: String = ""

    @SerializedName("birthday")
    var birthday: String = ""

    @SerializedName("employee_rank_id")
    var employeeRankId: Int = 0

    @SerializedName("employee_rank_name")
    var employeeRankName: String = ""

    @SerializedName("salary_level_id")
    var salaryLevelId: Int = 0

    @SerializedName("salary_level")
    var salaryLevel: String = ""

    @SerializedName("working_session_id")
    var workingSessionId: Int = 0

    @SerializedName("working_session_time")
    var workingSessionTime: String = ""

    @SerializedName("working_session_name")
    var workingSessionName: String = ""

    @SerializedName("manage_areas")
    var manageAreas: List<Any> = ArrayList()

    @SerializedName("area_id")
    var areaId: Int = 0

    @SerializedName("area_name")
    var areaName: String = ""

    @SerializedName("id_in_timekeeper")
    var idInTimekeeper: Int = 0

    @SerializedName("point_used")
    var pointUsed: Double = 0.0

    @SerializedName("total_point")
    var totalPoint: Double = 0.0

    @SerializedName("is_working")
    var isWorking: Int = 0

    @SerializedName("is_bypass_checkin")
    var isBypassCheckin: Int = 0

    @SerializedName("is_quit_job")
    var isQuitJob: Int = 0

    @SerializedName("working_from")
    var workingFrom: String = ""

    @SerializedName("restaurant_brand_name")
    var restaurantBrandName: String = ""

    fun getGenderName(): String {
        return if (gender == 1) {
            "Nam"
        } else {
            "Nữ"
        }
    }
}