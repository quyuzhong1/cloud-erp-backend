package com.erp.server.scm.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.entity.ContractInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 合同管理表 服务类
 * </p>
 *
 * @author will
 * @since 2025-06-16
 */
public interface ContractInfoService extends SuperService<ContractInfoEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ContractInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return
    */
    Boolean update(ContractInfoDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2025-06-16
    * @param pagingParamDTO
    * @return PagingVO<ContractInfoDTO.ListDTO>>
    */
    PagingVO<ContractInfoDTO.ListDTO> paging(PagingDTO<ContractInfoDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return List<ContractInfoDTO.TabListDTO>>
    */
    List<ContractInfoDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2025-06-16
    * @param id
    * @return
    */
    ContractInfoDTO.ViewDTO view(String id);

     /**
     * 提交审核
     * @author will
     * @date: 2025-06-16
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2025-06-16
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2025-06-16
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @return
    */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
    * 导出Excel
    * @author will
    * @date: 2025-06-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(ContractInfoDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, ContractInfoEntity entity);

    BatchResultDTO enable(String id, Boolean disabled);

    ExportZipResultDTO exportZip(ContractInfoDTO.PagingParamDTO dto);
}
