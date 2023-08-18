package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoB2cEntity;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * B2C销售订单表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cService extends SuperService<SoB2cEntity> {

      /**
      * 分页列表查询
      * @author Will
      * @date: 2023-08-18
      * @param pagingParamDTO
      * @return PagingVO<SoB2cDTO.ListDTO>>
      */
      PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return List<SoB2cDTO.TabListDTO>>
     */
     List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Will
     * @date: 2023-08-18
     * @param id
     * @return
     */
     SoB2cDTO.ViewDTO view(String id);

     /**
     * 新增
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
     String add(SoB2cDTO.AddDTO dto);

     /**
     * 修改
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
    BatchResultDTO update(SoB2cDTO.UpdateDTO dto);

     /**
     * 新增并提交审核
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
     void addAndSubmit(SoB2cDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author Will
     * @date: 2023-08-18
     * @param dto
     * @return
     */
     void updateAndSubmit(SoB2cDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Will
     * @date: 2023-08-18
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author Will
    * @date: 2023-08-18
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author Will
    * @date: 2023-08-18
    * @param dto
    * @param response
    * @return
    */
    void exportList(SoB2cDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity);

}
