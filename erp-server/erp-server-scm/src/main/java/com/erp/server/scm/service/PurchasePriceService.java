package com.erp.server.scm.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.excel.PurchasePriceExportExcelDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购价目表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceService extends SuperService<PurchasePriceEntity> {

    
    /**
     * 添加采购价目表
     * @author yl
     * @date 2023-03-24 12:22
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     */
    PurchasePriceEntity add(PurchasePriceDTO.AddDTO dto);

    
    /**
     * 获取采购价目详情
     * @author yl
     * @date 2023-03-27 9:11
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceDTO.ViewDTO
     */
    PurchasePriceDTO.ViewDTO view(String id);

    /**
     * 修改采购价目
     * @author yl
     * @date 2023-03-27 10:52
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     */
    PurchasePriceEntity updatePurchasePrice(PurchasePriceDTO.UpdateDTO dto);
    
    /**
     * 保存并提交审核 价目
     * @author yl
     * @date 2023-03-27 11:46
     * @param dto
     * @return java.lang.Boolean
     */
    PurchasePriceEntity addAndSubmit(PurchasePriceDTO.AddDTO dto);

    /**
     * 修改并审核采购价目
     * @author yl
     * @date 2023-03-27 11:58
     * @param dto
     * @return java.lang.Boolean
     */
    PurchasePriceEntity updateAndSubmit(PurchasePriceDTO.UpdateDTO dto);

    
    
    /**
     * 批量删除采购价目信息
     * @author yl
     * @date 2023-03-27 12:04
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO deleteEntity(PurchasePriceEntity entity);

    /**
     * 采购价目表 提交审核
     * @author yl
     * @date 2023-03-27 12:11
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO submitEntity(PurchasePriceEntity entity);

    /**
     * 审核
     * @author yl
     * @date 2023-03-27 12:29
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(PurchasePriceEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 16:18
     * @param entity
     * @param type
     * @param comment
     * @return Boolean
     */
    Boolean approveEnd (PurchasePriceEntity entity, String type, String comment);

    /**
     * 取消流程
     * @author yl
     * @date 2023-03-27 14:04
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO cancelProcessEntity(PurchasePriceEntity entity);

    /**
     * 采购信息分页
     * @author yl
     * @date 2023-03-27 14:40
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchasePriceDTO.PagingViewDTO>
     */
    PagingVO<PurchasePriceDTO.PagingViewDTO> paging(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto);

    
    /**
     * 采购价目表导出
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-27 17:55
     */
    void exportPurchasePrice(PurchasePriceDTO.PagingParamDTO dto);

    /**
     * 删除供应商的时候后 看是否有关联 如果有就不能删除
     * @author yl
     * @date 2023-04-14 11:00
     * @param supplierIds
     * @return void
     */
    void checkIsRefSupplier(List<String> supplierIds);

    /**
     * 更新金蝶同步状态
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 修改状态
     * @Author Luo_WG
     * @Date 2023/6/30 19:47
     * @param ids
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDTO.SupplierSkuPrice>
     **/
    List<PurchasePriceDTO.SupplierSkuPrice> listSupplierSkuPrice(List<String> ids);

    /**
     * @description: 查询所有供应商价格
     * @author Will
     * @date: 2023/10/27 10:05
     * @param ids
     * @return List<SupplierSkuPrice>
     */
    List<PurchasePriceDTO.SupplierSkuPrice> listAllSupplierSkuPrice(List<String> ids);

    /**
     * 采购价目表导入
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author zhangchunlin
     * @date 2023-08-03 18:00:00
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 批量导入
     * @param handList
     */
    void batchImport(List<PurchasePriceDTO.ImportAddDTO> handList);

    /**
     * 下载模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 反审核
     * @param entity
     * @return
     */
    BatchResultDTO disApprove(PurchasePriceEntity entity,List<PurchasePriceDetailEntity> detailEntityList,List<PurchasePriceChangeDetailEntity> changeDetailEntityList);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/9/21 15:21
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean updateDetailRemark(List<String> ids,String remark);
    /**
     * @description: tab查询
     * @author Will
     * @date: 2024/1/19 18:49
     * @param dto
     * @return List<TabListDTO>
     */
    List<PurchasePriceDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<PurchasePriceExportExcelDTO> exportPurchasePrice(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto);

    List<PurchasePriceDTO.PriceDTO> batchGetPurchasePrice(List<PurchasePriceDTO.PriceDTO> list);

    BatchResultDTO updateOutPlatformCode(PurchasePriceEntity entity, String outPlatformCode);

    List<PurchasePriceEntity> listByCodes(List<String> codes);

    void updateApproveStatus(PurchasePriceDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO);
}
