package com.erp.server.oms.sdk.authorize;

import com.common.business.annotation.PlatformAnnotate;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.dto.TemuCommonDTO;
import com.sdk.oms.temu.dto.TemuWarehouseDTO;
import com.sdk.oms.temu.service.TemuClient;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ebay授权和校验
 *
 **/
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.TE_MU)
public class TeMuAuthorize implements IShopAuthorizeService<T> {

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private TemuClient temuClient;

    /**
     * 获取授权地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        throw new ServiceException("授权功能未开通");
    }

    /**
     * 授权校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        String shopId = dto.getShopId();
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
        if (shopInfoEntity == null) {
            throw new ServiceException("店铺不存在");
        }
        ShopAuthEntity shopAuthEntity = shopAuthService.getByShopId(shopId);
        if(shopAuthEntity == null) {
            throw new ServiceException("店铺授权信息不存在");
        }
        if(StringUtils.isBlank(shopInfoEntity.getDictAreaCode())){
            throw new ServiceException("区域不能为空");
        }
        Map<String,Object> extendMap = shopInfoEntity.getExtendData();
        if(Objects.isNull(extendMap) || !extendMap.containsKey("clientId") || !extendMap.containsKey("clientSecret")){
            throw new ServiceException("店铺appkey和appsecret必填");
        }
        if(StringUtils.isBlank(shopAuthEntity.getAccessToken())){
            throw new ServiceException("token不能为空");
        }
        String clientId = (String) extendMap.get("clientId");
        String clientSecret = (String) extendMap.get("clientSecret");
        String accessToken = shopAuthEntity.getAccessToken();
        TemuResp<TemuWarehouseDTO> temuResp = temuClient.getWarehouseList(new TemuCommonDTO( shopInfoEntity.getDictAreaCode(), clientId, clientSecret, accessToken));
        if(!temuResp.getSuccess()){
            throw new ServiceException("授权失败,{}",temuResp.getErrorMsg());
        }
        shopInfoEntity.setAuthTime(LocalDateTime.now());
        shopInfoEntity.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfoService.updateById(shopInfoEntity);
        return true;
    }

    /**
     * 取消授权
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        String shopId = dto.getShopId();
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
        if (shopInfoEntity == null) {
            throw new ServiceException("店铺不存在");
        }
        ShopAuthEntity shopAuthEntity = shopAuthService.getByShopId(shopId);
        if(shopAuthEntity == null) {
            throw new ServiceException("店铺授权信息不存在");
        }
        shopInfoEntity.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        shopInfoEntity.setExtendData(new HashMap<>());
        shopInfoEntity.setAuthExpireDate(null);
        shopInfoEntity.setAuthTime(null);
        shopInfoService.updateById(shopInfoEntity);
        shopAuthService.removeByShopId(shopAuthEntity.getId());
        return true;

    }
    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        return Boolean.TRUE;
    }
}
