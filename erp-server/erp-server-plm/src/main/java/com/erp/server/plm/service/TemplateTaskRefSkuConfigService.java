package com.erp.server.plm.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.TemplateTaskRefSkuConfigEntity;

import java.util.List;

/**
 * 模板任务与sku字段配置关系表(TemplateTaskRefSkuConfig)表服务接口
 *
 * @author yl
 * @since 2022-11-24 16:19:15
 */
public interface TemplateTaskRefSkuConfigService  extends IService<TemplateTaskRefSkuConfigEntity> {

    /**
     * 保存模板与sku配置关系表
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-11-24 16:28
     */
    void saveTemplateTaskRefSkuConfig(String templateId, String productId,List<CopySourceDTO> taskSourceList);

    
    /**
     * 新增产品 复制模板任务与sku 关系
     * @author yl
     * @date 2023-03-10 10:30
     * @param flagId
     * @param productId
     * @param taskSourceList
     * @return java.util.List<java.lang.String>
     */
    List<String> copyTemplateTaskSkuConfig(String flagId, String productId, List<CopySourceDTO> taskSourceList);

    void addTemplateTaskRefSkuConfig(String taskId,String templateId, String fieldConfigType, String fieldJson);

    TemplateTaskRefSkuConfigEntity getByTaskId(String id);

    /**
     * 修改任务的时候  如果选择不关联就要删除sku 与任务的关系
     * @author yl
     * @date 2023-03-10 17:27
     * @param taskId
     * @return void
     */
    void removeByTaskId(String taskId);
}
