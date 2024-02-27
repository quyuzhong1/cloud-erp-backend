package com.erp.server.srm.service;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.common.business.service.SuperService;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 送货单明细 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
public interface DeliveryOrderDetailService extends SuperService<DeliveryOrderDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    void add(List<DeliveryOrderDetailDTO.AddDTO> dto, String mainId);

    List<DeliveryOrderDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表id集合查询
     * @author Will
     * @date: 2024/1/26 9:50
     * @param mainIdList
     * @return List<DeliveryOrderDetailEntity>
     */
    List<DeliveryOrderDetailEntity> listByMainIdList(List<String> mainIdList);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    Boolean update(List<DeliveryOrderDetailDTO.UpdateDTO> dto,String mainId);


    boolean deleteByMainIds(List<String> mainIds);

    Map<String,List<DeliveryOrderDetailDTO.PrintDTO>> mapPrintByMainIds(List<String> mainIds);

    List<DeliveryOrderDetailEntity> listDetailByDetailSourceIds(List<String> purchaseDetailIds);

}
