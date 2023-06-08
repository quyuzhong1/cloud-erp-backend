package com.erp.server.scm.service;
import com.erp.model.scm.entity.SubcontractOrderEntity;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SubcontractOrderDTO;

 import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 委外订单 服务类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
public interface SubcontractOrderService extends SuperService<SubcontractOrderEntity> {

      /**
      * 分页列表查询
      * @author will
      * @date: 2023-06-08
      * @param pagingParamDTO
      * @return PagingVO<SubcontractOrderDTO.ListDTO>>
      */
      PagingVO<SubcontractOrderDTO.ListDTO> paging(PagingDTO<SubcontractOrderDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return List<SubcontractOrderDTO.TabListDTO>>
     */
     List<SubcontractOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author will
     * @date: 2023-06-08
     * @param id
     * @return
     */
     SubcontractOrderDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     String add(SubcontractOrderDTO.AddDTO dto);

     /**
     * 修改
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void update(SubcontractOrderDTO.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void addAndSubmit(SubcontractOrderDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author will
     * @date: 2023-06-08
     * @param dto
     * @return
     */
     void updateAndSubmit(SubcontractOrderDTO.UpdateDTO dto);

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
    void exportList(SubcontractOrderDTO.ExportDTO dto, HttpServletResponse response);

}
