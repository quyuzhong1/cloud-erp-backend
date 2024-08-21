package com.erp.server.srm.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;

import java.util.List;

/**
 * <p>
 * 采购对账单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationService extends SuperService<PoReconciliationEntity> {

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(PoReconciliationDTO.UpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 12:12
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);
    /**
     * @param dto
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:21
     */
    void exportList(PoReconciliationDTO.PagingParamDTO dto);
    /**
     * @description: 对账确认
     * @author Will
     * @date: 2024/1/23 11:53
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO confirm(String id);
    /**
     * @description: 取消确认
     * @author Will
     * @date: 2024/1/23 12:10
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelConfirm(String id);
    /**
     * @description: 查询列表统计
     * @author Will
     * @date: 2024/1/23 17:18
     * @param dto
     * @return List<TabListDTO>
     */
    List<PoReconciliationDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 查询主表
     * @author Will
     * @date: 2024/1/23 17:24
     * @param id
     * @return ViewDTO
     */
    PoReconciliationDTO.ViewDTO viewMain(String id);
    /**
     * @description: 查询明细
     * @author Will
     * @date: 2024/1/23 17:25
     * @param dto
     * @return List<ViewDTO>
     */
    List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto);
    /**
     * @param dto
     * @description: 导出对账单
     * @author Will
     * @date: 2024/1/25 10:44
     */
    void exportPoReconciliation(PoReconciliationDTO.PagingParamDTO dto);

    Integer countByStatus(String supplierId, String status);
}
