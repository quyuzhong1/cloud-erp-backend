package com.erp.server.oms.service.impl;


import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.server.oms.mapper.ShopSysUserAuthMapper;
import com.erp.server.oms.service.ShopSysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
        return null;
    }


}
