package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;

import java.util.List;


/**
 * <p>
 * 委外订单明细 服务类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
public interface SubcontractOrderDetailService extends SuperService<SubcontractOrderDetailEntity> {

    /**
     * @description: 更新交货状态
     * @author Will
     * @date: 2023/6/12 17:55
     * @param code
     * @param ids
     */
    void updateArrivalStatusByIds(String code, List<String> ids);
    /**
     * @description: 根据委外订单ids删除明细
     * @author Will
     * @date: 2023/6/12 18:04
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/6/12 18:17
     * @param detailList
     * @param mainId
     */
    void add(List<SubcontractOrderDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改明细
     * @author Will
     * @date: 2023/6/12 18:18
     * @param detailList
     * @param mainId
     */
    void update(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/6/12 18:39
     * @param mainId
     */
    List<SubcontractOrderDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据父级id查询
     * @author Will
     * @date: 2023/6/13 15:25
     * @param detailId
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listByParentId(String detailId);
    /**
     * @description: 根据主表id和Sku集合查询
     * @author Will
     * @date: 2023/6/14 9:13
     * @param mainId
     * @param skuNoList
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listByMainIdAndSku(String mainId, List<String> skuNoList);
    /**
     * @description: 根据来源明细ids查询有效明细
     * @author Will
     * @date: 2023/6/14 15:06
     * @param sourceDetailIds
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);
}
