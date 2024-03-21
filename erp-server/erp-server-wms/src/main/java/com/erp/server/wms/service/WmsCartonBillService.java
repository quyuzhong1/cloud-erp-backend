package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsCartonBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;

import java.util.List;

/**
 * <p>
 * 发货单箱子信息明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface WmsCartonBillService extends SuperService<WmsCartonBillEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    */
    BaseResultDTO.AddDTO add(FirstMileCartonBillDTO.AddDTO dto);

    /**
     * 根据装箱id删除箱子明细
     * @Author Luo_WG
     * @Date 2023/11/28 16:31
     * @param cartonIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByCartonIds(List<String> cartonIds);

    /**
     * 根据发货单id删除箱子明细
     * @Author Luo_WG
     * @Date 2023/11/28 16:31
     * @param sourceIds
     * @return java.lang.Boolean
     **/
    Boolean deleteBySourceIds(List<String> sourceIds);

    /**
     * 根据发货单id查询箱子明细
     * @Author Luo_WG
     * @Date 2023/11/29 11:17
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonBillEntity>
     **/
    List<WmsCartonBillEntity> listBySourceIds(List<String> sourceIds);


}
