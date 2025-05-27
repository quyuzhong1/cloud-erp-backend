package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * <p>
 * 流程配置 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Mapper
public interface CfgProcessMapper extends BaseMapper<CfgProcessEntity> {
    /**
     * @description: 高级查询流程配置
     * @author: hcg
     * @date: 2025/5/12 17:45
     * @param: query, params
     * @return: CfgProcessDTO.ListDTO
     **/
    IPage<CfgProcessDTO.ProcessViewDTO> getProcessWithRuleAndAggregatedExps(Page query, @Param("params") CfgProcessDTO.SearchParamDTO params);

    /**
     * @description: view
     * @author: hcg
     * @date: 2025/5/12 17:45
     * @param: query, params
     * @return: CfgProcessDTO.ListDTO
     **/
    CfgProcessDTO.ViewDTO getViewDTOById(String id);

    /**
     * 高级查询tab
     *
     * @param params
     * @return
     * @author hcg
     * @date: 2025-05-12
     */
    List<CfgProcessDTO.TabListDTO> tabList(@Param("params") CfgProcessDTO.SearchParamDTO params);

}
