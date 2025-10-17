package com.erp.server.plm.service;
import com.erp.model.plm.entity.AssetPurchaseChangeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.AssetPurchaseChangeDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
public interface AssetPurchaseChangeService extends SuperService<AssetPurchaseChangeEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetPurchaseChangeDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetPurchaseChangeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param pagingParamDTO
    * @return PagingVO<AssetPurchaseChangeDTO.ListDTO>>
    */
    PagingVO<AssetPurchaseChangeDTO.ListDTO> paging(PagingDTO<AssetPurchaseChangeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return List<AssetPurchaseChangeDTO.TabListDTO>>
    */
    List<AssetPurchaseChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    AssetPurchaseChangeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetPurchaseChangeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetPurchaseChangeDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wtr
     * @date: 2025-10-16
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(AssetPurchaseChangeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetPurchaseChangeEntity entity);

}
