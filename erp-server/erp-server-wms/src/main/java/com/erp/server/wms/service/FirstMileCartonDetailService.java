package com.erp.server.wms.service;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonDetailDTO;

import java.util.List;

/**
 * <p>
 * 发货单箱子信息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface FirstMileCartonDetailService extends SuperService<FirstMileCartonDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void add(FirstMileCartonDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void update(FirstMileCartonDTO.UpdateDTO dto, String mainId);

    /**
     * 根据装箱id查询箱子产品信息
     * @Author Luo_WG
     * @Date 2023/11/28 18:55
     * @param cartonIds
     * @return java.util.List<com.erp.model.wms.entity.FirstMileCartonDetailEntity>
     **/
    List<FirstMileCartonDetailEntity> listByCartonIds(List<String> cartonIds);


}
