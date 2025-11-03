package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.vo.ProductChangePagingVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;

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
    /**
     * @description: 根据变更id查询变更字段
     * @author Will
     * @date: 2023/2/14 18:35
     * @param id
     * @return List<String>
     */
    List<String> listChangeField(String id);

    /**
     * tab页
     */
    List<ProductChangePagingVO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 检查库存是否大于零
     * 此方法用于检查给定商品的库存是否大于零如果库存大于零，则根据库存状态统计数量，并抛出异常
     *
     */
    void checkInventoryGreaterThanZero(ProductInfoEntity productInfoEntity, String propertyId , String skuId) ;
    /**
     * 审核
     * @author will
     * @date 2025/5/16 18:13
     * @param approveOneDTO
     * @return BatchResultDTO
     */
    BatchResultDTO approve(ApproveOneDTO approveOneDTO);
    /**
     * 审核结束
     * @author will
     * @date 2025/5/16 18:25
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, ProductChangeEntity entity);
    /**
     * 取消流程
     * @author will
     * @date 2025/5/19 09:20
     * @param dto
     * @return BatchResultDTO
     */
   BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto);
    /**
     * 提交
     * @author will
     * @date 2025/5/19 09:57
     * @param id
     * @param isProcess
     * @return BatchResultDTO
     */
    BatchResultDTO submit(String id, Boolean isProcess);
}
