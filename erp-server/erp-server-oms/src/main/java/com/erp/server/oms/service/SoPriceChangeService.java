package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO;
import com.erp.model.oms.entity.SoPriceChangeEntity;

import java.util.List;

/**
 * <p>
 * 销售价变更表 服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceChangeService extends SuperService<SoPriceChangeEntity> {


    /**
     * 添加销售价目变更
     * @author yl
     * @date 2023-03-28 11:49
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceChangeEntity
     */
    SoPriceChangeEntity add(SoPriceChangeDTO.AddDTO dto);

    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-28 14:08
     * @param dto
     * @return java.lang.Boolean
     */
    SoPriceChangeEntity addAndSubmit(SoPriceChangeDTO.AddDTO dto);

    /**
     * 销售价目变更详情
     * @author yl
     * @date 2023-03-28 14:24
     * @param id
     * @return com.erp.model.scm.dto.SoPriceChangeDTO.UpdateDTO
     */
    SoPriceChangeDTO.ViewDTO view(String id);


    /**
     * 修改销售价目变更
     * @author yl
     * @date 2023-03-28 16:40
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceChangeEntity
     */
    String updateSoPriceChange(SoPriceChangeDTO.UpdateDTO dto);

    /**
     * 销售价目变更 提交审核
     * @author yl
     * @date 2023-03-28 16:47
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids, Boolean isStartProcess);

    /**
     * 销售价目变更 审核
     * @author yl
     * @date 2023-03-28 16:52
     * @param entity
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(SoPriceChangeEntity entity, ApproveOneDTO dto);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 18:53
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd (ApproveOneDTO dto, SoPriceChangeEntity entity);

    /**
     * 取消流程
     * @author yl
     * @date 2023-03-28 16:56
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

    /**
     * 分页获取销售价目变更数据
     * @author yl
     * @date 2023-03-28 17:15
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SoPriceChangeDTO.PagingViewDTO>
     */
    PagingVO<SoPriceChangeDTO.PagingViewDTO> paging(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto);

    /**
     * 修改并审核
     * @author yl
     * @date 2023-03-29 9:42
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoPriceChangeDTO.UpdateDTO dto);

    /**
     * 删除 销售价目变更
     * @author will
     * @date 2023-03-28 16:42
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);
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
    void export(SoPriceChangeDTO.PagingParamDTO dto);
    /**
     * @description: tab列表
     * @author Will
     * @date: 2024/1/20 9:22
     * @param dto
     * @return List<TabListDTO>
     */
    List<SoPriceChangeDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * 销售调价表导出
     * @author will
     * @date 2025/3/25 15:00
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO>
     */
    PagingVO<SoPriceChangeExportExcelDTO> exportSoPriceChange(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto);

    /**
     * 根据销售价目表id  获取对应产品信息
     * @author yl
     * @date 2023-03-31 16:07
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO>
     */
    List<SoPriceChangeDetailDTO.ViewDTO> getSkuChangeList(SoPriceChangeDetailDTO.SkuChangeParamDTO dto);

}
