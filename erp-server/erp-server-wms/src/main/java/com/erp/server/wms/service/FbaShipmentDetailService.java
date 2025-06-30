package com.erp.server.wms.service;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;

import java.util.List;

/**
 * <p>
 * FBI拣货明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaShipmentDetailService extends SuperService<FbaShipmentDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    String add(FbaShipmentDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaShipmentDetailDTO.UpdateDTO dto);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/10/31 19:27
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.FbaShipmentDetailEntity>
     **/
    List<FbaShipmentDetailEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据主表id删除详情
     * @Author Luo_WG
     * @Date 2023/11/6 10:43
     * @param mainIds
     * @return void
     **/
    Boolean removeByMainIds(List<String> mainIds);

    /**
     * 根据唯一值获取明细记录
     * @param shipmentCode
     * @param asin
     * @param msku
     * @return
     */
    FbaShipmentDetailEntity getDetail(String shipmentCode, String asin, String msku);

    void updateDetailByReceiveList(FbaShipmentEntity fbaShipmentEntity);
}
