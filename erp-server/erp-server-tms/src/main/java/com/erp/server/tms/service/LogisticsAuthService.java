package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流授权表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsAuthService extends SuperService<LogisticsAuthEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsAuthDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.UpdateDTO update(LogisticsAuthDTO.UpdateDTO dto);

    /**
     * 获取到授权详情
     * @author yl
     * @date 2023-11-10 17:50
     */
    LogisticsAuthDTO.ViewDTO view(String id);

    /**
     * 取消授权
     *@parms id
     *@return
     *@author yl
     *@date 2023-11-15
     */
    BatchResultDTO cancel(String mainId);

    /**
     * 根据渠道id 获取授权信息
     *@parms channelId
     *@return
     *@author yl
     *@date 2023-11-20
     */
    LogisticsAuthEntity getByChannelId(String channelId);

    /**
     * 根据授权id组装授权信息
     *
     * @param authId
     * @param shopId
     * @param logisticsPlatform
     * @return
     */
    Map<String, String> getLogisticsAuthConfig(String authId,String shopId,String logisticsPlatform);

    /**
     * 授权完成后 同步销售渠道
     * @param logisticsPlatform
     * @param authConfig
     */
    void syncUpdateSaleChannel(String logisticsPlatform,Map<String, String> authConfig);

    /**
     * 根据供应商id 获取信息
     * @param id
     * @param mainId
     * @return
     */
    LogisticsAuthEntity getByMainId(String id, String mainId);

    /**
     * 根据id获取授权信息
     *@parms channelId
     *@return 
     *@author yl
     *@date 2023-11-23
     */
    LogisticsSupplierDTO.AuthDTO getAuthByChannelId(String channelId);




    /**
     * 根据id获取授权信息
     *@parms channelId
     *@return
     *@author yl
     *@date 2023-11-23
     */
    LogisticsSupplierDTO.AuthDTO getAuthBySupplierId(String logisticsSupplierId);

    /**
     * 根据渠道id查询关联的平台信息
     * @Author Luo_WG
     * @Date 2024/1/25 17:24
     * @param channelIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsSupplierDTO.AuthChannelViewDTO>
     **/
    List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelView(List<String> channelIdList);

    /**
     * 根据授权信息进行校验
     * @param logisticsPlatform
     * @param authConfig
     * @return
     */
    ApiResult authLogistics(String logisticsPlatform,Map<String, String> authConfig);

    /**
     *
     *@parms authId
     *@return authStatus
     *@author yl
     *@date 2023-11-24
     */
    void updateLogisticsAuthStatus(String mainId, String authStatus);

    /**
     * 根据供应商获取授权列表
     * @param supplierIds
     * @return
     */
    List<LogisticsAuthEntity> listByMainIds(List<String> supplierIds);

    /**
     * 虾皮新增店铺授权
     * @param authMap
     * @return
     */
    Map<String, String> addShopeeShopAuth(Map<String, String> authMap);
}
