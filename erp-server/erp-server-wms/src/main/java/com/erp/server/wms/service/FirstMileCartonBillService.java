package com.erp.server.wms.service;
import com.erp.model.wms.entity.FirstMileCartonBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;

/**
 * <p>
 * 发货单箱子信息明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface FirstMileCartonBillService extends SuperService<FirstMileCartonBillEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileCartonBillDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(FirstMileCartonBillDTO.UpdateDTO dto);


}
