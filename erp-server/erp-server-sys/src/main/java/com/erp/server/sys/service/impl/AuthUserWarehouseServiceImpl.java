package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.erp.model.sys.entity.AuthUserWarehouseEntity;
import com.erp.model.sys.enums.AuthDataTypeEnum;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.mapper.AuthUserWarehouseMapper;
import com.erp.server.sys.service.AuthUserWarehouseService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
//import com.erp.server.sys.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 用户-仓库权限 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@Service
public class AuthUserWarehouseServiceImpl extends SuperServiceImpl<AuthUserWarehouseMapper, AuthUserWarehouseEntity> implements AuthUserWarehouseService {
//    @Autowired
//    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AuthUserWarehouseDTO.AddDTO addDTO) {
        AuthUserWarehouseEntity authUserWarehouseEntity = new AuthUserWarehouseEntity();
        BeanMapperUtils.copy(addDTO, authUserWarehouseEntity);

        // 数据处理
        handleData(authUserWarehouseEntity);

        log.info("开始新增用户-仓库权限");
        boolean save = super.save(authUserWarehouseEntity);
        if(!save) {
            throw new ServiceException("用户-仓库权限保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "用户-仓库权限" , authUserWarehouseEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, authUserWarehouseEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(authUserWarehouseEntity.getId(), authUserWarehouseEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AuthUserWarehouseDTO.UpdateDTO addOrUpdateDTO) {
        AuthUserWarehouseEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "用户-仓库权限"));
        AuthUserWarehouseEntity authUserWarehouseEntity =  BeanMapperUtils.map(AuthUserWarehouseEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(authUserWarehouseEntity);
        log.info("编辑 开始修改用户-仓库权限数据，id：【{}】", old.getId());
        boolean save = super.updateById(authUserWarehouseEntity);
        if(!save) {
            throw new ServiceException("用户-仓库权限保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录用户-仓库权限日志数据，id：【{}】", authUserWarehouseEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), authUserWarehouseEntity.getId(), "用户-仓库权限");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, authUserWarehouseEntity, null, authUserWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SysUserDTO.WarehouseDTO> getWarehouseUserList(String userId) {
        if (CharSequenceUtil.isBlank(userId)){
            return Collections.emptyList();
        }
        return baseMapper.getWarehouseUserList(userId, null);
    }

    @Override
    public String getWarehousePermissionSql(String warehouseTableField , String dynamicDataSource) {
        if (CharSequenceUtil.isBlank(warehouseTableField)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        LoginUser defaultLoginUser = UserContext.getDefaultLoginUser();
        if ("0".equals(defaultLoginUser.getUid())){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        List<SysUserDTO.WarehouseDTO> warehouseUserList = this.getWarehouseUserList(defaultLoginUser.getUid());
        if (CollUtil.isEmpty(warehouseUserList)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        String authType = warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getAuthType).filter("all"::equals).findFirst().orElse("part");
        if ("all".equals(authType)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        StringBuilder sqlString = new StringBuilder();
        //店铺
        List<String> warehouseTableFieldList = Arrays.asList(warehouseTableField.split(","));
        int warehouseTableFieldSize = warehouseTableFieldList.size();
        if (CollectionUtils.isNotEmpty(warehouseUserList)) {
        	boolean isDoris = (StringUtils.isNotBlank(dynamicDataSource) && dynamicDataSource.equals(DynamicDataSourceTypeEnum.DORIS.getCode()));
            if ("part".equals(authType)){
            	if(isDoris) {
            		if (warehouseTableFieldSize == 1) {
                    	sqlString.append(" AND ");
                        SqlUtils.appendPermissionSql(sqlString, warehouseTableFieldList.get(0), warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()));
                    } else {
                    	sqlString.append(" AND (");
                    	SqlUtils.appendPermissionSql(sqlString, warehouseTableFieldList.get(0), warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()));
                        sqlString.append(" OR ");
                        for (int i = 1; i < warehouseTableFieldSize; i++) {
                        	SqlUtils.appendPermissionSql(sqlString, warehouseTableFieldList.get(0), warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()));
                        }
                        sqlString.append(" ) ");
                    }
            	}else {
            		if (warehouseTableFieldSize == 1) {
                        sqlString.append(" AND string_to_array(").append(warehouseTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()), ",")).append("',',')");
                    } else {
                        sqlString.append(" AND (string_to_array(").append(warehouseTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()), ",")).append("',',')");
                        sqlString.append(" OR ");
                        for (int i = 1; i < warehouseTableFieldSize; i++) {
                            sqlString.append("string_to_array(").append(warehouseTableFieldList.get(i)).append(",',') && string_to_array('").append(StringUtils.join(warehouseUserList.stream().map(SysUserDTO.WarehouseDTO::getWarehouseId).collect(Collectors.toList()), ",")).append("',','))");
                        }
                    }
            	}
            }
        }
        return sqlString.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveOrUpdate(String uid, List<String> warehouseIdList, String warehouseAuthType) {
        if (CharSequenceUtil.isBlank(uid)){
            return;
        }
        List<AuthUserWarehouseEntity> list = this.lambdaQuery().eq(AuthUserWarehouseEntity::getUserId, uid).list();
        if (AuthDataTypeEnum.ENUM_ALL.getCode().equals(warehouseAuthType)){
            AuthUserWarehouseEntity auth = list.stream().filter(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType())).findFirst().orElse(null);
            if (Objects.isNull(auth)){
                //清空历史
                this.lambdaUpdate().eq(AuthUserWarehouseEntity::getUserId, uid).remove();
                //新增全部授权
                AuthUserWarehouseEntity entity = new AuthUserWarehouseEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_ALL.getCode());
                entity.setUserId(uid);
                this.save(entity);
            }
        }else if (AuthDataTypeEnum.ENUM_PART.getCode().equals(warehouseAuthType)){
            //删除移除的权限
            List<String> ids = new ArrayList<>();
            if (CollUtil.isNotEmpty(list)){
                ids = list.stream().map(AuthUserWarehouseEntity::getWarehouseId).collect(Collectors.toList());
                List<String> deleteIdList = list.stream().filter(e -> !warehouseIdList.contains(e.getWarehouseId()) || AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()))
                        .map(AuthUserWarehouseEntity::getId).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(deleteIdList)){
                    this.removeByIds(deleteIdList);
                }
            }
            //添加新增权限
            List<AuthUserWarehouseEntity> addList = new ArrayList<>();
            List<String> finalIds = ids;
            if (CollUtil.isEmpty(warehouseIdList)){
                AuthUserWarehouseEntity entity = new AuthUserWarehouseEntity();
                entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                entity.setUserId(uid);
                entity.setWarehouseId("-1");
                addList.add(entity);
            }else {
                warehouseIdList.forEach(warehouseId ->{
                    if (CollUtil.isEmpty(finalIds) || !finalIds.contains(warehouseId)){
                        AuthUserWarehouseEntity entity = new AuthUserWarehouseEntity();
                        entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
                        entity.setUserId(uid);
                        entity.setWarehouseId(warehouseId);
                        addList.add(entity);
                    }
                });
            }
            if (CollUtil.isNotEmpty(addList)){
                this.saveBatch(addList);
            }
        }
    }

    @Override
    public List<SysUserDTO.WarehouseDTO> listWarehouseIdByUserIds(List<String> userIds) {
        if (CollUtil.isEmpty(userIds)){
            return Collections.emptyList();
        }
        return baseMapper.getWarehouseUserList(null,userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserWarehouseAuth(AuthUserWarehouseDTO.AddUserWarehouseAuthDTO addUserWarehouseAuthDTO) {
        if (Objects.isNull(addUserWarehouseAuthDTO) || CharSequenceUtil.isBlank(addUserWarehouseAuthDTO.getUserId()) || CollUtil.isEmpty(addUserWarehouseAuthDTO.getWarehouseIds())){
            return;
        }
        String userId = addUserWarehouseAuthDTO.getUserId();
        List<String> warehouseIds = addUserWarehouseAuthDTO.getWarehouseIds();
        List<AuthUserWarehouseEntity> list = this.lambdaQuery().eq(AuthUserWarehouseEntity::getUserId, userId).list();
        if (CollUtil.isNotEmpty(list)){
            //是否全部店铺权限
            boolean allShop = list.stream().allMatch(e -> AuthDataTypeEnum.ENUM_ALL.getCode().equals(e.getAuthType()));
            if (allShop){
                //全部权限就不用添加用户权限了
                return;
            }
        }
        List<AuthUserWarehouseEntity> addList = new ArrayList<>();
        warehouseIds.forEach(warehouseId ->{
            AuthUserWarehouseEntity entity = new AuthUserWarehouseEntity();
            entity.setAuthType(AuthDataTypeEnum.ENUM_PART.getCode());
            entity.setUserId(userId);
            entity.setWarehouseId(warehouseId);
            addList.add(entity);
        });
        if (CollUtil.isNotEmpty(addList)){
            this.saveBatch(addList);
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AuthUserWarehouseEntity authUserWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
