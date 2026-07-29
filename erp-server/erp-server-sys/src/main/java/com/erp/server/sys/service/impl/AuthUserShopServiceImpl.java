package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.SqlUtils;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.erp.model.sys.enums.AuthDataTypeEnum;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.AuthUserShopMapper;
import com.erp.server.sys.service.AuthUserShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户-店铺权限 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@Service
public class AuthUserShopServiceImpl extends SuperServiceImpl<AuthUserShopMapper, AuthUserShopEntity> implements AuthUserShopService {
    @Lazy
    @Resource
    private AuthUserShopService service;
    @Resource
    private RedisService redisService;

    @Override
    @Cacheable(value = "cache:sys:shopAuth:getShopUserList", key = "#userId")
    public List<SysUserDTO.ShopDTO> getShopUserList(String userId) {
        if (CharSequenceUtil.isBlank(userId)) {
            return Collections.emptyList();
        }
        return baseMapper.getShopUserList(userId, null);
    }

    @Override
    public String getShopPermissionSql(String shopTableField, String dynamicDataSource) {
        if (CharSequenceUtil.isBlank(shopTableField)) {
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        LoginUser defaultLoginUser = UserContext.getDefaultLoginUser();
        if ("0".equals(defaultLoginUser.getUid())) {
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        List<SysUserDTO.ShopDTO> shopUserList = service.getShopUserList(defaultLoginUser.getUid());
        if (CollUtil.isEmpty(shopUserList)) {
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        String authType = shopUserList.stream().map(SysUserDTO.ShopDTO::getAuthType).filter("all"::equals).findFirst().orElse("part");
        if ("all".equals(authType)) {
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        StringBuilder sqlString = new StringBuilder();
        //店铺
        List<String> shopTableFieldList = Arrays.asList(shopTableField.split(","));
        if ("part".equals(authType)) {
            List<String> shopIdList = shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).distinct().collect(Collectors.toList());
            SqlUtils.appendBlankOrInPermissionSql(sqlString, shopTableFieldList, shopIdList);
        }
        return sqlString.toString();
    }

    @Override
    public List<SysUserDTO.ShopDTO> listShopIdByUserIds(List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return Collections.emptyList();
        }
        List<String> userIds = userIdList.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SysUserDTO.ShopDTO> result = new ArrayList<>();
        List<String> missUserIds = new ArrayList<>();
        for (String userId : userIds) {
            String redisKey = String.format("cache:sys:shopAuth:getShopUserList::%s", userId);
            List<SysUserDTO.ShopDTO> cacheList = redisService.getCacheObject(redisKey);
            if (cacheList != null) {
                result.addAll(cacheList);
            } else {
                missUserIds.add(userId);
            }
        }
        if (CollectionUtils.isNotEmpty(missUserIds)) {
            List<SysUserDTO.ShopDTO> dbList = baseMapper.getShopUserList(null, missUserIds);
            Map<String, List<SysUserDTO.ShopDTO>> dbMap = dbList.stream()
                    .collect(Collectors.groupingBy(SysUserDTO.ShopDTO::getUserId));
            for (String userId : missUserIds) {
                List<SysUserDTO.ShopDTO> list = dbMap.getOrDefault(userId, new ArrayList<>());
                String redisKey = String.format("cache:sys:shopAuth:getShopUserList::%s", userId);
                redisService.setCacheObject(redisKey, list, 8L, TimeUnit.HOURS);
                result.addAll(list);
            }
        }
        return result;
    }

    @Override
    public List<String> listUserIdByShopIdList(List<String> shopIdList) {
        if (CollectionUtils.isEmpty(shopIdList)) {
            return Collections.emptyList();
        }
        List<AuthUserShopEntity> list = lambdaQuery().in(AuthUserShopEntity::getShopId, shopIdList)
                .or()
                .eq(AuthUserShopEntity::getAuthType, ShopAuthTypeEnum.ENUM_ALL.getCode())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(AuthUserShopEntity::getUserId).distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "cache:sys:shopAuth:getShopUserList", key = "#uid")
    public void batchSaveOrUpdate(String uid, List<String> shopIdList, String shopAuthType, boolean ifAdd) {
        if (CharSequenceUtil.isBlank(uid)) {
            return;
        }
        List<AuthUserShopEntity> oldList = this.lambdaQuery().eq(AuthUserShopEntity::getUserId, uid).list();
        if (AuthDataTypeEnum.ENUM_ALL.getCode().equals(shopAuthType)) {
            AuthUserShopEntity auth = oldList.stream().filter(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType())).findFirst().orElse(null);
            if (Objects.isNull(auth)) {
                //清空历史
                this.lambdaUpdate().eq(AuthUserShopEntity::getUserId, uid).remove();
                //新增全部授权
                AuthUserShopEntity entity = new AuthUserShopEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_ALL.getCode());
                entity.setUserId(uid);
                this.save(entity);
            }
        } else if (AuthDataTypeEnum.ENUM_PART.getCode().equals(shopAuthType)) {
            if (CollUtil.isNotEmpty(oldList)) {
                if (!ifAdd) {
                    List<String> deleteIdList = oldList.stream().filter(e -> !shopIdList.contains(e.getShopId()) || AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()))
                            .map(AuthUserShopEntity::getId).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(deleteIdList)) {
                        this.removeByIds(deleteIdList);
                    }
                }

                Set<String> existingIds = oldList.stream()
                        .map(AuthUserShopEntity::getShopId)
                        .collect(Collectors.toSet());
                shopIdList.removeIf(existingIds::contains);

            }
            //添加新增权限
            List<AuthUserShopEntity> addList = new ArrayList<>();
            if (CollUtil.isNotEmpty(shopIdList)) {
                shopIdList.forEach(shopId -> {
                    AuthUserShopEntity entity = new AuthUserShopEntity();
                    entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                    entity.setUserId(uid);
                    entity.setShopId(shopId);
                    addList.add(entity);
                });
            } else {
                AuthUserShopEntity entity = new AuthUserShopEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                entity.setUserId(uid);
                entity.setShopId("-1");
                addList.add(entity);
            }

            if (CollUtil.isNotEmpty(addList)) {
                this.saveBatch(addList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserShopAuth(AuthUserShopDTO.AddUserShopAuthDTO addUserShopAuthDTO) {
        if (Objects.isNull(addUserShopAuthDTO) || CharSequenceUtil.isBlank(addUserShopAuthDTO.getUserId()) || CollUtil.isEmpty(addUserShopAuthDTO.getShopIdList())) {
            return;
        }
        String userId = addUserShopAuthDTO.getUserId();
        List<String> shopIdList = addUserShopAuthDTO.getShopIdList();
        List<AuthUserShopEntity> list = this.lambdaQuery().eq(AuthUserShopEntity::getUserId, userId).list();
        if (CollUtil.isNotEmpty(list)) {
            //是否全部店铺权限
            boolean allShop = list.stream().allMatch(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()));
            if (allShop) {
                //全部权限就不用添加用户权限了
                return;
            }
        }
        List<AuthUserShopEntity> addList = new ArrayList<>();
        shopIdList.forEach(shopId -> {
            AuthUserShopEntity entity = new AuthUserShopEntity();
            entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
            entity.setUserId(userId);
            entity.setShopId(shopId);
            addList.add(entity);
        });
        if (CollUtil.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }
    }

    @Override
    public List<AuthUserShopDTO.ShopAuthListDTO> listAuthShop(AuthUserShopDTO.ShopAuthParamDTO paramDTO) {
        //当前登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<SysUserDTO.ShopDTO> authShopList = this.listShopIdByUserIds(Collections.singletonList(userInfo.getUid()));
        if (CollUtil.isEmpty(authShopList)) {
            log.error("当前用户没有店铺权限,用户:{}", userInfo.getUid());
            return Collections.emptyList();
        }
        //获取平台下的店铺
        List<ShopInfoEntity> omsShopList = FeignQuery.create(ShopInfoEntity.class).eq(ShopInfoEntity::getDictPlatform, paramDTO.getPlatform()).eq(ShopInfoEntity::getDisabled, Boolean.FALSE).list();
        if (CollUtil.isEmpty(omsShopList)) {
            log.error("平台下没有店铺信息,平台:{}", paramDTO.getPlatform());
            return Collections.emptyList();
        }
        long authCount = authShopList.stream().map(SysUserDTO.ShopDTO::getAuthType).filter(obj -> obj.contains(ShopAuthTypeEnum.ENUM_ALL.getCode())).count();
        if (authCount > 0) {
            return omsShopList.stream().map(obj -> new AuthUserShopDTO.ShopAuthListDTO(obj.getDictPlatform(), obj.getId(), obj.getName())).collect(Collectors.toList());
        }
        List<String> shopIdList = authShopList.stream().map(SysUserDTO.ShopDTO::getShopId).distinct().collect(Collectors.toList());
        return omsShopList.stream().filter(obj -> shopIdList.contains(obj.getId())).map(obj -> new AuthUserShopDTO.ShopAuthListDTO(obj.getDictPlatform(), obj.getId(), obj.getName())).collect(Collectors.toList());
    }
}
