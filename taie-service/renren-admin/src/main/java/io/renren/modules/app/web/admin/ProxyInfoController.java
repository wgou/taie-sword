package io.renren.modules.app.web.admin;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.entity.ProxyInfoEntity;
import io.renren.modules.app.param.ProxyInfoParam;
import io.renren.modules.app.service.ProxyInfoService;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.sys.dto.SysUserDTO;
import io.renren.modules.sys.service.SysUserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("proxyInfo")
@RestController
public class ProxyInfoController extends BaseController{
    
    @Resource
    private ProxyInfoService proxyInfoService;
    @Resource
    private SysUserService sysUserService;
    
    
    /**
     * 分页查询代理列表
     */
    @RequestMapping("page")
    public Result<PageData<ProxyInfoEntity>> page(@RequestBody ProxyInfoParam param) {
        Page<ProxyInfoEntity> page = new Page<>(param.getPage(), param.getLimit());
        QueryWrapper<ProxyInfoEntity> query = new QueryWrapper<>();
        LambdaQueryWrapper<ProxyInfoEntity> lambda = query.lambda();
        // 包名
        if (StringUtils.isNotEmpty(param.getPkg())) {
            lambda.eq(ProxyInfoEntity::getPkg, param.getPkg());
        }
        // 代理用户
        if (StringUtils.isNotEmpty(param.getProxyUser())) {
            lambda.eq(ProxyInfoEntity::getProxyUser, param.getProxyUser());
        }
        
        // 按创建时间倒序
        lambda.orderByDesc(ProxyInfoEntity::getCreated);
        
        Page<ProxyInfoEntity> pageData = proxyInfoService.page(page, lambda);
        return Result.toSuccess(new PageData<>(pageData.getRecords(), pageData.getTotal()));
    }
    
    /**
     * 新增代理信息
     */
    @RequestMapping("save")
    public Result<String> save(@RequestBody ProxyInfoEntity entity) {
        // 参数校验
        if (StringUtils.isEmpty(entity.getPkg())) {
            return Result.toError("包名不能为空");
        }
        if (StringUtils.isEmpty(entity.getProxyUser())) {
            return Result.toError("代理用户不能为空");
        }
        
        SysUserDTO proxyUser = sysUserService.getByUsername(entity.getProxyUser());
        if(proxyUser == null)   return Result.toError("该代理账户不存在");
        // 检查是否已存在相同的代理配置
        QueryWrapper<ProxyInfoEntity> query = new QueryWrapper<>();
        query.lambda()
            .eq(ProxyInfoEntity::getPkg, entity.getPkg())
            .eq(ProxyInfoEntity::getProxyUser, entity.getProxyUser());
        
        if (proxyInfoService.count(query) > 0) {
            return Result.toError("该代理配置已存在");
        }
        entity.setCreateUser(SecurityUser.getUser().getUsername());
        log.info(JSON.toJSONString(entity));
        // 保存
        boolean success = proxyInfoService.save(entity);
        if (success) {
            return Result.toSuccess("新增成功");
        } else {
            return Result.toError("新增失败");
        }
    }
    
    /**
     * 删除代理信息
     */
    @RequestMapping("delete")
    public Result<String> delete(@RequestBody ProxyInfoEntity entity) {
        if (entity.getId() == null) {
            return Result.toError("ID不能为空");
        }
        
        boolean success = proxyInfoService.removeById(entity.getId());
        if (success) {
            return Result.toSuccess("删除成功");
        } else {
            return Result.toError("删除失败");
        }
    }
}
