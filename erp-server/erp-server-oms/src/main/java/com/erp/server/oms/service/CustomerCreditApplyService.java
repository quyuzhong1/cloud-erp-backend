package com.erp.server.oms.service;
import com.erp.model.oms.entity.CustomerCreditApplyEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 客户授信 服务类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
public interface CustomerCreditApplyService extends SuperService<CustomerCreditApplyEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CustomerCreditApplyDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    Boolean update(CustomerCreditApplyDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author lrp
    * @date: 2025-08-28
    * @param pagingParamDTO
    * @return PagingVO<CustomerCreditApplyDTO.ListDTO>>
    */
    PagingVO<CustomerCreditApplyDTO.ListDTO> paging(PagingDTO<CustomerCreditApplyDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return List<CustomerCreditApplyDTO.TabListDTO>>
    */
    List<CustomerCreditApplyDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    CustomerCreditApplyDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(CustomerCreditApplyDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    void updateAndSubmit(CustomerCreditApplyDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author lrp
     * @date: 2025-08-28
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author lrp
    * @date: 2025-08-28
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author lrp
    * @date: 2025-08-28
    * @param dto
    * @param response
    * @return
    */
    void exportList(CustomerCreditApplyDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, CustomerCreditApplyEntity entity);

    BatchResultDTO cancel(String ids);
}
