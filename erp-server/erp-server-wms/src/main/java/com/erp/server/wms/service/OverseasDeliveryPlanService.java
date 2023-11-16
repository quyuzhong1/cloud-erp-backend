package com.erp.server.wms.service;
import com.erp.model.wms.entity.OverseasDeliveryPlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 发货计划 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface OverseasDeliveryPlanService extends SuperService<OverseasDeliveryPlanEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasDeliveryPlanDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Luo_wg
    * @date: 2023-11-16
    * @param pagingParamDTO
    * @return PagingVO<OverseasDeliveryPlanDTO.ListDTO>>
    */
    PagingVO<OverseasDeliveryPlanDTO.ListDTO> paging(PagingDTO<OverseasDeliveryPlanDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return List<OverseasDeliveryPlanDTO.TabListDTO>>
    */
    List<OverseasDeliveryPlanDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Luo_wg
    * @date: 2023-11-16
    * @param id
    * @return
    */
    OverseasDeliveryPlanDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(OverseasDeliveryPlanDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void updateAndSubmit(OverseasDeliveryPlanDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Luo_wg
     * @date: 2023-11-16
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Luo_wg
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Luo_wg
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author Luo_wg
    * @date: 2023-11-16
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author Luo_wg
    * @date: 2023-11-16
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(OverseasDeliveryPlanDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, OverseasDeliveryPlanEntity entity);

}
