package com.erp.server.wms.service;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SubcontractIssueDTO;
import com.common.business.vo.PagingVO;

import java.util.List;

/**
 * <p>
 * 委外发料单 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
public interface SubcontractIssueService extends SuperService<SubcontractIssueEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SubcontractIssueDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    Boolean update(SubcontractIssueDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author will
    * @date: 2024-01-08
    * @param pagingParamDTO
    * @return PagingVO<SubcontractIssueDTO.ListDTO>>
    */
    PagingVO<SubcontractIssueDTO.ListDTO> paging(PagingDTO<SubcontractIssueDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return List<SubcontractIssueDTO.TabListDTO>>
    */
    List<SubcontractIssueDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author will
    * @date: 2024-01-08
    * @param id
    * @return
    */
    SubcontractIssueDTO.ViewDTO view(String id);

     /**
     * 提交审核
     * @author will
     * @date: 2024-01-08
     * @param id
     * @return
     */
    BatchResultDTO submit(String id,Boolean isProcess);

    /**
    * 审核
    * @author will
    * @date: 2024-01-08
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2024-01-08
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author will
    * @date: 2024-01-08
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author will
    * @date: 2024-01-08
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author will
    * @date: 2024-01-08
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author will
     * @date: 2024-01-08
     */
    void exportList(SubcontractIssueDTO.PagingParamDTO dto);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SubcontractIssueEntity entity);

    /**
     * @description: 委外明细分页
     * @author Will
     * @date: 2024/1/8 16:03
     * @param dto
     * @return List<SubcontractDetailListDTO>
     */
    List<SubcontractIssueDTO.SubcontractDetailListDTO> listSubcontractDetail(SubcontractIssueDTO.DetailPagingParamDTO dto);
    /**
     * @description: 根据来源id查询
     * @author Will
     * @date: 2024/1/15 10:49
     * @param sourceIdList
     * @return List<SubcontractIssueEntity>
     */
    List<SubcontractIssueEntity> listBySourceIdList(List<String> sourceIdList);
    /**
     * @description: 自动新增
     * @author Will
     * @date: 2024/1/19 9:47
     * @param dto
     * @return AddDTO
     */
    BaseResultDTO.AddDTO autoAdd(SubcontractIssueDTO.AutoAddDTO dto);
    /**
     * @description: 根据业务id更新金蝶id
     * @author Will
     * @date: 2024/1/27 9:43
     * @param businessId
     * @param syncKingdeeId
     */
    Boolean updateSyncKingdeeId(String businessId, String syncKingdeeId);

    PagingVO<SubcontractIssueDTO.ListDTO> exportSubcontractIssue(PagingDTO<SubcontractIssueDTO.PagingParamDTO> dto);
}
