package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.modules.workflow.dto.ProcessPassDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.controller.AuditParamDTO;

import java.util.List;

/**
 * 变更信息表(ProductChange)表服务接口
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
public interface ProductChangeService  extends IService<ProductChangeEntity> {


    Boolean add(AddChangeDTO dto);

    PagingVO<List<ProductChangePagingVO>> paging(PagingDTO<SearchPagingDTO> dto);

    Boolean cancellation(String id);

    List<ChangeInfoDTO> getChangeByType(String type, String searchKeyword);

    ProductChangeDTO details(String id);

    Boolean edit(UpdateChangeDTO dto);

    void approvalPass(AuditParamDTO dto);

    void approvalNoPass(AuditParamDTO dto);

    void processPass(ProcessPassDTO dto);

    Boolean restartAudit(String id);

    ProductBomChangeDTO getBomDetails(ProductChangeEntity changeEntity);

    /**
     * SKU 详情
     * @author yl
     * @date 2023-02-02 9:35
     * @param changeEntity
     * @return com.erp.model.plm.dto.ProductChangeDTO
     */
    ProductChangeDTO skuDetails(ProductChangeEntity changeEntity);

    List<String> getBySourceId(List<String> sourceIds);

    /**
     *
     * @param searchKeyword
     * @return
     */
    List<String> getChangeSearchCondition(String searchKeyword);

    List<ApproveNodeRecordVO> auditInfo(String id);
}
