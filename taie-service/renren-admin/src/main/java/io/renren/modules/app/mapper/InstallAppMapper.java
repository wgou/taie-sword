package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.InstallApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InstallAppMapper extends BaseMapper<InstallApp> {
    @Select("select device_id from install_app where package_name = #{packageName}")
    List<String> selectByPackageName(@Param("packageName") String packageName);
}
