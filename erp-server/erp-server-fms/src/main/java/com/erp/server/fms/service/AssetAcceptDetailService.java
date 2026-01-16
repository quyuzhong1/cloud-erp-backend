package com.erp.server.fms.service;

import cn.hutool.json.JSONArray;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.fms.dto.AssetAcceptDetailDTO;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 资产验收表明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetAcceptDetailService extends SuperService<AssetAcceptDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetAcceptDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetAcceptDetailDTO.UpdateDTO dto);

    Integer getAcceptQtyByDetailId(String detailId);

    Map<String, BigDecimal> getAcceptableQtyByDetailId(List<String> detailId);
    /**
     * 根据主表id查询
     * @author will
     * @date 2025/12/30 12:31
     * @param mainIdList
     * @return List<AssetAcceptDetailEntity>
     */
    List<AssetAcceptDetailEntity> listByMainIdList(List<String> mainIdList);
    /**
     * 更新金蝶明细id
     * @author will
     * @date 2025/12/30 16:36
     * @param list
     * @return void
     */
    void updateKingdeeDetailId(JSONArray list);
}
