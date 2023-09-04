package com.erp.server.oms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.server.oms.mapper.ShopSysUserAuthMapper;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.ShopSysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 店铺权限设置表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-09-01
 */
@Slf4j
@Service
public class ShopSysUserAuthServiceImpl extends SuperServiceImpl<ShopSysUserAuthMapper, ShopSysUserAuthEntity> implements ShopSysUserAuthService {

    @Resource
    private ShopInfoService shopInfoService;

    @Override
    public Boolean batchAuth(ShopSysUserAuthDTO.BatchAuthDTO dto) {
        String authType = dto.getAuthType();
        List<String> userIdList = dto.getUserIdList();
        List<ShopSysUserAuthDTO.AddDTO> addList = new ArrayList<>();
        if (ShopAuthTypeEnum.ENUM_ALL.getCode().equals(authType)) {
           addList = userIdList.stream().map(obj -> new ShopSysUserAuthDTO.AddDTO("", obj, authType)).collect(Collectors.toList());
        }
        if (ShopAuthTypeEnum.ENUM_PART.getCode().equals(authType)) {
            if (CollectionUtils.isEmpty(dto.getShopIdList())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_USER_AUTH_PART);
            }
            for (String obj : userIdList) {
                List<ShopSysUserAuthDTO.AddDTO> list = dto.getShopIdList().stream().map(e -> new ShopSysUserAuthDTO.AddDTO(e, obj, authType)).collect(Collectors.toList());
                addList.addAll(list);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            List<ShopSysUserAuthEntity> resultList = BeanMapperUtils.copyList(ShopSysUserAuthEntity.class, addList);
            this.saveBatch(resultList);
        }
        return Boolean.TRUE;
    }

    @Override
    public ShopSysUserAuthDTO.ViewDTO view(ShopSysUserAuthDTO.ViewParamDTO dto) {
        String userId = dto.getUserId();
        List<ShopSysUserAuthEntity> list = this.listByUserId(userId);
        if (CollectionUtils.isEmpty(list)) {
            return new ShopSysUserAuthDTO.ViewDTO();
        }
        ShopSysUserAuthDTO.ViewDTO viewDTO = new ShopSysUserAuthDTO.ViewDTO();
        viewDTO.setUserId(userId);
        viewDTO.setAuthType(list.get(0).getAuthType());
        //全部指定则无需返回店铺信息
        if (ShopAuthTypeEnum.ENUM_ALL.getCode().equals(list.get(0).getAuthType())) {
            return viewDTO;
        }

        //店铺信息
        List<String> shopIdList = list.stream().map(ShopSysUserAuthEntity::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        //平台信息
        List<String> dictPlatformList = shopList.stream().map(ShopInfoEntity::getDictPlatform).collect(Collectors.toList());


        for (ShopSysUserAuthEntity entity : list) {
            //店铺
            ShopInfoEntity shopInfoEntity = shopList.stream().filter(obj -> obj.getId().equals(entity.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            ShopSysUserAuthDTO.ViewShopDTO viewShopDTO = new ShopSysUserAuthDTO.ViewShopDTO();
            viewShopDTO.setShopId(entity.getShopId());
            viewShopDTO.setShopName(shopInfoEntity.getName());
            viewShopDTO.setDictPlatform(shopInfoEntity.getDictPlatform());
        }

        return null;
    }

    /**
     * @description: 根据用户id查询权限设置数据
     * @author Will
     * @date: 2023/9/4 10:08
     * @param userId
     * @return List<ShopSysUserAuthEntity>
     */
    private List<ShopSysUserAuthEntity> listByUserId(String userId) {
        return  lambdaQuery().eq(ShopSysUserAuthEntity::getUserId,userId).list();
    }
}
