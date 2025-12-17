package com.erp.server.workflow.listeners;

import com.erp.rpc.plm.feign.PlmTaskFeign;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 一般任务审核监听
 *
 * @Classname

 * @Date 2022-10-18 11:02
 * @Created by yl
 */
@Service
public class GeneralTaskResultListener implements ExecutionListener {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        //这个是流程id
        String parentActivityInstanceId = delegateExecution.getParentActivityInstanceId();
        CompletableFuture.supplyAsync(() -> {
            plmTaskFeign.approvalTaskPass(parentActivityInstanceId);
            return Boolean.TRUE;
        });
    }
}
