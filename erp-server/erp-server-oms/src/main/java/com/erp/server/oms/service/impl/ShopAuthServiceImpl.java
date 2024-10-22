package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopAuthDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.mapper.ShopAuthMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.ShopAuthService;
import com.sdk.oms.shopee.dto.base.request.AuthRequest;
import com.sdk.oms.shopee.dto.product.request.ProductRequest;
import com.sdk.oms.shopee.dto.product.response.ItemInfo;
import com.sdk.oms.shopee.service.ShopeeAuthService;
import com.sdk.oms.shopee.service.ShopeeProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 店铺授权表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class ShopAuthServiceImpl extends SuperServiceImpl<ShopAuthMapper, ShopAuthEntity> implements ShopAuthService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private ShopeeAuthService shopeeAuthService;
    private ShopInfoServiceImpl shopInfoService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopeeProductService shopeeProductService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ShopAuthDTO.AddDTO addDTO) {
        ShopAuthEntity shopAuthEntity = new ShopAuthEntity();
        BeanMapperUtils.copy(addDTO, shopAuthEntity);

        // 数据处理
        handleData(shopAuthEntity);

        log.info("开始新增店铺授权单");
        boolean save = super.save(shopAuthEntity);
        if (!save) {
            throw new ServiceException("店铺授权单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "店铺授权单", shopAuthEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shopAuthEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return shopAuthEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShopAuthDTO.UpdateDTO updateDTO) {
        ShopAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "店铺授权单"));
        ShopAuthEntity shopAuthEntity = BeanMapperUtils.map(ShopAuthEntity.class, updateDTO);

        // 数据处理
        handleData(shopAuthEntity);
        log.info("编辑 开始修改店铺授权单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shopAuthEntity);
        if (!save) {
            throw new ServiceException("店铺授权单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录店铺授权单日志数据，id：【{}】", shopAuthEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), shopAuthEntity.getId(), "店铺授权单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shopAuthEntity, null, shopAuthEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 获取到授权信息 根据店铺id
     *
     * @param shopId
     * @return com.erp.model.oms.entity.ShopAuthEntity
     * @author yl
     * @date 2023-08-29 16:15
     */
    @Override
    public ShopAuthEntity getByShopId(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return null;
        }
        return this.lambdaQuery().eq(ShopAuthEntity::getShopId, shopId).
                last("LIMIT 1").one();
    }

    /**
     * 删除根据店铺id
     *
     * @param id
     * @return void
     * @author yl
     * @date 2023-08-29 16:48
     */
    @Override
    public void removeByShopId(String id) {
        lambdaUpdate().eq(ShopAuthEntity::getShopId, id).remove();

    }

    @Override
    public String getShopeeCodeUrl(ShopAuthorizeUrlDTO dto) {
        if (Objects.isNull(dto)) {
            throw new ServiceException("店铺记录id不能为空");
        }
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        try {
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.nonNull(cfgAppClient)) {
                String redirect = cfgAppClient.getRedirectUrl() + "?id=" + dto.getShopId() + "&platformCode=" + dto.getPlatformCode();
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .redirect(redirect)
                        .build();
                return shopeeAuthService.getCodeUrl(authRequest);
            } else {
                throw new ServiceException("虾皮基础配置未找到");
            }
        } catch (Exception e) {
            throw new ServiceException("erp-dmp服务调用异常");
        }

    }

    /**
     * @param type
     * @param stauts 是否授权
     * @return
     */
    @Override
    public List<ShopAuthEntity> getShopeeShopList(String type, String stauts) {
        //获取已授权店铺配置
        return baseMapper.getShopeeShopList(type, stauts);
    }

    @Override
    public List<ShopAuthEntity> getAuthShopByPlatformType(String platformType) {
        if (StringUtils.isBlank(platformType)) return Collections.emptyList();
        return baseMapper.getAuthShopByPlatformType(platformType);
    }

    @Override
    public ShopAuthEntity getShopeeShopById(String shopeeId) {
        LambdaQueryWrapper<ShopAuthEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopAuthEntity::getShopeeId, shopeeId);
        queryWrapper.eq(ShopAuthEntity::getIsDeleted, false);

        List<ShopAuthEntity> shopAuthEntities = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(shopAuthEntities)) {
            return shopAuthEntities.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updateShopeeToken(ShopAuthEntity shopAuthEntity) {
        Optional.ofNullable(shopAuthEntity.getShopeeId()).orElseThrow(() -> new ServiceException("店铺id不能为空"));
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        try {
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                throw new ServiceException("虾皮基础配置未找到");
            }
            ShopAuthEntity shopAuth = this.getByShopId(shopAuthEntity.getShopId());
            if (Objects.isNull(shopAuth)) {
                throw new ServiceException("店铺配置为空");
            }

            if (shopAuth.getType().equals(AuthTypeEnum.SHOP.getCode())) {
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .refreshToken(shopAuthEntity.getRefreshToken())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                        .build();
                shopeeAuthService.refreshShopToken(authRequest);
            }
        } catch (Exception e) {
            throw new ServiceException("erp-dmp服务调用异常");
        }

        baseMapper.updateById(shopAuthEntity);
    }

    @Override
    public Boolean updateShopAuthById(ShopAuthEntity shopAuthEntity) {
        return this.updateById(shopAuthEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshToken(String shopAuthId, String accessToken, String refreshToken, Integer expiresIn, LocalDateTime tokenExpireTime) {
        this.lambdaUpdate().set(ShopAuthEntity::getAccessToken, accessToken)
                .set(ShopAuthEntity::getRefreshToken, refreshToken)
                .set(ShopAuthEntity::getExpiresIn, expiresIn)
                .set(ShopAuthEntity::getToken,accessToken)
                .set(ShopAuthEntity::getTokenExpireTime, tokenExpireTime)
                .set(ShopAuthEntity::getUpdateTime, LocalDateTime.now())
                .eq(ShopAuthEntity::getId, shopAuthId)
                .update();
    }

    @Override
    public List<ShopAuthEntity> listTokenExpiresShop() {
        return baseMapper.listTokenExpiresShop();
    }

    @Override
    public Boolean updateRefreshTokenError(String shopAuthId, String msg) {
        return this.lambdaUpdate().set(ShopAuthEntity::getRefreshStatus, 1)
                .set(ShopAuthEntity::getRefreshErrorMsg, msg)
                .eq(ShopAuthEntity::getId, shopAuthId)
                .update();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ShopAuthEntity shopAuthEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean batchUpdateShopAuthById(List<ShopAuthEntity> shopAuthEntityList) {
        return this.updateBatchById(shopAuthEntityList);
    }

    @Override
    public List<ShopAuthEntity> listShopAuthByShopIds(List<String> shopIdList) {
        return this.lambdaQuery()
                .in(ShopAuthEntity::getShopId, shopIdList)
                .list();
    }
}
