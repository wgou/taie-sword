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

        List<AlbumPicEntity> dbInput = albumPics.parallelStream().map(abEntity -> {
            try {
	                String fileName = generateFileName(deviceId);
	                String filePath = path + File.separator + deviceId + File.separator +fileName;
	                saveBase64ImageFast(abEntity.getBase64(), filePath);
	              //  abEntity.setBase64(null);
	
	                String pathUrl = domain + "/files/"+deviceId+"/" + fileName;
	
	                abEntity.setImgPath(pathUrl);
	                abEntity.setPkg(pkg);
	                abEntity.setDeviceId(deviceId);
	                return abEntity;
            } catch (Exception e) {
            	e.printStackTrace();
                log.error("相册图片处理失败 deviceId={}，异常={}", deviceId, e.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).toList();

        this.saveBatch(dbInput);
    }
    private static void saveBase64ImageFast(String base64, String filePath) throws Exception {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("Base64字符串不能为空");
        }

        int index = base64.indexOf(",");
        if (index > 0) {
            base64 = base64.substring(index + 1);
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64);

        Path path = Paths.get(filePath);
        Path parent = path.getParent();

        // 严格保证目录存在
        Files.createDirectories(parent);

        Files.write(path, decodedBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }


    private String generateFileName(String deviceId) {
        return String.format("album_%s_%s.png",
                deviceId,
                UUID.randomUUID().toString().replace("-", ""));
    }
    
    
 
	
}
