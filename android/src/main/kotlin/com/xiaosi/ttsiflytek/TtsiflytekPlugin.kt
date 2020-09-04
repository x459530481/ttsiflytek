package com.xiaosi.ttsiflytek

import android.content.Context
import android.os.Bundle
import android.os.Environment
import com.iflytek.cloud.*
import com.iflytek.cloud.util.ResourceUtil
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class TtsiflytekPlugin: MethodCallHandler {
  // 语音合成对象
  private var mTts: SpeechSynthesizer? = null

  // 默认本地发音人
  var voicerLocal = "xiaoyan"

  companion object {
    var mContext: Context? = null
    @JvmStatic
    fun registerWith(registrar: Registrar) {
//      registrar
//              .platformViewRegistry()
//              .registerViewFactory(
//                      "plugins.xiaosi.ttsiflytek", ScanViewFactory(registrar.messenger()));
      mContext = registrar.activity().applicationContext
      val channel = MethodChannel(registrar.messenger(), "ttsiflytek")
      channel.setMethodCallHandler(TtsiflytekPlugin())
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "initTTS") {
      // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
      // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
      // 参数间使用“,”分隔。
      // 设置你申请的应用appid

      // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
      val param = StringBuffer()
      var appid = ""
      if (call.hasArgument("appid")) {
        appid = call.argument<Any>("appid").toString()
      }
      if(appid.equals("")){
        println("appid is null !!!!!!!!!!")
        return
      }
      param.append("appid=$appid")
      param.append(",")
      // 设置使用v5+
      param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC)
      SpeechUtility.createUtility(mContext, param.toString())

      // 初始化合成对象
      mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener)
      result.success("Success")
    } else if (call.method == "play") {
      // 开始合成
      // 收到onCompleted 回调时，合成结束、生成合成音频
      // 合成的音频格式：只支持pcm格式
      println("TTS:play")
      var text = "测试文字"
      if (call.hasArgument("txt")) {
        text = call.argument<Any>("txt").toString()
      }
      if (text == null || text.length == 0) {
        return
      }
      // 设置参数
      setParam()
      println("准备点击： " + System.currentTimeMillis())
      val code: Int = mTts?.startSpeaking(text, mTtsListener) ?: 999
      //			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
      if (code != ErrorCode.SUCCESS) {
        println("语音合成失败,错误码: $code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
      }
      result.success("Success")
    } else if (call.method == "cancel") {
      // 取消合成
      println("TTS:cancel")
      mTts?.stopSpeaking()
      result.success("Success")
    } else if (call.method == "pause") {
      println("TTS:pause")
      // 暂停播放
      mTts?.pauseSpeaking()
      result.success("Success")
    } else if (call.method == "resume") {
      println("TTS:resume")
      // 继续播放
      mTts?.resumeSpeaking()
      result.success("Success")
    } else if (call.method == "destroy") {
      println("TTS:destroy")
      if (null != mTts) {
        mTts?.stopSpeaking()
        // 退出时释放连接
        mTts?.destroy()
      }
      result.success("Success")
    } else {
      result.notImplemented()
    }
  }

  //  @Override
  //  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  //    channel.setMethodCallHandler(null);
  //  }
  /**
   * 初始化监听。
   */
  private val mTtsInitListener = InitListener { code ->
    println("InitListener init() code = $code")
    if (code != ErrorCode.SUCCESS) {
      println("初始化失败,错误码：$code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
    } else {
      // 初始化成功，之后可以调用startSpeaking方法
      // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
      // 正确的做法是将onCreate中的startSpeaking调用移至这里
    }
  }

  /**
   * 参数设置
   */
  private fun setParam() {
    // 清空参数
    mTts?.setParameter(SpeechConstant.PARAMS, null)

    //设置合成
    //设置使用本地引擎
    mTts?.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
    //设置发音人资源路径
    mTts?.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath())
    //设置发音人
    mTts?.setParameter(SpeechConstant.VOICE_NAME, voicerLocal)

    //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
    //设置合成语速
    mTts?.setParameter(SpeechConstant.SPEED, "50")
    //设置合成音调
    mTts?.setParameter(SpeechConstant.PITCH, "50")
    //设置合成音量
    mTts?.setParameter(SpeechConstant.VOLUME, "50")
    //设置播放器音频流类型
    mTts?.setParameter(SpeechConstant.STREAM_TYPE, "3")
    //	mTts.setParameter(SpeechConstant.STREAM_TYPE, AudioManager.STREAM_MUSIC+"");

    // 设置播放合成音频打断音乐播放，默认为true
    mTts?.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")

    // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
    mTts?.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
    mTts?.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().toString() + "/msc/tts.wav")
  }

  //获取发音人资源路径
  private fun getResourcePath(): String? {
    val tempBuffer = StringBuffer()
    val type = "tts"
    //合成通用资源
    tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "$type/common.jet"))
    tempBuffer.append(";")
    //发音人资源
    tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, type + "/" + voicerLocal + ".jet"))
    return tempBuffer.toString()
  }

  //缓冲进度
  private var mPercentForBuffering = 0

  //播放进度
  private var mPercentForPlaying = 0

  /**
   * 合成回调监听。
   */
  private val mTtsListener: SynthesizerListener = object : SynthesizerListener {
    override fun onSpeakBegin() {
      //showTip("开始播放");
//      Log.d(TtsDemo.TAG,"开始播放："+ System.currentTimeMillis());
      println("开始播放：" + System.currentTimeMillis())
    }

    override fun onSpeakPaused() {
      println("暂停播放")
    }

    override fun onSpeakResumed() {
      println("继续播放")
    }

    override fun onBufferProgress(percent: Int, beginPos: Int, endPos: Int,
                                  info: String) {
      // 合成进度
      mPercentForBuffering = percent
      println(String.format("缓冲进度为%d%%，播放进度为%d%%",
              mPercentForBuffering, mPercentForPlaying))
    }

    override fun onSpeakProgress(percent: Int, beginPos: Int, endPos: Int) {
      // 播放进度
      mPercentForPlaying = percent
      println(String.format("缓冲进度为%d%%，播放进度为%d%%",
              mPercentForBuffering, mPercentForPlaying))
    }

    override fun onCompleted(error: SpeechError) {
      if (error == null) {
        println("播放完成")
      } else if (error != null) {
        println(error.getPlainDescription(true))
      }
    }

    override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle) {
      // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
      // 若使用本地能力，会话id为null
      if (SpeechEvent.EVENT_SESSION_ID == eventType) {
        val sid = obj.getString(SpeechEvent.KEY_EVENT_AUDIO_URL)
        println("session id =$sid")
      }

      //实时音频流输出参考
      /*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
				byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
				Log.e("MscSpeechLog", "buf is =" + buf);
			}*/
    }
  }
}