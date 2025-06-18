package com.erp.server.oms.service;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.dto.excel.SoPriceExportExcelDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售价目表 服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceService extends SuperService<SoPriceEntity> {


    /**
     * 添加销售价目表
     * @author will
     * @date 2025-03-24 12:22
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceEntity
     */
    SoPriceEntity add(SoPriceDTO.AddDTO dto);


    /**
     * 获取销售价目详情
     * @author will
     * @date 2025-03-27 9:11
     * @param id
     * @return com.erp.model.scm.dto.SoPriceDTO.ViewDTO
     */
    SoPriceDTO.ViewDTO view(String id);

    /**
     * 修改销售价目
     * @author will
     * @date 2025-03-27 10:52
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceEntity
     */
    SoPriceEntity updateSoPrice(SoPriceDTO.UpdateDTO dto);

    /**
     * 保存并提交审核 价目
     * @author will
     * @date 2025-03-27 11:46
     * @param dto
     * @return java.lang.Boolean
     */
    SoPriceEntity addAndSubmit(SoPriceDTO.AddDTO dto);

    /**
     * 修改并审核销售价目
     * @author will
     * @date 2025-03-27 11:58
     * @param dto
     * @return java.lang.Boolean
     */
    SoPriceEntity updateAndSubmit(SoPriceDTO.UpdateDTO dto);

    /**
     * 批量删除销售价目信息
     * @author will
     * @date 2025-03-27 12:04
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO delete(SoPriceEntity entity);

    /**
     * 销售价目表 提交审核
     * @author will
     * @date 2025-03-27 12:11
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO submit(SoPriceEntity entity);

    /**
     * 审核
     * @author will
     * @date 2025-03-27 12:29
     * @param entity
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(SoPriceEntity entity, ApproveOneDTO dto);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 16:18
     * @param entity
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd (ApproveOneDTO dto, SoPriceEntity entity);

    /**
     * 取消流程
     * @author will
     * @date 2025-03-27 14:04
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO cancelProcess(SoPriceEntity entity);

    /**
     * 销售信息分页
     * @author will
     * @date 2025-03-27 14:40
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SoPriceDTO.PagingViewDTO>
     */
    PagingVO<SoPriceDTO.PagingViewDTO> paging(PagingDTO<SoPriceDTO.PagingParamDTO> dto);

    /**
     * 销售价目表导出
     * @param dto
     * @return void
     * @author will
     * @date 2025-03-27 17:55
     */
    void exportSoPrice(SoPriceDTO.PagingParamDTO dto);

    /**
     * 销售价目表导入
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
    void batchImport(List<SoPriceDTO.ImportAddDTO> handList);

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
    BatchResultDTO disApprove(SoPriceEntity entity, List<SoPriceChangeDetailEntity> changeDetailEntityList);
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
    List<SoPriceDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 导出销售价目
     * @param dto
     * @return SoPriceExportExcelDTO
     */
    PagingVO<SoPriceExportExcelDTO> exportSoPrice(PagingDTO<SoPriceDTO.PagingParamDTO> dto);
    /**
     * 批量查询销售价目表
     * @author will
     * @date 2025/3/27 11:46
     * @param list
     * @return java.util.List<com.erp.model.oms.dto.SoPriceDTO.PriceDTO>
     */
    List<SoPriceDTO.PriceDTO> batchGetSoPrice(List<SoPriceDTO.PriceParamDTO> list);
}
