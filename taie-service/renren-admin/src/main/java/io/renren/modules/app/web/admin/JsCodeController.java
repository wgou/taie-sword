package io.renren.modules.app.web.admin;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.exception.RenException;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.app.common.Utils;
import io.renren.modules.app.entity.JsCode;
import io.renren.modules.app.service.JsCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("jsCode")
@RestController
public class JsCodeController extends BaseController {
    @Resource
    private JsCodeService jsCodeService;

    @RequestMapping("page")
    public Result<PageData<JsCode>> page(@RequestBody JSONObject jsonObject) {
        Page<JsCode> page = parsePage(jsonObject);
        QueryWrapper<JsCode> query = new QueryWrapper<>();
        LambdaQueryWrapper<JsCode> lambda = query.lambda();
        String name = jsonObject.getString("name");
        if (StringUtils.isNotEmpty(name)) {
            lambda.like(JsCode::getName, name);
        }
        String identification = jsonObject.getString("identification");
        if (StringUtils.isNotEmpty(identification)) {
            lambda.like(JsCode::getIdentification, identification);
        }

        lambda.orderByDesc(JsCode::getUpdateDate);
        Page<JsCode> pageData = jsCodeService.page(page, lambda);
        return Result.toSuccess(new PageData<JsCode>(pageData.getRecords(), pageData.getTotal()));
    }

    @RequestMapping("update")
    public Result<Void> update(@RequestBody JsCode jsCode) {
        if (jsCode.getId() != null) {
            if (StringUtils.isEmpty(jsCode.getCode())) {
                throw new RenException("code is empty!");
            }
            //重新计算md5
            jsCode.setCodeMd5(DigestUtil.md5Hex(jsCode.getCode()));
            jsCode.setUpdateDate(Utils.now());

            jsCodeService.updateById(jsCode);
            return Result.toSuccess();
        }
        return Result.toError("id is empty");
    }

    @RequestMapping("save")
    public Result<Void> save(@RequestBody JsCode jsCode) {
        if (StringUtils.isEmpty(jsCode.getCode())) {
            throw new RenException("code is empty!");
        }

        jsCode.setCodeMd5(DigestUtil.md5Hex(jsCode.getCode()));
        jsCode.setUpdateDate(Utils.now());
        jsCodeService.save(jsCode);
        return Result.toSuccess();

    }

    @RequestMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id){
        jsCodeService.removeById(id);
        return Result.toSuccess();
    }

    @RequestMapping("/{id}")
    public Result<JsCode> getById(@PathVariable("id")Long id){
        JsCode jsCode = jsCodeService.getById(id);
        return Result.toSuccess(jsCode);
    }


}
