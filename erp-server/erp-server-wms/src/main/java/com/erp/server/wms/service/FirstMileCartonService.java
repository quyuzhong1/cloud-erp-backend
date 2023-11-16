package com.erp.server.wms.service;
import com.erp.model.wms.entity.FirstMileCartonEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonDTO;

/**
 * <p>
 * 发货单箱规信息 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface FirstMileCartonService extends SuperService<FirstMileCartonEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileCartonDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(FirstMileCartonDTO.UpdateDTO dto);


}
