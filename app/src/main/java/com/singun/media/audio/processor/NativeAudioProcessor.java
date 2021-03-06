package com.singun.media.audio.processor;

import com.singun.media.audio.AudioConfig;
import com.singun.wrapper.WebRTC.ProcessorConfig;
import com.singun.wrapper.WebRTC.WebRTCWrapper;

/**
 * Created by singun on 2017/3/3 0003.
 */

class NativeAudioProcessor {
    private WebRTCWrapper mWebRTCWrapper;

    private AudioConfig mAudioConfig;
    private AudioProcessConfig mAudioProcessConfig;

    public NativeAudioProcessor(AudioConfig audioConfig, AudioProcessConfig audioProcessConfig) {
        mAudioConfig = audioConfig;
        mAudioProcessConfig = audioProcessConfig;

        mWebRTCWrapper = new WebRTCWrapper();
        if (mAudioProcessConfig.noiseSuppress && !AndroidAudioProcessorSupport.isSupportNoiseSuppressor()) {
            enableNativeProcessor();
        }
    }

    public void enableNativeProcessor() {
        if (!mWebRTCWrapper.isInit()) {
            mWebRTCWrapper.init(mAudioConfig.sampleRateInHz);
        }
    }

    public boolean processAudioData(AudioConfig audioConfig, int length) {
        if (needProcessData()) {
            short[] bufferCopy = new short[length];
            System.arraycopy(audioConfig.audioDataIn, 0, bufferCopy, 0, length);

            short[] bufferResult = processData(bufferCopy, length);

            audioConfig.audioDataOut = bufferResult;

            return true;
        } else {
            return false;
        }
    }

    public short[] processAudioData(short[] data, int length) {
        if (needProcessData()) {
            short[] bufferCopy = new short[length];
            System.arraycopy(data, 0, bufferCopy, 0, length);

            return processData(bufferCopy, length);
        } else {
            return null;
        }
    }

    private boolean needProcessData() {
        return (mAudioProcessConfig.noiseSuppress && NativeAudioProcessorSupport.isSupportNoiseSuppressor()) ||
                (mAudioProcessConfig.gainControl && NativeAudioProcessorSupport.isSupportGainControl()) ||
                (mAudioProcessConfig.echoCancel && NativeAudioProcessorSupport.isSupportEchoCanceler());
    }

    private short[] processData(short[] data, int length) {
        short[] dataOut = null;
        if (mAudioProcessConfig.noiseSuppress) {
            dataOut = mWebRTCWrapper.processNoiseSuppress(data, length);
        }
        if (mAudioProcessConfig.echoCancel) {
            dataOut = mWebRTCWrapper.processEchoCancel(data, dataOut, length);
        }
        if (mAudioProcessConfig.gainControl) {
            short[] dataIn = dataOut == null ? data : dataOut;
            dataOut = mWebRTCWrapper.processGainControl(dataIn, length);
        }
        return dataOut;
    }

    public void setNoiseSuppressMode(int mode) {
        mWebRTCWrapper.setNoiseSuppressMode(mode);
    }

    public void setGainControlConfig(int db, int dbfs) {
        mWebRTCWrapper.setGainControlConfig(db, dbfs);
    }

    public ProcessorConfig getConfig() {
        return mWebRTCWrapper.getConfig();
    }

    public void release() {
        if (mWebRTCWrapper.isInit()) {
            mWebRTCWrapper.release();
        }
    }
}
