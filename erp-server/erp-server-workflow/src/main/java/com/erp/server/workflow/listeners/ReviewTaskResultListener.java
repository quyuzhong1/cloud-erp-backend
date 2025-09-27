package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 评审任务 会签结束
 *

 * @Date 2022-10-18 11:18
 * @Created by yl
 */
@Service
public class ReviewTaskResultListener implements ExecutionListener {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String parentActivityInstanceId = delegateExecution.getParentActivityInstanceId();
        CompletableFuture.supplyAsync(() -> {
            plmTaskFeign.approvalTaskPass(parentActivityInstanceId);
            return Boolean.TRUE;
        });
    }
}
