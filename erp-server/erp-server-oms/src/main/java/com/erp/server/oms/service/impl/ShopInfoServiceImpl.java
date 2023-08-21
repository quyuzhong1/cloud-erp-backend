package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.PlatformDictEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.ShopInfoMapper;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SysDictFeign sysDictFeign;


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
        }

        BeanMapper.copy(dto, shop);
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
        for (String countryId : countryIdList) {
            String countryName = countryList.stream().filter(c -> c.getId().equals(countryId)).findFirst().
                    map(DictCountryEntity::getNameCn).orElse("");
            if (StringUtils.isNotBlank(countryName)) {
                ShopInfoEntity shop = new ShopInfoEntity();
                BeanMapper.copy(dto, shop);
                shop.setDictCountryId(countryId);
                shop.setName(name + countryName);
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
//        shopInfo.setCustomerCode(dto.getCustomerCode());
//        shopInfo.setShopCode(dto.getShopCode());
//        shopInfo.setPlatformDict(dto.getPlatformDict());
//        shopInfo.setName(dto.getName());
        Boolean result = this.updateById(shopInfo);
        if (result) {
            return shopInfo.getId();
        }
        return "";
    }


    /**
     * 初始同步店铺信息
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-06 12:23
     */
    @Override
    public Boolean initialSync() {
        //获取到dmp 店铺
        List<DmpShopInfoEntity> dmpShopList = dmpTaskFeign.listShop();
        List<String> kingdeeCustomerIds = dmpShopList.stream().map(DmpShopInfoEntity::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = customerInfoService.listByKingdeeIdList(kingdeeCustomerIds);
        List<ShopInfoEntity> shopInfoList = this.list();
        for (ShopInfoEntity item : shopInfoList) {
            // String shopCode = item.getShopCode();
            DmpShopInfoEntity dmpShop = dmpShopList.stream().filter(d -> d.getPlatformShopNo().equals("")).
                    findFirst().orElse(null);
            //表示是没有
            if (Objects.isNull(dmpShop)) {
                continue;
            }
            String customerInfoCode = customerInfoList.stream().filter(c -> StringUtils.isNotBlank(dmpShop.getCustomerId()) && c.getSyncKingdeeId().equals(dmpShop.getCustomerId())).
                    findFirst().map(CustomerInfoEntity::getCode).orElse("");
            item.setCustomerCode(customerInfoCode);

        }
        return this.updateBatchById(shopInfoList);
    }

    /**
     * 店铺分页
     * @param dto
     * @return
     */
    @Override
    public PagingVO<ShopDTO.PagingViewDTO> paging(PagingDTO<ShopDTO.PagingParamDTO> dto) {
        ShopDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);


        return null;
    }

}
