package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.entity.OtherOutstockDetailEntity;

import java.util.List;

/**
 * <p>
 * 其他出库明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface OtherOutstockDetailService extends SuperService<OtherOutstockDetailEntity> {
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/19 15:14
     * @param detailList
     * @param mainId
     */
    void add(List<OtherOutstockDetailDTO.AddDTO> detailList, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/19 15:25
     * @param detailList
     * @param mainId
     */
    void update(List<OtherOutstockDetailDTO.UpdateDTO> detailList, String mainId);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/5/19 15:30
     * @param mainId
     * @return List<OtherOutstockDetailEntity>
     */
    List<OtherOutstockDetailEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/5/19 16:15
     * @param mainIds
     * @return List<OtherOutstockDetailEntity>
     */
    List<OtherOutstockDetailEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2023/5/19 15:36
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
}
