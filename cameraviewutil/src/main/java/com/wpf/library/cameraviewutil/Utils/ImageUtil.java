package com.wpf.library.cameraviewutil.Utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

//import com.wpf.library.libyuv_android.Data.FilterMode;
//import com.wpf.library.libyuv_android.Data.RotationMode;
//import com.wpf.library.libyuv_android.YUVUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by 王朋飞 on 2018/4/24.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class ImageUtil {

//    public static @Nullable
//    byte[] image2I420(@NonNull Image image, boolean onlyY,
//                      int viewWidth, int viewHeight,
//                      @Nullable Rect dstRect,
//                      @Nullable RotationMode rotationMode) {
//        return image2I420(image, onlyY, viewWidth, viewHeight, dstRect, FilterMode.kFilterNone, rotationMode);
//    }
//
//    public static @Nullable
//    byte[] image2I420(@NonNull Image image, boolean onlyY,
//                      int viewWidth, int viewHeight,
//                      @Nullable Rect dstRect,
//                      @Nullable FilterMode filterMode,
//                      @Nullable RotationMode rotationMode) {
//        if (image.getFormat() == ImageFormat.JPEG || onlyY) {
//            Image.Plane plane = getYPlane(image);
//            if (plane == null) return null;
//            byte[] yBuffer = byteBuffer2Byte(plane.getBuffer());
//            return yBuffer;
//        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
//            Image.Plane planeY = getYPlane(image);
//            Image.Plane planeU = getUPlane(image);
//            Image.Plane planeV = getVPlane(image);
//            if (planeY == null || planeU == null || planeV == null) return null;
//            int width = image.getWidth();
//            int height = image.getHeight();
//            byte[] yBufferDst = new byte[width * height];
//            byte[] uBufferDst = new byte[width * height / 4];
//            byte[] vBufferDst = new byte[width * height / 4];
//
//            int result = YUVUtils.instance().Convert_Android420ToI420(
//                    byteBuffer2Byte(planeY.getBuffer()), width,
//                    byteBuffer2Byte(planeU.getBuffer()), width,
//                    byteBuffer2Byte(planeV.getBuffer()), width,
//                    planeU.getPixelStride(),
//                    yBufferDst, width,
//                    uBufferDst, width / 2,
//                    vBufferDst, width / 2,
//                    width, height);
//            if (result != 0) return null;
//            if (filterMode != null && rotationMode != null) {
//                int widthResult = viewWidth, heightResult = viewHeight;
//                if (rotationMode == RotationMode.kRotate90 || rotationMode == RotationMode.kRotate270) {
//                    widthResult = viewHeight;
//                    heightResult = viewWidth;
//                }
//                byte[] yBufferResult = new byte[widthResult * heightResult];
//                byte[] uBufferResult = new byte[widthResult * heightResult / 4];
//                byte[] vBufferResult = new byte[widthResult * heightResult / 4];
//
//                result = YUVUtils.instance().Scale_I420Scale(
//                        yBufferDst, width,
//                        uBufferDst, width / 2,
//                        vBufferDst, width / 2,
//                        width, height,
//                        yBufferResult, widthResult,
//                        uBufferResult, widthResult / 2,
//                        vBufferResult, widthResult / 2,
//                        widthResult, heightResult,
//                        filterMode);
//
//                if (result != 0) return null;
//                yBufferDst = new byte[widthResult * heightResult];
//                uBufferDst = new byte[widthResult * heightResult / 4];
//                vBufferDst = new byte[widthResult * heightResult / 4];
//
//                result = YUVUtils.instance().Rotate_I420Rotate(
//                        yBufferResult, widthResult,
//                        uBufferResult, widthResult / 2,
//                        vBufferResult, widthResult / 2,
//                        yBufferDst, heightResult,
//                        uBufferDst, heightResult / 2,
//                        vBufferDst, heightResult / 2,
//                        widthResult, heightResult,
//                        rotationMode);
//
//                if (result != 0) return null;
//                return getI420(yBufferDst, uBufferDst, vBufferDst);
//            }
//            return getI420(yBufferDst, uBufferDst, vBufferDst);
//        }
//        return null;
//    }
//
//    public static void Image2I420(@NonNull Image image, boolean onlyY,
//                                  @NonNull byte[] yBytes,
//                                  @Nullable byte[] uBytes,
//                                  @Nullable byte[] vBytes,
//                                  int viewWidth, int viewHeight,
//                                  @Nullable FilterMode filterMode,
//                                  @Nullable RotationMode rotationMode) {
//        if (image.getFormat() == ImageFormat.JPEG || onlyY) {
//            Image.Plane plane = getYPlane(image);
//            if (plane == null) return;
//            yBytes = byteBuffer2Byte(plane.getBuffer());
//        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
//            Image.Plane planeY = getYPlane(image);
//            Image.Plane planeU = getUPlane(image);
//            Image.Plane planeV = getVPlane(image);
//            if (planeY == null || planeU == null || planeV == null) return;
//            int width = image.getWidth();
//            int height = image.getHeight();
//            byte[] yBufferDst = new byte[width * height];
//            byte[] uBufferDst = new byte[width * height / 4];
//            byte[] vBufferDst = new byte[width * height / 4];
//
//            int result = YUVUtils.instance().Convert_Android420ToI420(
//                    byteBuffer2Byte(planeY.getBuffer()), width,
//                    byteBuffer2Byte(planeU.getBuffer()), width,
//                    byteBuffer2Byte(planeV.getBuffer()), width,
//                    planeU.getPixelStride(),
//                    yBufferDst, width,
//                    uBufferDst, width / 2,
//                    vBufferDst, width / 2,
//                    width, height);
//            if (filterMode != null && rotationMode != null) {
//                int widthResult = viewWidth, heightResult = viewHeight;
//                if (rotationMode == RotationMode.kRotate90 || rotationMode == RotationMode.kRotate270) {
//                    widthResult = viewHeight;
//                    heightResult = viewWidth;
//                }
//                byte[] yBufferResult = new byte[widthResult * heightResult];
//                byte[] uBufferResult = new byte[widthResult * heightResult / 4];
//                byte[] vBufferResult = new byte[widthResult * heightResult / 4];
//
//                result = YUVUtils.instance().Scale_I420Scale(
//                        yBufferDst, width,
//                        uBufferDst, width / 2,
//                        vBufferDst, width / 2,
//                        width, height,
//                        yBufferResult, widthResult,
//                        uBufferResult, widthResult / 2,
//                        vBufferResult, widthResult / 2,
//                        widthResult, heightResult,
//                        filterMode);
//
//                yBufferDst = new byte[widthResult * heightResult];
//                uBufferDst = new byte[widthResult * heightResult / 4];
//                vBufferDst = new byte[widthResult * heightResult / 4];
//
//                result = YUVUtils.instance().Rotate_I420Rotate(
//                        yBufferResult, widthResult,
//                        uBufferResult, widthResult / 2,
//                        vBufferResult, widthResult / 2,
//                        yBufferDst, heightResult,
//                        uBufferDst, heightResult / 2,
//                        vBufferDst, heightResult / 2,
//                        widthResult, heightResult,
//                        rotationMode);
//
//            }
//            if(result == 0) {
//                yBytes = yBufferDst;
//                uBytes = uBufferDst;
//                vBytes = vBufferDst;
//            }
//        }
//    }

    public static byte[] getI420(@NonNull byte[] yByteArray,
                                 @Nullable byte[] uByteArray,
                                 @Nullable byte[] vByteArray) {
        int ySize = yByteArray.length;
        int uSize = uByteArray != null ? uByteArray.length : 0;
        int vSize = vByteArray != null ? vByteArray.length : 0;

        byte[] I420 = new byte[ySize + uSize + vSize];

        System.arraycopy(yByteArray, 0, I420, 0, ySize);
        if (uByteArray != null)
            System.arraycopy(uByteArray, 0, I420, ySize, uSize);
        if (vByteArray != null)
            System.arraycopy(vByteArray, 0, I420, ySize + uSize, vSize);
        return I420;
    }

//    public static @Nullable
//    byte[] image2JPEG(@NonNull Image image, boolean onlyY,
//                      @Nullable Rect dstRect,
//                      RotationMode rotationMode) {
//        switch (rotationMode) {
//            case kRotate0:
//            case kRotate180:
//                return NV21toJPEGByte(image2I420(image, onlyY, dstRect, rotationMode),
//                        new Rect(0, 0, image.getWidth(), image.getHeight()), null);
//            case kRotate90:
//            case kRotate270:
//                return NV21toJPEGByte(image2I420(image, onlyY, dstRect, rotationMode),
//                        new Rect(0, 0, image.getHeight(), image.getWidth()), null);
//            default:
//                return null;
//        }
//    }

//    public static @Nullable
//    byte[] NV21ToJPEG(byte[] nv21, int width, int height, @Nullable Rect dstRect,
//                      RotationMode rotationMode) {
//        switch (rotationMode) {
//            case kRotate0:
//            case kRotate180:
//                return NV21toJPEGByte(nv21, new Rect(0, 0, width, height), dstRect);
//            case kRotate90:
//            case kRotate270:
//                return NV21toJPEGByte(nv21, new Rect(0, 0, height, width), dstRect);
//            default:
//                return null;
//        }
//    }

    public static @Nullable
    Image.Plane getYPlane(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length != 3) return null;
        return planes[0];
    }

    public static @Nullable
    Image.Plane getUPlane(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length != 3) return null;
        return planes[1];
    }

    public static @Nullable
    Image.Plane getVPlane(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length != 3) return null;
        return planes[2];
    }

    public static @Nullable ByteBuffer imageToJPEGBuffer(@NonNull Image image, boolean onlyY) {
        if (image.getFormat() == ImageFormat.JPEG || onlyY) {
            Image.Plane[] planes = image.getPlanes();
            if (planes == null || planes.length == 0)
                return null;
            Image.Plane plane = planes[0];
            return plane.getBuffer();
        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
            Image.Plane planeY = getYPlane(image);
            Image.Plane planeU = getUPlane(image);
            Image.Plane planeV = getVPlane(image);
            if (planeY == null || planeU == null || planeV == null) return null;
            byte[] yBuffer = byteBuffer2Byte(planeY.getBuffer());
            byte[] uBuffer = byteBuffer2Byte(planeU.getBuffer());
            byte[] vBuffer = byteBuffer2Byte(planeV.getBuffer());
            return NV21toJPEGBuffer(YUV_420_888toNV21(yBuffer, uBuffer, vBuffer),
                    new Rect(0, 0, image.getWidth(), image.getHeight()), null);
        }
        return null;
    }

    public static byte[] YUV_420_888toNV21(@NonNull byte[] yByteArray,
                                           @NonNull byte[] uByteArray,
                                           @NonNull byte[] vByteArray) {
        int ySize = yByteArray.length;
        int uSize = uByteArray.length;
        int vSize = vByteArray.length;

        byte[] yuv = new byte[ySize + uSize + vSize];

        System.arraycopy(yByteArray, 0, yuv, 0, ySize);
        System.arraycopy(vByteArray, 0, yuv, ySize, vSize);
        System.arraycopy(uByteArray, 0, yuv, ySize + vSize, uSize);
        return yuv;
    }

    public UVType getI420UVType(int pixel_stride_u, int row_stride_u,
                                int pixel_stride_v, int row_stride_v,
                                int width) {
        if (pixel_stride_u == 2 && row_stride_u == width
                && pixel_stride_v == 2 && row_stride_v == width) {
            return UVType.SemiPlanar;
        } else if (pixel_stride_u == 1 && row_stride_u * 2 == width
                && pixel_stride_v == 1 && row_stride_v * 2 == width) {
            return UVType.Planar;
        }
        return UVType.Planar;
    }

    private static ByteBuffer getBuffer(byte[] data) {
        return ByteBuffer.wrap(data);
    }

    public static @Nullable
    byte[] NV21toJPEGByte(@Nullable byte[] yuv, @NonNull Rect srcRect, @Nullable Rect dstRect) {
        if (yuv == null || srcRect.isEmpty()) return null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(yuv, ImageFormat.NV21, srcRect.width(), srcRect.height(), null);
            yuvImage.compressToJpeg(dstRect == null ? srcRect : dstRect, 100, out);
            byte[] data = out.toByteArray();
            out.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable
    ByteBuffer NV21toJPEGBuffer(byte[] nv21, @NonNull Rect srcRect, @Nullable Rect dstRect) {
        byte[] data = NV21toJPEGByte(nv21, srcRect, dstRect);
        if (data != null)
            return ByteBuffer.wrap(data);
        return null;
    }

    public static @Nullable
    Bitmap yuv2Bitmap(@NonNull byte[] yuvPlannerData, int rotation,
                      int viewWidth, int viewHeight,
                      @Nullable Rect scaleRect) {
        if (yuvPlannerData.length == 0) return null;
        if (rotation < 0 || rotation >= 4)
            throw new IllegalArgumentException("rotation must 0...3");
        Bitmap bmpTemp = BitmapFactory.decodeByteArray(
                yuvPlannerData, 0, yuvPlannerData.length);
        if (bmpTemp == null) {
            Log.e("ImageUtil", "yuvPlannerData is error");
            return null;
        }
        Bitmap result = bmpTemp;
        if (rotation != 0) {
            Matrix mtx = new Matrix();
            mtx.setRotate(rotation * 90);
            result = Bitmap.createBitmap(result, 0, 0, bmpTemp.getWidth(), bmpTemp.getHeight(), mtx, true);
        }
        if (viewWidth != 0 && viewHeight != 0) {
            result = Bitmap.createScaledBitmap(result, viewWidth, viewHeight, true);
        }
        if (scaleRect != null && !scaleRect.isEmpty()) {
            result = Bitmap.createBitmap(result, scaleRect.left, scaleRect.top, scaleRect.width(), scaleRect.height());
        }
        if (result != bmpTemp)
            bmpTemp.recycle();
        return result;
    }

    public static Bitmap yuv2Bitmap(@NonNull byte[] imageData, int rotation) {
        return yuv2Bitmap(imageData, rotation, 0, 0, null);
    }

    public static Bitmap yuv2Bitmap(@NonNull byte[] imageData) {
        return yuv2Bitmap(imageData, 0, 0, 0, null);
    }

    public static byte[] byteBuffer2Byte(@Nullable ByteBuffer imageData) {
        if (imageData == null || imageData.capacity() == 0) return null;
        byte[] result = new byte[imageData.capacity()];
        imageData.get(result);
        return result;
    }

    public static byte[] bitmap2Byte(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return out.toByteArray();
    }

    public static void rotateRect(@NonNull Rect srcRect, int rotation) {
        switch (rotation % 4) {
            case -3:
            case -1:
            case 1:
            case 3:
                srcRect.set(srcRect.height() - srcRect.bottom, srcRect.left,
                        srcRect.height() - srcRect.top, srcRect.right);
                break;
        }
    }

    public static void changeRect(@NonNull Rect srcRect, @NonNull Rect dstRect, @NonNull Rect changeRect, int rotation) {
        float sx, sy;
        switch (rotation % 4) {
            case -3:
                sx = (float) dstRect.width() / srcRect.height();
                sy = (float) dstRect.height() / srcRect.width();
                changeRect.set((int) ((srcRect.height() - changeRect.bottom) * sx),
                        (int) (changeRect.left * sy),
                        (int) ((srcRect.height() - changeRect.top) * sx),
                        (int) (changeRect.right * sy));
                break;
            case -2:
                sx = (float) dstRect.width() / srcRect.width();
                sy = (float) dstRect.height() / srcRect.height();
                changeRect.set((int) ((srcRect.width() - changeRect.right) * sx),
                        (int) ((srcRect.height() - changeRect.bottom) * sy),
                        (int) ((srcRect.width() - changeRect.left) * sx),
                        (int) ((srcRect.height() - changeRect.top) * sy));
                break;
            case -1:
                sx = (float) dstRect.width() / srcRect.height();
                sy = (float) dstRect.height() / srcRect.width();
                changeRect.set((int) (changeRect.top * sx),
                        (int) ((srcRect.width() - changeRect.right) * sy),
                        (int) (changeRect.bottom * sx),
                        (int) ((srcRect.width() - changeRect.left) * sy));
                break;
            case 0:
                sx = (float) dstRect.width() / srcRect.width();
                sy = (float) dstRect.height() / srcRect.height();
                changeRect.set((int) (changeRect.left * sx), (int) (changeRect.top * sy),
                        (int) (changeRect.right * sx), (int) (changeRect.bottom * sy));
                break;
            case 1:
                sx = (float) dstRect.width() / srcRect.height();
                sy = (float) dstRect.height() / srcRect.width();
                changeRect.set((int) ((srcRect.height() - changeRect.bottom) * sx),
                        (int) (changeRect.left * sy),
                        (int) ((srcRect.height() - changeRect.top) * sx),
                        (int) (changeRect.right * sy));
                break;
            case 2:
                sx = (float) dstRect.width() / srcRect.width();
                sy = (float) dstRect.height() / srcRect.height();
                changeRect.set((int) ((srcRect.width() - changeRect.right) * sx),
                        (int) ((srcRect.height() - changeRect.bottom) * sy),
                        (int) ((srcRect.width() - changeRect.left) * sx),
                        (int) ((srcRect.height() - changeRect.top) * sy));
                break;
            case 3:
                sx = (float) dstRect.width() / srcRect.height();
                sy = (float) dstRect.height() / srcRect.width();
                changeRect.set((int) (changeRect.top * sx),
                        (int) ((srcRect.width() - changeRect.right) * sy),
                        (int) (changeRect.bottom * sx),
                        (int) ((srcRect.width() - changeRect.left) * sy));
                break;
        }
    }

    public enum UVType {
        Planar,
        SemiPlanar,
        PackedSemiPlanar
    }
}
