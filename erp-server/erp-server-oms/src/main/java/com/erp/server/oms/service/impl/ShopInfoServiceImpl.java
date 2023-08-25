package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.oms.enums.PlatformDictEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
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
@Service
public class ShopInfoServiceImpl extends SuperServiceImpl<ShopInfoMapper, ShopInfoEntity> implements ShopInfoService {


    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DictBasicService dictBasicService;


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

        checkName("", dto.getName());
        if (shopify.getCode().equals(dictPlatform)) {
            if (StringUtils.isBlank(dto.getDomain())) {
                throw new ServiceException("域名不能为空");
            }
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
     * 处理
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-08-21 15:07
     */
    public Boolean handleAmazonShop(ShopDTO.AddDTO dto) {
        if (StringUtils.isBlank(dto.getDictAreaId())) {
            throw new ServiceException("区域不能为空");
        }
        if (CollectionUtils.isEmpty(dto.getDictCountryIdList())) {
            throw new ServiceException("国家不能为空");
        }
        //国家集合
        List<String> countryIdList = dto.getDictCountryIdList();
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
        List<ShopInfoEntity> addList = new ArrayList<>(countryIdList.size());
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
        for (String countryId : countryIdList) {
            String countryName = countryList.stream().filter(c -> c.getId().equals(countryId)).findFirst().
                    map(DictCountryEntity::getNameCn).orElse("");
            String shopName = name.concat(countryName);
            checkName("", shopName);
            if (StringUtils.isNotBlank(countryName)) {
                ShopInfoEntity shop = new ShopInfoEntity();
                BeanMapper.copy(dto, shop);
                shop.setDictCountryId(countryId);
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
        //区域的id
        List<String> areaIdList = list.stream().map(ShopDTO.PagingViewDTO::getDictAreaId).collect(Collectors.toList());
        //国家id
        List<String> countryIdList = list.stream().map(ShopDTO.PagingViewDTO::getDictCountryId).collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);
        List<DictGlobalAreaEntity> areaList = sysDictFeign.listGlobalAreaByIds(areaIdList);

        List<String> flagList = new ArrayList<>();
        for (ShopDTO.PagingViewDTO item : list) {
            //平台
            String dictPlatform = item.getDictPlatform();
            //账号
            String account = item.getAccount();
            //区域
            String dictAreaId = item.getDictAreaId();

            Boolean notContains = !flagList.contains(account);
            item.setDictPlatform(dictPlatform);
            String areaId = item.getDictAreaId();
            String areaName = areaList.stream().filter(a -> a.getId().equals(areaId)).
                    map(DictGlobalAreaEntity::getRegionName).findFirst().orElse("");
            item.setAreaName(areaName);
            String countryId = item.getDictCountryId();
            String countryName = countryList.stream().filter(a -> a.getId().equals(countryId)).
                    map(DictCountryEntity::getNameCn).findFirst().orElse("");
            item.setCountryName(countryName);
            Boolean disabled = item.getDisabled();
            String disabledName = disabled ? "禁用" : "启用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            item.setAuthStatusName(AuthStatusEnum.getName(authStatus));

            if (notContains) {
                item.setName("");
                item.setCountryName("");
                item.setDisabledName("");
                item.setAuthStatusName("");
                item.setCreateUserName("");
                item.setCreateTime(null);
                item.setAuthTime(null);
                item.setUpdateTime(null);
                item.setUpdateUserName("");
            }

            flagList.add(account);

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
        String areaId = StringUtils.isNotBlank(view.getDictAreaId()) ? view.getDictAreaId() : "";
        String countryId = StringUtils.isNotBlank(view.getDictCountryId()) ? view.getDictCountryId() : "";
        //平台
        String dictPlatform = view.getDictPlatform();
        String dictType = DictBasicEnum.PLATFORM.getType();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(dictType, dictPlatform);
        String platformName = Objects.nonNull(dictBasic) ? dictBasic.getName() : "";
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Arrays.asList(countryId));
        List<DictGlobalAreaEntity> areaList = sysDictFeign.listGlobalAreaByIds(Arrays.asList(areaId));

        String areaName = areaList.stream().filter(a -> a.getId().equals(areaId)).
                map(DictGlobalAreaEntity::getRegionName).findFirst().orElse("");
        view.setAreaName(areaName);
        String countryName = countryList.stream().filter(a -> a.getId().equals(countryId)).
                map(DictCountryEntity::getNameCn).findFirst().orElse("");
        view.setCountryName(countryName);
        view.setPlatformName(platformName);
        return view;
    }


}
