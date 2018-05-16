package io.dz.faceunlock

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.orhanobut.logger.Logger
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions
import com.qiniu.util.Auth
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.fotoapparat.Fotoapparat
import io.fotoapparat.FotoapparatSwitcher
import io.fotoapparat.facedetector.processor.FaceDetectorProcessor
import io.fotoapparat.parameter.LensPosition
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition
import io.fotoapparat.parameter.selector.SizeSelectors
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class MainActivity : RxAppCompatActivity() {

    private val permissionsDelegate = PermissionsDelegate(this)
    private var hasCameraPermission: Boolean = false

    private var fotoapparatSwitcher: FotoapparatSwitcher? = null
    private var frontFotoapparat: Fotoapparat? = null

    private var isCheck = false

    private var tempImageFace: String? = null

    private var isUnlock = false// 人脸比对解锁/采集

    private var handler: Handler? = null

    private var checkTimeOutRunnable: Runnable? = null

    private var mContext: Context? = null

    private var image_url2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContext = this

        var sp = getSharedPreferences("face_unlock", Context.MODE_PRIVATE)

        image_url2 = sp.getString("face_img", null)
        tempImageFace = "${getExternalFilesDir(null).absolutePath}/face_${System.currentTimeMillis()}.png"

        isUnlock = image_url2 != null

        hasCameraPermission = permissionsDelegate.hasCameraPermission()

        if (hasCameraPermission) {
            camera_view.visibility = View.VISIBLE
        } else {
            permissionsDelegate.requestCameraPermission()
        }

        frontFotoapparat = createFotoapparat(LensPosition.FRONT)

        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(frontFotoapparat!!)
        fotoapparatSwitcher!!.switchTo(frontFotoapparat!!)
        handler = Handler()
//        checkTimeOutRunnable = Runnable {
//            verifyFaceFail()
//        }
//        handler!!.postDelayed(checkTimeOutRunnable, 10000)
        if (isUnlock) {
            iv_face_success.visibility = View.VISIBLE
            ll_camera.visibility = View.GONE

            btn_submit.isEnabled = true
            btn_submit.text = "比对"
            btn_submit.setOnClickListener({
                btn_submit.text = "正在比对.."
                iv_face_success.visibility = View.GONE
                ll_camera.visibility = View.VISIBLE
                unlockFaceImage()
            })
        }
    }


    private fun createFotoapparat(position: LensPosition): Fotoapparat {
        return Fotoapparat
                .with(this)
                .into(camera_view)
                .photoSize(SizeSelectors.smallestSize())
                .previewScaleType(ScaleType.CENTER_CROP)
                .lensPosition(lensPosition(position))
                .frameProcessor(
                        FaceDetectorProcessor.with(this)
                                .listener { faces ->
                                    if (!isCheck && faces.size > 0) {
                                        takePicture()
                                    }
                                    rectanglesView!!.setRectangles(faces)
                                }
                                .build()
                )
                .build()
    }

    override fun onStart() {
        super.onStart()
        if (hasCameraPermission) {
            fotoapparatSwitcher!!.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (iv_face_success.visibility == View.GONE && hasCameraPermission) {
            fotoapparatSwitcher!!.stop()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            fotoapparatSwitcher?.start()
            camera_view.visibility = View.VISIBLE
        }
    }


    private fun takePicture() {
        isCheck = true
        var photoResult = frontFotoapparat?.takePicture()

        photoResult
                ?.toBitmap()
                ?.whenAvailable { bitmapPhoto ->
                    Observable.just(bitmapPhoto).observeOn(Schedulers.newThread()).doOnNext({ bitmapFile ->
                        ImageTool.save(rotateBitmap(bitmapFile.bitmap, -bitmapFile.rotationDegrees), tempImageFace, Bitmap.CompressFormat.PNG)
//                        photoResult.saveToFile(File(tempImageFace))
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe {
                        if (!isUnlock) {
                            checkFaceImage()
                        } else {
                            unlockFaceImage()
                        }
                    }
                }

    }

    private fun checkFaceImage() {
        var file = File(tempImageFace)
        if (file.exists()) {
            var imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            var builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", "K94tC5wqszOgcMOsgPrd-obzfzHWmXvI")
                    .addFormDataPart("api_secret", "pUraBpAMwu7DGxU-BK3MEa0Qn3l95SMT")
                    .addFormDataPart("return_attributes", "facequality")
                    .addFormDataPart("image_file", file.name, imageBody)
            var parts = builder.build().parts()
            UploadApi.getApiService()
                    .uploadFile("https://api-cn.faceplusplus.com/facepp/v3/detect", parts)
                    .compose(this.bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Any> {
                        override fun onError(e: Throwable) {
                            verifyFaceFail()
                        }

                        override fun onComplete() {
                        }

                        override fun onNext(t: Any) {
                            Logger.e(JSON.toJSONString(t))
                            Logger.json(JSON.toJSONString(t))
                            var response = JSON.parseObject(JSON.toJSONString(t), FaceImgInfo::class.java)

                            if (TextUtils.isEmpty(response.error_message) && response.faces.size > 0) {
                                var facequality = response.faces[0].attributes.facequality
                                //如果图片质量达到人脸检测标准
                                if (facequality.value >= facequality.threshold) {
                                    upLoadFaceUrl()
                                } else {
                                    isCheck = false
                                }
                            } else {
                                isCheck = false
                            }
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                    })
        }
    }

    // 人脸比对解锁
    private fun unlockFaceImage() {
        var file = File(tempImageFace)
        if (file.exists()) {
            var imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            var builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", "K94tC5wqszOgcMOsgPrd-obzfzHWmXvI")
                    .addFormDataPart("api_secret", "pUraBpAMwu7DGxU-BK3MEa0Qn3l95SMT")
                    .addFormDataPart("return_attributes", "facequality")
                    .addFormDataPart("image_file1", file.name, imageBody)
                    .addFormDataPart("image_url2", image_url2)
            var parts = builder.build().parts()
            UploadApi.getApiService()
                    .uploadFile("https://api-cn.faceplusplus.com/facepp/v3/compare", parts)
                    .compose(this.bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Any> {
                        override fun onError(e: Throwable) {
                            Logger.e(e.message)
                            Toast.makeText(mContext, "比对失败，请重新识别", Toast.LENGTH_SHORT).show()
                            verifyFaceFail()
                        }

                        override fun onComplete() {
                        }

                        override fun onNext(t: Any) {
                            Logger.e(JSON.toJSONString(t))
                            Logger.json(JSON.toJSONString(t))
                            var response = JSON.parseObject(JSON.toJSONString(t), FaceImgInfo::class.java)

                            if (TextUtils.isEmpty(response.error_message) && response.thresholds != null && response.confidence > 0) {
                                val value = JSON.parseObject(response.thresholds).getDoubleValue("1e-5")
                                Logger.e(value.toString())
                                //如果人脸比对质量达到阈值
                                if (response.confidence >= value) {
                                    handler?.removeCallbacks(checkTimeOutRunnable)
                                    Toast.makeText(mContext, "比对成功", Toast.LENGTH_SHORT).show()
                                    compareSucess()
                                } else {
                                    Toast.makeText(mContext, "比对失败，请重新识别", Toast.LENGTH_SHORT).show()
                                    verifyFaceFail()
                                }
                            } else {
                                isCheck = false
                            }
                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                    })
        }
    }

    //验证成功
    private fun verifyFaceSuccess() {
        handler?.removeCallbacks(checkTimeOutRunnable)
        var editor = getSharedPreferences("face_unlock", Context.MODE_PRIVATE).edit()
        editor.putString("face_img", image_url2)
        editor.commit()

        handler?.removeCallbacks(checkTimeOutRunnable)
        isUnlock = true

        if (hasCameraPermission) {
            fotoapparatSwitcher!!.stop()
        }

        iv_face_success.visibility = View.VISIBLE
        ll_camera.visibility = View.GONE

        btn_submit.isEnabled = true
        btn_submit.text = "比对"
        btn_submit.setOnClickListener({
            btn_submit.text = "正在比对.."
            iv_face_success.visibility = View.GONE
            ll_camera.visibility = View.VISIBLE
            if (hasCameraPermission) {
                fotoapparatSwitcher!!.start()
            }
            unlockFaceImage()
        })
    }

    //验证失败
    private fun verifyFaceFail() {
        if (!isUnlock) {
            tv_face_tip.setTextColor(Color.RED)
            tv_face_tip.text = "识别失败，请调整光源角度再尝试"
            btn_submit.isEnabled = true
            btn_submit.text = "重新识别"
            btn_submit.setOnClickListener({
                tv_face_tip.text = ""
                btn_submit.text = "验证中.."
                isCheck = false
                handler?.postDelayed(checkTimeOutRunnable, 10000)
            })
        } else {
            isCheck = false
            tv_face_tip.setTextColor(Color.RED)
            tv_face_tip.text = "比对失败，请调整光源角度再尝试"
        }
    }

    // 比对成功
    private fun compareSucess() {
        tv_face_tip.setTextColor(Color.GREEN)
        tv_face_tip.text = "比对成功!"
        btn_submit.text = "重置"
        btn_submit.isEnabled = true
        iv_face_success.visibility = View.VISIBLE
        ll_camera.visibility = View.GONE
        if (hasCameraPermission) {
            fotoapparatSwitcher!!.stop()
        }
        faceReset()
    }

    private fun faceReset() {
        btn_submit.setOnClickListener({
            isCheck = false
            isUnlock = false
            var editor = getSharedPreferences("face_unlock", Context.MODE_PRIVATE).edit()
            editor.remove("face_img")
            editor.commit()
            btn_submit.isEnabled = false
            btn_submit.text = "识别中"
            tv_face_tip.text = ""
            iv_face_success.visibility = View.GONE
            ll_camera.visibility = View.VISIBLE
            if (hasCameraPermission) {
                fotoapparatSwitcher!!.start()
            }
        })
    }

    private var uploadManager: UploadManager? = null

    private fun upLoadFaceUrl() {
        if (uploadManager == null)
            uploadManager = UploadManager()

        //...生成上传凭证，然后准备上传
        val accessKey = "4GGiMm_HbCuDTHDpa8tV-_Rx6SGa7AOr61hKMB01"
        val secretKey = "FQOnQT4W03dONKWnsOasGKKEbDkI0MNfdjHcirz-"
        val bucket = "face"
        val auth = Auth.create(accessKey, secretKey)
//        val upToken = ""
        val upToken = auth.uploadToken(bucket, null, 3600 * 24 * 365, null, true)

        var file = File(tempImageFace)
        val key = file.name

        uploadManager!!.put(file, key, upToken, { key, info, response ->
            if (info!!.isOK) {
                image_url2 = "http://p8syeqse8.bkt.clouddn.com/$key"
                verifyFaceSuccess()
            } else {
                verifyFaceFail()
            }
        }, UploadOptions(null, "test-type", true, null, null))

    }

    private fun rotateBitmap(bitmap: Bitmap, rotationAngleDegree: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        var newW = w
        var newH = h
        if (rotationAngleDegree == 90 || rotationAngleDegree == 270) {
            newW = h
            newH = w
        }
        val rotatedBitmap = Bitmap.createBitmap(newW, newH, bitmap.config)
        val canvas = Canvas(rotatedBitmap)

        val rect = Rect(0, 0, newW, newH)
        val matrix = Matrix()
        val px = rect.exactCenterX()
        val py = rect.exactCenterY()
        matrix.postTranslate((-bitmap.width / 2).toFloat(), (-bitmap.height / 2).toFloat())
        matrix.postRotate(rotationAngleDegree.toFloat())
        matrix.postTranslate(px, py)
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG))
        matrix.reset()

        return rotatedBitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(checkTimeOutRunnable)
        handler = null
    }
}
