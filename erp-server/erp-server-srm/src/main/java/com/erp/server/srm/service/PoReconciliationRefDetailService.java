package com.erp.server.srm.service;

import com.common.business.service.SuperService;
import com.erp.model.srm.dto.PoReconciliationRefDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.model.srm.entity.PoReconciliationRefDetailEntity;

import java.util.List;

/**
 * <p>
 * 采购对账单明细已对账信息 服务类
 * </p>
 *
 * @author will
 * @since 2025-12-22
 */
public interface PoReconciliationRefDetailService extends SuperService<PoReconciliationRefDetailEntity> {

    /**
     * 修改
     * @author will
     * @date: 2024-01-19
     * @param detailList
     * @param poReconciliationId
     * @return
     */
    Boolean srmUpdate(List<PoReconciliationRefDetailDTO.UpdateDTO> detailList, String poReconciliationId);

    /**
     * 编辑
     * @author will
     * @date 2025/12/22 18:48
     * @param detailList
     * @param poReconciliationId
     * @return Boolean
     */
    Boolean update(List<PoReconciliationRefDetailDTO.ScmUpdateDTO> detailList, String poReconciliationId);

    /**
     * 根据ids列表查询已对账明细
     * @author will
     * @date 2025/12/23 12:26
     * @param ids
     * @return List<PoReconciliationRefDetailEntity>
     */
    List<PoReconciliationRefDetailEntity> listPoReconciliationIdList(List<String> ids);


    /**
     * 根据ids列表查询已对账明细
     * @author will
     * @date 2025/12/23 12:26
     * @param ids
     * @return List<PoReconciliationRefDetailEntity>
     */
    List<PoReconciliationRefDetailEntity> listPoReconciliationDetailIdList(List<String> ids);
    /**
     * 根据来源编码和sku查询
     * @author will
     * @date 2025/6/13 15:23
     * @param sourceCodeList
     * @param skuNOList
     * @return List<PoReconciliationRefDetailEntity>
     */
    List<PoReconciliationRefDetailEntity> listBySourceCodeAndSku(String poReconciliationId,List<String> sourceCodeList, List<String> skuNOList);
    /**
     * 添加对账明细信息
     * @author will
     * @date 2025/12/23 16:30
     * @param poReconciliationDetailList
     * @param id
     * @return void
     */
    void batchAdd(List<PoReconciliationDetailEntity> poReconciliationDetailList, String id);
    /**
     * 列表数据处理
     * @author will
     * @date 2025/12/23 18:44
     * @param list
     * @param isSrm
     * @return void
     */
    void fillList(List<PoReconciliationRefDetailDTO.ListDTO> list, Boolean isSrm);
    /**
     * 查询明细数据
     * @author will
     * @date 2025/12/23 18:53
     * @param dto
     * @return List<ViewDTO>
     */
    List<PoReconciliationRefDetailDTO.ViewDTO> viewDetail(PoReconciliationRefDetailDTO.PagingParamDTO dto);
    /**
     * 根据对账单id列表删除已对账明细
     * @author will
     * @date 2025/12/24 11:12
     * @param poReconciliationIdList
     * @return void
     */
    void deleteByPoReconciliationIdList(List<String> poReconciliationIdList);
}
