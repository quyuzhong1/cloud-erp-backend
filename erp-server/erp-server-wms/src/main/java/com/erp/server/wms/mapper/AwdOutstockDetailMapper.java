package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.AwdOutstockDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.AwdOutstockDetailDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
 */
@Mapper
public interface AwdOutstockDetailMapper extends BaseMapper<AwdOutstockDetailEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AwdOutstockDetailDTO.ListDTO> paging(Page query, @Param("params") AwdOutstockDetailDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AwdOutstockDetailDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AwdOutstockDetailDTO.ListDTO> listExport(@Param("params") AwdOutstockDetailDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AwdOutstockDetailDTO.TabListDTO> tabList(@Param("params") AwdOutstockDetailDTO.PagingParamDTO searchParam);
}
