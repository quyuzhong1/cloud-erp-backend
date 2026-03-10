package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.AwdOutstockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.AwdOutstockDTO;
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
public interface AwdOutstockMapper extends BaseMapper<AwdOutstockEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AwdOutstockDTO.ListDTO> paging(Page query, @Param("params") AwdOutstockDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AwdOutstockDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AwdOutstockDTO.ListDTO> listExport(@Param("params") AwdOutstockDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AwdOutstockDTO.TabListDTO> tabList(@Param("params") AwdOutstockDTO.PagingParamDTO searchParam);
}
