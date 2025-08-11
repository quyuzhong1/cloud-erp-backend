package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * sku标准成本明细表 Mapper 接口
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Mapper
public interface SkuStdCostDetailMapper extends BaseMapper<SkuStdCostDetailEntity> {


    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<SkuStdCostDetailDTO.ListDTO> paging(Page query, @Param("params") SkuStdCostDetailDTO.PagingParamDTO params);

    /**
     * 状态数量
     * @param params
     * @return
     */
    List<ApproveStatusQtyDTO> listCount(@Param("params") SkuStdCostDetailDTO.PagingParamDTO params);

    /**
     * 导出Excel查询
     * @param params
     * @return
     */
    IPage<SkuStdCostDetailDTO.ListDTO> listExport(Page query, @Param("params") SkuStdCostDetailDTO.ExportDTO params);


    /**
     * 获取状态统计
     * @param searchParam
     * @return
     */
    List<SkuStdCostDetailDTO.TabListDTO> tabList(@Param("params") SkuStdCostDetailDTO.PagingParamDTO searchParam);


    Long allNewCount(@Param("params") SkuStdCostDetailDTO.PagingParamDTO searchParam);

    List<SkuStdCostDetailDTO.ListDTO> listDTOByIds(@Param("params") BaseIdsDTO.IdsDTO params);


    List<SkuStdCostDetailDTO.ListDTO> lastList(@Param("skuIds")List<String> skuIds, @Param("approveStatus") String approveStatus);

    IPage<SkuStdCostDetailDTO.ListDTO> historyPaging(Page query, @Param("params") SkuStdCostDetailDTO.HistoryPagingParamDTO params);
}
