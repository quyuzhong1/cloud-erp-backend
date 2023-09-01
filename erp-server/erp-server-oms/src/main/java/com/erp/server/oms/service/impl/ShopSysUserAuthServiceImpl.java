package com.erp.server.oms.service.impl;


import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.server.oms.mapper.ShopSysUserAuthMapper;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.ShopSysUserAuthService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ShopSysUserAuthDTO.AddDTO addDTO) {
        ShopSysUserAuthEntity shopSysUserAuthEntity = new ShopSysUserAuthEntity();
        BeanMapperUtils.copy(addDTO, shopSysUserAuthEntity);

        // 数据处理
        handleData(shopSysUserAuthEntity);

        log.info("开始新增店铺权限设置单");
        boolean save = super.save(shopSysUserAuthEntity);
        if(!save) {
            throw new ServiceException("店铺权限设置单保存失败");
        }
        return shopSysUserAuthEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShopSysUserAuthDTO.UpdateDTO updateDTO) {
        ShopSysUserAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "店铺权限设置单"));
        ShopSysUserAuthEntity shopSysUserAuthEntity =  BeanMapperUtils.map(ShopSysUserAuthEntity.class, updateDTO);

        // 数据处理
        handleData(shopSysUserAuthEntity);
        log.info("编辑 开始修改店铺权限设置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shopSysUserAuthEntity);
        if(!save) {
            throw new ServiceException("店铺权限设置单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean batchAuth(ShopSysUserAuthDTO.BatchAuthDTO dto) {
        String authType = dto.getAuthType();
        List<String> userIdList = dto.getUserIdList();
        if (ShopAuthTypeEnum.ENUM_ALL.getCode().equals(authType)) {
            List<ShopSysUserAuthDTO.AddDTO> addList = userIdList.stream().map(obj -> new ShopSysUserAuthDTO.AddDTO("", obj, authType)).collect(Collectors.toList());
        }
        if (ShopAuthTypeEnum.ENUM_PART.getCode().equals(authType)) {
            if (CollectionUtils.isEmpty(dto.getShopIdList())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_USER_AUTH_PART);
            }
            List<ShopSysUserAuthDTO.AddDTO> addList = userIdList.stream().map(obj ->{
                for (String shopId : dto.getShopIdList()) {
                  return new ShopSysUserAuthDTO.AddDTO(shopId, obj, authType);
                }
                return null;
            }).collect(Collectors.toList());

        }

        return null;
    }



    /**
    * 新增修改处理数据
    */
    private void handleData(ShopSysUserAuthEntity shopSysUserAuthEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
