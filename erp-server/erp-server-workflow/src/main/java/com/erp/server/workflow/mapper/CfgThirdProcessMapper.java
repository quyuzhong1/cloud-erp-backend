package com.erp.server.workflow.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgThirdProcessDTO;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 三方审批生成 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-05-23
 */
@Mapper
public interface CfgThirdProcessMapper extends BaseMapper<CfgThirdProcessEntity> {

    /**
     * @description: 高级查询三方审批生成
     *
     * @author: hcg
     * @date: 2025-05-23
     * @param: searchParam
     * @return: java.util.List<com.erp.model.workflow.dto.CfgThirdProcessDTO.TabListDTO>
     *
     */
    List<CfgThirdProcessDTO.TabListDTO> tabList(@Param("params") CfgThirdProcessDTO.PagingParamDTO searchParam);

    /**
     * @description: 三方审批生成分页
     * @param query
     * @param params
     * @return
     */
    IPage<CfgThirdProcessDTO.ListDTO> paging(Page query, @Param("params")  CfgThirdProcessDTO.PagingParamDTO params);

    CfgThirdProcessDTO.ViewDTO getView(String id);
}
