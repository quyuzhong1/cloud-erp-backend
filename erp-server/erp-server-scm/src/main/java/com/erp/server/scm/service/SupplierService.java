package com.erp.server.scm.service;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierTabCountDTO;
import com.erp.model.scm.dto.excel.SupplierExportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 供应商表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierService extends SuperService<SupplierEntity> {

    /**
     * 保存供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 15:12
     */
    SupplierEntity addSupplier(SupplierDTO.AddDTO dto);


    /**
     * 保存并提交审核供应商
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 9:13
     */
    Boolean addAndSubmit(SupplierDTO.AddDTO dto);


    /**
     * 供应商详情
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     * @author yl
     * @date 2023-03-20 10:00
     */
    SupplierDTO.SupplierViewDTO view(String supplierId);


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 10:56
     */
    String updateSupplier(SupplierDTO.UpdateDTO dto);


    /**
     * 分页获取供应商信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-20 14:11
     */
    PagingVO<SupplierDTO.PagingViewDTO> paging(PagingDTO<SupplierDTO.PagingParamDTO> dto);


    /**
     * 根据表id集合删除 数据
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 18:38
     */
    Boolean deleteByIds(List<String> ids);


    /**
     * 批量提交审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:07
     */
    BatchResultDTO submit(SupplierEntity entity);


    /**
     * 审核 供应商
     *
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-20 19:41
     */
    BatchResultDTO approve(SupplierEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * @param type
     * @param comment
     * @param entity
     * @return Boolean
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/11 12:02
     */
    Boolean approveEnd(SupplierEntity entity,String type, String comment);


    /**
     * 更改供应商更改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 8:56
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 更新SRM协同状态
     * @param dto
     * @return
     */
    ApiResult<String> updateSrmStatus(UpdateStateDTO dto);
    /**
     * 获取供应商
     * 获取 审核通过且开启的供应商
     *
     * @return
     * @author yl
     */
    List<Map<String, Object>> listApproveSupplier();

    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 10:26
     */
    BatchResultDTO disApprove(SupplierEntity entity);

    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-24 10:58
     */
    void downloadTemplate(HttpServletResponse response);


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-24 18:23
     */
    Boolean updateAndSubmit(SupplierDTO.UpdateDTO dto);


    /**
     * 供应商导出
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-29 14:50
     */
    void exportSupplier(SupplierDTO.PagingParamDTO dto);


    /**
     * 供应商导入
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-30 9:44
     */
    Boolean importFile(MultipartFile excelFile,String type, HttpServletResponse response);

    /**
     * 获取供应商的一些信息
     *
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.ViewDTO
     * @author yl
     * @date 2023-03-30 10:48
     */
    SupplierDTO.ViewDTO getBySupplierId(String supplierId);


    /**
     * 批量保存 导入的供应商
     *
     * @param addDTO
     * @return void
     * @author yl
     * @date 2023-03-30 20:01
     */
    void batchImportSupplier(SupplierDTO.ImportAddDTO addDTO,String type);

    /**
     * 查询是否 有供应商占用 要删除的id 如果有就不能删除
     *
     * @param deleteIdList
     * @return int
     * @author yl
     * @date 2023-03-31 11:07
     */
    int occupiedGrade(List<String> deleteIdList);


    /**
     * 阶段审核通过后 更改供应商的阶段
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-03-31 16:45
     */
    void updatePhase(List<SupplierPhaseEntity> list);

    /**
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     * @description: 同步金蝶状态
     * @author Will
     * @date: 2023/4/25 18:48
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 根据供应商类型 获取对应供应商
     * @author yl
     * @date 2023-05-23 16:31
     * @param categoryType
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     */
    List<BaseIdDTO> listSupplierByCategoryType(String categoryType);

    /**
     * 获取供应商简单信息
     * @param ids
     * @return
     */
    Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(List<String> ids);

    /**
     * 根据供应商类型 获取对应供应商（审核通过）
     * @param categoryType
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     */
    List<SupplierDTO.SupplierSimpleDTO> listApproveSupplierByCategoryType(String categoryType);
    /**
     * @description: 根据供应商编码查询
     * @author Will
     * @date: 2023/7/5 17:41
     * @param supplierCodes
     * @return List<SupplierEntity>
     */
    List<SupplierEntity> listByCodes(List<String> supplierCodes);

    /**
     * 根据供应商id获取列表
     *
     * @param ids
     * @return
     */
    List<SupplierEntity> listByIds(List<String> ids);

    /**
     * 根据名称获取供应商
     * @author yl
     * @date 2023-09-22 19:51
     * @param supplierNames
     * @return java.util.List<com.erp.model.scm.entity.SupplierEntity>
     */
    List<SupplierEntity> listBySupplierByNames(List<String> supplierNames);
    /**
     * @description: 撤销流程
     * @author Will
     * @date: 2023/12/1 16:01
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);

    SupplierEntity getSupplierByUid(String uid);

    /**
     * 统计状态数量
     * @return
     */
    List<SupplierTabCountDTO> getTabCount();

    List<SupplierEntity> listByPurchaseUserId(String purchaseUserId);
    /**
     * @description: 根据供应商id集合查询
     * @author Will
     * @date: 2024/1/24 18:38
     * @param supplierIdList
     * @return List<SupplierDefaultDTO>
     */
    List<SupplierDTO.SupplierDefaultDTO> listDefaultBySupplierIdList(List<String> supplierIdList);

    void updateCategory(SupplierDTO.BatchUpdateCategoryDTO dto);

    PagingVO<BaseDropDownDTO.RemarkDTO> pagingSelect(PagingDTO<BaseDropDownDTO.SelectDTO> dto);

    PagingVO<SupplierExportExcelDTO> exportSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto);

    Boolean updateVoucherNo(List<String> ids, String voucherNo);

    SupplierEntity add(SupplierDTO.InsertDTO addDTO);

    void updateApproveStatus(SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO);
    /**
     * 动态导出供应商
     * @author will
     * @date 2025/7/28 09:26
     * @param dto
     * @return PagingVO<DynamicExcelDTO>
     */
    PagingVO<DynamicExcelDTO> exportDynamicSupplier(PagingDTO<SupplierDTO.PagingParamDTO> dto);
}