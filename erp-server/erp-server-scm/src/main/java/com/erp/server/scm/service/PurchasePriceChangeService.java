package com.erp.server.scm.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.excel.PurchasePriceChangeExportExcelDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;

import java.util.List;

/**
 * <p>
 * 采购价变更表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceChangeService extends SuperService<PurchasePriceChangeEntity> {

    
    /**
     * 添加采购价目变更
     * @author yl
     * @date 2023-03-28 11:49
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     */
    String add(PurchasePriceChangeDTO.AddDTO dto);

    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-28 14:08
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(PurchasePriceChangeDTO.AddDTO dto);

    /**
     * 采购价目变更详情
     * @author yl
     * @date 2023-03-28 14:24
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.UpdateDTO
     */
    PurchasePriceChangeDTO.ViewDTO view(String id);

    
    /**
     * 修改采购价目变更
     * @author yl
     * @date 2023-03-28 16:40
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     */
    String updatePurchasePriceChange(PurchasePriceChangeDTO.UpdateDTO dto);

    /**
     * 删除 采购价目变更
     * @author yl
     * @date 2023-03-28 16:42
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 采购价目变更 提交审核
     * @author yl
     * @date 2023-03-28 16:47
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submitApprove(List<String> ids,Boolean isStartProcess);

    /**
     * 采购价目变更 审核
     * @author yl
     * @date 2023-03-28 16:52
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(PurchasePriceChangeEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 18:53
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return Boolean
     */
    BatchResultDTO approveEnd (PurchasePriceChangeEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 取消流程
     * @author yl
     * @date 2023-03-28 16:56
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 分页获取采购价目变更数据
     * @author yl
     * @date 2023-03-28 17:15
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchasePriceChangeDTO.PagingViewDTO>
     */
    PagingVO<PurchasePriceChangeDTO.PagingViewDTO> paging(PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto);

    /**
     * 修改并审核
     * @author yl
     * @date 2023-03-29 9:42
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(PurchasePriceChangeDTO.UpdateDTO dto);

    /**
     * 根据采购价目表id  获取对应产品信息
     * @author yl
     * @date 2023-03-31 16:07
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO>
     */
    List<PurchasePriceChangeDetailDTO.ViewDTO> getSkuChangeList(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto);
    /**
     * 更新金蝶同步状态
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);
    /**
     * @description: 更新明细备注
     * @author Will
     * @date: 2023/9/22 15:08
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean updateDetailRemark(List<String> ids, String remark);
    /**
     * @param dto
     * @description:
     * @author Will
     * @date: 2023/10/18 16:30
     */
    void export(PurchasePriceChangeDTO.PagingParamDTO dto);

    /**
     * 修复历史数据
     * @author yl
     * @date 2023-10-23 19:19
     * @param
     * @return void
     */
    void tempUpdateHistoryDb();
    /**
     * @description: tab列表
     * @author Will
     * @date: 2024/1/20 9:22
     * @param dto
     * @return List<TabListDTO>
     */
    List<PurchasePriceChangeDTO.TabListDTO> tabList(PermissionsDTO dto);

    PagingVO<PurchasePriceChangeExportExcelDTO> exportPurchasePriceChange(PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto);
}
