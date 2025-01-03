package com.erp.server.mrp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 建议采购 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
public interface PurchaseSuggestService extends SuperService<PurchaseSuggestEntity> {
    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:48
     * @param params
     * @return List<ListDTO>
     */
    List<PurchaseSuggestDTO.ListDTO> list(PurchaseSuggestDTO.ListParamDTO params);

    /**
    * 新增
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO dto);

    /**
     * 添加采购建议（系统）
     * @author will
     * @date 2024/11/12 10:42
     * @param purchaseSuggestEntity
     */
    void addPurchaseSuggestSys (PurchaseSuggestEntity purchaseSuggestEntity);

    /**
    * 修改
    * @author will
    * @date: 2024-08-29
    * @param dto
    * @return
    */
    Boolean update(PurchaseSuggestDTO.UpdateDTO dto);

    /**
     * 导入采购
     * @author will
     * @date 2024/10/24 15:44
     * @param updateDTO
     * @return Boolean
     */
    Boolean importUpdate(PurchaseSuggestDTO.ImportUpdateDTO updateDTO);

    /**
     * 采购建议导出数据查询
     * @author will
     * @date 2024/9/6 14:54
     * @param dto
     * @return PagingVO<PurchaseSuggestionDTO>
     */
    PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> listPurchaseSuggestion(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto);

    /**
     * 采购建议
     * @param detailId 明细id
     */
    List<PurchaseSuggestEntity> listByReplenishmentId(String detailId);
    /**
     * 列表查询
     * @author will
     * @date 2024/10/16 10:22
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    PagingVO<PurchaseSuggestDTO.ListDTO> paging(PagingDTO<PurchaseSuggestDTO.PagingParamDTO> dto);
    /**
     * 下载模板
     * @author will
     * @date 2024/10/16 10:51
     * @param response 
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 锁定
     * @author will
     * @date 2024/10/16 11:48
     * @param id 
     * @return BatchResultDTO
     */
    BatchResultDTO locking(String id);
    /**
     * 确定
     * @author will
     * @date 2024/10/16 11:48
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO confirm(String id);
    /**
     * 作废
     * @author will
     * @date 2024/10/16 11:49
     * @param id
     * @param remark 
     * @return BatchResultDTO
     */
    BatchResultDTO invalid(String id, String remark);
    /**
     * 导出
     * @author will
     * @date 2024/10/16 15:28
     * @param pagingParamDTO
     * @return Boolean
     */
    Boolean export(DeliverySuggestDTO.PagingParamDTO pagingParamDTO);
    /**
     * 更新备注
     * @author will
     * @date 2024/10/22 17:15
     * @param id
     * @param remark
     * @return BatchResultDTO
     */
    BatchResultDTO updateRemark(String id, String remark);
    /**
     * 导入采购建议
     * @author will
     * @date 2024/10/24 15:22
     * @param excelFile
     * @param response
     */
    void importPurchaseSuggest(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 查询需要生成采购建议合并的数据
     * @author will
     * @date 2024/10/31 9:40
     * @return List<PurchaseSuggestEntity>
     */
    List<PurchaseSuggestEntity> listGeneratePurchaseSuggestMerge(PurchaseSuggestEntity purchaseSuggestEntity);
}
