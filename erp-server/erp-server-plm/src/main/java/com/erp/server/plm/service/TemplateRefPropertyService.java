package com.erp.server.plm.service;


import com.common.core.serveice.SuperService;
import com.erp.model.plm.dto.TemplatePropertyDTO;
import com.erp.model.plm.entity.TemplateRefPropertyEntity;

import java.util.List;

/**
 * <p>
 * 模板属性关系表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-06
 */
public interface TemplateRefPropertyService extends SuperService<TemplateRefPropertyEntity> {

    
    /**
     * 保存模板与产品属性的关系表
     * @author yl
     * @date 2023-03-06 14:13
     * @param id
     * @param productPropertyIdList
     * @return void
     */
    void saveRef(String id, List<String> productPropertyIdList);

    /**
     * 根据模板id 集合获取到 属性信息
     * @author yl
     * @date 2023-03-06 16:26
     * @param templateIdList
     * @return java.util.List<com.erp.model.plm.dto.TemplatePropertyDTO>
     */
    List<TemplatePropertyDTO> getByTemplateIds(List<String> templateIdList);
}
