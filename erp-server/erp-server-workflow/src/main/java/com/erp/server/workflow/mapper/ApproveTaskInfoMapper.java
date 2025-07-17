package com.erp.server.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 三方生成查询 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-05-27
 */
@Mapper
public interface ApproveTaskInfoMapper extends BaseMapper<ApproveTaskInfoEntity> {
    /**
     * tab列表
     * @author will
     * @date 2025/5/27 10:06
     * @param searchParam
     * @return List<TabListDTO>
     */
    List<ApproveTaskInfoDTO.TabListDTO> tabList(@Param("searchParam") ProcessDelegateDTO.PagingParamDTO searchParam);
    /**
     * 分页查询
     * @author will
     * @date 2025/5/27 10:28
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ApproveTaskInfoDTO.ListDTO> paging(Page query,@Param("params") ApproveTaskInfoDTO.PagingParamDTO params);
}
