package com.erp.server.mrp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 建议采购(合并后) 服务类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
public interface PurchaseSuggestMergeService extends SuperService<PurchaseSuggestMergeEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-10-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSuggestMergeDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-10-21
    * @param dto
    * @return
    */
    Boolean update(PurchaseSuggestMergeDTO.UpdateDTO dto);
    /**
     * 导入修改
     * @author will
     * @date 2024/10/24 18:10
     * @param updateDTO
     * @return Boolean
     */
    Boolean importUpdate(PurchaseSuggestMergeDTO.ImportUpdateDTO updateDTO);

    /**
     * 分页查询
     * @author will
     * @date 2024/10/22 15:59
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<PurchaseSuggestMergeDTO.ListDTO> paging(PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> dto);
    /**
     * 下载模板
     * @author will
     * @date 2024/10/22 16:29
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 锁定
     * @author will
     * @date 2024/10/22 16:30
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO locking(String id);
    /**
     * 确认
     * @author will
     * @date 2024/10/22 17:01
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO confirm(String id);
    /**
     * 作废
     * @author will
     * @date 2024/10/22 17:09
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO invalid(String id, String remark);
    /**
     * 导出
     * @author will
     * @date 2024/10/22 17:11
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO);
    /**
     * 更新备注
     * @author will
     * @date 2024/10/22 17:20
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     * 生成采购建议合并数据
     * @author will
     * @date 2024/10/31 9:26
     */
    void generatePurchaseSuggestMerge ();
    /**
     * 导入采购计划合并数据
     * @author will
     * @date 2024/10/24 17:24
     * @param excelFile
     * @param response
     */
    void importPurchaseSuggestMerge(MultipartFile excelFile, HttpServletResponse response);
}
