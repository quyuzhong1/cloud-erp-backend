package com.erp.server.wms.service;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;

import java.util.List;

/**
 * <p>
 * 组包预报详情 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastDetailService extends SuperService<PackageForecastDetailEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param mainId
    * @param detailList
    * @return
    */
    void add(String mainId, List<PackageForecastDetailDTO.AddDTO> detailList);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param detailIdList
    * @return
    */
    Boolean update(PackageForecastEntity entity, List<String> detailIdList);

    /**
     * 获取对应详情
     * @param id
     * @return
     */
    List<PackageForecastDetailDTO.ViewDTO> listDetailViewByMainId(String id);

    List<PackageForecastDetailEntity> listDbByMainId(String mainId);

    void removeByMainId(String mainId,String logisticsSupplierId);

    List<PackageForecastDetailDTO.ViewDTO> detailQuery(PackageForecastDTO.DetailQueryParamDTO dto);

    /**
     * 更新订单明细状态
     * @param orderCode
     * @param status
     */
    void updateStatusByOrderCode(String orderCode, String status);

    /**
     * @description: 根据销售订单id集合查询
     * @author Will
     * @date: 2024/4/1 14:52
     * @param soIdList
     * @return List<PackageForecastDetailEntity>
     */
    List<PackageForecastDetailEntity> listBySoIdList(List<String> soIdList);
}
