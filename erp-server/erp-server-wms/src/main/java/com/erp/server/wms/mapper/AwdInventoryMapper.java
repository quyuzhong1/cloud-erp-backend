package com.erp.server.wms.mapper;
import com.erp.model.wms.dto.FbaInventoryDTO;
import com.erp.model.wms.entity.AwdInventoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.wms.dto.AwdInventoryDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wtr
 * @since 2025-12-26
 */
@Mapper
public interface AwdInventoryMapper extends BaseMapper<AwdInventoryEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<AwdInventoryDTO.ListDTO> paging(Page query, @Param("params") AwdInventoryDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") AwdInventoryDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<AwdInventoryDTO.ListDTO> listExport(@Param("params") AwdInventoryDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<AwdInventoryDTO.TabListDTO> tabList(@Param("params") AwdInventoryDTO.PagingParamDTO searchParam);

    AwdInventoryDTO.SummaryNumber summaryNumber(@Param("params") AwdInventoryDTO.PagingParamDTO params);
}
