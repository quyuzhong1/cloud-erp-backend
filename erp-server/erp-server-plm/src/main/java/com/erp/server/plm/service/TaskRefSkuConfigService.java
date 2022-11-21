package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.TaskRefSkuConfigEntity;

/**
 * 任务sku配置关系表(TaskRefSkuConfig)表服务接口
 *
 * @author Lambda
 * @since 2022-11-21 14:01:00
 */
public interface TaskRefSkuConfigService  extends IService<TaskRefSkuConfigEntity> {



    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(String id);


    void addSkuField(String taskId, String productId, String fieldConfigType, String fieldJson);
}
