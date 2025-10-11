package com.erp.server.oms.service;
import com.erp.model.oms.entity.PackagePlanDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.PackagePlanDetailDTO;

import java.util.List;

/**
 * <p>
 * 组包计划明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
 */
public interface PackagePlanDetailService extends SuperService<PackagePlanDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-10-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackagePlanDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-10-09
    * @param dto
    * @return
    */
    Boolean update(PackagePlanDetailDTO.UpdateDTO dto);


    List<PackagePlanDetailEntity> getBySoId(String soId);

    /**
     * 根据销售订单id更新小包条码
     *
     * @param packagePlanId
     * @param soId
     * @param barcode
     */
    void updateBarcodeBySoId(String packagePlanId, String soId, String barcode);

    /**
     * 根据主表id查询明细
     * @param ids
     * @return
     */
    List<PackagePlanDetailEntity> getByMainIds(List<String> ids);
}
