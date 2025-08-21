package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleInitialLedgerDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 样品期初台账 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleInitialLedgerService extends SuperService<SampleInitialLedgerEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleInitialLedgerDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleInitialLedgerDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-08-21
    * @param pagingParamDTO
    * @return PagingVO<SampleInitialLedgerDTO.ListDTO>>
    */
    PagingVO<SampleInitialLedgerDTO.ListDTO> paging(PagingDTO<SampleInitialLedgerDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return List<SampleInitialLedgerDTO.TabListDTO>>
    */
    List<SampleInitialLedgerDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    SampleInitialLedgerDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SampleInitialLedgerDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    void updateAndSubmit(SampleInitialLedgerDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuhaotian
     * @date: 2025-08-21
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuhaotian
    * @date: 2025-08-21
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @param response
    * @return
    */
    void exportList(SampleInitialLedgerDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SampleInitialLedgerEntity entity);

}
