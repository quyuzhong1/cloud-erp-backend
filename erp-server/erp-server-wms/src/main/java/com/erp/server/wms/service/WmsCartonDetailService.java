package com.erp.server.wms.service;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 发货单箱子信息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsCartonDetailService extends SuperService<WmsCartonDetailEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/11/29 10:39
     * @param dto
     * @param cartonId 规格表id
     * @param sourceId 来源id
     **/
    void add(WmsCartonDTO.AddDTO dto, String cartonId, String sourceId);

    /**
     * 根据装箱id查询箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/28 18:55
     * @param cartonIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonDetailEntity>
     **/
    List<WmsCartonDetailEntity> listByCartonIds(List<String> cartonIds);

    /**
     * 根据发货单id查询箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/29 11:15
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonDetailEntity>
     **/
    List<WmsCartonDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据箱规id删除箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/29 10:35
     * @param cartonIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByCartonIds(List<String> cartonIds);

    /**
     * 根据发货单id删除箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/29 10:35
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean deleteBySourceIds(List<String> mainIds);


    /**
     * 装箱详情清单(以箱号和SKU号维度)
     * @param mainId
     * @return
     */
    List<WmsCartonDTO.PackingItemDTO> boxInfoBySourceId(String mainId);
}
