package com.erp.server.tms.service;
import com.erp.model.tms.entity.SmallBagCostAllocationDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.SmallBagCostAllocationDetailDTO;

import java.util.List;

/**
 * <p>
 * 小包费用分摊明细 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-11-29
 */
public interface SmallBagCostAllocationDetailService extends SuperService<SmallBagCostAllocationDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SmallBagCostAllocationDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-11-29
    * @param dto
    * @return
    */
    Boolean update(SmallBagCostAllocationDetailDTO.UpdateDTO dto);

    /**
     * 根据主表id查询明细
     * @Author Luo_WG
     * @Date 2024/12/3 15:15
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.SmallBagCostAllocationDetailEntity>
     **/
    List<SmallBagCostAllocationDetailEntity> listByMainIds(List<String> mainIds);


}
