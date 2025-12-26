package com.erp.server.srm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 采购对账单明细 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationDetailScmService extends SuperService<PoReconciliationDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param addList
    * @return
    */
    BaseResultDTO.AddDTO add(List<PoReconciliationDetailDTO.AddDTO> addList);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDetailDTO.ListDTO> paging(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     */
    void exportList(PoReconciliationDetailDTO.PagingParamDTO dto);
    /**
     * @description: 生成对账单
     * @author Will
     * @date: 2024/1/23 18:04
     * @param dto
     * @return Boolean
     */
    Boolean generatePoReconciliation(PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto);
    /**
     * @description: 分页格式化
     * @author Will
     * @date: 2024/1/25 11:44
     * @param list

     */
    void fillList(List<PoReconciliationDetailDTO.ListDTO> list,Boolean isSrm);
    /**
     * @description: 根据来源明细id集合查询
     * @author Will
     * @date: 2024/1/25 16:58
     * @param sourceDetailIdList
     * @return List<PoReconciliationDetailEntity>
     */
    List<PoReconciliationDetailEntity> listDetailBySourceDetailIdList(List<String> sourceDetailIdList);
    /**
     * @description: 根据来源明细id集合删除
     * @author Will
     * @date: 2024/1/25 17:02
     * @param sourceDetailIdList
     */
    void deleteDetailBySourceDetailIdList(List<String> sourceDetailIdList,boolean isFromDisApprove);
    /**
     * @description: 自动生成对账单
     * @author Will
     * @date: 2024/2/2 14:41
     * @param startDate
     * @param endDate
     */
    void autoGeneratePoReconciliation(LocalDate startDate, LocalDate endDate);
    /**
     * @description: 更新业务状态
     * @author Will
     * @date: 2024/2/2 17:33
     * @param statusDTO
     */
    void updateBusinessStatusBySourceIdList(PoReconciliationDetailDTO.UpdateBusinessStatusDTO statusDTO);

    /**
     * 获取供应商未确认订单明细数量
     * @param supplierId
     * @return
     */
    Integer countSupplierUnConfirmOrderDetail(String supplierId);

    PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetailScm(PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto);
    /**
     * 手动生成
     * @author will
     * @date 2025/6/11 15:49
     * @param paramDTO
     * @return BatchResultDTO
     */
    BatchResultDTO manualGenerate(PoReconciliationDetailDTO.GenerateParamDTO paramDTO);
    /**
     * 状态更新
     * @author will
     * @date 2025/6/11 15:51
     * @param id
     * @param status
     * @return BatchResultDTO
     */
    BatchResultDTO updateStatus(String id,String status);
    /**
     * 添加设置
     * @author will
     * @date 2025/6/11 16:00
     * @return void
     */
    Boolean addSetting(PoReconciliationDetailDTO.AddSettingDTO addSettingDTO);
    /**
     * 更新待对账明细的对账状态
     * @author will
     * @date 2025/12/24 10:10
     * @param detailIdList
     * @return void
     */
    void autoUpdateStatus(List<String> detailIdList);
    /**
     * 根据主表id查询
     * @author will
     * @date 2025/12/24 18:44
     * @param mainId
     * @return List<PoReconciliationDetailEntity>
     */
    List<PoReconciliationDetailEntity> listByMainId(String mainId);
}
