package com.erp.server.dmp.inout.handler.input.task.mongo;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入任务mongo基础处理器，被mongo任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputErpPushMongoHandler extends DmpInputBaseMongoHandler{
	@Override
		protected List<String> convertKey(String originalKey) {
			if("id".equals(originalKey)) {
				return Collections.singletonList("messageId");
			}
			return super.convertKey(originalKey);
		}
}
