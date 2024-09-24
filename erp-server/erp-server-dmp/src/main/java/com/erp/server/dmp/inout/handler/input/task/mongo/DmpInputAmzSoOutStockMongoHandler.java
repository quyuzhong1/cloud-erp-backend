package com.erp.server.dmp.inout.handler.input.task.mongo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 亚马逊配送转mongo处理器，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAmzSoOutStockMongoHandler extends DmpInputBaseMongoHandler{
	
}
