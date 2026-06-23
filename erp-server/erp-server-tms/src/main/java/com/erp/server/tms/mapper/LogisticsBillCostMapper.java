package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
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
    List<LogisticsBillCostDTO.TabCountDTO> listCount(@Param("params")LogisticsBillCostDTO.PagingParamDTO pagingParamDTO,@Param("type")String type);

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
    /**
     * 列表展示合计
     * @author will
     * @date 2026/1/20 12:20
     * @param params
     * @return ListDTO
     */
    LogisticsBillCostDTO.TotalCountDTO listTotalCostValueCount(@Param("params")LogisticsBillCostDTO.PagingParamDTO params);

    List<String> listByCanPushAllocation(@Param("params") TmsAsyncTaskRecordDTO.PushParamsDTO params);

    /**
     * 游标分页查询可下推分摊的费用ID（keyset pagination）
     * 直接 JOIN logistics_bill 过滤无需分摊的单据，每次仅加载一批
     *
     * @param params 查询条件（含 lastId 游标位置、batchSize 批大小）
     * @return 当前批次的费用ID列表，按 id 升序
     * @author jack
     * @date 2026-04-22
     */
    List<String> pageByCanPushAllocation(@Param("params") TmsAsyncTaskRecordDTO.PushParamsDTO params);

    /**
     * 统计可下推分摊的费用总条数，用于设置任务的 detailCount
     *
     * @param params 查询条件
     * @return 总条数
     * @author jack
     * @date 2026-04-22
     */
    Integer countByCanPushAllocation(@Param("params") TmsAsyncTaskRecordDTO.PushParamsDTO params);
    /**
     * 查询计费重合计
     * @author will
     * @date 2026/4/15 10:53
     * @param params
     * @return java.math.BigDecimal
     */
    BigDecimal listTotalBillingWeightLogisticsCount(@Param("params") LogisticsBillCostDTO.PagingParamDTO params);
    /**
     * 批量确认导入数据
     *
     * @param confirmList 导入确认数据
     * @param reconciliationStatus 对账状态
     * @param confirmUserId 确认人ID
     * @param confirmUserName 确认人名称
     * @return 更新条数
     */
    int batchConfirmImport(@Param("confirmList") List<ImportHistoryRecordDTO.ImportConfirmDTO> confirmList,
                           @Param("reconciliationStatus") String reconciliationStatus,
                           @Param("confirmUserId") String confirmUserId,
                           @Param("confirmUserName") String confirmUserName);
}
