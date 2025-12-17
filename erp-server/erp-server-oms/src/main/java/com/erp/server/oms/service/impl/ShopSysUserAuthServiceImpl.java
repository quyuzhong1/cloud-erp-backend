package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.enums.AuthDataTypeEnum;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.ShopSysUserAuthMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.ShopSysUserAuthService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private AuthDataFeign authDataFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        //删除
        this.removeByUserIdList(userIdList);
        //新增
        if (CollectionUtils.isNotEmpty(addList)) {
            List<ShopSysUserAuthEntity> resultList = BeanMapperUtils.copyList(ShopSysUserAuthEntity.class, addList);
            this.saveBatch(resultList);
        }
        //更新用户管理的更新人和时间
        sysUserFeign.updateSysUserTime(userIdList);
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
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.ERROR_92053);
        }
        List<ShopSysUserAuthDTO.ViewShopDTO> detailList = new ArrayList<>();
        for (ShopSysUserAuthEntity entity : list) {
            //店铺
            ShopInfoEntity shopInfoEntity = shopList.stream().filter(obj -> obj.getId().equals(entity.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            //平台
            String dictPlatformName = dictList.stream().filter(obj -> obj.getValue().equals(shopInfoEntity.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            ShopSysUserAuthDTO.ViewShopDTO viewShopDTO = new ShopSysUserAuthDTO.ViewShopDTO();
            viewShopDTO.setShopId(entity.getShopId());
            viewShopDTO.setShopName(shopInfoEntity.getName());
            viewShopDTO.setDictPlatform(shopInfoEntity.getDictPlatform());
            viewShopDTO.setDictPlatformName(dictPlatformName);
            detailList.add(viewShopDTO);
        }
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    @Override
    public List<ShopSysUserAuthDTO.ViewDTO> listShopSysUserAuthByUserIdList(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.EMPTY_LIST;
        }
        //店铺权限设置信息
        List<SysUserDTO.ShopDTO> list = authDataFeign.listShopIdByUserIds(userIdList);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //所有店铺
        List<ShopInfoEntity> shopInfoList = shopInfoService.list();

        List<ShopSysUserAuthDTO.ViewDTO> resultList = new ArrayList<>();
        Map<String, List<SysUserDTO.ShopDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getUserId().concat(String.valueOf(obj.getAuthType()))));
        for (Map.Entry<String, List<SysUserDTO.ShopDTO>> entry :  map.entrySet()) {
            List<SysUserDTO.ShopDTO> value = entry.getValue();
            ShopSysUserAuthDTO.ViewDTO viewDTO = new ShopSysUserAuthDTO.ViewDTO();
            viewDTO.setUserId(value.get(0).getUserId());
            viewDTO.setAuthType(value.get(0).getAuthType());
            //店铺
            List<ShopSysUserAuthDTO.ViewShopDTO> detailList = new ArrayList<>();
            //全部指定则返回全部店铺信息
            if (ShopAuthTypeEnum.ENUM_ALL.getCode().equals(value.get(0).getAuthType())) {
                //全部指定
                if (CollectionUtils.isEmpty(shopInfoList)) {
                    continue;
                }
                for (ShopInfoEntity shopInfoEntity : shopInfoList) {
                    ShopSysUserAuthDTO.ViewShopDTO viewShopDTO = new ShopSysUserAuthDTO.ViewShopDTO();
                    viewShopDTO.setShopId(shopInfoEntity.getId());
                    viewShopDTO.setShopName(shopInfoEntity.getName());
                    viewShopDTO.setDictPlatform(shopInfoEntity.getDictPlatform());
                    viewShopDTO.setDisabled(shopInfoEntity.getDisabled());
                    viewShopDTO.setType(shopInfoEntity.getType());
                    detailList.add(viewShopDTO);
                }
            } else {
                //选择指定
                for (SysUserDTO.ShopDTO shopSysUserAuthEntity : value) {
                    //店铺信息
                    ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> obj.getId().equals(shopSysUserAuthEntity.getShopId())).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(shopInfoEntity)) {
                        continue;
                    }
                    ShopSysUserAuthDTO.ViewShopDTO viewShopDTO = new ShopSysUserAuthDTO.ViewShopDTO();
                    viewShopDTO.setShopId(shopSysUserAuthEntity.getShopId());
                    viewShopDTO.setShopName(shopInfoEntity.getName());
                    viewShopDTO.setDictPlatform(shopInfoEntity.getDictPlatform());
                    viewShopDTO.setDisabled(shopInfoEntity.getDisabled());
                    viewShopDTO.setType(shopInfoEntity.getType());
                    detailList.add(viewShopDTO);
                }
            }
            viewDTO.setDetailList(detailList);
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    public List<ShopSysUserAuthDTO.ViewShopDTO> listUserAuthShop(ShopSysUserAuthDTO.UserAuthShopParamDTO dto) {
        //店铺查询(默认有仓库权限的店铺)
        List<ShopSysUserAuthDTO.ViewShopDTO> viewShopDTOS = shopInfoService.listUserAuthShop(dto.getDictPlatform());
        //标识仓库权限
        List<SysUserDTO.WarehouseDTO> warehouseUserList = authDataFeign.getWarehouseUserList(dto.getUserId());
        if (CollUtil.isEmpty(warehouseUserList)){
            return viewShopDTOS;
        }
        SysUserDTO.WarehouseDTO warehouseDTO = warehouseUserList.stream().filter(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType())).findFirst().orElse(null);
        if (Objects.nonNull(warehouseDTO)){
            return viewShopDTOS;
        }
        List<String> warehouseIdList = warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).distinct().collect(Collectors.toList());
        viewShopDTOS.forEach(e -> {
            if (!warehouseIdList.contains(e.getWarehouseId())){
                e.setHasWarehouseAuth(false);
            }
        });
        return viewShopDTOS;
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

    /**
     * @description: 根据用户id集合查询权限设置数据
     * @author Will
     * @date: 2023/9/4 10:08
     * @param userIdList
     * @return List<ShopSysUserAuthEntity>
     */
    private List<ShopSysUserAuthEntity> listByUserIdList(List<String> userIdList) {
        return  lambdaQuery().in(ShopSysUserAuthEntity::getUserId,userIdList).list();
    }

    /**
     * 根据用户id集合删除
     */
    private  Boolean removeByUserIdList (List<String> userIdList) {
        return lambdaUpdate().in(ShopSysUserAuthEntity::getUserId,userIdList).remove();
    }
}
