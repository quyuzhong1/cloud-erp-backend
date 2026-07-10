package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.model.wms.entity.FbsInventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * FBS库存 Mapper 接口
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Mapper
public interface FbsInventoryMapper extends BaseMapper<FbsInventoryEntity> {

    /**
     * 分页查询
     *
     * @param query 分页参数
     * @param params 查询参数
     * @return 分页数据
     */
    IPage<FbsInventoryDTO.ListDTO> paging(Page<FbsInventoryDTO.ListDTO> query, @Param("params") FbsInventoryDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     *
     * @param params 查询参数
     * @return 导出数据
     */
    List<FbsInventoryDTO.ListDTO> listExport(@Param("params") FbsInventoryDTO.ExportDTO params);

    /**
     * 列表汇总数量
     *
     * @param params 查询参数
     * @return 汇总数量
     */
    FbsInventoryDTO.SummaryNumber summaryNumber(@Param("params") FbsInventoryDTO.PagingParamDTO params);
}
