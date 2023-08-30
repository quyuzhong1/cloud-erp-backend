package com.erp.server.oms.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.erp.model.oms.entity.DictAmazonAreaCountryEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.oms.service.DictAmazonAreaCountryService;
import com.erp.server.oms.service.ShopAuthService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.oms.enums.PlatformDictEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 店铺表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Slf4j
@Service
public class ShopInfoServiceImpl extends SuperServiceImpl<ShopInfoMapper, ShopInfoEntity> implements ShopInfoService {


    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ShopSdkServer shopSdkServer;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private DictAmazonAreaCountryService dictAmazonAreaCountryService;


    /**
     * 添加店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-06-29 10:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(ShopDTO.AddDTO dto) {
        ShopInfoEntity shop = new ShopInfoEntity();
        String dictPlatform = dto.getDictPlatform();
        //亚马逊
        PlatformDictEnum amazon = PlatformDictEnum.AMAZON;
        //shopify
        PlatformDictEnum shopify = PlatformDictEnum.SHOPIFY;
        //如果是亚马逊
        if (amazon.getCode().equals(dictPlatform)) {
            Boolean result = handleAmazonShop(dto);
            return result;
        }


        if (shopify.getCode().equals(dictPlatform)) {
            if (StringUtils.isBlank(dto.getDomain())) {
                throw new ServiceException("域名不能为空");
            }
            checkDomain("", dto.getDomain());
        }

        BeanMapper.copy(dto, shop);
        String salesOrgId = dto.getSalesOrgId();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
        String orgName = CollectionUtils.isNotEmpty(orgList) ? orgList.get(0).getName() : "";
        shop.setSalesOrgName(orgName);
        //负责人
        String chargeId = dto.getChargeId();
        String chargeName = "";
        if (StringUtils.isNotBlank(chargeId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(chargeId);
            if (Objects.nonNull(user)) {
                chargeName = user.getUserName();
            }
        }
        shop.setChargeName(chargeName);

        Boolean result = this.save(shop);
        return result;

    }


    /**
     * 检查域名
     *
     * @param id
     * @param domain
     */
    private void checkDomain(String id, String domain) {
        long count = this.lambdaQuery().ne(StringUtils.isNotBlank(id), ShopInfoEntity::getId, id).
                eq(ShopInfoEntity::getDomain, domain).last("LIMIT 1").count();
        if (count > 0) {
            throw new ServiceException(domain + "二级域名已存在");
        }
    }

    /**
     * 处理
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-08-21 15:07
     */
    public Boolean handleAmazonShop(ShopDTO.AddDTO dto) {
        if (StringUtils.isBlank(dto.getDictAreaCode())) {
            throw new ServiceException("区域不能为空");
        }
        if (CollectionUtils.isEmpty(dto.getDictCountryCodeList())) {
            throw new ServiceException("国家不能为空");
        }
        //国家集合
        List<String> countryCodeList = dto.getDictCountryCodeList();
        List<DictAmazonAreaCountryEntity> countryList = dictAmazonAreaCountryService.listByCountryCodes(countryCodeList);
        List<ShopInfoEntity> addList = new ArrayList<>(countryList.size());
        //店铺名称
        String name = dto.getName();
        String salesOrgId = dto.getSalesOrgId();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
        String orgName = CollectionUtils.isNotEmpty(orgList) ? orgList.get(0).getName() : "";
        //负责人
        String chargeId = dto.getChargeId();
        String chargeName = "";
        if (StringUtils.isNotBlank(chargeId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(chargeId);
            if (Objects.nonNull(user)) {
                chargeName = user.getUserName();
            }
        }
        for (String countryCode : countryCodeList) {
            String countryName = countryList.stream().filter(c -> c.getCountryCode().equals(countryCode)).findFirst().
                    map(DictAmazonAreaCountryEntity::getCountryName).orElse("");
            String shopName = name.concat(countryName);
            checkName("", shopName);
            if (StringUtils.isNotBlank(countryName)) {
                ShopInfoEntity shop = new ShopInfoEntity();
                BeanMapper.copy(dto, shop);
                shop.setDictCountryCode(countryCode);
                shop.setName(shopName);
                shop.setSalesOrgName(orgName);
                shop.setChargeName(chargeName);
                addList.add(shop);
            }

        }

        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return Boolean.FALSE;

    }

    /**
     * 检查店铺名称是否存在
     *
     * @param id
     * @param name
     */
    private void checkName(String id, String name) {
        long count = this.lambdaQuery().eq(StringUtils.isNotBlank(id), ShopInfoEntity::getId, id).
                eq(ShopInfoEntity::getName, name).last("LIMIT 1").count();
        if (count > 0) {
            throw new ServiceException(name + "店铺名已存在");
        }
    }

    /**
     * 修改店铺
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-07-03 9:05
     */
    @Override
    public String updateShop(ShopDTO.UpdateDTO dto) {
        ShopInfoEntity shopInfo = this.getById(dto.getId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        checkName(dto.getId(), dto.getName());
        shopInfo.setName(dto.getName());
        String salesOrgId = dto.getSalesOrgId();
        //负责人
        String chargeId = dto.getChargeId();
        String chargeName = "";
        if (StringUtils.isNotBlank(chargeId)) {
            FindUserDTO user = sysUserFeign.getUserByUserId(chargeId);
            if (Objects.nonNull(user)) {
                chargeName = user.getUserName();
            }
        }
        shopInfo.setChargeName(chargeName);
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
        String orgName = CollectionUtils.isNotEmpty(orgList) ? orgList.get(0).getName() : "";
        shopInfo.setSalesOrgId(dto.getSalesOrgId());
        shopInfo.setSalesOrgName(orgName);
        shopInfo.setChargeId(dto.getChargeId());
        Boolean result = this.updateById(shopInfo);
        if (result) {
            return shopInfo.getId();
        }
        return "";
    }


    /**
     * 店铺分页
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<ShopDTO.PagingViewDTO> paging(PagingDTO<ShopDTO.PagingParamDTO> dto) {
        ShopDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<ShopDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<ShopDTO.PagingViewDTO> list) {

        //国家id
        List<String> countryIdList = list.stream().map(ShopDTO.PagingViewDTO::getDictCountryCode).collect(Collectors.toList());
        List<DictAmazonAreaCountryEntity> countryList = dictAmazonAreaCountryService.listByCountryCodes(countryIdList);
        List<BaseDropDownDTO.CommonDTO> areaList = dictAmazonAreaCountryService.areaList();
        for (ShopDTO.PagingViewDTO item : list) {
            //平台
            String dictPlatform = item.getDictPlatform();
            item.setDictPlatform(dictPlatform);
            String areaId = item.getDictAreaCode();
            String areaName = areaList.stream().filter(a -> a.getCode().equals(areaId)).
                    map(BaseDropDownDTO.CommonDTO::getValue).findFirst().orElse("");
            item.setAreaName(areaName);
            String countryId = item.getDictCountryCode();
            String countryName = countryList.stream().filter(a -> a.getCountryCode().equals(countryId)).
                    map(DictAmazonAreaCountryEntity::getCountryName).findFirst().orElse("");
            item.setCountryName(countryName);
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "禁用" : "启用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            String authStatusName = AuthStatusEnum.getName(authStatus);
            item.setAuthStatusName(authStatusName);

        }

    }

    private List<ShopInfoEntity> listByAccountList(List<String> accountList) {
        if (CollectionUtils.isEmpty(accountList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ShopInfoEntity::getAccount, accountList).orderByDesc(ShopInfoEntity::getId).list();
    }


    /**
     * 启用或者禁用店铺
     *
     * @param shop     店铺信息
     * @param disabled 禁用状态
     * @return
     */
    @Override
    public BatchResultDTO updateStatus(ShopInfoEntity shop, Boolean disabled) {
        if (Objects.nonNull(shop)) {
            //数据库的禁用状态
            Boolean dbDisabled = shop.getDisabled();
            if (dbDisabled.equals(disabled)) {
                throw new ServiceException("存在相同的状态");
            }
            shop.setDisabled(disabled);
            this.updateById(shop);
            return BatchResultDTO.success(shop.getName(), OperationTypeEnum.DISABLED);

        }
        return BatchResultDTO.fail(shop.getName(), "店铺不存在");

    }


    /**
     * 获取详情
     *
     * @param id
     * @return com.erp.model.oms.dto.ShopDTO.ViewDTO
     * @author yl
     * @date 2023-08-22 16:13
     */
    @Override
    public ShopDTO.ViewDTO view(String id) {
        ShopInfoEntity shop = this.getById(id);
        if (Objects.isNull(shop)) {
            throw new ServiceException("店铺不存在");
        }
        ShopDTO.ViewDTO view = new ShopDTO.ViewDTO();

        BeanMapper.copy(shop, view);
        String authStatus = shop.getAuthStatus();
        view.setAuthStatusName(AuthStatusEnum.getName(authStatus));
        String areaId = StringUtils.isNotBlank(view.getDictAreaCode()) ? view.getDictAreaCode() : "";
        String countryId = StringUtils.isNotBlank(view.getDictCountryCode()) ? view.getDictCountryCode() : "";
        //平台
        String dictPlatform = view.getDictPlatform();
        String dictType = DictBasicEnum.PLATFORM.getType();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(dictType, dictPlatform);
        String platformName = Objects.nonNull(dictBasic) ? dictBasic.getName() : "";
        List<DictAmazonAreaCountryEntity> countryList = dictAmazonAreaCountryService.listByCountryCodes(Arrays.asList(countryId));


        String countryName = countryList.stream().filter(a -> a.getCountryCode().equals(countryId)).
                map(DictAmazonAreaCountryEntity::getCountryName).findFirst().orElse("");
        String areaName = countryList.stream().filter(a -> a.getCountryCode().equals(countryId)).
                map(DictAmazonAreaCountryEntity::getRegionName).findFirst().orElse("");
        view.setAreaName(areaName);
        view.setCountryName(countryName);
        view.setPlatformName(platformName);
        return view;
    }

    @Override
    public Boolean updateShopInfoById(ShopInfoEntity shopInfoEntity) {
        return lambdaUpdate()
                .eq(ShopInfoEntity::getId, shopInfoEntity.getId())
                .set(ShopInfoEntity::getIsGenTask, shopInfoEntity.getIsGenTask())
                .update();
    }


    /**
     * 获取到店铺授权utl
     *
     * @param id
     * @return java.lang.String
     * @author yl
     * @date 2023-08-28 20:00
     */
    @Override
    public String getShopAuthUrl(String id) {
        ShopInfoEntity shop = this.getById(id);
        if (Objects.isNull(shop)) {
            throw new ServiceException("店铺不存在");
        }
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        String url = shopSdkServer.getShopAuthorizeUrl(cfgAppClient, shop.getDomain(), shop.getId());
        return url;
    }


    /**
     * 店铺授权
     *
     * @param code
     * @param hmac
     * @param host
     * @param shop
     * @param timestamp
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(String code, String hmac, String host, String shop, String timestamp) {
        String bodyStr = "";
        try {
            ShopInfoEntity shopInfo = this.getByDomain(shop);
            if (Objects.isNull(shopInfo)) {
                throw new ServiceException("店铺不存在");
            }
            AppClientEnum appClient = AppClientEnum.SHOP_ACCESS_TOKEN;
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            findDTO.setBusinessType(appClient.getBusinessType());
            findDTO.setDictPlatform(appClient.getPlatform());
            findDTO.setPlatformType(appClient.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                throw new ServiceException("shopify应用授权配置不存在");
            }
            AuthorizeDTO.FindShopAuthorizeDTO findShopAuthorize = new AuthorizeDTO.FindShopAuthorizeDTO();
            findShopAuthorize.setAccessTokenUrl(cfgAppClient.getUrl());
            findShopAuthorize.setClientId(cfgAppClient.getClientId());
            findShopAuthorize.setClientSecret(cfgAppClient.getClientSecret());
            findShopAuthorize.setCode(code);
            findShopAuthorize.setHmac(hmac);
            findShopAuthorize.setHost(host);
            findShopAuthorize.setShop(shop);
            findShopAuthorize.setTimestamp(timestamp);
            bodyStr = shopSdkServer.getShopAuthorizeInfo(findShopAuthorize);
            JSONObject jsonObject = JSONObject.parseObject(bodyStr);
            //token
            String accessToken = jsonObject.getOrDefault("access_token", "").toString();
            //过期时间
            Integer expiresIn = Integer.valueOf(jsonObject.getOrDefault("expires_in", 0).toString());
            if (StringUtils.isBlank(accessToken)) {
                return Boolean.FALSE;
            }
            String shopId = shopInfo.getId();
            //根据店铺id 获取到授权信息
            ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
            if (Objects.isNull(shopAuth)) {
                shopAuth = new ShopAuthEntity();
            }
            shopAuth.setShopId(shopId);
            shopAuth.setAccessToken(accessToken);
            shopAuth.setToken(accessToken);
            shopAuth.setExpiresIn(expiresIn);
            shopAuth.setAppClientId(cfgAppClient.getId());
            shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            shopAuthService.saveOrUpdate(shopAuth);
            return this.updateById(shopInfo);

        } catch (Exception e) {
            log.error("店铺授权出错了===> bodyStr==>{} e==>{}", bodyStr, e);
        }

        return Boolean.FALSE;
    }

    /**
     * 根据域名来查询
     *
     * @param shopDomain
     * @return com.erp.model.oms.entity.ShopInfoEntity
     * @author yl
     * @date 2023-08-29 18:08
     */
    private ShopInfoEntity getByDomain(String shopDomain) {
        return this.lambdaQuery().eq(ShopInfoEntity::getDomain, shopDomain).last("LIMIT 1").one();
    }


    /**
     * 取消授权
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-29 16:41
     */
    @Override
    public Boolean cancelAuthorize(String id) {
        ShopInfoEntity shopInfo = this.getById(id);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        //授权状态
        String authStatus = shopInfo.getAuthStatus();
        if (!AuthStatusEnum.ALREADY.getCode().equals(authStatus)) {
            throw new ServiceException("该店铺未授权,无需取消授权");
        }
        shopInfo.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        Boolean result = this.updateById(shopInfo);
        if (result) {
            shopAuthService.removeByShopId(id);
        }
        return result;
    }

    @Override
    public String index(String hmac, String host, String shop, String timestamp) {
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        String params = "host=" + host + "&shop=" + shop + "&timestamp=" + timestamp;
        Boolean checkResult = shopSdkServer.verifyShop(params, hmac, shop, cfgAppClient.getClientSecret());
        if (!checkResult) {
            throw new ServiceException("店铺授权检验未通过");
        }
        String grantOptions = "per-user";
        String path = String.format(cfgAppClient.getUrl(), shop, cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
        return path;
    }


}
