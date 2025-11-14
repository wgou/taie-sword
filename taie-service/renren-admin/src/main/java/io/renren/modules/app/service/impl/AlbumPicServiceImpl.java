package io.renren.modules.app.service.impl;

import java.io.File;
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

        List<AlbumPicEntity> dbInput = albumPics.parallelStream().map(abEntity -> {
            try {
	                String fileName = generateFileName(deviceId);
	                String filePath = path + File.separator + deviceId + File.separator +fileName;
	                log.info("deviceId:{}  base64:{}",deviceId,abEntity.getBase64());
	                saveBase64ImageFast(abEntity.getBase64(), filePath);
	              //  abEntity.setBase64(null);
	
	                String pathUrl = domain + "/files/"+deviceId+"/" + fileName;
	
	                abEntity.setImgPath(pathUrl);
	                abEntity.setPkg(pkg);
	                abEntity.setDeviceId(deviceId);
	                return abEntity;
            } catch (Exception e) {
                log.error("相册图片处理失败 deviceId={}，异常={}", deviceId, e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toList();

        this.saveBatch(dbInput);
    }

    private void saveBase64ImageFast(String base64, String filePath) throws Exception {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("Base64字符串不能为空");
        }

        // 去掉 data:image/...;base64, 前缀
        int index = base64.indexOf(",");
        if (index > 0) {
            base64 = base64.substring(index + 1);
        }

        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);

        File file = new File(filePath);
        file.getParentFile().mkdirs();

        java.nio.file.Files.write(file.toPath(), decodedBytes);
    }


    private String generateFileName(String deviceId) {
        return String.format("album_%s_%s.png",
                deviceId,
                UUID.randomUUID().toString().replace("-", ""));
    }
	
}
