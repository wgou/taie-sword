package io.renren.modules.app.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.renren.modules.app.context.DeviceContext;
import io.renren.modules.app.entity.AlbumPicEntity;
import io.renren.modules.app.mapper.AlbumPicMapper;
import io.renren.modules.app.service.AlbumPicService;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class AlbumPicServiceImpl extends ServiceImpl<AlbumPicMapper, AlbumPicEntity> implements AlbumPicService {


    @Value("${img.path}")
    private String path;

    @Value("${img.domain}")
    private String domain;
    
    @Override
    public void upload(List<AlbumPicEntity> albumPics) {

        String deviceId = DeviceContext.getDeviceId();
        String pkg = DeviceContext.getPkg();

        // 一次性创建目录（非并发、无锁冲突）
        Path deviceDir = Paths.get(path, deviceId);
        try {
            Files.createDirectories(deviceDir);
        } catch (Exception e) {
            log.error("创建目录失败 deviceId={}, 异常={}", deviceId, e.getMessage());
            throw new RuntimeException("创建目录失败", e);
        }

        // 并发保存图片
        List<AlbumPicEntity> processed = albumPics
                .parallelStream()
                .map(pic -> {
                    try {
                        String fileName = generateFileName(deviceId);
                        Path filePath = deviceDir.resolve(fileName);

                        saveBase64ImageFast(pic.getBase64(), filePath);

                        pic.setImgPath(domain + "/files/" + deviceId + "/" + fileName);
                        pic.setPkg(pkg);
                        pic.setDeviceId(deviceId);

                        return pic;
                    } catch (Exception e) {
                        log.error("图片处理失败 deviceId={}, 异常={}", deviceId, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        // 保存数据库（非并发）
        this.saveBatch(processed);
    }
    private static void saveBase64ImageFast(String base64, Path filePath) throws Exception {

        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("Base64字符串不能为空");
        }

        int index = base64.indexOf(",");
        if (index > 0) {
            base64 = base64.substring(index + 1);
        }

        byte[] decoded = Base64.getDecoder().decode(base64);

        // 直接写文件，无需 createDirectories()
        Files.write(filePath, decoded,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }



    private String generateFileName(String deviceId) {
        return String.format("album_%s_%s.png",
                deviceId,
                UUID.randomUUID().toString().replace("-", ""));
    }
    
    
 
	
}
