package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.tms.enums.DictCostAttributionEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自发货费用 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-06
 */
public interface LogisticsBillCostService extends SuperService<LogisticsBillCostEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsBillCostDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-06
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillCostDTO.UpdateDTO dto,Boolean isImport);

    /**
     * @description: tab列表
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return List<TabListDTO>
     */
    List<LogisticsBillCostDTO.TabListDTO> tabList(PermissionsDTO dto, DictCostAttributionEnum attribution);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/11/13 15:35
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<LogisticsBillCostDTO.ListDTO> paging(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);
    /**
     * @description: 状态变更
     * @author Will
     * @date: 2023/11/13 15:36
     * @param id
     * @param reconciliationStatus
     * @return BatchResultDTO
     */
    BatchResultDTO updateReconciliationStatus(String id, String reconciliationStatus , LocalDateTime confirmTime);
    
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2023/11/13 15:36
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/11/13 15:37
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/11/13 16:20
     */
    Boolean exportExcel(LogisticsBillCostDTO.PagingParamDTO dto);

    void handleData(LogisticsBillCostEntity entity);

    /**
     * @description: 分页数据处理
     * @author Will
     * @date: 2024/5/13 14:54
     * @param records
     */
    void handleDataPaging( List<LogisticsBillCostDTO.ListDTO> records);

    Boolean invalidByLogisticsBillId(String logisticsBillId);

    List<LogisticsBillCostEntity> getByLogisticsBillIds(List<String> ids);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2024/3/25 11:46
     * @param id
     * @return ViewDTO
     */
    LogisticsBillCostDTO.ViewDTO view(String id);
    /**
     * @description: 根据物流单Id集合删除
     * @author Will
     * @date: 2024/3/25 14:11
     * @param logisticsBillDetailIdList

     */
    void deleteByLogisticsBillDetailIdList(List<String> logisticsBillDetailIdList);

    List<LogisticsBillCostEntity> listByLogisticsBillIdList(List<String> mainIdList);

    /**
     * @description: 根据物流单明细id集合查询
     * @author Will
     * @date: 2024/5/11 14:13
     * @param logisticsBillDetailIdList
     * @return List<LogisticsBillCostEntity>
     */
    List<LogisticsBillCostEntity> listByLogisticsBillDetailIdList (List<String> logisticsBillDetailIdList);

    /**
     * 根据销售出库单 获取销售出库单自发货费用列表
     * @param ids
     * @return
     */
    List<LogisticsBillCostDTO.OutStockDTO> listBillCostByOutstockIds(List<String> ids);
    /**
     * @description: 尾程费用导出查询
     * @author Will
     * @date: 2024/5/9 20:10
     * @param dto
     * @return List<ListDTO>
     */
    List<LogisticsBillCostDTO.ListDTO> listLogisticsLastMileCostExport(LogisticsBillCostDTO.PagingParamDTO dto);
    /**
     * @description: 导入数据处理
     * @author Will
     * @date: 2024/5/9 20:16
     * @param successList
     * @param errorList
     */
    void handleImportSuccessList (List<LogisticsBillCostExcelDTO> successList, List<LogisticsBillCostExcelDTO > errorList,String dictCostAttribution );
    /**
     * @description: 更新店铺
     * @author Will
     * @date: 2024/5/11 18:38
     * @param dto
     * @return Boolean
     */
    Boolean updateShopCharge(LogisticsBillCostDTO.UpdateShopChargeDTO dto);

    BigDecimal getActualLogisticCost(String soId);

    /**
     * 根据物流单和对账单id更新数据状态
     * @param logisticsBillIds
     * @param reconciliationId
     * @param status
     */
    void updateStatusByLogisticsBillIdsAndReconciliationId(List<String> logisticsBillIds, String reconciliationId, String status);

    /**
     * 根据对账单重算费用清单
     *
     * @param mainEntity
     * @param list
     * @param actualMap
     */
    void updateLogisticsBillCost(TmsFirstMileReconciliationEntity mainEntity, List<TmsFirstMileReconciliationDetailEntity> list, Map<String, TmsFirstMileReconciliationDetailEntity> actualMap);

    PagingVO<LogisticsBillCostDTO.ListDTO> exportLogisticsBillCost(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto);

    /**
     * 根据对账id删除已生成对账单产生的费用明细
     * @param reconciliationId
     * @param logisticsBillIds
     */
    void removeByReconciliationIds(String reconciliationId, List<String> logisticsBillIds);
    /**
     * 删除对账单费用记录
     * @param reconciliationId
     * @param logisticsBillIds
     */
    void removeRefByReconciliationIds(String reconciliationId, List<String> logisticsBillIds);
}
