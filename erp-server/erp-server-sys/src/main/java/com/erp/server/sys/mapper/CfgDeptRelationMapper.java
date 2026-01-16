package com.erp.server.sys.mapper;
import com.erp.model.sys.entity.CfgDeptRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.sys.dto.CfgDeptRelationDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 部门关联表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2025-12-29
 */
@Mapper
public interface CfgDeptRelationMapper extends BaseMapper<CfgDeptRelationEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgDeptRelationDTO.ListDTO> paging(Page query, @Param("params") CfgDeptRelationDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgDeptRelationDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgDeptRelationDTO.ListDTO> listExport(@Param("params") CfgDeptRelationDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgDeptRelationDTO.TabListDTO> tabList(@Param("params") CfgDeptRelationDTO.PagingParamDTO searchParam);
}
