package com.erp.server.scm.service;
import com.erp.model.scm.dto.AssetNoticeDetailDTO;
import com.erp.model.scm.entity.AssetNoticeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.AssetNoticeDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.bind.annotation.RequestBody;
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
public interface AssetNoticeService extends SuperService<AssetNoticeEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetNoticeDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    Boolean update(AssetNoticeDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-10-16
    * @param pagingParamDTO
    * @return PagingVO<AssetNoticeDTO.ListDTO>>
    */
    PagingVO<AssetNoticeDTO.ListDTO> paging(PagingDTO<AssetNoticeDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return List<AssetNoticeDTO.TabListDTO>>
    */
    List<AssetNoticeDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wtr
    * @date: 2025-10-16
    * @param id
    * @return
    */
    AssetNoticeDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetNoticeDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wtr
    * @date: 2025-10-16
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetNoticeDTO.UpdateDTO dto);

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

    List<AssetNoticeDTO.ViewGeneratePurchaseOrderDTO> viewGeneratePurchaseOrder(List<String> idList);

    Boolean generatePurchaseOrder(List<AssetNoticeDTO.ListGeneratePurchaseOrderDTO> dtoList);

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
    void exportList(AssetNoticeDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetNoticeEntity entity);


    Boolean importFile(BaseDTO.ImportDTO dto);

    BatchResultDTO invalid(AssetNoticeEntity entity,String remark);

    void handleImportSuccessList(List<AssetNoticeDetailDTO.MoldImportDTO> successList);

    /**
     *
     * @param dto
     */
    void importAssetNotice(BaseDTO.ImportDTO dto);
}
