package io.renren.modules.app.config;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.renren.common.constant.Constant;
import io.renren.modules.app.entity.ProxyInfoEntity;
import io.renren.modules.app.param.PkgParam;
import io.renren.modules.app.service.ProxyInfoService;
import io.renren.modules.sys.entity.SysUserEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class PkgAuthAspect {
	
	
	@Resource
	ProxyInfoService proxyInfoService; 


	@Before("execution(* io.renren.modules.app.web.admin..*(..)) ")
    public void modifyArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof PkgParam) { // 替换为你的参数对象类
            	PkgParam param = (PkgParam) arg;
            	SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
            	if(!user.getUsername().equals(Constant.SUPPER_ADMIN) ){
        			param.setPkg(getPkg(user.getUsername())); 
            	}
            }
        }
    }
	   
		
	private String  getPkg(String userName) {
		ProxyInfoEntity childPools = proxyInfoService.getOne(new LambdaQueryWrapper<ProxyInfoEntity>().eq(ProxyInfoEntity::getProxyUser, userName));
		if(childPools == null) throw new RuntimeException("The current user has not created porxy pkg");
		return childPools.getPkg();
	}
}
