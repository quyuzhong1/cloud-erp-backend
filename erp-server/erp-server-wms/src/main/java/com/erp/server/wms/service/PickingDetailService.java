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
      * 根据来源Id删除拣货详情
      * @Author Luo_WG
      * @Date 2023/5/15 11:11
      * @param ids ids
      * @return void
      **/
     Boolean deleteBySourceId(List<String> ids);
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
}
