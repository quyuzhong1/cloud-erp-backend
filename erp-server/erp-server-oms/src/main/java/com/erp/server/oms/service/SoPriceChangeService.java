package com.erp.server.oms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoPriceChangeDTO;
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
     * 添加采购价目变更
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
     * 采购价目变更详情
     * @author yl
     * @date 2023-03-28 14:24
     * @param id
     * @return com.erp.model.scm.dto.SoPriceChangeDTO.UpdateDTO
     */
    SoPriceChangeDTO.ViewDTO view(String id);


    /**
     * 修改采购价目变更
     * @author yl
     * @date 2023-03-28 16:40
     * @param dto
     * @return com.erp.model.scm.entity.SoPriceChangeEntity
     */
    String updateSoPriceChange(SoPriceChangeDTO.UpdateDTO dto);

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
    BatchResultDTO approve(SoPriceChangeEntity entity, String type, String comment, Boolean isNeedProcess);

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
    BatchResultDTO approveEnd (SoPriceChangeEntity entity, String type, String comment, Boolean isNeedProcess);

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

    PagingVO<SoPriceChangeExportExcelDTO> exportSoPriceChange(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto);

}
