package com.erp.server.wms.service;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;

import java.util.List;

/**
 * <p>
 * FBI发货单物流信息表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaDeliveryLogisticsService extends SuperService<FbaDeliveryLogisticsEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/11/3 15:32
     * @param dto 新增参数
     * @param mainId 主表id
     * @param code 主单据编号
     * @return java.lang.String
     **/
    String add(FbaDeliveryLogisticsDTO.AddDTO dto, String mainId, String code);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return
    */
    Boolean update(FbaDeliveryLogisticsDTO.UpdateDTO dto, String mainId);

    /**
     * 根据主键id删除物流信息
     * @Author Luo_WG
     * @Date 2023/11/3 15:20
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean removeByMainIds(List<String> mainIds);

    /**
     * 根据主表id查询物流信息
     * @Author Luo_WG
     * @Date 2023/11/3 14:02
     * @param mainId
     * @return com.erp.model.wms.entity.FbaDeliveryDetailEntity
     **/
    FbaDeliveryLogisticsEntity listByMainId(String mainId);

    /**
     *
     * @Author Luo_WG
     * @Date 2023/10/30 18:35
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.DeliveryLogisticsView>>
     **/
    List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> updateLogisticsView(BaseIdsDTO.IdsDTO ids);

    /**
     * 更新物流信息列表保存
     * @Author Luo_WG
     * @Date 2023/11/3 16:27
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean saveUpdateLogistics(List<FbaDeliveryLogisticsDTO.DeliveryLogisticsSave> dto);
}
