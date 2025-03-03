package com.erp.server.sys.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.erp.server.sys.constant.SysConstant;
import com.erp.server.sys.convert.AuthUserConvert;
import com.erp.server.sys.mapper.AuthUserShopMapper;
import com.erp.server.sys.service.AuthUserShopService;
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
import com.erp.model.sys.dto.AuthUserShopDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

    @GlobalTransactional(rollbackFor = Exception.class)
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
        return baseMapper.getShopUserList(userId);
    }

    @Override
    public String getShopPermissionSql(String shopTableField) {
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
        Integer dataScope = shopUserList.stream().min(Comparator.comparing(SysUserDTO.ShopDTO::getDataScope)).map(SysUserDTO.ShopDTO::getDataScope).orElse(MathUtil.ONE);
        if (MathUtil.ZERO.equals(dataScope)){
            return SysConstant.ADMIN_PERMISSON_SQL;
        }
        StringBuilder sqlString = new StringBuilder();
        //店铺
        List<String> shopTableFieldList = Arrays.asList(shopTableField.split(","));
        int shopTableFieldSize = shopTableFieldList.size();
        if (CollectionUtils.isNotEmpty(shopUserList)) {
            if (1 == dataScope){
                if (shopTableFieldSize == 1) {
                    sqlString.append(" AND string_to_array(").append(shopTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',',')");
                } else {
                    sqlString.append(" AND (string_to_array(").append(shopTableFieldList.get(0)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',',')");
                    sqlString.append(" OR ");
                    for (int i = 1; i < shopTableFieldSize; i++) {
                        sqlString.append("string_to_array(").append(shopTableFieldList.get(i)).append(",',') && string_to_array('").append(StringUtils.join(shopUserList.stream().map(SysUserDTO.ShopDTO::getShopId).collect(Collectors.toList()), ",")).append("',','))");
                    }
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
            AuthUserShopEntity one = this.lambdaQuery().eq(AuthUserShopEntity::getDataScope, e.getDataScope()).eq(AuthUserShopEntity::getShopId, e.getShopId()).eq(AuthUserShopEntity::getUserId, e.getUserId()).last("limit 1").one();
            if (Objects.isNull(one)){
                this.save(e);
            }else {
                e.setId(one.getId());
                this.updateById(e);
            }
        }));
    }
}
