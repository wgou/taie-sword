package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.UnlockScreenPwd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UnlockScreenPwdMapper extends BaseMapper<UnlockScreenPwd> {
    @Select("SELECT * from ast_unlock_screen_pwd where android_id = #{deviceId} and type = #{type} order by create_date desc limit 1")
    UnlockScreenPwd last(@Param("deviceId") String deviceId, @Param("type") int type);
}

