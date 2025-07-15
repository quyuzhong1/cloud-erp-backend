package com.erp.server.workflow.context;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/20 12:14
 */

import com.erp.server.workflow.handler.ProcessFormHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-20
 *@Description:
 *@Version: 1.0
 */
@Component
public class ProcessFormFactory {
    @Resource
    private List<ProcessFormHandler> handlers;

    /**
     * 根据事件名获取具体处理对象
     * @param event 事件名，
     */
    public ProcessFormHandler getAssembleFormHandler(String event) {
        for (ProcessFormHandler handler : handlers) {
            if (handler.isMatch(event)) {
                return handler;
            }
        }
        return null;
    }

    public ProcessFormHandler getConstructBillHandler(String event) {
        for (ProcessFormHandler handler : handlers) {
            if (handler.getEventType().getCode().equals(event)) {
                return handler;
            }
        }
        return null;
    }
}
