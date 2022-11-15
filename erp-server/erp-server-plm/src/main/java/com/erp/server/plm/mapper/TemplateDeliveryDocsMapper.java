package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.TemplateDeliveryDocsDTO;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


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
     * @param ids
     * @return IPage<TemplateDeliveryDocsEntity>
     */
    IPage<TemplateDeliveryDocsEntity> paging(Page query, @Param("params")  TemplateDeliveryDocsDTO params, @Param("ids")  List<String> ids);
}




