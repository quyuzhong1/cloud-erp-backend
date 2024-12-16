package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;

import java.util.List;

/**
 * <p>
 * 拣货明细 服务类
 * </p>
 *
 * @author will
 * @since 2023-05-11
 */
public interface PickingDetailService extends SuperService<PickingDetailEntity> {
     /**
      * 新增拣货明细
      * @author Will
      * @date: 2023/5/12 11:23
      * @param detailList
      */
     void add(List<PickingDetailDTO.CommonDTO> detailList);

     /**
      * @description: 根据来源id查询拣货信息
      * @author Will
      * @date: 2023/5/16 12:20
      * @param dto
      * @return List<ListDTO>
      */
     List<PickingDetailDTO.ListDTO> listPickingDetailBySourceId(PickingDetailDTO.SearchParamDTO dto);


     /**
      * 根据明细id 即来源明细id 获取到拣货信息
      * @author yl
      * @date 2023-06-08 9:42
      * @param detailIds
      * @return
      */
     List<PickingDetailEntity> listPickingDetailBySourceDetailIds(List<String> detailIds);

     /**
      * 清除拣货单明细
      * @param deliveryId 发货单id
      */
    void cleanException(String deliveryId);

     void updateByChange(List<PickingDetailEntity> updatePickingList);
    /**
     * 根据主表id集合查询拣货明细
     * @author will
     * @date 2024/11/27 11:55
     * @param mainIdList
     * @return List<PickingDetailEntity>
     */
    List<PickingDetailEntity> listByMainIdList(List<String> mainIdList);
}
