package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.CfgFileParseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.CfgFileParseDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 清洗配置主表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
@Mapper
public interface CfgFileParseMapper extends BaseMapper<CfgFileParseEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgFileParseDTO.ListDTO> paging(Page query, @Param("params") CfgFileParseDTO.PagingParamDTO params);

    /**
    * 导出分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgFileParseDTO.ListDTO> exportPaging(Page query, @Param("params") CfgFileParseDTO.ExportDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgFileParseDTO.PagingParamDTO params);

    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgFileParseDTO.TabListDTO> tabList(@Param("params") CfgFileParseDTO.PagingParamDTO searchParam);
}
