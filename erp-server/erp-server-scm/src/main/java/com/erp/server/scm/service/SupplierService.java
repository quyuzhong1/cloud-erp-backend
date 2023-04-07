package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SupplierDTO;
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
     * @author yl
     * @date 2023-03-17 15:12
     * @param dto
     * @return java.lang.Boolean
     */
    String addSupplier(SupplierDTO.AddDTO dto);

    
    /**
     * 保存并提交审核供应商
     * @author yl
     * @date 2023-03-20 9:13
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SupplierDTO.AddDTO dto);

    
    /**
     * 供应商详情
     * @author yl
     * @date 2023-03-20 10:00
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.updateDTO
     */
    SupplierDTO.SupplierViewDTO view(String supplierId);

    
    /**
     * 修改供应商信息
     * @author yl
     * @date 2023-03-20 10:56
     * @param dto
     * @return java.lang.Boolean
     */
    String updateSupplier(SupplierDTO.UpdateDTO dto);

    
    /**
     * 分页获取供应商信息
     * @author yl
     * @date 2023-03-20 14:11
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierDTO.PagingViewDTO>
     */
    PagingVO<SupplierDTO.PagingViewDTO> paging(PagingDTO<SupplierDTO.PagingParamDTO> dto);

    
    /**
     * 根据表id集合删除 数据
     * @author yl
     * @date 2023-03-20 18:38
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    
    /**
     * 批量提交审核
     * @author yl
     * @date 2023-03-20 19:07
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    
    /**
     * 审核 供应商
     * @author yl
     * @date 2023-03-20 19:41
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    
    /**
     * 更改供应商更改状态
     * @author yl
     * @date 2023-03-21 8:56
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO dto);

    /**
     * 获取供应商
     * 获取 审核通过且开启的供应商
     *@author yl
     * @return
     */
    List<Map<String,Object>> listApproveSupplier();

    /**
     * 反审核
     * @author yl
     * @date 2023-03-23 10:26
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean disApprove(List<String> ids);

    /**
     * 下载模板
     * @author yl
     * @date 2023-03-24 10:58
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);

    
    /**
     * 修改并审核
     * @author yl
     * @date 2023-03-24 18:23
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SupplierDTO.UpdateDTO dto);

    
    /**
     * 供应商导出
     * @author yl
     * @date 2023-03-29 14:50
     * @param dto
     * @param response
     * @return void
     */
    void exportSupplier(SupplierDTO.ExportDTO dto, HttpServletResponse response);


    
    /**
     * 供应商导入
     * @author yl
     * @date 2023-03-30 9:44
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 获取供应商的一些信息
     * @author yl
     * @date 2023-03-30 10:48
     * @param supplierId
     * @return com.erp.model.scm.dto.SupplierDTO.ViewDTO
     */
    SupplierDTO.ViewDTO getBySupplierId(String supplierId);

    
    /**
     * 批量保存 导入的供应商
     * @author yl
     * @date 2023-03-30 20:01
     * @param addList
     * @return void
     */
    void batchImportSupplier(List<SupplierDTO.ImportAddDTO> addList);

    /**
     *  查询是否 有供应商占用 要删除的id 如果有就不能删除
     * @author yl
     * @date 2023-03-31 11:07
     * @param deleteIdList
     * @return int
     */
    int occupiedGrade(List<String> deleteIdList);

    
    /**
     * 阶段审核通过后 更改供应商的阶段
     * @author yl
     * @date 2023-03-31 16:45
     * @param list
     * @return void
     */
    void updatePhase(List<SupplierPhaseEntity> list);
}
