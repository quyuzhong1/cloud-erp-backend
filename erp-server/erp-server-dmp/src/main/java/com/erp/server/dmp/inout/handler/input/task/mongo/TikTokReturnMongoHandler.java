package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * dmp输入任务mongo金蝶订单转换处理器，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class TikTokReturnMongoHandler extends DmpInputBaseMongoHandler{
	@Override
	protected List<String> getNextLevelIdList(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		return Collections.singletonList("");
	}
}
