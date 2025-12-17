package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
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
     * @param logisticsPlatform
     * @param channelStatus
     */
    void updateSaleChannelByPlatform(String logisticsPlatform,Integer channelStatus);
    /**
     * 根据类型获取列表
     * @param platformType
     * @return
     */
    List<SaleChannelDTO> listByType(String platformType,String servicePlatform);

    /**
     * 根据物流平台获取 到原始渠道信息
     * @param logisticsPlatform
     * @param servicePlatform
     * @return
     */
    List<LogisticsSaleChannelEntity> listByLogisticsPlatform(String logisticsPlatform,String servicePlatform);

    /**
     *
     *@parms platform 平台
     *@params code 编码
     *@return
     *@author yl
     *@date 2023-12-07
     */
    LogisticsSaleChannelEntity getByPlatform(String platform, String code);

    PagingVO<SaleChannelDTO> pagingSelect(PagingDTO<LogisticsSaleChannelDTO.QueryDTO> dto);
}
