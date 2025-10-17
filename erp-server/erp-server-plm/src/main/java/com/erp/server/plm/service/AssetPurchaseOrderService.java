package com.erp.server.plm.service;
import com.erp.model.plm.dto.AssetNoticeDetailDTO;
import com.erp.model.plm.entity.AssetPurchaseOrderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.AssetPurchaseOrderDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import org.springframework.web.multipart.MultipartFile;

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
public interface AssetPurchaseOrderService extends SuperService<AssetPurchaseOrderEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetPurchaseOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetPurchaseOrderDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param pagingParamDTO
    * @return PagingVO<AssetPurchaseOrderDTO.ListDTO>>
    */
    PagingVO<AssetPurchaseOrderDTO.ListDTO> paging(PagingDTO<AssetPurchaseOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return List<AssetPurchaseOrderDTO.TabListDTO>>
    */
    List<AssetPurchaseOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    AssetPurchaseOrderDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetPurchaseOrderDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetPurchaseOrderDTO.UpdateDTO dto);

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

    AssetPurchaseOrderDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(AssetPurchaseOrderDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetPurchaseOrderEntity entity);


    Boolean updateContractStampStatus(PurchaseOrderDTO.ContractStampStatusParamsDTO dto);

}
