package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.DictBasicValueEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.*;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private CustomerB2cService customerB2cService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private CommonService commonService;

    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

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
        //检查店铺是否存在
        checkIsExist("", dto.getDictPlatform(), dto.getAccount(), dto.getDictAreaCode(), dto.getDictCountryCodeList());
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
        if (result) {
            //店铺客户信息
            autoCreateShopCustomer(shop);
        }
        return result;

    }


    /**
     * 店铺保存成功后 自动创建客户
     *
     * @param shop
     */
    public void autoCreateShopCustomer(ShopInfoEntity shop) {
        CustomerDTO.AddDTO customer = new CustomerDTO.AddDTO();
        customer.setUseOrgId(shop.getSalesOrgId());
        customer.setInnerOrgId(shop.getSalesOrgId());
        //平台
        customer.setPlatformType(shop.getDictPlatform());
        String countryId = shop.getDictCountryCode();
        String currency = "CNY";
        if (StringUtils.isNotBlank(countryId)) {
            //根据国家查询
            List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Arrays.asList(countryId));
            if (CollectionUtils.isNotEmpty(countryList)) {
                currency = countryList.get(0).getCurrencyCode();
            }
        }

        if (StringUtils.isBlank(countryId)) {
            countryId = DictValueEnum.GL.getCode();
        }
        customer.setName(shop.getName());
        customer.setCountryId(countryId);
        //币种
        customer.setCurrency(currency);
        customer.setSellerId(shop.getChargeId());
        customer.setConditionDict(DictBasicValueEnum.ONLINE_STORE_PAYMENT.getCode());
        customer.setSourceId(shop.getId());
        customer.setSourceType(SourceTypeEnum.SHOP.getCode());
        String id = customerB2cService.addAndSubmit(customer);
        if (StringUtils.isNotBlank(id)) {
            CustomerB2cEntity customerB2c = customerB2cService.getById(id);
            if (Objects.nonNull(customerB2c)) {
                shop.setCustomerId(customerB2c.getId());
                shop.setCustomerCode(customerB2c.getCode());
                this.updateById(shop);
            }
        }
        BaseApproveParamDTO approveParamDTO = new BaseApproveParamDTO();
        approveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
        approveParamDTO.setIds(Arrays.asList(id));
        customerB2cService.approve(approveParamDTO);

    }

    /**
     * 检查店铺是否存在
     *
     * @param id
     * @param dictPlatform
     * @param account
     * @param dictAreaCode
     * @param dictCountryCodeList
     */
    private void checkIsExist(String id, String dictPlatform, String account, String dictAreaCode, List<String> dictCountryCodeList) {
        List<ShopInfoEntity> shopInfoList = this.lambdaQuery().ne(StringUtils.isNotBlank(id), ShopInfoEntity::getId, id).
                eq(ShopInfoEntity::getDictPlatform, dictPlatform).
                eq(ShopInfoEntity::getAccount, account).
                eq(StringUtils.isNotBlank(dictAreaCode), ShopInfoEntity::getDictAreaCode, dictAreaCode).
                in(CollectionUtils.isNotEmpty(dictCountryCodeList), ShopInfoEntity::getDictCountryCode, dictCountryCodeList).
                list();
        if (CollectionUtils.isNotEmpty(shopInfoList)) {
            if (StringUtils.isBlank(dictAreaCode)) {
                throw new ServiceException(ApiError.ERROR_SHOP_EXIST, dictPlatform, account);
            } else {
                String countryName = shopInfoList.stream().map(ShopInfoEntity::getCountryName).distinct().
                        collect(Collectors.joining(","));
                throw new ServiceException(ApiError.ERROR_SHOP_COUNTRY_EXIST, dictPlatform, account, countryName);
            }
        }

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
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryCodeList);
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
            String countryName = countryList.stream().filter(c -> c.getId().equals(countryCode)).findFirst().
                    map(DictCountryEntity::getNameCn).orElse("");
            String shopName = name.concat(countryName);
            if (StringUtils.isNotBlank(countryName)) {
                ShopInfoEntity shop = new ShopInfoEntity();
                BeanMapper.copy(dto, shop);
                shop.setDictCountryCode(countryCode);
                shop.setName(shopName);
                shop.setCountryName(countryName);
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
        String key = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicEntity> dictList = dictBasicService.getByKeyList(Arrays.asList(key));
        for (ShopDTO.PagingViewDTO item : list) {
            //平台
            String dictPlatform = item.getDictPlatform();
            String platformName = dictList.stream().filter(d -> d.getValue().equals(dictPlatform)).
                    findFirst().map(DictBasicEntity::getName).orElse("");
            item.setPlatformName(platformName);
            item.setAreaName(item.getDictAreaCode());
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
            // 禁用启用任务
            dmpTaskFeign.disabledPlatformTask(new PlatformTaskDTO.DisabledDTO(shop.getId(), shop.getDictPlatform(), disabled));
            return BatchResultDTO.success(shop.getId(), shop.getName(), OperationTypeEnum.DISABLED);
        }
        return BatchResultDTO.fail(shop.getId(), shop.getName(), "店铺不存在");

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
        //平台
        String dictPlatform = view.getDictPlatform();
        String dictType = DictBasicTypeEnum.PLATFORM.getType();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(dictType, dictPlatform);
        String platformName = Objects.nonNull(dictBasic) ? dictBasic.getName() : "";
        view.setAreaName(shop.getDictAreaCode());
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(String code, String hmac, String host, String shop, String timestamp) {
        String bodyStr = "";
        try {
            // 二级域名
            String secondDomain = shop;
            if (shop.contains(ShopifyConstant.DOMAIN)) {
                secondDomain = shop.replace(ShopifyConstant.DOMAIN, "");
            }
            ShopInfoEntity shopInfo = this.getByDomain(secondDomain);
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
            shopInfo.setAuthTime(LocalDateTime.now());
            shopAuthService.saveOrUpdate(shopAuth);
            boolean result = this.updateById(shopInfo);
            // 授权后添加任务
            dmpTaskFeign.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.TRUE);
            updateShopInfoById(shopInfo);
            // 添加到缓存redis
            ShopifyShopInfoDTO shopInfoDTO = initShopInfoDTO(shopInfo, accessToken);
            // platform-token:平台名称:店铺ID
            String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopId);
            redisUtil.set(tokenKey, shopInfoDTO);
            return result;
        } catch (Exception e) {
            log.error("店铺授权出错了===> bodyStr==>{} e==>{}", bodyStr, e);
        }

        return Boolean.FALSE;
    }

    /**
     * shopInfo Entity 转换DTO
     */
    private ShopifyShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, String accessToken) {
        return new ShopifyShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(accessToken)
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 店铺全域名: SHOP_NAME.myshopify.com
                .setShopDomain(shopInfo.getDomain().concat(ShopifyConstant.DOMAIN));
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
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
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
            // 删除授权
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            updateShopInfoById(shopInfo);
        }
        // 移除缓存
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPIFY.getCode(), shopInfo.getId());
        Object shopInfoObj = redisUtil.get(tokenKey);
        if (null != shopInfoObj) {
            redisUtil.del(tokenKey);
        }
        return result;
    }

    @Override
    public String getShopifyAuthorizeUrl(String hmac, String host, String shop, String timestamp) {
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        String params = "host=" + host + "&shop=" + shop + "&timestamp=" + timestamp;
//        Boolean checkResult = shopSdkServer.verifyShop(params, hmac, shop, cfgAppClient.getClientSecret());
//        if (!checkResult) {
//            throw new ServiceException("店铺授权检验未通过");
//        }
//        String grantOptions = "per-user";
        // 离线模式：token无过期
        String grantOptions = "offline-access";
        String path = String.format(cfgAppClient.getUrl(), shop, cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
        return path;
    }

    @Override
    public List<ShopDTO.ListTreeDTO> listTree() {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<String> platformList = list.stream().map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = this.listByPlatformList(platformList);

        List<ShopDTO.ListTreeDTO> resultList = new ArrayList<>();
        for (DictBasicDTO.ViewDTO viewDTO : list) {
            //平台信息
            ShopDTO.ListTreeDTO listTreeDTO = new ShopDTO.ListTreeDTO();
            listTreeDTO.setId(viewDTO.getId());
            listTreeDTO.setName(viewDTO.getName());

            //店铺信息
            List<ShopInfoEntity> shopList = shopInfoList.stream().filter(obj -> obj.getDictPlatform().equals(viewDTO.getValue())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(shopList)) {
                resultList.add(listTreeDTO);
                continue;
            }
            List<ShopDTO.ListChildTreeDTO> listChildList = new ArrayList<>();
            for (ShopInfoEntity shopInfoEntity : shopList) {
                ShopDTO.ListChildTreeDTO listChildTreeDTO = new ShopDTO.ListChildTreeDTO();
                listChildTreeDTO.setId(shopInfoEntity.getId());
                listChildTreeDTO.setName(shopInfoEntity.getName());
                listChildTreeDTO.setDisabled(shopInfoEntity.getDisabled());
                listChildList.add(listChildTreeDTO);
            }
            listTreeDTO.setListChildList(listChildList);
            resultList.add(listTreeDTO);
        }
        return resultList;
    }


    /**
     * 检查店铺是否授权
     *
     * @param id
     * @return
     */
    @Override
    public Boolean checkShopIsAuthorize(String id) {
        ShopInfoEntity shopInfo = this.getById(id);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        String authStatus = shopInfo.getAuthStatus();
        return AuthStatusEnum.ALREADY.getCode().equals(authStatus);
    }

    @Override
    public List<ShopInfoEntity> listAuth(ShopDTO.PlatformDTO platformDTO) {
        LoginUser userInfo = commonService.getUserInfo();
        List<ShopSysUserAuthDTO.ViewDTO> shopSysUserAuthList = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
        if (CollectionUtils.isEmpty(shopSysUserAuthList)) {
            return Collections.EMPTY_LIST;
        }
        ShopSysUserAuthDTO.ViewDTO viewDTO = shopSysUserAuthList.get(0);
        List<String> shopIdList ;
        if (StringUtils.isNotBlank(platformDTO.getDictPlatform())) {
            shopIdList = viewDTO.getDetailList().stream().filter(obj -> obj.getDictPlatform().equals(platformDTO.getDictPlatform())).map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
        } else {
            shopIdList = viewDTO.getDetailList().stream().map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
        }

        return this.listByIds(shopIdList);
    }

    /**
     * @param platformList
     * @return List<ShopInfoEntity>
     * @description: 根据平台集合查询
     * @author Will
     * @date: 2023/9/7 16:39
     */
    private List<ShopInfoEntity> listByPlatformList(List<String> platformList) {
        if (CollectionUtils.isEmpty(platformList)) {
            return Collections.EMPTY_LIST;
        }
        List<ShopInfoEntity> list = lambdaQuery().in(ShopInfoEntity::getDictPlatform, platformList).list();
        return list;
    }

    @Override
    public List<String> accountList() {
        return lambdaQuery().select(ShopInfoEntity::getAccount).
                groupBy(ShopInfoEntity::getAccount).list().stream().map(ShopInfoEntity::getAccount).collect(Collectors.toList());
    }


    /**
     * 获取到shopfily安装的url
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2023-09-06 16:34
     */
    @Override
    public String getShopifyInstallUrl(String id) {
        ShopInfoEntity shopInfo = this.getById(id);
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        // 全域名：SHOP_NAME.myshopify.com
        String fullDomain = shopInfo.getDomain().concat(ShopifyConstant.DOMAIN);
//        String grantOptions = "per-user";
        // 离线模式：token无过期
        String grantOptions = "offline-access";
        String path = String.format(cfgAppClient.getUrl(), fullDomain, cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
        return path;

    }


}
