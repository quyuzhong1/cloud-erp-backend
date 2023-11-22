package com.erp.server.tms.service;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售平台物流渠道表 服务类
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
 */
public interface LogisticsSaleChannelService extends SuperService<LogisticsSaleChannelEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsSaleChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2023-11-08
    * @param dto
    * @return
    */
    Boolean update(LogisticsSaleChannelDTO.UpdateDTO dto);


    Boolean saveOrUpdateSaleChannel(LogisticsSaleChannelEntity logisticsSaleChannelEntity);

    /**
     * 根据授权和平台类型更新数据启用状态
     * @param authId
     * @param logisticsPlatform
     * @param channelStatus
     */
    void updateSaleChannelByAuthId(String authId,String logisticsPlatform,Integer channelStatus);
    /**
     * 根据类型获取列表
     * @param platformType
     * @return
     */
    List<SaleChannelDTO> listByType(String platformType);

    /**
     * 根据数据来源获取源数据
     * @param platformType
     * @return
     */
    List<LogisticsSaleChannelEntity> listByDataSource(String platformType,String overseasWarehouseId,Integer status);

    /**
     *  根据授权状态和同步状态查询数据
     * @author yl
     *
     * @param authId
     * @param isSync
     * @date: 2023-11-08
     * @return
     */
    List<LogisticsSaleChannelEntity> listByAuthId(String authId, Boolean isSync);

    /**
     * 异步拉取销售渠道数据
     */
    void asyncUpdateSaleChannel(Map<String, String> authMap);
}
