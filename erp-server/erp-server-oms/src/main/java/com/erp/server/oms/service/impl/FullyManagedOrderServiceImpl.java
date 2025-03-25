package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.FullyManagedTabEnum;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.query.FullyManagedQueryHandler;
import com.erp.server.oms.query.SoB2cQueryHandler;
import com.erp.server.oms.service.FullyManagedOrderService;
import com.erp.server.oms.service.ShopSysUserAuthService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName FullyManagedOrderServiceImpl
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
@Slf4j
@Service
public class FullyManagedOrderServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements FullyManagedOrderService {
    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;
    @Resource
    @Qualifier("soB2cTabExecutorPool")
    private ExecutorService soB2cTabExecutorPool;
    @Resource
    private FullyManagedQueryHandler fullyManagedQueryHandler;
    @Resource
    private SoB2cService soB2cService;
    
    @Override
    public List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO param) {
        FullyManagedTabEnum[] values = FullyManagedTabEnum.values();
        List<Future<SoB2cDTO.TabListDTO>> futureList = new ArrayList<>();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO = handleShopSysUserAuth();
        for (FullyManagedTabEnum item : values) {
            Future<SoB2cDTO.TabListDTO> submit = soB2cTabExecutorPool.submit(() -> {
                SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
                searchParamDTO.setPermissionSql(param.getPermissionSql());
                SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
                String tabSql = fullyManagedQueryHandler.getTabSql(item.getCode());
                HashMap<String,String> map = new HashMap<>();
                map.put("default",tabSql);
                searchParamDTO.setSqlMap(map);
                //查询店铺设置权限
                Integer count;
                if (ObjectUtil.isEmpty(shopAuthResultDTO)) {
                    count = MathUtil.ZERO;
                } else {
                    count = this.baseMapper.listFullManagedCount(searchParamDTO, shopAuthResultDTO);
                }
                resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
                resultDTO.setTabFlag(item.getCode());
                resultDTO.setTabFlagName(item.getName());
                return resultDTO;
            });
            futureList.add(submit);
        }
        for(Future<SoB2cDTO.TabListDTO> f : futureList) {
            try {
                list.add(f.get());
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return list;
    }

    /**
     * 查询店铺权限设置
     */
    private SoB2cDTO.ShopAuthResultDTO handleShopSysUserAuth() {
        SoB2cDTO.ShopAuthResultDTO resultDTO = new SoB2cDTO.ShopAuthResultDTO();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ObjectUtil.isEmpty(userInfo) || StringUtils.isBlank(userInfo.getUid())) {
            return null;
        }
        List<ShopSysUserAuthDTO.ViewDTO> list = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        resultDTO.setUserId(userInfo.getUid());
        resultDTO.setAuthType(list.get(0).getAuthType());
        return resultDTO;
    }
}
