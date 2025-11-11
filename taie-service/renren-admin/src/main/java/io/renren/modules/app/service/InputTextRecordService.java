package io.renren.modules.app.service;

import io.renren.common.service.BaseService;
import io.renren.modules.app.entity.InputTextRecord;
import io.renren.modules.app.message.proto.Message;
import io.renren.modules.app.param.InputTextRecordParam;
import io.renren.modules.app.vo.InputTextGroup;


public interface InputTextRecordService extends BaseService<InputTextRecord> {
    
    /**
     * 聚合查询输入日志
     * @param param 查询参数
     * @return 聚合后的输入日志分组
     */
	InputTextGroup queryGroupedRecords(InputTextRecordParam param);
	
	
	void adminInputText(Message.InputText inputText);

}
