package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgConditionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgConditionDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 条件配置表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-04-22
 */
@Mapper
public interface CfgConditionMapper extends BaseMapper<CfgConditionEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgConditionDTO.ListDTO> paging(Page query, @Param("params") CfgConditionDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgConditionDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgConditionDTO.ListDTO> listExport(@Param("params") CfgConditionDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgConditionDTO.TabListDTO> tabList(@Param("params") CfgConditionDTO.PagingParamDTO searchParam);
}
