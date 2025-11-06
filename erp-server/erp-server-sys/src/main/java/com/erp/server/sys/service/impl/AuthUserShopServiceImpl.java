package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.SqlUtils;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.oms.enums.ShopAuthTypeEnum;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.erp.model.sys.enums.AuthDataTypeEnum;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.convert.AuthUserConvert;
import com.erp.server.sys.mapper.AuthUserShopMapper;
import com.erp.server.sys.service.AuthUserShopService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AuthUserShopDTO.AddDTO addDTO) {
        AuthUserShopEntity authUserShopEntity = new AuthUserShopEntity();
        BeanMapperUtils.copy(addDTO, authUserShopEntity);

        // 数据处理
        handleData(authUserShopEntity);

        log.info("开始新增用户-店铺权限");
        boolean save = super.save(authUserShopEntity);
        if(!save) {
            throw new ServiceException("用户-店铺权限保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "用户-店铺权限" , authUserShopEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, authUserShopEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(authUserShopEntity.getId(), authUserShopEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AuthUserShopDTO.UpdateDTO addOrUpdateDTO) {
        AuthUserShopEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "用户-店铺权限"));
        AuthUserShopEntity authUserShopEntity =  BeanMapperUtils.map(AuthUserShopEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(authUserShopEntity);
        log.info("编辑 开始修改用户-店铺权限数据，id：【{}】", old.getId());
        boolean save = super.updateById(authUserShopEntity);
        if(!save) {
            throw new ServiceException("用户-店铺权限保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录用户-店铺权限日志数据，id：【{}】", authUserShopEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), authUserShopEntity.getId(), "用户-店铺权限");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, authUserShopEntity, null, authUserShopEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AuthUserShopEntity authUserShopEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    public List<SysUserDTO.ShopDTO> getShopUserList(String userId) {
        if (CharSequenceUtil.isBlank(userId)){
            return Collections.emptyList();
        }
        return baseMapper.getShopUserList(userId,null);
    }

    @Override
    public String getShopPermissionSql(String shopTableField , String dynamicDataSource) {
        if (CharSequenceUtil.isBlank(shopTableField)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        LoginUser defaultLoginUser = UserContext.getDefaultLoginUser();
        if ("0".equals(defaultLoginUser.getUid())){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        List<SysUserDTO.ShopDTO> shopUserList = this.getShopUserList(defaultLoginUser.getUid());
        if (CollUtil.isEmpty(shopUserList)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        String authType = shopUserList.stream().map(SysUserDTO.ShopDTO::getAuthType).filter("all"::equals).findFirst().orElse("part");
        if ("all".equals(authType)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        StringBuilder sqlString = new StringBuilder();
        //店铺
        List<String> shopTableFieldList = Arrays.asList(shopTableField.split(","));
        int shopTableFieldSize = shopTableFieldList.size();
        if ("part".equals(authType)){
        	boolean isDoris = (StringUtils.isNotBlank(dynamicDataSource) && dynamicDataSource.equals(DynamicDataSourceTypeEnum.DORIS.getCode()));
        	if(isDoris) {
                if (shopTableFieldSize == 1) {
                    sqlString.append(" AND ((").append(shopTableFieldList.get(0)).append( " = '') OR (");
                    SqlUtils.appendPermissionSql(sqlString, shopTableFieldList.get(0), shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()));
                    sqlString.append(" )) ");
                } else {
                    sqlString.append(" AND ((");
                    sqlString.append(shopTableFieldList.get(0)).append( " = '')");
                    for (int i = 1; i < shopTableFieldSize; i++) {
                        sqlString.append(" OR (");
                        sqlString.append(shopTableFieldList.get(i)).append(" = ''");
                        sqlString.append(" )");
                    }
                    sqlString.append(" OR (");
                    SqlUtils.appendPermissionSql(sqlString, shopTableFieldList.get(0), shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()));
                    sqlString.append(" )");
                    for (int i = 1; i < shopTableFieldSize; i++) {
                        sqlString.append("OR (");
                        SqlUtils.appendPermissionSql(sqlString, shopTableFieldList.get(i), shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()));
                        sqlString.append(" )");
                    }
                    sqlString.append(" )");
                }
            }else {
                if (shopTableFieldSize == 1) {
                    sqlString.append(" AND ((").append(shopTableFieldList.get(0)).append( " = '') OR (");
                    sqlString.append(" string_to_array(").append(shopTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',',')))");
                } else {
                    sqlString.append(" AND ((");
                    sqlString.append(shopTableFieldList.get(0)).append( " = '')");
                    for (int i = 1; i < shopTableFieldSize; i++) {
                        sqlString.append(" OR (");
                        sqlString.append(shopTableFieldList.get(i)).append(" = ''");
                        sqlString.append(" )");
                    }
                    sqlString.append(" OR (");
                    sqlString.append(" string_to_array(").append(shopTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',','))");
                    sqlString.append(" )");
                    for (int i = 1; i < shopTableFieldSize; i++) {
                        sqlString.append("OR (");
                        sqlString.append("string_to_array(").append(shopTableFieldList.get(i)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',','))");
                    }
                    sqlString.append(" )");
                }
            }
        }
        return sqlString.toString();
    }

    @Override
    public void initShopDataOmsToSys() {
        List<ShopSysUserAuthEntity> list = FeignQuery.list(ShopSysUserAuthEntity.class);
        if (CollUtil.isEmpty(list)){
            return;
        }
        List<AuthUserShopEntity> shopEntityList = AuthUserConvert.INSTANCE.OmsShopAuthToSysShopAuth(list);
        List<List<AuthUserShopEntity>> partition = ListUtil.partition(shopEntityList, MathUtil.NUMBER_100);
        partition.forEach(authUserShopEntities -> authUserShopEntities.forEach(e->{
            AuthUserShopEntity one = this.lambdaQuery().eq(AuthUserShopEntity::getAuthType, e.getAuthType()).eq(AuthUserShopEntity::getShopId, e.getShopId()).eq(AuthUserShopEntity::getUserId, e.getUserId()).last("limit 1").one();
            if (Objects.isNull(one)){
                this.save(e);
            }else {
                e.setId(one.getId());
                this.updateById(e);
            }
        }));
    }

    @Override
    public List<SysUserDTO.ShopDTO> listShopIdByUserIds(List<String> userIdList) {
        if (CollUtil.isEmpty(userIdList)){
            return Collections.emptyList();
        }
        return baseMapper.getShopUserList(null,userIdList);
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
    public void batchSaveOrUpdate(String uid, List<String> shopIdList, String shopAuthType) {
        if (CharSequenceUtil.isBlank(uid)){
            return;
        }
        List<AuthUserShopEntity> list = this.lambdaQuery().eq(AuthUserShopEntity::getUserId, uid).list();
        if (AuthDataTypeEnum.ENUM_ALL.getCode().equals(shopAuthType)){
            AuthUserShopEntity auth = list.stream().filter(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType())).findFirst().orElse(null);
            if (Objects.isNull(auth)){
                //清空历史
                this.lambdaUpdate().eq(AuthUserShopEntity::getUserId, uid).remove();
                //新增全部授权
                AuthUserShopEntity entity = new AuthUserShopEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_ALL.getCode());
                entity.setUserId(uid);
                this.save(entity);
            }
        }else if (AuthDataTypeEnum.ENUM_PART.getCode().equals(shopAuthType)){
            //删除移除的权限
            List<String> ids = new ArrayList<>();
            if (CollUtil.isNotEmpty(list)){
                ids = list.stream().map(AuthUserShopEntity::getShopId).collect(Collectors.toList());
                List<String> deleteIdList = list.stream().filter(e -> !shopIdList.contains(e.getShopId()) || AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()))
                        .map(AuthUserShopEntity::getId).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(deleteIdList)){
                    this.removeByIds(deleteIdList);
                }
            }
            //添加新增权限
            List<AuthUserShopEntity> addList = new ArrayList<>();
            List<String> finalIds = ids;
            if (CollUtil.isNotEmpty(shopIdList)){
                shopIdList.forEach(shopId ->{
                    if (CollUtil.isEmpty(finalIds) || !finalIds.contains(shopId)){
                        AuthUserShopEntity entity = new AuthUserShopEntity();
                        entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                        entity.setUserId(uid);
                        entity.setShopId(shopId);
                        addList.add(entity);
                    }
                });
            }else {
                AuthUserShopEntity entity = new AuthUserShopEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                entity.setUserId(uid);
                entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                entity.setShopId("-1");
                addList.add(entity);
            }

            if (CollUtil.isNotEmpty(addList)){
                this.saveBatch(addList);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserShopAuth(AuthUserShopDTO.AddUserShopAuthDTO addUserShopAuthDTO) {
        if (Objects.isNull(addUserShopAuthDTO) || CharSequenceUtil.isBlank(addUserShopAuthDTO.getUserId()) || CollUtil.isEmpty(addUserShopAuthDTO.getShopIdList())){
            return;
        }
        String userId = addUserShopAuthDTO.getUserId();
        List<String> shopIdList = addUserShopAuthDTO.getShopIdList();
        List<AuthUserShopEntity> list = this.lambdaQuery().eq(AuthUserShopEntity::getUserId, userId).list();
        if (CollUtil.isNotEmpty(list)){
            //是否全部店铺权限
            boolean allShop = list.stream().allMatch(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()));
            if (allShop){
                //全部权限就不用添加用户权限了
                return;
            }
        }
        List<AuthUserShopEntity> addList = new ArrayList<>();
        shopIdList.forEach(shopId ->{
            AuthUserShopEntity entity = new AuthUserShopEntity();
            entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
            entity.setUserId(userId);
            entity.setShopId(shopId);
            addList.add(entity);
        });
        if (CollUtil.isNotEmpty(addList)){
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
