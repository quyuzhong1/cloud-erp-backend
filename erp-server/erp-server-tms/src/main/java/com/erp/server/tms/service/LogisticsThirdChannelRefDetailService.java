package com.erp.server.tms.service;
import com.erp.model.tms.entity.LogisticsThirdChannelRefDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDetailDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;

import java.util.List;

/**
 * <p>
 * 物流-第三方渠道关系明细表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-07-30
 */
public interface LogisticsThirdChannelRefDetailService extends SuperService<LogisticsThirdChannelRefDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-07-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-07-30
    * @param dto
    * @return
    */
    Boolean update(LogisticsThirdChannelRefDetailDTO.UpdateDTO dto);

    /**
     * 更新明细
     * @param logisticsThirdChannelRefEntity
     * @param detailList
     */
    void updateDetail(LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity, List<LogisticsThirdChannelRefDetailEntity> detailList);

    /**
     * 根据主表id查询明细
     * @param mainIds
     * @return
     */
    List<LogisticsThirdChannelRefDetailEntity> listByMainIdList(List<String> mainIds);

    void removeByMainId(String mainId);
}
