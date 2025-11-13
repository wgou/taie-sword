package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.Device;
import io.renren.modules.app.entity.SmsInfoEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsInfoMapper extends BaseMapper<SmsInfoEntity> {
}
