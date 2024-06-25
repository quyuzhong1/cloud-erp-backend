package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp输入任务数据库字段转换处理器，使用替换key方法，目前支持{-,_}，被dmp任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputReplaceDmpHandler extends DmpInputBaseDmpHandler{
	
	@Override
	protected List<String> convertKey(String originalKey) {
		return Collections.singletonList(originalKey.replace("-", "").replace("_", ""));
	}
}
