package com.erp.server.mrp.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    BaseResultDTO.AddDTO addOrUpdate(PurchaseSuggestMergeDTO.AddOrUpdateDTO dto);

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
    void generatePurchaseSuggestMerge (String purchaseSuggestId);
    /**
     * 导入采购计划合并数据
     * @author will
     * @date 2024/10/24 17:24
     * @param excelFile
     * @param response
     */
    void importPurchaseSuggestMerge(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 根据平台和sku查询
     * @author will
     * @date 2024/11/6 10:34
     * @param platformTypeList
     * @param platformList
     * @param skuIdList
     * @return List<PurchaseSuggestMergeEntity>
     */
    List<PurchaseSuggestMergeEntity> listByPlatformListAndSkuIdList(List<String> platformTypeList, List<String> platformList, List<String> skuIdList);
    /**
     * 查询采购bom信息
     * @author will
     * @date 2024/11/12 16:52
     * @param id
     * @return List<PurchaseSuggestBomDTO>
     */
    List<DeliverySuggestDTO.PurchaseSuggestBomDTO> listPurchaseSuggestBom(String id);

    /**
     * 查询锁定数量
     * @author will
     * @date 2024/11/25 18:08
     * @param entity
     * @return Integer
     */
    Integer getLockingQty(PurchaseSuggestMergeEntity entity);
}
