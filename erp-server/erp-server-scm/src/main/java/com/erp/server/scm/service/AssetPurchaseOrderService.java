package com.erp.server.scm.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.scm.dto.AssetPurchaseOrderDetailDTO;
import com.erp.model.scm.entity.AssetPurchaseOrderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.common.business.vo.PagingVO;
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


    Boolean updateContractStampStatus(AssetPurchaseOrderDTO.ContractStampStatusParamsDTO dto);


    /**
     * 下拉选择列表（支持关键字查询）
     * @param paramDTO 查询参数
     * @return 下拉选择列表
     */
    List<AssetPurchaseOrderDTO.SelectDTO> selectList(AssetPurchaseOrderDTO.SelectParamDTO paramDTO);
    /**
     * 下拉选择列表（支持关键字查询）
     * @param paramDTO 查询参数
     * @return 下拉选择列表
     */
    /**
     * 查询资产采购订单明细（用于资产验收单添加明细）
     *
     * @param assetPurchaseOrderId 资产采购订单ID
     * @return 明细列表
     */
    List<AssetPurchaseOrderDTO.DetailForAcceptDTO> queryDetailsForAccept(String assetPurchaseOrderId);

    /**
     * 根据订单编号查询资产采购订单（用于导入）
     * 查询未删除且审核通过的订单及其明细
     *
     * @param code 订单编号
     * @return 资产采购订单信息（包含明细）
     */
    AssetPurchaseOrderDTO.DetailWithSkuDTO getByCode(String code);

    void handleImportSuccessList(List<AssetPurchaseOrderDetailDTO.MoldImportDTO> successList) throws Exception;

    Boolean exportPurchaseContract(String id, HttpServletResponse response);

    void exportAssetPurchaseContractPdf(String id, HttpServletResponse response);

    AssetPurchaseOrderDTO.ExportPdfDTO listPurchaseContractPdf(String id);

    ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>>  getAcceptByDetailId(String detailId);

    List<AssetPurchaseOrderDTO.ViewGenerateAssetAcceptDTO> viewGenerateAssetAccept(BaseIdsDTO.IdsDTO dto);

    Boolean generateAssetAccept(List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList);

    AssetPurchaseOrderDTO.ViewGeneratePurchaseChangeDTO viewGeneratePurchaseChangeOrder(BaseIdsDTO.IdsDTO dto);

    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

}
