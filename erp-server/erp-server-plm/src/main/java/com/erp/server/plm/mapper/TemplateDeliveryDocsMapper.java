package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsShowDTO;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * @Entity entity..TemplateDeliveryDocs
 */
@Mapper
public interface TemplateDeliveryDocsMapper extends BaseMapper<TemplateDeliveryDocsEntity> {
    /**
     * @description: 模板输出物列表查询
     * @author Will
     * @date: 2022/11/14 18:47
     * @param query
     * @param params
     * @return IPage<TemplateDeliveryDocsShowDTO>
     */
    IPage<TemplateDeliveryDocsShowDTO> paging(Page query, @Param("params") TemplateSearchDTO params);
}




