package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.FishTemplates;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FishTemplatesMapper extends BaseMapper<FishTemplates> {

    @Select("select code,label  from ast_fish_template where status = 1")
    List<FishTemplates> list();
}
