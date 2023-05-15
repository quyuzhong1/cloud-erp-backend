package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
public interface TransferInfoService extends SuperService<TransferInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 11:22
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<TransferInfoDTO.ListDTO> paging(PagingDTO<TransferInfoDTO.SearchParamDTO> dto);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return List<ListStatusCountDTO>
     */
    List<TransferInfoDTO.ListStatusCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String add(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/5/15 11:23
     * @param dto
     * @return String
     */
    String addAndSubmit(TransferInfoDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean update(TransferInfoDTO.UpdateDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(TransferInfoDTO.UpdateDTO dto);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 查看详情
     * @author Will
     * @date: 2023/5/15 11:25
     * @param id
     * @return ViewDTO
     */
    TransferInfoDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/5/15 11:25
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/5/15 11:25
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/5/15 11:26
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/5/15 11:26
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/5/15 11:26
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(TransferInfoDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 根据来源ids查询
     * @author Will
     * @date: 2023/5/15 11:26
     * @param sourceIds
     * @return List<TransferInfoEntity>
     */
    List<TransferInfoEntity> listBySourceIds(List<String> sourceIds);
}
