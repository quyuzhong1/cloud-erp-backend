package com.erp.server.tms.mapper;
import com.erp.model.tms.entity.CfgFileParseFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.tms.dto.CfgFileParseFileDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 清洗配置-文件识别规则子表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
@Mapper
public interface CfgFileParseFileMapper extends BaseMapper<CfgFileParseFileEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<CfgFileParseFileDTO.ListDTO> paging(Page query, @Param("params") CfgFileParseFileDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") CfgFileParseFileDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<CfgFileParseFileDTO.ListDTO> listExport(@Param("params") CfgFileParseFileDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<CfgFileParseFileDTO.TabListDTO> tabList(@Param("params") CfgFileParseFileDTO.PagingParamDTO searchParam);
}
