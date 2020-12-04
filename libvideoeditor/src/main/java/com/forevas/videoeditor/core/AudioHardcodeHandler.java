package com.forevas.videoeditor.core;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.forevas.videoeditor.jni.AudioJniUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * desc 硬编码实现音频操作
 * 1、从视频中提取音频
 * 2、音频混音，并控制两个音频的声音大小
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AudioHardcodeHandler {
    /**
     * 从视频中提取到音频文件，输出文件为aac
     */
    public static void getAudioFromVideo(String inputFile, String outputFile) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(inputFile);

        int audioTrackIndex = -1;
        boolean hasAudio = false;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = extractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//                int bitRate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
//                Log.e("hero", "---sampleRate---" + sampleRate + "--channelCount--" + channelCount);
                audioTrackIndex = i;
                hasAudio = true;
                break;
            }
        }
        if (hasAudio) {
            extractor.selectTrack(audioTrackIndex);
            MediaMuxer mediaMuxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat trackFormat = extractor.getTrackFormat(audioTrackIndex);
            int writeAudioIndex = mediaMuxer.addTrack(trackFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            long stampTime = 0;
            int count = 0;//已转换的时长

            extractor.readSampleData(byteBuffer, 0);
            if (extractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                extractor.advance();
            }
            /**计算两个帧的平均时间？便于给出一个近似的进度*/
            extractor.readSampleData(byteBuffer, 0);
            long secondTime = extractor.getSampleTime();
            extractor.advance();
            extractor.readSampleData(byteBuffer, 0);
            long thirdTime = extractor.getSampleTime();
            stampTime = Math.abs(thirdTime - secondTime);
//            Log.e("hero", "---每帧的近似时间为---stampTime--" + stampTime);
            extractor.unselectTrack(audioTrackIndex);
            extractor.selectTrack(audioTrackIndex);
            while (true) {
                int readSampleSize = extractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                extractor.advance();//移动到下一帧
                bufferInfo.size = readSampleSize;
                bufferInfo.flags = extractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += extractor.getSampleTime();
                count += stampTime;

//                handler.obtainMessage(1,count/1000+"/"+size+"  "+Math.round(count/10f/size)+"%").sendToTarget();
                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
            }
            mediaMuxer.stop();
            mediaMuxer.release();
            extractor.release();
        } else {
            Log.e("hero", "---source video has no audio track---");
        }
    }

    /**
     * 音频混合
     */
    public static void audioMix(File[] rawAudioFiles, final String outFile, int firstVol, int secondVol) throws IOException {
        File file = new File(outFile);
        if (file.exists()) {
            file.delete();
        }

        final int fileSize = rawAudioFiles.length;

        FileInputStream[] audioFileStreams = new FileInputStream[fileSize];
        File audioFile = null;

        FileInputStream inputStream;
        byte[][] allAudioBytes = new byte[fileSize][];
        boolean[] streamDoneArray = new boolean[fileSize];
        byte[] buffer = new byte[9 * 1024];


        for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
            audioFile = rawAudioFiles[fileIndex];
            audioFileStreams[fileIndex] = new FileInputStream(audioFile);
        }
        final boolean[] isStartEncode = {false};
        while (true) {

            for (int streamIndex = 0; streamIndex < fileSize; ++streamIndex) {

                inputStream = audioFileStreams[streamIndex];
                if (!streamDoneArray[streamIndex] && ( inputStream.read(buffer)) != -1) {
                    allAudioBytes[streamIndex] = Arrays.copyOf(buffer, buffer.length);
                } else {
                    streamDoneArray[streamIndex] = true;
                    allAudioBytes[streamIndex] = new byte[9 * 1024];
                }
            }

//            byte[] mixBytes =  normalizationMix(allAudioBytes, firstVol, secondVol);
            byte[] mixBytes =  nativeAudioMix(allAudioBytes, firstVol, secondVol);
            putPCMData(mixBytes);
            //mixBytes 就是混合后的数据
//            Log.e("hero", "-----混音后的数据---" + mixBytes.length);
            if (!isStartEncode[0]){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isStartEncode[0] = true;
                        try {
                            Log.e("hero","start encode thread.....");
                            PCM2AAC("audio/mp4a-latm", outFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("hero"," encode error-----------error------");
                        }
                    }
                }).start();
            }
            boolean done = true;
            for (boolean streamEnd : streamDoneArray) {
                if (!streamEnd) {
                    done = false;
                }
            }

            if (done) {
                isDecodeOver = true;
                break;
            }
        }

    }
    /**
     * mono to stereo
     * */
    public static void mono2Stereo(String inputFile, final String outputFile) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(inputFile);

        int trackCount = extractor.getTrackCount();
        int audioTrackIndex = -1;
        boolean hasAudio = false;
        String mime = "";
        MediaFormat trackFormat = null;
        for (int i = 0; i < trackCount; i++) {
            trackFormat = extractor.getTrackFormat(i);
            mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                int sampleRate = trackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//                int bitRate = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
//                Log.e("hero", "---sampleRate---" + sampleRate + "--channelCount--" + channelCount);
                audioTrackIndex = i;
                hasAudio = true;
                break;
            }
        }
        if (hasAudio) {
            extractor.selectTrack(audioTrackIndex);

            MediaCodec mediaDecode =  MediaCodec.createDecoderByType(mime);//MP3解码器
            mediaDecode.configure(trackFormat,null,null,0);
            mediaDecode.start();

            ByteBuffer[] decodeInputBuffers = mediaDecode.getInputBuffers();
            ByteBuffer[] decodeOutputBuffers = mediaDecode.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean codeOver = false;
            final boolean[] isStartEncode = {false};

            while (!codeOver) {
                for (int i = 0; i < decodeInputBuffers.length - 1; i++) {
                    int inputIndex = mediaDecode.dequeueInputBuffer(-1);//获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
                    if (inputIndex < 0) {
                        Log.e("hero", "----codeOver===true");
                        codeOver = true;
                        break;
                    }

                    ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];//拿到inputBuffer
                    inputBuffer.clear();//清空之前传入inputBuffer内的数据
                    int sampleSize = extractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中
                    if (sampleSize < 0) {//小于0 代表所有数据已读取完成
//                        Log.e("hero", "----codeOver===true--数据读取完成");
                        codeOver = true;
                    } else {
                        mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);//通知MediaDecode解码刚刚传入的数据
                        extractor.advance();//MediaExtractor移动到下一取样处
                    }
                }

                int outputIndex = mediaDecode.dequeueOutputBuffer(bufferInfo, 10000);

                ByteBuffer outputBuffer;
                byte[] chunkPCM;
                byte[] stereoBytes;
                while (outputIndex >= 0) {
//                outputBuffer = decodeOutputBuffers[outputIndex];

                    outputBuffer = getOutputBuffer(mediaDecode,outputIndex);
                    chunkPCM = new byte[bufferInfo.size];
                    stereoBytes = new byte[bufferInfo.size * 2];
                    outputBuffer.get(chunkPCM);
                    outputBuffer.clear();
                    for (int i = 0; i < chunkPCM.length; i += 2) {
                        stereoBytes[i*2+0] = chunkPCM[i];
                        stereoBytes[i*2+1] = chunkPCM[i+1];
                        stereoBytes[i*2+2] = chunkPCM[i];
                        stereoBytes[i*2+3] = chunkPCM[i+1];
                    }
                    //stereoBytes立体声数据，然后送入公共数组，用于编码器编码成aac数据
                    putPCMData(stereoBytes);
                    //继续读数据
                    if (!isStartEncode[0]){
                        //说明还没有开启编码线程，马上开启
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    isStartEncode[0] = true;
                                    Log.e("hero","start encode thread.....");
                                    PCM2AAC("audio/mp4a-latm",outputFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    mediaDecode.releaseOutputBuffer(outputIndex, false);
                    outputIndex = mediaDecode.dequeueOutputBuffer(bufferInfo, 10000);
                }
            }
            isDecodeOver = true;
        }

    }
    private static ByteBuffer getOutputBuffer(MediaCodec codec, int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else{
            return codec.getOutputBuffers()[index];
        }
    }

    private static ArrayList<byte[]> chunkPCMDataContainer;
    private static void putPCMData(byte[] pcmChunk) {
        synchronized (AudioHardcodeHandler.class) {//记得加锁
            if (chunkPCMDataContainer == null){
                chunkPCMDataContainer = new ArrayList<>();
            }
            chunkPCMDataContainer.add(pcmChunk);
        }
    }
    private static byte[] getPCMData() {
        synchronized (AudioHardcodeHandler.class) {//记得加锁
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }

            byte[] pcmChunk = chunkPCMDataContainer.get(0);//每次取出index 0 的数据
            chunkPCMDataContainer.remove(pcmChunk);//取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
            return pcmChunk;
        }
    }
    /**
     * 原始pcm数据，转aac音频
     * */
    static boolean isDecodeOver = false;
    public static void PCM2AAC(String encodeType, String outputFile) throws IOException {
        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;
        byte[] chunkAudio;
        int outBitSize;
        int outPacketSize;
        byte[] chunkPCM;
        //初始化编码器
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(encodeType,44100,2);//mime type 采样率 声道数
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);//比特率
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 500 * 1024);

        MediaCodec mediaEncode = MediaCodec.createEncoderByType(encodeType);
        mediaEncode.configure(encodeFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaEncode.start();

        ByteBuffer[] encodeInputBuffers = mediaEncode.getInputBuffers();
        ByteBuffer[] encodeOutputBuffers = mediaEncode.getOutputBuffers();
        MediaCodec.BufferInfo encodeBufferInfo = new MediaCodec.BufferInfo();

        //初始化文件写入流
        FileOutputStream fos = new FileOutputStream(new File(outputFile));
        BufferedOutputStream bos = new BufferedOutputStream(fos,500*1024);
//        Log.e("hero","--encodeBufferInfo---"+encodeBufferInfo.size);

        while (!chunkPCMDataContainer.isEmpty() || !isDecodeOver){
            for (int i = 0; i < encodeInputBuffers.length - 1; i++) {
                chunkPCM=getPCMData();//获取解码器所在线程输出的数据 代码后边会贴上
                if (chunkPCM == null) {
                    break;
                }
//                Log.e("hero","--AAC编码器--取数据---"+chunkPCM.length);

                inputIndex = mediaEncode.dequeueInputBuffer(-1);
                inputBuffer = encodeInputBuffers[inputIndex];
                inputBuffer.clear();//同解码器
                inputBuffer.limit(chunkPCM.length);
                inputBuffer.put(chunkPCM);//PCM数据填充给inputBuffer
                mediaEncode.queueInputBuffer(inputIndex, 0, chunkPCM.length, 0, 0);//通知编码器 编码
            }

            outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);//同解码器
            while (outputIndex >= 0) {//同解码器

                outBitSize=encodeBufferInfo.size;
                outPacketSize=outBitSize+7;//7为ADTS头部的大小
                outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
                outputBuffer.position(encodeBufferInfo.offset);
                outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
                chunkAudio = new byte[outPacketSize];
                addADTStoPacket(chunkAudio,outPacketSize);//添加ADTS 代码后面会贴上
                outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中 偏移量offset=7 你懂得
                outputBuffer.position(encodeBufferInfo.offset);
                try {
//                    Log.e("hero","---保存文件----"+chunkAudio.length);
                    bos.write(chunkAudio,0,chunkAudio.length);//BufferOutputStream 将文件保存到内存卡中 *.aac
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaEncode.releaseOutputBuffer(outputIndex,false);
                outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);
            }
        }
        mediaEncode.stop();
        mediaEncode.release();
        fos.close();

    }


    /**
     * 写入ADTS头部数据
     * */
    public static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * jni进行音频的混音处理，提升速度
     * */
    public static byte[] nativeAudioMix(byte[][] allAudioBytes, float firstVol, float secondVol){
        if (allAudioBytes == null || allAudioBytes.length == 0)
            return null;

        byte[] realMixAudio = allAudioBytes[0];

        //如果只有一个音频的话，就返回这个音频数据
        if(allAudioBytes.length == 1)
            return realMixAudio;

        return AudioJniUtils.audioMix(allAudioBytes[0], allAudioBytes[1], realMixAudio, firstVol, secondVol);
    }
    /**
     * 归一化混音
     * */
    public static byte[] normalizationMix(byte[][] allAudioBytes, float firstVol, float secondVol){
        if (allAudioBytes == null || allAudioBytes.length == 0)
            return null;

        byte[] realMixAudio = allAudioBytes[0];

        //如果只有一个音频的话，就返回这个音频数据
        if(allAudioBytes.length == 1)
            return realMixAudio;


        int row = realMixAudio.length /2;
        short[][] sourecs = new short[allAudioBytes.length][row];
        for (int r = 0; r < 2; ++r) {
            for (int c = 0; c < row; ++c) {
                sourecs[r][c] = (short) ((allAudioBytes[r][c * 2] & 0xff) | (allAudioBytes[r][c * 2 + 1] & 0xff) << 8);
            }
        }

        //coloum第一个音频长度 / 2
        short[] result = new short[row];
        //转成short再计算的原因是，提供精确度，高端的混音软件据说都是这样做的，可以测试一下不转short直接计算的混音结果
        for (int i = 0; i < row; i++) {
            int a = (int) (sourecs[0][i] * firstVol);
            int b = (int) (sourecs[1][i] * secondVol);
            if (a <0 && b<0){
                int i1 = a  + b  - a  * b / (-32768);
                if (i1 > 32767){
                    result[i] = 32767;
                }else if (i1 < - 32768){
                    result[i] = -32768;
                }else {
                    result[i] = (short) i1;
                }
            }else if (a > 0 && b> 0){
                int i1 = a + b - a  * b  / 32767;
                if (i1 > 32767){
                    result[i] = 32767;
                }else if (i1 < - 32768){
                    result[i] = -32768;
                }else {
                    result[i] = (short) i1;
                }
            }else {
                int i1 = a + b ;
                if (i1 > 32767){
                    result[i] = 32767;
                }else if (i1 < - 32768){
                    result[i] = -32768;
                }else {
                    result[i] = (short) i1;
                }
            }
        }
       return toByteArray(result);
    }
    public static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2 +1] = (byte) ((src[i] & 0xFF00) >> 8);
            dest[i * 2] = (byte) ((src[i] & 0x00FF));
        }

        return dest;
    }
}
