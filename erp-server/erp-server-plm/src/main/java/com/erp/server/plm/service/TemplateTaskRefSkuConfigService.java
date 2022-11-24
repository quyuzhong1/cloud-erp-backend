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
    void saveTemplateTaskRefSkuConfig(String templateId, String productId);

    void copyTemplateTaskSkuConfig(String flagId, String productId, List<CopySourceDTO> taskSourceList);
}
