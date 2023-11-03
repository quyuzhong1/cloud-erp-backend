package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;

import java.util.List;

/**
 * <p>
 * FBI发货单明细表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaDeliveryDetailService extends SuperService<FbaDeliveryDetailEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    void add(FbaDeliveryDTO.AddDTO dto, String mainId);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    void update(FbaDeliveryDTO.UpdateDTO dto, String mainId);

    /**
     * 根据来源详情id查询发货详情
     * @Author Luo_WG
     * @Date 2023/11/2 19:03
     * @param detailIds
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryDetailEntity>
     **/
    List<FbaDeliveryDetailEntity> listBySourceDetailIds(List<String> detailIds);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/3 14:02
     * @param id
     * @return java.util.List<com.erp.model.wms.entity.FbaDeliveryDetailEntity>
     **/
    List<FbaDeliveryDetailEntity> listByMainId(String id);


}
