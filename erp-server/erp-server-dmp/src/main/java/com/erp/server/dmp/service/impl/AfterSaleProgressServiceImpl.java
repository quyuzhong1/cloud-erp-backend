package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.entity.AfterSaleProgressEntity;
import com.erp.server.dmp.mapper.AfterSaleProgressMapper;
import com.erp.server.dmp.service.AfterSaleProgressService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 售后进度记录表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-03
 */
@Slf4j
@Service
public class AfterSaleProgressServiceImpl extends SuperServiceImpl<AfterSaleProgressMapper, AfterSaleProgressEntity> implements AfterSaleProgressService {
    @Autowired
    private OperateLogService operateLogService;



    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSaleProgressDTO.AddDTO addDTO) {
        AfterSaleProgressEntity afterSaleProgressEntity = new AfterSaleProgressEntity();
        BeanMapperUtils.copy(addDTO, afterSaleProgressEntity);

        // 数据处理
        handleData(afterSaleProgressEntity);

        log.info("开始新增售后进度记录单");
        boolean save = super.save(afterSaleProgressEntity);
        if(!save) {
            throw new ServiceException("售后进度记录单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后进度记录单" , afterSaleProgressEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, afterSaleProgressEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(afterSaleProgressEntity.getId(), afterSaleProgressEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSaleProgressDTO.UpdateDTO addOrUpdateDTO) {
        AfterSaleProgressEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "售后进度记录单"));
        AfterSaleProgressEntity afterSaleProgressEntity =  BeanMapperUtils.map(AfterSaleProgressEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(afterSaleProgressEntity);
        log.info("编辑 开始修改售后进度记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(afterSaleProgressEntity);
        if(!save) {
            throw new ServiceException("售后进度记录单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录售后进度记录单日志数据，id：【{}】", afterSaleProgressEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSaleProgressEntity.getId(), "售后进度记录单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, afterSaleProgressEntity, null, afterSaleProgressEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<AfterSaleProgressEntity> listByMainIds(List<String> ids) {
        if(CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<AfterSaleProgressEntity> list = lambdaQuery().in(AfterSaleProgressEntity::getMainId, ids).list();
        // 根据 index 进行数值排序
        list.sort(Comparator.comparingInt(AfterSaleProgressEntity::getIndex));
        return list;
    }

    @Override
    public Boolean updateStatus(String mainId, String node, String remark) {
        if(StringUtils.isBlank(mainId) || StringUtils.isBlank(node)){
            return Boolean.FALSE;
        }

        lambdaUpdate().set(AfterSaleProgressEntity::getNodeTime, LocalDateTime.now())
                .set(AfterSaleProgressEntity::getRemark, remark)
                .eq(AfterSaleProgressEntity::getMainId, mainId)
                .eq(AfterSaleProgressEntity::getNode, node)
                .update();

        return Boolean.TRUE;

    }

    @Override
    public AfterSaleProgressEntity getByNode(String id, String node) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(node)) {
            return null;
        }
        // 查询数据
        return lambdaQuery().eq(AfterSaleProgressEntity::getMainId, id).eq(AfterSaleProgressEntity::getNode, node).one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AfterSaleProgressEntity afterSaleProgressEntity) {
    }



}
