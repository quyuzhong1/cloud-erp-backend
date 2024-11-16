package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.PackingTaskDTO;
import com.erp.model.wms.entity.PackingTaskDetailEntity;
import com.erp.server.wms.mapper.PackingTaskDetailMapper;
import com.erp.server.wms.service.PackingTaskDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackingTaskDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 装箱任务明细表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@Service
public class PackingTaskDetailServiceImpl extends SuperServiceImpl<PackingTaskDetailMapper, PackingTaskDetailEntity> implements PackingTaskDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackingTaskDetailDTO.AddDTO addDTO) {
        PackingTaskDetailEntity packingTaskDetailEntity = new PackingTaskDetailEntity();
        BeanMapperUtils.copy(addDTO, packingTaskDetailEntity);

        // 数据处理
        handleData(packingTaskDetailEntity);

        log.info("开始新增装箱任务明细单");
        boolean save = super.save(packingTaskDetailEntity);
        if(!save) {
            throw new ServiceException("装箱任务明细单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务明细单" , packingTaskDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, packingTaskDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(packingTaskDetailEntity.getId(), packingTaskDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackingTaskDetailDTO.UpdateDTO updateDTO) {
        PackingTaskDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "装箱任务明细单"));
        PackingTaskDetailEntity packingTaskDetailEntity =  BeanMapperUtils.map(PackingTaskDetailEntity.class, updateDTO);

        // 数据处理
        handleData(packingTaskDetailEntity);
        log.info("编辑 开始修改装箱任务明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(packingTaskDetailEntity);
        if(!save) {
            throw new ServiceException("装箱任务明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录装箱任务明细单日志数据，id：【{}】", packingTaskDetailEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packingTaskDetailEntity.getId(), "装箱任务明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packingTaskDetailEntity, null, packingTaskDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<PackingTaskDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(PackingTaskDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public void removeByMainId(String mainId) {
        if (StrUtil.isNotBlank(mainId)){
            lambdaUpdate().eq(PackingTaskDetailEntity::getMainId, mainId).remove();
        }
    }
    /**
     * 根据主表id进行sku分组统计
     * @param mainIds
     * @return
     */
    @Override
    public List<PackingTaskDTO.DetailDTO> listDetailByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return baseMapper.listDetailByMainIds(mainIds);
    }

    @Override
    public Integer countDeliveryQty(String id) {
        if (StrUtil.isNotBlank(id)){
            return baseMapper.countDeliveryQty(id);
        }
        return 0;
    }

    /**
     * 模糊搜索装箱任务明细
     * @param searchKey
     * @return
     */
    @Override
    public List<PackingTaskDetailDTO.ViewDTO> searchProductBySearchKey(String taskId, String searchKey) {
        if (CharSequenceUtil.isBlank(searchKey)){
            return Collections.emptyList();
        }
        return baseMapper.searchProductBySearchKey(taskId, searchKey);
    }

    @Override
    public List<PackingTaskDetailEntity> listBySourceIds(List<String> sourceDetailIds) {
        if (CollectionUtils.isEmpty(sourceDetailIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(PackingTaskDetailEntity::getSourceDetailId, sourceDetailIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PackingTaskDetailEntity packingTaskDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
