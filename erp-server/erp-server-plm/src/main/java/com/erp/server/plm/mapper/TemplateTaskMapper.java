package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.TemplateSearchDTO;
import com.erp.model.plm.dto.TemplateTaskShowDTO;
import com.erp.model.plm.entity.TemplateTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Classname TemplateTaskMapper
 * @Description TODO
 * @Date 2022-09-20 15:33
 * @Created by yl
 */
@Mapper
public interface TemplateTaskMapper  extends BaseMapper<TemplateTaskEntity> {
    /**
     * @description: 模板任务列表查询
     * @author Will
     * @date: 2022/11/14 9:35
     * @param query
     * @param params
     * @return IPage<TemplateTaskShowDTO>
     */
    IPage<TemplateTaskShowDTO> paging(Page query, @Param("params") TemplateSearchDTO params);
}
