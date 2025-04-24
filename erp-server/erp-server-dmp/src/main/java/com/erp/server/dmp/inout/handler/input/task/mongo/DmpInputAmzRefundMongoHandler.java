package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * dmp输入任务mongo转换处理器，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAmzRefundMongoHandler extends DmpInputBaseMongoHandler{

	@Override
	protected List<Map<String, Object>> getDataList(DmpInputTaskFileContentTypeEnum contentType , List<String> resultList) {
		List<Map<String, Object>> dataList = super.getDataList(contentType, resultList);
		List<Map<String, Object>> resultDataList = new LinkedList<>();
		for (Map<String, Object> respMap : dataList) {
			Object refundEventListObj = respMap.get("refundEventList");
			if (null == refundEventListObj){
				continue;
			}
			List<Map<String, Object>> refundEventList = (List<Map<String, Object>>) refundEventListObj;
			resultDataList.addAll(refundEventList);
		}
		return resultDataList;
	}
}
