package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.plm.dto.ProjectTemplateDTO;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 项目模板信息 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectTemplateMapper extends BaseMapper<ProjectTemplateEntity> {

    List<StartItemSourceDTO> getStartItemSource(@Param("sourceType") Integer sourceType);
    /**
     * @description: 模板管理列表查询
     * @author Will
     * @date: 2022/11/11 12:19
     * @param query
     * @param params
     * @return IPage<ProjectTemplateDTO>
     */
    IPage<ProjectTemplateDTO> paging(Page query, @Param("params") BaseSearchDTO params);

    /**
     * 获取立项模板产品属性
     * @author yl
     * @date 2023-02-21 14:52
     * @param type
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    List<Map<String, Object>> getProductPropertyList(@Param("type") String type);

    /**
     * 获取模板id 和名称
     * @author yl
     * @date 2023-03-06 18:31
     * @param propertyId
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     */
    List<Map<String, Object>> getByPropertyId(@Param("propertyId") String propertyId);
}
