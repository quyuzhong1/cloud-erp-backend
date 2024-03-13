package com.erp.server.scm.service;

import cn.hutool.json.JSONArray;
import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import org.apache.commons.math3.util.Pair;

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
     * @param isFinishDelivery
     */
    void updateArrivalStatusByIds(String code, List<String> ids,Boolean isFinishDelivery);
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
     * @description: 根据变更更新
     * @author Will
     * @date: 2023/6/27 14:38
     * @param detailList
     * @param mainId
     */
     void updateByChange(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId);

    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/6/12 18:39
     * @param mainId
     */
    List<SubcontractOrderDetailEntity> listByMainId(String mainId);

    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2023/6/12 18:39
     * @param mainIdList
     */
    List<SubcontractOrderDetailEntity> listByMainIds(List<String> mainIdList);
    /**
     * @description: 根据父级id查询
     * @author Will
     * @date: 2023/6/13 15:25
     * @param detailIds
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listByParentIds(List<String> detailIds);
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


    /**
     * @description: 根据来源明细ids查询未下推采购单的委外订单
     * @author Will
     * @date: 2023/6/14 15:06
     * @param sourceDetailIds
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listBySourceDetailIdsWithNoPurchase(List<String> sourceDetailIds);

    /**
     * @description: 变更新增明细
     * @author Will
     * @date: 2023/6/20 10:34
     * @param detailList
     * @param mainId
     */
    void addByChange(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 更新来源明细id
     * @author Will
     * @date: 2023/6/20 10:40
     * @param pairList
     */
    void updateSourceDetailId(List<Pair<String, String>> pairList);

    /**
     * @description: 更新明细金蝶id
     * @author Will
     * @date: 2023/5/4 18:36
     * @param list
     */
    void updateKingdeeDetailId(JSONArray list);
    /**
     * @description: 根据父级Id查询子级委外明细
     * @author Will
     * @date: 2024/1/29 10:20
     * @param parentIdList
     * @return List<SubcontractOrderDetailEntity>
     */
    List<SubcontractOrderDetailEntity> listChildSubcontractDetailByIds(List<String> parentIdList);
}
