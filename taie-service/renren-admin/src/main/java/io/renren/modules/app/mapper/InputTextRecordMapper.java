package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.InputTextRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InputTextRecordMapper extends BaseMapper<InputTextRecord> {

    @Select("select * from input_text_record FINAL where device_id = #{deviceId} and id = #{id} and package_name = #{packageName} order by time desc")
    List<InputTextRecord> queryList(@Param("deviceId") String deviceId, @Param("id") String id, @Param("packageName") String packageName);
}
