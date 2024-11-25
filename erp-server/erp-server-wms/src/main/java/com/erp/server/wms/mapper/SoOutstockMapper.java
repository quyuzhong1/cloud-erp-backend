package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryContainer;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售订单出库单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoOutstockMapper extends BaseMapper<SoOutstockEntity> {

    IPage<SoOutstockDTO.PagingViewDTO> paging(Page query, @Param("params") SoOutstockDTO.PagingParamDTO params);

    List<SoOutstockDTO.PagingViewDTO> listExport(@Param("params") SoOutstockDTO.ExportDTO dto);
    Page<SoOutstockDTO.PagingViewDTO> listExport(@Param("page") Page<SoOutstockDTO.PagingViewDTO> page, @Param("params") SoOutstockDTO.ExportDTO dto);

    List<InOutStockDTO> listInventoryInOut(@Param("ids") List<String> idList);

    List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(@Param("soId") String soId);

    List<SoOutstockDTO.ApproveCountDTO> listApproveCount(@Param("permissionSql")String permissionSql);

    /**
     * 根据来源id查询出库id 临时使用修复数据
     * @return
     */
    List<String> getIdsByTemp(@Param("tableName") String tableName);

    /**
     * PDA:分页列表
     * @Author Luo_WG
     * @Date 2023/8/22 14:42
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>
     **/
    IPage<SoOutstockDTO.PdaPagingViewDTO> pdaPaging(Page query, @Param("params") SoOutstockDTO.PdaPagingParamDTO params);

    /**
     * pda:列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/22 15:06
     * @param params
     * @return java.lang.Integer
     **/
    Integer listCount(@Param("params") SoOutstockDTO.PagingParamDTO params);


    Integer pdaListCount(@Param("params") SoOutstockDTO.PagingParamDTO params);


    /**
     * 临时接口批量更新
     * @param soOutstockList
     */
    void updateBatch(@Param("params") List<SoOutstockEntity> soOutstockList);
    /**
     * @description: 查询合计数据
     * @author Will
     * @date: 2023/11/1 14:25
     * @param params
     * @return PagingTotalDTO
     */
    SoOutstockDTO.PagingTotalDTO getTotalByQuery(@Param("params") SoOutstockDTO.PagingParamDTO params);

    List<SoOutstockEntity> listByAdvanceQuery(@Param("params") AdvanceQueryContainer container);

    /**
     * 根据条件获取数据对比系统数据
     * @param params
     * @return
     */
    List<Map<String, String>> getDataCompareByCondition(@Param("params") WmsDataCompareTaskDTO.SoOutstockDTO params);

    Integer getDataCompareByConditionCount(@Param("params") WmsDataCompareTaskDTO.SoOutstockDTO params);

    /**
     * 根据发货单id查询装箱清单
     * @Author Luo_WG
     * @Date 2023/11/28 17:15
     * @param id
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDetailDTO.ListPackingDetailDTO>
     **/
    List<WmsCartonDetailDTO.ListPackingDetailDTO> listPackingDetail(@Param("ids") List<String> id);

    /**
     * 导出装箱信息
     * @param dto
     * @return
     */
    List<WmsCartonSpecDTO.ExportPackingDTO> exportPacking(@Param("params") SoOutstockDTO.ExportDTO dto);

    List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuByMainId(@Param("mainId") String id);

    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateDeclare(@Param("params") TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    List<FirstMileDeliveryDTO.LogisticStatisticsDTO> logisticStatistics( FirstMileDeliveryDTO.StatisticsReq deliveryStaticsReq);

    List<SoOutstockDetailEntity> listApproveBySourceDetailIds(@Param("ids") List<String> ids);

    List<SoOutstockDTO.AmountDTO> listAmountBySkuIds(@Param("params") SoOutstockDTO.ListAmountParamDTO params);
}
