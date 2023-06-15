package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.SubcontractOrderDTO;
import com.erp.model.scm.entity.SubcontractOrderEntity;

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
    /**
     * 根据委外订单父级SKU明细id查询采购订单数据
     * @author Will
     * @date: 2023/6/9 16:20
     * @param detailId
     * @return List<PurchaseOrderDTO.ListDTO>
     */
    List<PurchaseOrderDTO.ListDTO> listPurchaseOrderByDetailId(String detailId);
    /**
     * @description: 结束交货
     * @author Will
     * @date: 2023/6/9 16:33
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean finishDelivery(List<String> ids,String remark);
    /**
     * @description: 下推采购单显示
     * @author Will
     * @date: 2023/6/12 14:01
     * @param ids
     * @return List<ViewGeneratePoDTO>
     */
    List<SubcontractOrderDTO.ViewGeneratePoDTO> viewGeneratePo(List<String> ids);
    /**
     * 下推采购订单保存
     * @author Will
     * @date: 2023/6/12 14:16
     * @param list
     */
    void generatePo(ValidList<SubcontractOrderDTO.GeneratePoDTO> list);
    /**
     * 添加已有产品显示
     * @author Will
     * @date: 2023/6/12 15:38
     * @param dto
     * @return List<ViewAddDetailDTO>
     */
    List<SubcontractOrderDTO.ViewAddDetailDTO> viewAddDetail(SubcontractOrderDTO.ViewAddDetailParamDTO dto);
    /**
     * @description: 作废
     * @author Will
     * @date: 2023/6/15 9:37
     * @param ids
     * @param remark
     */
    void invalid(List<String> ids, String remark);}
