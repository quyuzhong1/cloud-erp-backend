package com.erp.server.workflow.service;

/**
 * 飞书审批示例详情拉取接口服务
 * @author will
 * @date 2025/10/21 19:11
 */
public interface FsInstancesService {

    /**
     * 拉取飞书审批示例详情
     * @author will
     * @date 2025/10/21 19:13
     * @return void
     */
    void pullFsInstancesDetails(String data);
}
