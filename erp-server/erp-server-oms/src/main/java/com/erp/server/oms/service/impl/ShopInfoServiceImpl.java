package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.DictBasicValueEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.*;
import com.sdk.oms.shopee.dto.base.ShopeeAuth;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.dto.base.request.AuthRequest;
import com.sdk.oms.shopee.dto.merchant.request.MerchantRequest;
import com.sdk.oms.shopee.dto.merchant.response.MerchantResponse;
import com.sdk.oms.shopee.dto.shop.request.ShopRequest;
import com.sdk.oms.shopee.dto.shop.response.ShopResponse;
import com.sdk.oms.shopee.service.ShopeeAuthService;
import com.sdk.oms.shopee.service.ShopeeMerchantService;
import com.sdk.oms.shopee.service.ShopeeShopService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.sdk.oms.shopify.utils.HmacVerificationUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    private CustomerInfoService customerInfoService;

    @Resource
    private ShopeeShopService shopeeShopService;

    @Resource
    private ShopeeMerchantService shopeeMerchantService;

    @Resource
    private CommonService commonService;

    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

    @Resource
    private ShopeeAuthService shopeeAuthService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

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
    public List<ShopInfoEntity> add(ShopDTO.AddDTO dto) {
        ShopInfoEntity shop = new ShopInfoEntity();
        String dictPlatform = dto.getDictPlatform();
        //亚马逊
        PlatformDictEnum amazon = PlatformDictEnum.AMAZON;
        //shopify
        PlatformDictEnum shopify = PlatformDictEnum.SHOPIFY;
        //检查店铺是否存在
        checkIsExist("", dto.getDictPlatform(), dto.getAccount(), dto.getDictAreaCode(), dto.getDictCountryCodeList());
        //检测仓库
        checkWarehouseExist(dto.getIsHaveWarehouse(), dto.getWarehouseId());
        //如果是亚马逊
        if (amazon.getCode().equals(dictPlatform)) {
            return this.handleAmazonShop(dto);
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
        String warehouseId = dto.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            shop.setWarehouseName(updateDTO.getName());
            shop.setWarehouseId(dto.getWarehouseId());
        }
        Boolean result = this.save(shop);
        return Collections.singletonList(shop);

    }

    private void checkWarehouseExist(Boolean isHaveWarehouse, String warehouseId) {
        if (Objects.nonNull(isHaveWarehouse) && isHaveWarehouse) {
            if (StringUtils.isBlank(warehouseId)) {
                throw new ServiceException(ApiError.ERROR_99001);
            }
        }
    }

    /**
     * 店铺保存成功后 自动创建客户
     *
     * @param shopId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String autoCreateShopCustomer(String shopId) {
        ShopInfoEntity shop = this.getById(shopId);
        if (Objects.isNull(shop)) {
            return "";
        }
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
        String id = customerInfoService.add(customer);
        if (StringUtils.isNotBlank(id)) {
            CustomerInfoEntity customerB2b = customerInfoService.getById(id);
            if (Objects.nonNull(customerB2b)) {
                shop.setCustomerId(customerB2b.getId());
                shop.setCustomerCode(customerB2b.getCode());
                this.updateById(shop);
            }
        }
        return id;


    }

    @Override
    public String getShopifyAuthorizeUrl(ShopifyAuthorizeUrlDTO dto) {
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = CfgAppClientDTO.FindDTO.init(appClient);
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (null == cfgAppClient){
            throw new ServiceException("Shopify系统配置缺失");
        }
        Map<String, String> paramsMap = BeanUtil.beanToMap(dto)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        // 校验
        boolean verifyResult = HmacVerificationUtils.verifyQueryString(cfgAppClient.getClientSecret(), new TreeMap<>(paramsMap));
        if (!verifyResult){
            throw new ServiceException("Verify Params Error");
        }
        // 生成跳转地址
        String grantOptions = "offline-access";
        return String.format(cfgAppClient.getUrl(), dto.getShop(), cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShopInfoEntity> handleAmazonShop(ShopDTO.AddDTO dto) {
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
        String warehouseId = dto.getWarehouseId();
        String warehouseName = "";
        if (StringUtils.isNotBlank(warehouseId)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            warehouseName = updateDTO.getName();
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));

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
                shop.setWarehouseName(warehouseName);
                addList.add(shop);
            }

        }

        if (CollectionUtils.isNotEmpty(addList)) {
            if (!this.saveBatch(addList)) {
                throw new ServiceException("批量保存失败");
            }
        }
        return addList;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public ShopDTO.RedirectDTO updateAndAuth(ShopDTO.UpdateDTO dto) {
        ShopInfoEntity entity = this.updateShop(dto);
        ShopAuthorizeUrlDTO authorizeUrlDTO = new ShopAuthorizeUrlDTO();
        authorizeUrlDTO.setShopId(entity.getId());
        authorizeUrlDTO.setPlatformCode(entity.getDictPlatform());

        String shopAuthorizeUrl = this.getShopAuthorizeUrl(authorizeUrlDTO);
        return new ShopDTO.RedirectDTO(entity.getId(), shopAuthorizeUrl);
    }

    @Override
    public ShopInfoEntity getRelatedShopByIdAndCountry(ShopInfoDTO.RelatedDTO relateDTO) {
        ShopInfoEntity shopInfo = getById(relateDTO.getShopId());
        if (null == shopInfo) {
            return null;
        }
        String platformShopCode = shopInfo.getPlatformShopCode();
        if (StringUtils.isBlank(platformShopCode)) {
            return null;
        }
        return lambdaQuery()
                .eq(ShopInfoEntity::getPlatformShopCode, platformShopCode)
                .eq(ShopInfoEntity::getDictCountryCode, relateDTO.getCountry())
                .last("LIMIT 1")
                .one();
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public ShopInfoEntity updateShop(ShopDTO.UpdateDTO dto) {
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
        //检测仓库
        checkWarehouseExist(dto.getIsHaveWarehouse(), dto.getWarehouseId());
        shopInfo.setChargeName(chargeName);
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
        String orgName = CollectionUtils.isNotEmpty(orgList) ? orgList.get(0).getName() : "";
        shopInfo.setSalesOrgId(dto.getSalesOrgId());
        shopInfo.setSalesOrgName(orgName);
        shopInfo.setChargeId(dto.getChargeId());
        shopInfo.setIossTaxNo(dto.getIossTaxNo());
        String warehouseId = dto.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            shopInfo.setWarehouseName(updateDTO.getName());
            shopInfo.setWarehouseId(dto.getWarehouseId());
        }

        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(dto.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        shopInfo.setCustomerId(customerInfoEntity.getId());
        shopInfo.setCustomerCode(customerInfoEntity.getCode());
        Boolean result = this.updateById(shopInfo);
        if (!result) {
            throw new ServiceException("更新失败");
        }
        return shopInfo;
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

        //客户名称
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(shop.getCustomerId());
        if (ObjectUtil.isNotEmpty(customerInfoEntity)) {
            view.setCustomerName(customerInfoEntity.getName());
        }
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
     * @param dto
     * @return
     */
    @Override
//    @GlobalTransactional(rollbackFor = Exception.class)
//    @Transactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        return AuthSaveHandler.shopAuthorize(dto.checkAndSetPlatform());
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
    @Override
    public ShopInfoEntity getByDomain(String shopDomain) {
        return this.lambdaQuery().eq(ShopInfoEntity::getDomain, shopDomain).last("LIMIT 1").one();
    }


    /**
     * 取消授权
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-29 16:41
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        return AuthSaveHandler.cleanShopAuthorize(dto);
    }

    @Override
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        return AuthSaveHandler.getShopAuthorizeUrl(dto);
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
        List<String> shopIdList;
        if (StringUtils.isNotBlank(platformDTO.getDictPlatform())) {
            shopIdList = viewDTO.getDetailList().stream().filter(obj -> obj.getDictPlatform().equals(platformDTO.getDictPlatform())).map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
        } else {
            shopIdList = viewDTO.getDetailList().stream().map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(shopIdList)) {
            return Collections.EMPTY_LIST;
        }
        return this.listByIds(shopIdList);
    }

    @Override
    public Boolean getShopeeReturn(ShopAuthDTO.ReturnDTO dto) {
        if (StringUtils.isEmpty(dto.getId())) {
            throw new ServiceException("虾皮授权时,ERP店铺ID不能为空");
        }
        ShopInfoEntity mainShopInfo = null;
        if (Objects.nonNull(dto.getMainAccountId())) {
            //主账号存在时，优先授权主装好记录
            ShopAuthEntity shopAuthEntity = shopAuthService.getShopeeShopById(String.valueOf(dto.getMainAccountId()));
            if (Objects.nonNull(shopAuthEntity) && StringUtils.isNotBlank(shopAuthEntity.getShopId())) {
                mainShopInfo = this.getById(shopAuthEntity.getShopId());
            }
        }
        if (Objects.isNull(mainShopInfo)) {
            mainShopInfo = this.getById(dto.getId());
        }
        if (Objects.isNull(mainShopInfo)) {
            throw new ServiceException("店铺记录id不存在:" + dto.getId());
        } else {
            //纠正店铺id
            dto.setId(mainShopInfo.getId());
        }
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                throw new ServiceException("虾皮基础配置未找到");
            }
        } catch (Exception e) {
            throw new ServiceException("erp-dmp服务调用异常");
        }
        //根据店铺授权还是主账号授权进行分开记录授权
        if (Objects.nonNull(dto.getShopId())) {
            AuthRequest authRequest = AuthRequest.builder()
                    .host(cfgAppClient.getUrl())
                    .code(dto.getCode())
                    .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                    .tmpPartnerKey(cfgAppClient.getClientSecret())
                    .shopId(dto.getShopId())
                    .build();
            ShopeeAuth shopeeAuth = shopeeAuthService.getShopAccountToken(authRequest);
            if (StringUtils.isNotEmpty(shopeeAuth.getError())) {
                throw new ServiceException("获取授权失败:" + shopeeAuth.getMessage());
            }
            updateShopeeToken(dto, shopeeAuth, cfgAppClient, AuthTypeEnum.SHOP.getCode());
        } else if (Objects.nonNull(dto.getMainAccountId())) {
            //获取主账户token 需要刷新子商铺的refresh_token
            AuthRequest authRequest = AuthRequest.builder()
                    .host(cfgAppClient.getUrl())
                    .code(dto.getCode())
                    .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                    .tmpPartnerKey(cfgAppClient.getClientSecret())
                    .mainAccountId(dto.getMainAccountId())
                    .build();
            ShopeeAuth shopeeAuth = shopeeAuthService.getMainAccountToken(authRequest);
            if (StringUtils.isNotEmpty(shopeeAuth.getError())) {
                throw new ServiceException("获取授权失败:" + shopeeAuth.getMessage());
            }
            updateShopeeToken(dto, shopeeAuth, cfgAppClient, AuthTypeEnum.MAIN.getCode());

        }
        return Boolean.TRUE;
    }

    private Boolean updateShopeeToken(ShopAuthDTO.ReturnDTO dto, ShopeeAuth shopeeAuth, CfgAppClientEntity cfgAppClient, String type) {
        Boolean flag = Boolean.FALSE;
        ShopInfoEntity shopInfo = this.getById(dto.getId());
        if (Objects.isNull(shopInfo)) {
            return flag;
        }
        String shopeeId = null;
        if (Objects.nonNull(dto.getShopId())) {
            shopeeId = String.valueOf(dto.getShopId());
        } else {
            shopeeId = String.valueOf(dto.getMainAccountId());
        }
        ShopAuthEntity shopAuth = shopAuthService.getShopeeShopById(String.valueOf(shopeeId));
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
            shopAuth.setShopId(dto.getId());
        }
        long partnerId = Long.parseLong(cfgAppClient.getClientId());
        shopAuth.setShopId(dto.getId());
        String refreshToken = shopeeAuth.getRefreshToken();
        String accessToken = shopeeAuth.getAccessToken();
        Long expireIn = shopeeAuth.getExpireIn();
        shopAuth.setType(type);
        if (AuthTypeEnum.MAIN.getCode().equals(type)) {
            shopInfo.setName("虾皮主店铺");
            //主账号授权
            if (Objects.nonNull(dto.getMainAccountId())) {
                shopAuth.setShopeeId(String.valueOf(dto.getMainAccountId()));
            }
        } else if (AuthTypeEnum.SHOP.getCode().equals(type)) {
            //店铺授权
            if (Objects.nonNull(dto.getShopId())) {
                shopAuth.setShopeeId(String.valueOf(dto.getShopId()));
            }
            //增加店铺名称获取
            this.getShopName(cfgAppClient, shopeeId, accessToken, shopInfo);
        } else {
            throw new ServiceException("授权异常");
        }
        shopAuth.setAccessToken(accessToken);
        shopAuth.setRefreshToken(refreshToken);
        shopAuth.setExpiresIn(Math.toIntExact(expireIn));
        shopAuth.setShopId(dto.getId());
        shopAuth.setAppClientId(cfgAppClient.getId());
        shopAuthService.saveOrUpdate(shopAuth);
        //增加店铺信息获取

        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        this.saveOrUpdate(shopInfo);
        //如果是主店铺
        if (AuthTypeEnum.MAIN.getCode().equals(type)) {
            List<Long> merchantIds = shopeeAuth.getMerchantIdList();
            if (CollectionUtils.isNotEmpty(merchantIds)) {
                for (Long merchantId : merchantIds) {
                    AuthRequest authRequest = AuthRequest.builder()
                            .host(cfgAppClient.getUrl())
                            .refreshToken(refreshToken)
                            .partnerId(partnerId)
                            .tmpPartnerKey(cfgAppClient.getClientSecret())
                            .merchantId(merchantId)
                            .build();
                    ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshMerchantToken(authRequest);
                    if (StringUtils.isEmpty(shopeeResponse.getError())) {
                        ShopInfoEntity shopInfo1 = new ShopInfoEntity();
                        //增加店铺名称获取
                        this.getMerchantName(cfgAppClient, String.valueOf(merchantId), shopeeResponse.getAccess_token(), shopInfo1);
                        shopInfo1.setAccount(shopInfo.getAccount());
                        //存在部分授权成功 部分失败可能
                        flag = saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.MERCHANT.getCode(), String.valueOf(merchantId), shopInfo1, cfgAppClient.getId());

                    }
                }
            }
            List<Long> shopIds = shopeeAuth.getShopIdList();
            if (CollectionUtils.isNotEmpty(shopIds)) {
                for (Long shopId : shopIds) {
                    AuthRequest authRequest = AuthRequest.builder()
                            .host(cfgAppClient.getUrl())
                            .refreshToken(refreshToken)
                            .partnerId(partnerId)
                            .tmpPartnerKey(cfgAppClient.getClientSecret())
                            .shopId(shopId)
                            .build();
                    ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshShopToken(authRequest);
                    if (StringUtils.isEmpty(shopeeResponse.getError())) {
                        ShopInfoEntity shopInfo1 = new ShopInfoEntity();
                        //增加店铺名称获取
                        this.getShopName(cfgAppClient, String.valueOf(shopId), shopeeResponse.getAccess_token(), shopInfo1);
                        shopInfo1.setAccount(shopInfo.getAccount());
                        //存在部分授权成功 部分失败可能
                        flag = saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.SHOP.getCode(), String.valueOf(shopId), shopInfo1, cfgAppClient.getId());

                    }
                }
            }
        }
        return flag;
    }

    private void getShopName(CfgAppClientEntity cfgAppClient, String shopeeId, String accessToken, ShopInfoEntity shopInfo) {
        //增加店铺名称获取
        ShopRequest shopRequest = ShopRequest.builder()
                .host(cfgAppClient.getUrl())
                .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                .tmpPartnerKey(cfgAppClient.getClientSecret())
                .shopId(Long.parseLong(shopeeId))
                .token(accessToken)
                .build();
        try {
            ShopResponse shopeeShopInfo = shopeeShopService.getShopInfo(shopRequest);
            if (StringUtils.isBlank(shopeeShopInfo.getError())) {
                shopInfo.setName(shopeeShopInfo.getShopName());
                shopInfo.setDictCountryCode(shopeeShopInfo.getRegion());
                List<DictCountryEntity> countryEntities = sysDictFeign.listCountryByIds(Collections.singletonList(shopeeShopInfo.getRegion()));
                if (CollectionUtils.isNotEmpty(countryEntities)) {
                    shopInfo.setCountryName(countryEntities.get(0).getNameCn());
                    shopInfo.setName(countryEntities.get(0).getNameCn() + " / " + shopInfo.getName());
                }
            }
        } catch (Exception e) {
            log.error("获取店铺名称异常：{}", e.getMessage());
        }
    }

    private void getMerchantName(CfgAppClientEntity cfgAppClient, String merchantId, String accessToken, ShopInfoEntity shopInfo) {
        //增加店铺名称获取
        MerchantRequest shopRequest = MerchantRequest.builder()
                .host(cfgAppClient.getUrl())
                .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                .tmpPartnerKey(cfgAppClient.getClientSecret())
                .merchantId(Long.parseLong(merchantId))
                .token(accessToken)
                .build();
        try {
            MerchantResponse shopeeShopInfo = shopeeMerchantService.getMerchantInfo(shopRequest);
            if (StringUtils.isBlank(shopeeShopInfo.getError())) {
                shopInfo.setName(shopeeShopInfo.getMerchantName());
                shopInfo.setDictCountryCode(shopeeShopInfo.getMerchantRegion());
                List<DictCountryEntity> countryEntities = sysDictFeign.listCountryByIds(Collections.singletonList(shopeeShopInfo.getMerchantRegion()));
                if (CollectionUtils.isNotEmpty(countryEntities)) {
                    shopInfo.setCountryName(countryEntities.get(0).getNameCn());
                    shopInfo.setName(countryEntities.get(0).getNameCn() + " / " + shopInfo.getName());
                }
            }
        } catch (Exception e) {
            log.error("获取店铺名称异常：{}", e.getMessage());
        }
    }

    /**
     * 新增 店铺和店主
     *
     * @param shopeeResponse
     * @param type
     * @param shopeeId
     */
    @Override
    public Boolean saveOrUpdateShopee(ShopeeTokenAuth shopeeResponse, String type, String shopeeId, ShopInfoEntity shopInfo, String cfClientId) {
        if (StringUtils.isNotEmpty(shopeeResponse.getError())) {
            log.error("授权异常：{}", shopeeResponse);
            return Boolean.FALSE;
        }
        String refreshToken = shopeeResponse.getRefresh_token();
        String accessToken = shopeeResponse.getAccess_token();
        Long expireIn = shopeeResponse.getExpire_in();
        ShopAuthEntity shopAuth = shopAuthService.getShopeeShopById(shopeeId);
        String shopId = null;
        //配置是否存在
        if (Objects.isNull(shopAuth)) {
            shopAuth = new ShopAuthEntity();
        } else {
            //配置存在时
            shopId = shopAuth.getShopId();
        }
        //店铺是否存在时
        ShopInfoEntity shopInfoEntity = new ShopInfoEntity();
        if (Objects.nonNull(shopId)) {
            shopInfoEntity = this.getById(shopId);
            if (Objects.isNull(shopInfoEntity)) {
                shopInfoEntity = new ShopInfoEntity();
            }
        }
        if (Objects.nonNull(shopInfo) && StringUtils.isNotEmpty(shopInfo.getName())) {
            shopInfoEntity.setName(shopInfo.getName());
        } else {
            shopInfoEntity.setName(shopeeId);
        }
        if (Objects.nonNull(shopInfo) && StringUtils.isNotEmpty(shopInfo.getDictCountryCode())) {
            shopInfoEntity.setDictCountryCode(shopInfo.getDictCountryCode());
        }
        if (Objects.nonNull(shopInfo) && StringUtils.isNotEmpty(shopInfo.getCountryName())) {
            shopInfoEntity.setCountryName(shopInfo.getCountryName());
        }
        if (Objects.nonNull(shopInfo) && StringUtils.isNotEmpty(shopInfo.getAccount())) {
            shopInfoEntity.setAccount(shopInfo.getAccount());
        }
        shopInfoEntity.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfoEntity.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
        //店铺
        this.saveOrUpdate(shopInfoEntity);
        shopId = shopInfoEntity.getId();
        shopAuth.setShopId(shopId);
        shopAuth.setType(type);
        shopAuth.setRefreshToken(refreshToken);
        shopAuth.setAccessToken(accessToken);
        shopAuth.setShopeeId(shopeeId);
        if (Objects.nonNull(expireIn)) {
            shopAuth.setExpiresIn(Math.toIntExact(expireIn));
        }
        shopAuth.setAppClientId(cfClientId);
        shopAuthService.saveOrUpdate(shopAuth);
        return Boolean.TRUE;
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
    public List<ShopInfoEntity> listShopByAmazon() {
        List<ShopInfoEntity> list = lambdaQuery().in(ShopInfoEntity::getDictPlatform, PlatformDictEnum.AMAZON.getCode()).list();
        return list;
    }

    @Override
    public List<String> accountList() {
        return lambdaQuery().select(ShopInfoEntity::getAccount).
                groupBy(ShopInfoEntity::getAccount).list().stream().map(ShopInfoEntity::getAccount).collect(Collectors.toList());
    }

    @Override
    public boolean checkExist(String dictCountryCode, String dictPlatform, String authStatus) {
        Integer count = lambdaQuery()
                .eq(StringUtils.isNotBlank(dictCountryCode), ShopInfoEntity::getDictCountryCode, dictCountryCode)
                .eq(StringUtils.isNotBlank(dictPlatform), ShopInfoEntity::getDictPlatform, dictPlatform)
                .eq(StringUtils.isNotBlank(authStatus), ShopInfoEntity::getAuthStatus, authStatus)
                .count();
        return count > 0;
    }

    @Override
    public List<ShopInfoEntity> listShopInfoByWarehouseIds(List<String> warehouseIds) {
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(ShopInfoEntity::getWarehouseId, warehouseIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopDTO.RedirectDTO addAndAuth(ShopDTO.AddDTO dto) {
        List<ShopInfoEntity> list = this.add(dto);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException("添加店铺失败");
        }
        ShopInfoEntity infoEntity = list.stream().findFirst().orElse(null);
        if (null == infoEntity) {
            throw new ServiceException("店铺为空");
        }
        List<String> shopIds = list.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
        ShopAuthorizeUrlDTO authorizeUrlDTO = new ShopAuthorizeUrlDTO();
        authorizeUrlDTO.setShopIdList(shopIds);
        authorizeUrlDTO.setPlatformCode(infoEntity.getDictPlatform());
        authorizeUrlDTO.setShopInfoEntityList(list);

        String shopAuthorizeUrl = this.getShopAuthorizeUrl(authorizeUrlDTO);
        return new ShopDTO.RedirectDTO(shopIds.get(0), shopAuthorizeUrl);
    }
}
