package io.renren.modules.app.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.MajorDataRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MajorDataRuleMapper extends BaseMapper<MajorDataRule> {

    //加入缓存
    default List<MajorDataRule> getRule(List<String> resourceIds, String packageName) {
        QueryWrapper<MajorDataRule> query = new QueryWrapper<>();
        LambdaQueryWrapper<MajorDataRule> lambda = query.lambda();
//        lambda.in(MajorDataRule::getResourceId, resourceIds);
        lambda.eq(MajorDataRule::getPackageName, packageName);
        return selectList(query);
    }
}
