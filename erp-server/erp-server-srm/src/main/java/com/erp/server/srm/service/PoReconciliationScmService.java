package com.erp.server.srm.service;

import com.common.business.dto.StatementDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购对账单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
public interface PoReconciliationScmService extends SuperService<PoReconciliationEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(PoReconciliationDTO.ScmUpdateDTO dto);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/1/20 12:12
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PoReconciliationDTO.ListDTO> paging(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2024/1/20 12:21
     * @param dto
     */
    void exportList(PoReconciliationDTO.PagingParamDTO dto);
    /**
     * @description: 采方确认
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
     * @description: 删除
     * @author Will
     * @date: 2024/1/23 14:19
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * @description: 签收
     * @author Will
     * @date: 2024/1/23 14:25
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO receive(String id);

    /**
     * @description: 查看详情(主表)
     * @author Will
     * @date: 2024/1/23 15:08
     * @param id
     * @return ViewDTO
     */
    PoReconciliationDTO.ViewDTO viewMain(String id);
    /**
     * @description: 查看详情(详情)
     * @author Will
     * @date: 2024/1/23 15:48
     * @param dto
     * @return List<ViewDTO>
     */
    List<PoReconciliationDetailDTO.ViewDTO> viewDetail(PoReconciliationDetailDTO.PagingParamDTO dto);
    /**
     * @description: 获取状态统计
     * @author Will
     * @date: 2024/1/23 15:59
     * @param dto
     * @return List<TabListDTO>
     */
    List<PoReconciliationDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @param dto
     * @description: 导出对账单
     * @author Will
     * @date: 2024/1/25 10:45
     */
    void exportPoReconciliation(PoReconciliationDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * @description: 查询待供方确认
     * @author Will
     * @date: 2024/1/27 15:19
     * @return List<AddPoReconciliationViewDTO>
     */
    List<PoReconciliationDetailDTO.AddPoReconciliationViewDTO> viewToBeSupplierConfirm(PermissionsDTO dto);

    /**
     * 更新对账单金额
     */
    void updateAmount (String id);

    PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationScmExport(PagingDTO<PoReconciliationDTO.PagingParamDTO> dto);

    StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliationScm(PoReconciliationDTO.PagingParamDTO dto);
}
