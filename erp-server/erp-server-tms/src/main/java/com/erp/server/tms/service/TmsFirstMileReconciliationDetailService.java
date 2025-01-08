package com.erp.server.tms.service;

import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.tms.dto.TmsCostDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.wms.entity.CfgAmzFulfillmentCenterEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 头程对账单明细 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface TmsFirstMileReconciliationDetailService extends SuperService<TmsFirstMileReconciliationDetailEntity> {

    /**
     * 新增
     *
     * @param dto DTO
     * @return AddDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * @date: 2024-03-25
     */
    Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO dto);

    /**
     * 分页
     *
     * @param dto DTO
     * @return Boolean
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto);

    /**
     * 更新状态
     *
     * @return BatchResultDTO
     * @author Jim
     * @date: 2024-03-25
     */
    BatchResultDTO updateStatus(String id, String status);

    /**
     * 导入
     *
     * @return ImportDTO
     * @author Jim
     * @date: 2024-03-25
     */
    TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsFirstMileReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response);

    /**
     * 待对账分页
     *
     * @author Jim
     * @date: 2024-03-25
     */
    PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto);


    /**
     * 导出
     *
     * @author Jim
     * @date: 2024-03-25
     */
    void exportList(TmsFirstMileReconciliationDetailDTO.ExportDTO dto);


    /**
     * 通过
     *
     * @author Jim
     * @date: 2024-03-25
     */
    List<TmsFirstMileReconciliationDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 更新
     *
     * @author Jim
     * @date: 2024-03-25
     */
    Boolean update(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, TmsFirstMileReconciliationEntity mainEntity);

    /**
     * 补充明细信息
     *
     * @author Jim
     * @date: 2024-03-25
     */
    void fillDetailList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList, String currency, CurrencyDTO.ViewDTO currencyViewDTO);

    /**
     * 根据来源IDS返回:预估/实际/差异
     *
     * @author Jim
     * @date: 2024-03-25
     */
    List<TmsFirstMileReconciliationDetailDTO.ListDTO> addWaitReconciliation(List<String> sourceIds);

    /**
     * 生成实际和差异记录
     * @param sourceListDTO
     * @param reconciliationCount
     * @param keepActual 是否保留实际账单
     * @return
     */
    List<TmsFirstMileReconciliationDetailDTO.ListDTO> generateAllTypeDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO,int reconciliationCount,boolean keepActual);

    Map<String, TmsFirstMileReconciliationDetailEntity> handleUpdateData(List<TmsFirstMileReconciliationDetailEntity> list, String mainId, List<TmsFirstMileReconciliationDetailEntity> oldList);

    void fillWaitReconciliationList(List<? extends TmsFirstMileReconciliationDetailDTO.ListDTO> records, String mainId);

    /**
     * 分页数据填充
     * @param records
     */
    void fillWaitReconciliationData(List<? extends TmsFirstMileReconciliationDetailDTO.ListDTO> records);

    CurrencyDTO.ViewDTO getCurrencyView(String currency);

    void checkRemoveByMainId(String id);

    void changeLogisticsBillCost(List<String> sourceIds, String reconciliationStatus);

    List<DictCountryDTO.ListDTO> checkAndFindCountry(FirstMileDeliveryEntity delivery, List<CfgAmzFulfillmentCenterEntity> centerList, List<DictCountryDTO.ListDTO> conuntryList);

    List<DictCountryDTO.ListDTO> defaultCountry(String toCountry, List<DictCountryDTO.ListDTO> countryList);

    /**
     * 根据mainId查询历史实际明细并转换UpdateDTO,格式Map<SourceId, Map<cfgCostId, UpdateDTO>>
     */
    Map<String, Map<String, TmsCostDetailDTO.UpdateDTO>> convertUpdateDTOAndMap(List<TmsFirstMileReconciliationDetailEntity> oldDetailList);

    /**
     * 自动生成对账单
     */
    void autoGenFirstMileReconciliation(LocalDate startDate, LocalDate endDate);

    void addOrUpdateCost(List<TmsFirstMileReconciliationDetailEntity> list);


    String getCurrencyById(String mainId);

    List<TmsFirstMileReconciliationDetailEntity> listByMainIdsBySort(List<String> mainIds);

    /**
     * 根据明细id进行查询对账单明细
     * @param sourceIds
     * @param type
     * @return
     */
    List<TmsFirstMileReconciliationDetailEntity> listBySourceIds(List<String> sourceIds, String type);

    /**
     * 根据业务单号获取对账明细
     *
     * @param businessCodes
     * @param status
     * @param type
     * @return
     */
    List<TmsFirstMileReconciliationDetailEntity> listBySourceIdsAndStatus(List<String> businessCodes, String status, String type);

    /**
     * 更新对账明细
     * @param detailList
     * @param old
     */
    void updateReconciliationDetail(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, TmsFirstMileReconciliationEntity old);

    PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> exportFirstMileReconciliationDetail(PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto);

    /**
     * 重置总物流单费用
     * @param codeList
     */
    void initTotalLogisticsCost(List<String> codeList);

    /**
     * 根据关联单号查询对账详情
     * @param relationCodeList
     */
    List<TmsFirstMileReconciliationDetailEntity> listByRelationCode(List<String> relationCodeList);
}
