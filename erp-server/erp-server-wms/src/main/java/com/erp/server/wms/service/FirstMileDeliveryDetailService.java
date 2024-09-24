package com.erp.server.wms.service;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.common.business.service.SuperService;

import java.util.List;

/**
 * <p>
 * 头程发货单明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FirstMileDeliveryDetailService extends SuperService<FirstMileDeliveryDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    void add(FirstMileDeliveryDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    void update(FirstMileDeliveryDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主表id删除详情信息
     * @Author Luo_WG
     * @Date 2023/11/3 15:25
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean removeByMainIds(List<String> mainIds);

    /**
     * 根据来源详情id查询发货详情
     * @Author Luo_WG
     * @Date 2023/11/2 19:03
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryDetailEntity>
     **/
    List<FirstMileDeliveryDetailEntity> listBySourceDetailIds(List<String> detailIds);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/3 14:02
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryDetailEntity>
     **/
    List<FirstMileDeliveryDetailEntity> listByMainIds(List<String> mainIds);

//    /**
//     * 根据主表id分组sku查询发货单所有产品发货及待装箱数
//     * @Author Luo_WG
//     * @Date 2023/11/28 18:41
//     * @param mainId
//     * @return java.util.List<com.erp.model.wms.dto.FirstMileDeliveryDTO.GroupSkuDTO>
//     **/
//    List<FirstMileDeliveryDTO.GroupSkuDTO> listGroupSkuByMainId(String mainId);

    /**
     * 根据主表获取明细列表
     * @param id
     * @return
     */
    List<FirstMileDeliveryDetailEntity> listDetailByMainId(String id);

    List<FirstMileDeliveryDetailEntity> listApprovedByFbaShipmentCodes(List<String> fbaShipmentCodeList);
}
