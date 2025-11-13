package io.renren.modules.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import io.renren.modules.app.entity.AlbumPicEntity;

public interface AlbumPicService extends IService<AlbumPicEntity> {
	
	
	public void upload(List<AlbumPicEntity> albumPics);
	
	
}
