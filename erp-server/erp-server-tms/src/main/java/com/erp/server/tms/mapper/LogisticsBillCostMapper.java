package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 自发货费用 Mapper 接口
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
@Mapper
public interface LogisticsBillCostMapper extends BaseMapper<LogisticsBillCostEntity> {
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2024/5/10 10:58
     * @param pagingParamDTO
     * @return Integer
     */
    Integer listCount(@Param("params")LogisticsBillCostDTO.PagingParamDTO pagingParamDTO);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/13 16:02
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<LogisticsBillCostDTO.ListDTO> paging(Page query,@Param("params") LogisticsBillCostDTO.PagingParamDTO params);
    /**
     * @description: 导出查询
     * @author Will
     * @date: 2023/11/13 16:23
     * @param params
     * @return List<ListDTO>
     */
    List<LogisticsBillCostDTO.ListDTO> listByExportExcel(@Param("params") LogisticsBillCostDTO.PagingParamDTO params);
    Page<LogisticsBillCostDTO.ListDTO> listByExportExcel(@Param("page") Page<LogisticsBillCostDTO.ListDTO> page, @Param("params") LogisticsBillCostDTO.PagingParamDTO params);

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(@Param("ids") List<String> ids);

    BigDecimal getActualLogisticCost(@Param("soId") String soId);

    /**
     * 查询汇率为0的费用
     * @return
     */
    List<LogisticsBillCostDTO.ExchangeRateDTO> listBillCostByExchangeRate();

    /**
     * 更新店铺负责人
     * @param shopId
     * @param userId
     * @param userName
     */
    void updateShopChargeId(@Param("shopId") String shopId, @Param("userId") String userId, @Param("userName") String userName);

    /**
     * 查询物流单费用没有物流单的数据
     * @return
     */
    List<LogisticsBillCostDTO.BillCostNoBillDTO> selectLogisticsBillCostNoBill();

    /**
     * 根据物流单Id和主单Id查询费用详情
     *
     * @param reconciliationIds
     * @param billIds
     * @param type
     * @return
     */
    List<LogisticsBillCostDTO.CostDetailDTO> listCostDetailByBillAndReconciliationIds(@Param("billIds") List<String> billIds, @Param("reconciliationIds") List<String> reconciliationIds,@Param("type") String type);
    /**
     *
     * @author will
     * @date 2025/8/21 16:26
     * @param params
     * @return List<String>
     */
    List<String> listLogisticsBillCostId(@Param("params") LogisticsBillCostDTO.ListParamDTO params);
}
