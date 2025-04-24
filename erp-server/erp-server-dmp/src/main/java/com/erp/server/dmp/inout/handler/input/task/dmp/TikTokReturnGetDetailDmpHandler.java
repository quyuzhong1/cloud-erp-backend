package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 获取马帮订单详情数据
 */
@Service
@Scope("prototype")
public class TikTokReturnGetDetailDmpHandler extends DmpInputDoNextDmpHandler{

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		return (List<Map<String, Object>>) dmpInputMongoEntity.get("returnLineItems");
	}

}
