package com.erp.server.wms.service;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonDetailDTO;

/**
 * <p>
 * 发货单箱子信息表 服务类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
public interface FirstMileCartonDetailService extends SuperService<FirstMileCartonDetailEntity> {

    /**
    * 新增
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileCartonDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_wg
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(FirstMileCartonDetailDTO.UpdateDTO dto);


}
