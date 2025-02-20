package vn.xdeuhug.seniorsociable.utility.ui.activity

import android.view.View
import com.denzcoskun.imageslider.constants.AnimationTypes
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.gson.Gson
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.utility.databinding.ActivityDetailDiseasesBinding
import vn.xdeuhug.seniorsociable.utility.model.Diseases


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 12 / 2023
 */
class DetailDiseaseActivity : AppActivity() {
    private lateinit var binding: ActivityDetailDiseasesBinding
    private var diseases = Diseases()
    override fun getLayoutView(): View {
        binding = ActivityDetailDiseasesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
    }

    private fun setDataForView() {
        diseases = Gson().fromJson(
            intent.getStringExtra(AppConstants.OBJECT_DISEASES), Diseases::class.java
        )
        setTextObject()
        val imageList: MutableList<SlideModel> = ArrayList()
        for (url in diseases.image!!) {
            imageList.add(SlideModel(url, diseases.name, ScaleTypes.FIT))
        }
        binding.tvTitle.text = diseases.name
        binding.imageSlider.setImageList(imageList)
        binding.imageSlider.setSlideAnimation(AnimationTypes.ZOOM_OUT)
        // TỔng quan
        // TỔng quan
        binding.tvTitleOverview.text = "Tổng quan"
        binding.tvOverview.text = diseases.overview
        // Nguyên nhân
        // Nguyên nhân
        binding.tvTitleReason.text = "Nguyên nhân"
        binding.tvReason.text = diseases.reason
        // Triệu chứng
        // Triệu chứng
        binding.tvTitleSymptom.text = "Triệu chứng"
        binding.tvSymptom.text = diseases.symptom
        // Đường lây truyền
        // Đường lây truyền
        binding.tvTitleTransmissionRoute.text = "Đường lây truyền"
        binding.tvTransmissionRoute.text = diseases.transmissionRoute
        // Đối tượng nguy cơ
        // Đối tượng nguy cơ
        binding.tvTitleSubjectsAtRisk.text = "Đối tượng nguy cơ"
        binding.tvSubjectsAtRisk.text = diseases.subjectsAtRisk
        // Phòng ngừa
        // Phòng ngừa
        binding.tvTitlePrevent.text = "Phòng ngừa"
        binding.tvPrevent.text = diseases.prevent
        // Biện pháp chuẩn đoán
        // Biện pháp chuẩn đoán
        binding.tvTitleDiagnosticMeasures.text = "Biện pháp chuẩn đoán"
        binding.tvDiagnosticMeasures.text = diseases.diagnosticMeasures
        // Biện pháp điều trị
        // Biện pháp điều trị
        binding.tvTitleTreatmentMeasures.text = "Biện pháp điều trị"
        binding.tvTreatmentMeasures.text = diseases.treatmentMeasures

        binding.btnBack.clickWithDebounce{
            finish()
        }
    }

    override fun initData() {
        //
    }

    fun setTextObject() {
        if (diseases.overview == null || diseases.overview!!.trim().isEmpty()) {
            diseases.overview = "Chưa xác định"
        }
        if (diseases.reason == null || diseases.reason!!.trim().isEmpty()) {
            diseases.reason = "Chưa xác định"
        }
        if (diseases.diagnosticMeasures == null || diseases.diagnosticMeasures!!.trim().isEmpty()) {
            diseases.diagnosticMeasures = "Chưa xác định"
        }
        if (diseases.prevent == null || diseases.prevent!!.trim().isEmpty()) {
            diseases.prevent = "Chưa xác định"
        }
        if (diseases.subjectsAtRisk == null || diseases.subjectsAtRisk!!.trim().isEmpty()) {
            diseases.subjectsAtRisk = "Chưa xác định"
        }
        if (diseases.transmissionRoute == null || diseases.transmissionRoute!!.trim().isEmpty()) {
            diseases.transmissionRoute = ("Chưa xác định")
        }
        if (diseases.treatmentMeasures == null || diseases.treatmentMeasures!!.trim().isEmpty()) {
            diseases.treatmentMeasures = ("Chưa xác định")
        }
        if (diseases.symptom == null || diseases.symptom!!.trim().isEmpty()) {
            diseases.symptom = "Chưa xác định"
        }
    }
}