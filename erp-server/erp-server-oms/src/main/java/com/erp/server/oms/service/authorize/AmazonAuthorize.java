package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpReportFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.walmart.api.WalmartStaticKey;
import com.sdk.oms.walmart.dto.WalmartShopInfoDTO;
import com.sdk.oms.walmart.dto.walmart.WalmartTokenDTO;
import com.sdk.oms.walmart.service.WalmartSdkClientService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 亚马逊授权和校验
 *
 * @author Jim
 * @since 2023-11-09
 **/
@Slf4j
@Component
@AuthSaveData(method = PlatformDictEnum.AMAZON)
public class AmazonAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private DmpReportFeign dmpReportFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取授权地址
     */
    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeDTO dto) {
        return "";
    }

    /**
     * 授权校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        String shopId = dto.getShopId();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_WALMART_SHOP_ID_NOT_NULL);
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (null == shopInfo) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        // 校验国家唯一
        boolean existSameCountry = shopInfoService.checkExist(shopInfo.getDictCountryCode(), shopInfo.getDictPlatform(), AuthStatusEnum.ALREADY.getCode());
        if (!existSameCountry) {
            throw new ServiceException(ApiError.ERROR_COUNTRY_COUNT_SHOP_EXIST, shopInfo.getCountryName());
        }

        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopInfo.setIsGenTask(Boolean.TRUE);
        boolean result = shopInfoService.updateById(shopInfo);
        if (!result) {
            throw new ServiceException("店铺授权保存失败");
        }
        // 授权后添加任务
        dmpTaskFeign.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));

        // 添加报告计划
        DmpSyncReportScheduleDTO dmpDTO = new DmpSyncReportScheduleDTO();
        BeanUtils.copyProperties(shopInfo, dmpDTO);
        dmpReportFeign.addReportSchedule(dmpDTO);

        return Boolean.TRUE;
    }

    /**
     * 取消授权
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        //授权状态
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            throw new ServiceException("该店铺未授权,无需取消授权");
        }
        shopInfo.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        Boolean result = shopInfoService.updateById(shopInfo);
        if (result) {
            // 删除授权
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        // 取消报告计划任务
        DmpSyncReportScheduleDTO dmpDTO = new DmpSyncReportScheduleDTO();
        BeanUtils.copyProperties(shopInfo, dmpDTO);
        dmpReportFeign.cancelReportSchedule(dmpDTO);
        return result;

    }
}
