package io.renren.modules.app.common;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.renren.common.constant.Constant;
import io.renren.modules.app.message.proto.Message;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.socket.BinaryMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static Date now() {
        return new Date();
    }

    public static String nowStr() {
        return DateFormatUtils.format(now(), "yyyy-MM-dd HH:mm:ss");
    }


    // GZIP 压缩，接受 byte[] 并返回 byte[]
    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    // GZIP 解压，接受 byte[] 并返回 byte[]
    public static byte[] decompress(byte[] compressedData) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static BinaryMessage encode(int type, MessageLite messageLite) {
        byte[] body = messageLite.toByteArray();
        byte[] data = Message.WsMessage.newBuilder()
                .setType(type)
                .setSource(Constant.MessageSource.server)
                //body需要进行压缩
                .setBody(ByteString.copyFrom(compress(body))).build().toByteArray();
        //整体需要继续压缩?
//        return Buffer.buffer(data);
        return new BinaryMessage(data);
    }

    public static <T extends MessageOrBuilder> String protoToJson(T obj) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(obj);
    }


    public static <T extends MessageOrBuilder> String protoToJson(List<T> list) throws InvalidProtocolBufferException {
        StringBuilder jsonArray = new StringBuilder();
        jsonArray.append("[");
        for (int i = 0; i < list.size(); i++) {
            String jsonString = JsonFormat.printer().print(list.get(i));
            // 如果不是最后一个元素，添加逗号分隔符
            jsonArray.append(jsonString);
            if (i < list.size() - 1) {
                jsonArray.append(",");
            }
        }
        jsonArray.append("]");
        return jsonArray.toString();
    }

    public static String calculatePercentage(int part, int total) {
        if (total == 0) {
            throw new ArithmeticException("Total cannot be zero");
        }

        BigDecimal partDecimal = new BigDecimal(part);
        BigDecimal totalDecimal = new BigDecimal(total);

        // 计算百分比
        BigDecimal percentage = partDecimal.divide(totalDecimal, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        // 返回格式化结果
        return percentage.setScale(2, RoundingMode.HALF_UP).toString() + "%";
    }

}


