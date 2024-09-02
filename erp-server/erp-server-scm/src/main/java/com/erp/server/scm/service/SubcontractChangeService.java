package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SubcontractChangeDTO;
import com.erp.model.scm.entity.SubcontractChangeEntity;

import java.util.List;

/**
 * <p>
 * 委外变更单 服务类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
public interface SubcontractChangeService extends SuperService<SubcontractChangeEntity> {

      /**
      * 分页列表查询
      * @author will
      * @date: 2023-06-08
      * @param pagingParamDTO
      * @return PagingVO<SubcontractChangeOrderDTO.ListDTO>>
      */
      PagingVO<SubcontractChangeDTO.ListDTO> paging(PagingDTO<SubcontractChangeDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return List<SubcontractChangeOrderDTO.TabListDTO>>
     */
     List<SubcontractChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author will
     * @date: 2023-06-08
     * @param id
     * @return
     */
     SubcontractChangeDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     String add(SubcontractChangeDTO.AddDTO dto);

     /**
     * 修改
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void update(SubcontractChangeDTO.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void addAndSubmit(SubcontractChangeDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void updateAndSubmit(SubcontractChangeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author will
     * @date: 2023-06-08
     * @param ids
     * @return
     */
     void submit(List<String> ids);

    /**
    * 审核
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @return
    */
    void approve(BaseApproveParamDTO dto);

    /**
    * 反审核
    * @author will
    * @date: 2023-06-08
    * @param ids
    * @return
    */
    void disApprove(List<String> ids);

    /**
    * 删除
    * @author will
    * @date: 2023-06-08
    * @param ids
    * @return
    */
    void delete(List<String> ids);

    /**
    * 撤销
    * @author will
    * @date: 2023-06-08
    * @param ids
    * @return
    */
    void cancelProcess(List<String> ids);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @author will
     * @date: 2023-06-08
     */
    void exportList(SubcontractChangeDTO.PagingParamDTO dto);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/6/19 18:50
     * @param ids
     * @param remark
     */
    void invalid(List<String> ids, String remark);

    /**
     * 修改金蝶同步信息
     * @Author Luo_WG
     * @Date 2023/5/25 10:43
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * @description: 查询变更单
     * @author Will
     * @date: 2023/6/28 14:58
     * @param ids
     * @return List<SubcontractChangeEntity>
     */
    List<SubcontractChangeEntity> listBySourceIds(List<String> ids);

    PagingVO<SubcontractChangeDTO.ListDTO> exportSubcontractChangeOrder(PagingDTO<SubcontractChangeDTO.PagingParamDTO> dto);
}
