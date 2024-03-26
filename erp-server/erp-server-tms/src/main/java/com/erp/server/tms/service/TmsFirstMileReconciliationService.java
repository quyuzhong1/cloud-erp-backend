package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 头程对账单 服务类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
public interface TmsFirstMileReconciliationService extends SuperService<TmsFirstMileReconciliationEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    Boolean update(TmsFirstMileReconciliationDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author Jim
    * @date: 2024-03-25
    * @param pagingParamDTO
    * @return PagingVO<TmsFirstMileReconciliationDTO.ListDTO>>
    */
    PagingVO<TmsFirstMileReconciliationDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return List<TmsFirstMileReconciliationDTO.TabListDTO>>
    */
    List<TmsFirstMileReconciliationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author Jim
    * @date: 2024-03-25
    * @param id
    * @return
    */
    TmsFirstMileReconciliationDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(TmsFirstMileReconciliationDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    void updateAndSubmit(TmsFirstMileReconciliationDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Jim
     * @date: 2024-03-25
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Jim
    * @date: 2024-03-25
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author Jim
    * @date: 2024-03-25
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author Jim
    * @date: 2024-03-25
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author Jim
    * @date: 2024-03-25
    * @param dto
    * @param response
    * @return
    */
    void exportList(TmsFirstMileReconciliationDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, TmsFirstMileReconciliationEntity entity);

}
