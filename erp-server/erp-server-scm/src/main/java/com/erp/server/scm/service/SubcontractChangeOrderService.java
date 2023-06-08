package com.erp.server.scm.service;
import com.erp.model.scm.entity.SubcontractChangeOrderEntity;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SubcontractChangeOrderDTO;

 import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 委外变更单 服务类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
public interface SubcontractChangeOrderService extends SuperService<SubcontractChangeOrderEntity> {

      /**
      * 分页列表查询
      * @author will
      * @date: 2023-06-08
      * @param pagingParamDTO
      * @return PagingVO<SubcontractChangeOrderDTO.ListDTO>>
      */
      PagingVO<SubcontractChangeOrderDTO.ListDTO> paging(PagingDTO<SubcontractChangeOrderDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return List<SubcontractChangeOrderDTO.TabListDTO>>
     */
     List<SubcontractChangeOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author will
     * @date: 2023-06-08
     * @param id
     * @return
     */
     SubcontractChangeOrderDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     String add(SubcontractChangeOrderDTO.AddDTO dto);

     /**
     * 修改
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void update(SubcontractChangeOrderDTO.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void addAndSubmit(SubcontractChangeOrderDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void updateAndSubmit(SubcontractChangeOrderDTO.UpdateDTO dto);

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
    * @author will
    * @date: 2023-06-08
    * @param dto
    * @param response
    * @return
    */
    void exportList(SubcontractChangeOrderDTO.ExportDTO dto, HttpServletResponse response);

}
