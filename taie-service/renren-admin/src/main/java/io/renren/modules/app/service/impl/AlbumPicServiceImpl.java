package io.renren.modules.app.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;

import io.renren.common.constant.Constant;
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
		  List<AlbumPicEntity> dbInput = Lists.newArrayList();
	      String deviceId = DeviceContext.getDeviceId();
	      String pkg = DeviceContext.getPkg();;
		for(AlbumPicEntity abEntity : albumPics) {
			String fileName = generateFileName(abEntity.getDeviceId());
            String filePath = path + File.separator + fileName;
            try { 
                // 保存图片
                saveBase64Image(abEntity.getBase64(), filePath);
                String pathUrl = domain + "/files/" + fileName;
                abEntity.setImgPath(pathUrl);
                abEntity.setPkg(pkg);
                abEntity.setDeviceId(deviceId);
                dbInput.add(abEntity);
            } catch (Exception e) {
            	log.error("相册图片处理失败。 {}",e);
            }
	  }
	   this.saveBatch(dbInput);
		
	}
	
	private void saveBase64Image(String base64, String filePath) throws Exception {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64字符串不能为空");
        }
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        byte[] decodedBytes = Base64.decodeBase64(base64);

        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (OutputStream out = new FileOutputStream(filePath)) {
            out.write(decodedBytes);
        }
    }

	
	

    private String generateFileName(String deviceId) {
        return String.format("album_%s_%s.png",
                deviceId,
                UUID.randomUUID().toString().replace("-", ""));
    }
	
}
