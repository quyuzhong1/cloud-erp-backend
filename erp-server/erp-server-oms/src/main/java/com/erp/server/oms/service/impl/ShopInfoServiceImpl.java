package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillCostFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.sdk.oms.amz.spapi.dto.AmazonTokenDTO;
import com.erp.server.oms.convert.ShopInfoConverter;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.sdk.oms.shopify.api.dto.AssociatedUserBean;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.sdk.oms.shopify.utils.HmacVerificationUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SHOP;

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

    private static final String CLIENT_SECRET = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";

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
    private LogisticsBillCostFeign logisticsBillCostFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private ShopeeShopService shopeeShopService;

    @Resource
    private ShopeeMerchantService shopeeMerchantService;


    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

    @Resource
    private ShopeeAuthService shopeeAuthService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

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
        //设置用户信息
        setCustom(dto.getCustomerId(), shop);
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
    public CustomerInfoEntity autoCreateShopCustomer(String shopId) {
        ShopInfoEntity shop = this.getById(shopId);
        if (Objects.isNull(shop)) {
            return null;
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
        KingdeeReceiptConditionEntity one = kingdeeReceiptConditionService.getOne(new LambdaQueryWrapper<KingdeeReceiptConditionEntity>().eq(KingdeeReceiptConditionEntity::getCode, DictBasicValueEnum.ONLINE_STORE_PAYMENT.getCode()));
        customer.setConditionDict(Objects.nonNull(one) ? one.getId() : "");
        customer.setSourceId(shop.getId());
        customer.setSourceType(SourceTypeEnum.SHOP.getCode());
        CustomerInfoEntity customerInfoEntity = customerInfoService.addOrGetCustom(customer);
        if (Objects.nonNull(customerInfoEntity)) {
            CustomerInfoEntity customerB2b = customerInfoService.getById(customerInfoEntity.getId());
            if (Objects.nonNull(customerB2b)) {
                shop.setCustomerId(customerB2b.getId());
                shop.setCustomerCode(customerB2b.getCode());
                this.updateById(shop);
            }
        }
        return customerInfoEntity;


    }

    @Override
    public String getShopifyAuthorizeUrl(ShopifyAuthorizeUrlDTO dto) {
        AppClientEnum appClient = AppClientEnum.SHOP_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = CfgAppClientDTO.FindDTO.init(appClient);
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (null == cfgAppClient) {
            throw new ServiceException("Shopify系统配置缺失");
        }
        Map<String, String> paramsMap = BeanUtil.beanToMap(dto)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        // 校验
        boolean verifyResult = HmacVerificationUtils.verifyQueryString(cfgAppClient.getClientSecret(), new TreeMap<>(paramsMap));
        if (!verifyResult) {
            throw new ServiceException("Verify Params Error");
        }
        // 生成跳转地址
        String grantOptions = "offline-access";
        return String.format(cfgAppClient.getUrl(), dto.getShop(), cfgAppClient.getClientId(), grantOptions, cfgAppClient.getRedirectUrl(), ShopifyConstant.SHOP_SCOPE);
    }

    @Override
    public AssociatedUserBean getShopifyShopByUserId(String id) {
        ShopInfoEntity entity = lambdaQuery().eq(ShopInfoEntity::getPlatformShopCode, id).last("LIMIT 1").one();
        if (ObjectUtil.isNotEmpty(entity)) {
            Map<String, Object> extendData = entity.getExtendData();
            AssociatedUserBean bean = BeanUtil.toBean(extendData, AssociatedUserBean.class);
            return bean;
        } else {
            return null;
        }
    }

    @Override
    public List<ShopInfoEntity> listByParams(ShopInfoDTO.ListParamDTO dto) {
        return lambdaQuery()
                .eq(ShopInfoEntity::getDictPlatform, dto.getDictPlatform())
                .eq(ShopInfoEntity::getAuthStatus, dto.getAuthStatus())
                .eq(ShopInfoEntity::getDisabled, false)
                .in(CollectionUtils.isNotEmpty(dto.getShopIdList()), ShopInfoEntity::getId, dto.getShopIdList())
                .list()
                ;
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
                eq(ShopInfoEntity::getType, ShopTypeEnum.OVERSEAS.getCode()).
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
        //旧负责人
        String oldChargeId = shopInfo.getChargeId();
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
        shopInfo.setVoecTaxNo(dto.getVoecTaxNo());
        String warehouseId = dto.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(dto.getWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            shopInfo.setWarehouseName(updateDTO.getName());
            shopInfo.setWarehouseId(dto.getWarehouseId());
        }
        shopInfo.setIsHaveWarehouse(dto.getIsHaveWarehouse());
        if (!dto.getIsHaveWarehouse()){
            shopInfo.setWarehouseName("");
            shopInfo.setWarehouseId("");
        }
        //设置用户信息
        setCustom(dto.getCustomerId(), shopInfo);
        Boolean result = this.updateById(shopInfo);
        if (!result) {
            throw new ServiceException("更新失败");
        }

        //负责人变更则更新物流单店铺负责人
        if (!StrUtil.equals(oldChargeId, dto.getChargeId())) {
            logisticsBillCostFeign.updateShopCharge(new LogisticsBillCostDTO.UpdateShopChargeDTO(shopInfo.getId(), dto.getChargeId()));
        }
        return shopInfo;
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
    public ShopInfoEntity updateInternalShop(ShopDTO.UpdateInternalDTO dto) {
        ShopInfoEntity shopInfo = this.getById(dto.getId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        //检查店铺是否存在
        checkInternalShopName(dto.getName(), dto.getId());
        //旧负责人
        String oldChargeId = shopInfo.getChargeId();
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
        String customerId = dto.getCustomerId();
        //设置用户信息
        setCustom(customerId, shopInfo);
        Boolean result = this.updateById(shopInfo);
        if (!result) {
            throw new ServiceException("更新失败");
        }

        //负责人变更则更新物流单店铺负责人
        if (!StrUtil.equals(oldChargeId, dto.getChargeId())) {
            logisticsBillCostFeign.updateShopCharge(new LogisticsBillCostDTO.UpdateShopChargeDTO(shopInfo.getId(), dto.getChargeId()));
        }
        return shopInfo;
    }

    /**
     * 设置用户信息
     * @param customerId
     * @param shopInfo
     */
    private void setCustom(String customerId, ShopInfoEntity shopInfo) {
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(customerId);
            if (ObjectUtil.isEmpty(customerInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92011);
            }
            ShopInfoEntity other = this.lambdaQuery().eq(ShopInfoEntity::getCustomerId,customerId).ne(StringUtils.isNotBlank(shopInfo.getId()),ShopInfoEntity::getId,shopInfo.getId()).last("limit 1").one();
            if(Objects.nonNull(other)){
                throw new ServiceException("【{}】已绑定店铺【{}】",customerInfoEntity.getName(),other.getName());
            }
            shopInfo.setCustomerId(customerInfoEntity.getId());
            shopInfo.setCustomerCode(customerInfoEntity.getCode());
        }else{
            shopInfo.setCustomerId("");
            shopInfo.setCustomerCode("");
        }
    }

    private void checkInternalShopName(String dto, String id) {
        List<ShopInfoEntity> shopInfoList = this.lambdaQuery().ne(StringUtils.isNotBlank(id), ShopInfoEntity::getId, id).
                eq(ShopInfoEntity::getName, dto).
                eq(ShopInfoEntity::getIsDeleted, false).
                eq(ShopInfoEntity::getType, ShopTypeEnum.INTERNAL.getCode()).
                list();
        if (CollectionUtils.isNotEmpty(shopInfoList)) {
            throw new ServiceException(ApiError.ERROR_97007);
        }
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
            if (PlatformDictEnum.TIK_TOK.getCode().equals(item.getDictPlatform())) {
                JSONObject jsonObject = JSONObject.parseObject(item.getExtendData());
                if (StringUtils.isBlank(jsonObject.getString("sellerType"))) {
                    continue;
                }
                item.setPlatformShopType(jsonObject.getString("sellerType"));
            }
            //客户名称
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(item.getCustomerId());
            if (Objects.nonNull(customerInfoEntity)) {
                item.setCustomerName(customerInfoEntity.getName());
            }
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(ShopInfoEntity shop, Boolean disabled) {
        if (Objects.nonNull(shop)) {
            //数据库的禁用状态
            Boolean dbDisabled = shop.getDisabled();
            if (dbDisabled.equals(disabled)) {
                throw new ServiceException("存在相同的状态");
            }
            shop.setDisabled(disabled);
            this.updateById(shop);
            if (!Objects.equals(ShopTypeEnum.INTERNAL.getCode(), shop.getType())) {
                // 禁用启用任务
                dmpTaskFeign.allAddOrUpdateTaskAndSchedule(new PlatformTaskDTO.DisabledDTO(shop.getId(),
                        shop.getName(),
                        shop.getDictPlatform(),
                        disabled,
                        shop.getDictCountryCode(),
                        shop.getPlatformShopCode()));
            }
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
                .set(StrUtil.isNotBlank(shopInfoEntity.getPlatformStatus()),ShopInfoEntity::getPlatformStatus, shopInfoEntity.getPlatformStatus())
                .set(Objects.nonNull(shopInfoEntity.getDisabled()), ShopInfoEntity::getDisabled, shopInfoEntity.getDisabled())
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
    public Boolean shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        return AuthSaveHandler.shopAuthorize(dto.checkAndSetPlatform(), response);
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
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
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(Math.toIntExact(expireIn));
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);
        shopAuth.setTokenExpireTime(tokenExpireTime);
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
        shopInfoEntity.setAuthTime(LocalDateTime.now());
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
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(Math.toIntExact(expireIn));
        //提前半小时设置token失效，以免失效了以后才刷新容易出错
        LocalDateTime tokenExpireTime = localDateTime.minusMinutes(30);
        shopAuth.setTokenExpireTime(tokenExpireTime);
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
    public List<ShopSysUserAuthDTO.ViewShopDTO> listShopByAmazonAuth() {
        ShopSysUserAuthDTO.UserAuthShopParamDTO dto = new ShopSysUserAuthDTO.UserAuthShopParamDTO();
        dto.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        return shopSysUserAuthService.listUserAuthShop(dto);
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
        for (ShopInfoEntity shop : list) {
            this.saveCustom(shop);
        }
        return new ShopDTO.RedirectDTO(shopIds.get(0), shopAuthorizeUrl);
    }

    @Override
    public void customersDataRequest(ShopifyWebhookDTO.CustomersDataRequestDTO dto, HttpServletResponse response, HttpServletRequest request) {
        // 使用 Jackson 序列化工具将 DTO 对象转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 处理请求体数据
        log.warn("Shopify ERP方法：customersDataRequest, dto: {}", requestBody);

        // 从请求头中获取HMAC
        String hmacHeader = request.getHeader("X-Shopify-Hmac-SHA256");
        String HttpHmacHeader = request.getHeader("HTTP_X_SHOPIFY_HMAC_SHA256");

        // 处理请求体数据
        log.warn("Shopify ERP方法：customersDataRequest, X-Shopify-Hmac-SHA256: {}", hmacHeader);
        log.warn("Shopify ERP方法：customersDataRequest, HTTP_X_SHOPIFY_HMAC_SHA256: {}", HttpHmacHeader);

        // 验证Webhook
        boolean verified = false;
        try {
            verified = verifyWebhook(requestBody, hmacHeader);
        } catch (Exception e) {
            log.warn("ERP方法：customersDataRequest, 加密入参计算hmac报错: {}", e.getMessage());
            e.printStackTrace();
        }

        // 如果验证失败，则返回HTTP 401错误
        if (!verified) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return;
    }

    @Override
    public void customersRedact(ShopifyWebhookDTO.CustomersRedactDTO dto, HttpServletResponse response, HttpServletRequest request) {
// 使用 Jackson 序列化工具将 DTO 对象转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 处理请求体数据
        log.warn("Shopify ERP方法：customersRedact, Request body: {}", requestBody);

        // 从请求头中获取HMAC
        String hmacHeader = request.getHeader("X-Shopify-Hmac-SHA256");
        String HttpHmacHeader = request.getHeader("HTTP_X_SHOPIFY_HMAC_SHA256");

        // 处理请求体数据
        log.warn("Shopify ERP方法：customersRedact, X-Shopify-Hmac-SHA256: {}", hmacHeader);
        log.warn("Shopify ERP方法：customersRedact, HTTP_X_SHOPIFY_HMAC_SHA256: {}", HttpHmacHeader);

        // 验证Webhook
        boolean verified = false;
        try {
            verified = verifyWebhook(requestBody, hmacHeader);
        } catch (Exception e) {
            log.warn("ERP方法：customersRedact, 加密入参计算hmac报错: {}", e.getMessage());
            e.printStackTrace();
        }

        // 如果验证失败，则返回HTTP 401错误
        if (!verified) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return;
    }

    @Override
    public void shopRedact(ShopifyWebhookDTO.ShopRedactDTO dto, HttpServletResponse response, HttpServletRequest request) {
        // 使用 Jackson 序列化工具将 DTO 对象转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 处理请求体数据
        log.warn("Shopify ERP方法：shopRedact, Request body: {}", requestBody);

        // 从请求头中获取HMAC
        String hmacHeader = request.getHeader("X-Shopify-Hmac-SHA256");
        String HttpHmacHeader = request.getHeader("HTTP_X_SHOPIFY_HMAC_SHA256");

        // 处理请求体数据
        log.warn("Shopify ERP方法：shopRedact, X-Shopify-Hmac-SHA256: {}", hmacHeader);
        log.warn("Shopify ERP方法：shopRedact, HTTP_X_SHOPIFY_HMAC_SHA256: {}", HttpHmacHeader);

        // 验证Webhook
        boolean verified = false;
        try {
            verified = verifyWebhook(requestBody, hmacHeader);
        } catch (Exception e) {
            log.warn("加密入参计算hmac报错: {}", e.getMessage());
            e.printStackTrace();
        }

        // 如果验证失败，则返回HTTP 401错误
        if (!verified) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return;
    }

    @Override
    public ResponseEntity<String> shopRedactTest(String data, HttpServletResponse response, HttpServletRequest request) {
        log.warn("Shopify: shopRedactTest 方法 入参：{}", data);
        String hmacHeader = request.getHeader("X_SHOPIFY_HMAC_SHA256");
        boolean verified = verifyHmac(data, hmacHeader);

        if (verified) {
            // Process webhook payload
            return ResponseEntity.ok("Webhook verified and processed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @Override
    public List<ShopInfoEntity> getRelatedShopById(String platformShopCode) {
        return lambdaQuery()
                .eq(ShopInfoEntity::getPlatformShopCode, platformShopCode)
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .eq(ShopInfoEntity::getDisabled, false)
                .list();
    }
    /**
     * 远程搜索
     */
    @Override
    public PagingVO<ShopDTO.ListDTO> pagingSelect(PagingDTO<ShopDTO.SelectDTO> dto) {
        ShopDTO.SelectDTO params = dto.getParams();
        if (params.getShowByAuth()){
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            List<ShopSysUserAuthDTO.ViewDTO> shopSysUserAuthList = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
            if (CollectionUtils.isEmpty(shopSysUserAuthList)) {
                return new PagingVO<>();
            }

            ShopSysUserAuthDTO.ViewDTO viewDTO = shopSysUserAuthList.get(0);
            List<String> shopIdList;
            if (StringUtils.isNotBlank(params.getDictPlatform())) {
                shopIdList = viewDTO.getDetailList().stream().filter(obj -> obj.getDictPlatform().equals(params.getDictPlatform()))
                        .map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
            } else {
                shopIdList = viewDTO.getDetailList().stream().map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(shopIdList)) {
                return new PagingVO<>();
            }
            params.setShopIdList(shopIdList);
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ShopDTO.ListDTO> pagResult = baseMapper.pagingSelect(query, params);
        List<ShopDTO.ListDTO> records = pagResult.getRecords();
        //排序
        pagResult.setRecords(records);
        return new PagingVO<>(pagResult);
    }

    @Override
    public PagingVO<ShopDTO.PagingViewDTO> exportShop(PagingDTO<ShopDTO.ExportDTO> dto) {
        Page<ShopDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            //填充数据
            fillDb(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<ShopDTO.AreaDTO> pagingSelectArea(PagingDTO<ShopDTO.AreaParamDTO> dto) {
        ShopDTO.AreaParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ShopDTO.AreaDTO> pagResult = baseMapper.pagingSelectArea(query, params);
        return new PagingVO<>(pagResult);
    }

    @Override
    public List<ShopDTO.ListDTO> listSelect(ShopDTO.SelectDTO dto) {
        PagingDTO<ShopDTO.SelectDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<ShopDTO.ListDTO> resultList = this.pagingSelect(pagingParamDTO);
        List<ShopDTO.ListDTO> list = (List<ShopDTO.ListDTO>) resultList.getList();
        return list;
    }

    @Override
    public List<String> listShopInfoByPlatform(String platform) {
        List<ShopInfoEntity> list = list(Wrappers.<ShopInfoEntity>lambdaQuery()
                .eq(ShopInfoEntity::getDictPlatform, platform)
                .eq(ShopInfoEntity::getDisabled, false));
        return list.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> deleteByIds(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ShopInfoEntity> shopInfoEntities = listByIds(ids);
        for (String id : ids) {
            BatchResultDTO deleteResult = new BatchResultDTO();
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(item -> Objects.equals(item.getId(), id)).findFirst().orElse(null);
            if (Objects.isNull(shopInfoEntity)) {
                deleteResult = BatchResultDTO.fail(id, id, "店铺不存在, 删除失败");
                resultDTOS.add(deleteResult);
                continue;
            }
            //只有禁用的店铺允许删除
            if (Objects.equals(shopInfoEntity.getDisabled(), false)) {
                deleteResult = BatchResultDTO.fail(id, shopInfoEntity.getAccount(), ApiError.ERROR_SHOP_UNDISABLED.msg);
                resultDTOS.add(deleteResult);
                continue;
            }
            try {
                boolean flag = removeById(id);
                if (flag) {
                    deleteResult = BatchResultDTO.success(shopInfoEntity.getId(), shopInfoEntity.getAccount(), OperationTypeEnum.DELETE);
                } else {
                    deleteResult = BatchResultDTO.fail(shopInfoEntity.getId(), shopInfoEntity.getAccount(), "店铺删除失败");
                }
            } catch (Exception e) {
                log.error("店铺删除失败", e);
                deleteResult = BatchResultDTO.fail(shopInfoEntity.getId(), shopInfoEntity.getAccount(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS;
    }

    /**
     * 导出
     *
     * @param dto
     * @author hyj
     * @date 2024/5/23 10:54
     */
    @Override
    public void listExport(ShopDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("店铺导出", EXPORT_OMS_SHOP.getCode(),dto);
    }

    private boolean verifyHmac(String data, String hmacHeader) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(CLIENT_SECRET.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] calculatedHmac = sha256Hmac.doFinal(data.getBytes());
            String calculatedHmacBase64 = Base64.getEncoder().encodeToString(calculatedHmac);

            log.warn("Shopify: shopRedactTest 方法 hmac解密：{}，  请求头：{}", calculatedHmacBase64, hmacHeader);

            return calculatedHmacBase64.equals(hmacHeader);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }


    // 验证Webhook
    private static boolean verifyWebhook(String data, String hmacHeader) throws NoSuchAlgorithmException, InvalidKeyException {
        // 使用HMAC-SHA256算法计算HMAC
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(CLIENT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        String calculatedHmac = Base64.getEncoder().encodeToString(hmacBytes);
        log.warn("Shopify  平台返回头hmac：{}, 入参加密计算hmac：{}", hmacHeader, calculatedHmac);
        // 安全比较计算得到的HMAC和请求头中的HMAC
        return calculatedHmac.equals(hmacHeader);
    }

    /**
     * 添加国内店铺
     *
     * @param dto
     * @return java.lang.String
     * @author hyj
     * @date 2024-05-23 16:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShopInfoEntity> addIntenal(ShopDTO.AddInternalDTO dto) {
        ShopInfoEntity shop = new ShopInfoEntity();
        //检查店铺是否存在
        checkInternalShopName(dto.getName(), "");
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
        //设置用户信息
        setCustom(dto.getCustomerId(), shop);
        shop.setType(ShopTypeEnum.INTERNAL.getCode());
        shop.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shop.setAuthTime(LocalDateTime.now());
        Boolean result = this.save(shop);
        return Collections.singletonList(shop);
    }


    @Override
    public List<BaseDropDownDTO.DisabledDTO> listShopSelect() {
        List<ShopInfoEntity> list = this.list();
        List<BaseDropDownDTO.DisabledDTO> resultList = ShopInfoConverter.INSTANCE.ShopInfoEntityToDisabledDTO(list);
        return resultList;
    }
    /**
     * 如果没有选客户，就进行绑定
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCustom(ShopInfoEntity shopInfoEntity) {
        if (StringUtils.isBlank(shopInfoEntity.getCustomerId())) {
            //店铺客户信息--如果存在则直接绑定原始的，不存在就创建并提交审核
            CustomerInfoEntity customerInfoEntity = this.autoCreateShopCustomer(shopInfoEntity.getId());
            if (Objects.nonNull(customerInfoEntity)) {
                ApproveStatusEnum approveStatus = customerInfoEntity.getApproveStatus();
                if (Objects.isNull( approveStatus)||!Objects.equals(ApproveStatusEnum.APPROVE.getStatus(), approveStatus.getStatus())) {
                    List<String> ids = Arrays.asList(customerInfoEntity.getId());
                    //提交
                    Boolean submitResult = customerInfoService.submit(ids);
                    if (submitResult) {
                        customerInfoEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
                        customerInfoService.approve(new BaseApproveParamDTO(ids, ApproveTypeEnum.PASS.getStatus(), "", Boolean.FALSE),customerInfoEntity);
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkAndSaveAllAmazonToken(AmazonTokenUpdateDTO updateDTO) {
        ShopInfoEntity shopInfo = updateDTO.getShopInfo();
        ShopAuthEntity shopAuth = updateDTO.getShopAuth();
        String accessToken = updateDTO.getAccessToken();
        String refreshToken = updateDTO.getRefreshToken();
        // 亚马逊关联的店铺列表
        List<ShopInfoEntity> entityList = getRelatedShopById(shopInfo.getPlatformShopCode());
        if (org.springframework.util.CollectionUtils.isEmpty(entityList)){
            // 更新当前店铺shopAuth
            shopAuth.setAccessToken(accessToken);
            shopAuth.setRefreshToken(refreshToken);
            shopAuthService.updateShopAuthById(shopAuth);
            return true;
        }
        List<String> shopIds = entityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<ShopAuthEntity> authList = shopAuthService.listShopAuthByShopIds(shopIds);
        if (org.springframework.util.CollectionUtils.isEmpty(authList)){
            // 更新当前店铺shopAuth
            shopAuth.setAccessToken(accessToken);
            shopAuth.setRefreshToken(refreshToken);
            shopAuthService.updateShopAuthById(shopAuth);
            return true;
        }
        // 批量更新
        authList.add(shopAuth);
        authList.forEach(e->{
            e.setAccessToken(accessToken);
            e.setRefreshToken(refreshToken);
        });
        shopAuthService.batchUpdateShopAuthById(authList);
        return true;
    }
}
