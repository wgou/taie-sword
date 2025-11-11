package io.renren.modules.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.renren.modules.app.entity.Log;

@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
