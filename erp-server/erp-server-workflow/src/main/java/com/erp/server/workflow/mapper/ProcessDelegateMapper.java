package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 委托审批 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-05-12
 */
@Mapper
public interface ProcessDelegateMapper extends BaseMapper<ProcessDelegateEntity> {
    /**
     * tab列表显示
     * @author will
     * @date 2025/5/12 18:18
     * @param searchParam
     * @return Integer
     */
    List<ProcessDelegateDTO.TabListDTO> tabList(@Param("params") ProcessDelegateDTO.PagingParamDTO searchParam);
    /**
     * 分页查询
     * @author will
     * @date 2025/5/12 18:32
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ProcessDelegateDTO.ListDTO> paging(Page query, @Param("params") ProcessDelegateDTO.PagingParamDTO params);
    /**
     * 根据流程定义id查询委托审批信息
     * @author will
     * @date 2025/5/19 17:48
     * @param processDefinitionId
     * @return List<ProcessDelegateEntity>
     */
    List<ProcessDelegateEntity> getByProcessDefinitionId(@Param("processDefinitionId")String processDefinitionId);
}
