package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryHisDTO;
import com.erp.model.wms.entity.VirtualInventoryHisEntity;
import com.erp.server.wms.mapper.VirtualInventoryHisMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.VirtualInventoryHisService;
import com.erp.server.wms.service.VirtualInventoryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 虚拟仓库存历史信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@Service
public class VirtualInventoryHisServiceImpl extends SuperServiceImpl<VirtualInventoryHisMapper, VirtualInventoryHisEntity> implements VirtualInventoryHisService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private VirtualInventoryService virtualInventoryService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualInventoryHisDTO.AddDTO addDTO) {
        VirtualInventoryHisEntity virtualInventoryHisEntity = new VirtualInventoryHisEntity();
        BeanMapperUtils.copy(addDTO, virtualInventoryHisEntity);

        // 数据处理
        handleData(virtualInventoryHisEntity);

        log.info("开始新增虚拟仓库存历史信息");
        boolean save = super.save(virtualInventoryHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓库存历史信息" , virtualInventoryHisEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualInventoryHisEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualInventoryHisEntity.getId(), virtualInventoryHisEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualInventoryHisDTO.UpdateDTO updateDTO) {
        VirtualInventoryHisEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓库存历史信息"));
        VirtualInventoryHisEntity virtualInventoryHisEntity =  BeanMapperUtils.map(VirtualInventoryHisEntity.class, updateDTO);

        // 数据处理
        handleData(virtualInventoryHisEntity);
        log.info("编辑 开始修改虚拟仓库存历史信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualInventoryHisEntity);
        if(!save) {
            throw new ServiceException("虚拟仓库存历史信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟仓库存历史信息日志数据，id：【{}】", virtualInventoryHisEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualInventoryHisEntity.getId(), "虚拟仓库存历史信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualInventoryHisEntity, null, virtualInventoryHisEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<VirtualInventoryHisEntity> listByParam(VirtualInventoryHisDTO.ParamDTO paramDTO) {
        return baseMapper.listByParam(paramDTO);
    }

    @Override
    public void hisVirtualInventoryJob() {
        LocalDate date = LocalDate.now().minusDays(1L);
        List<VirtualInventoryHisEntity> virtualInventoryHisList = baseMapper.listVirtualInventoryHisJobData(date);
        if (CollUtil.isEmpty(virtualInventoryHisList)) {
            return;
        }
        List<String> virtualTransFlowIdList = virtualInventoryHisList.stream().map(VirtualInventoryHisEntity::getVirtualTransFlowId).distinct().collect(Collectors.toList());
        List<String> batchNoList = virtualInventoryHisList.stream().map(VirtualInventoryHisEntity::getBatchNo).distinct().collect(Collectors.toList());
        List<VirtualInventoryHisEntity> oldList = listByVirtualTransFlowIdList(virtualTransFlowIdList, batchNoList,LocalDate.now());
        for (VirtualInventoryHisEntity hisEntity : virtualInventoryHisList) {
            //查询是否已有数据
            String id = oldList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getVirtualTransFlowId(), hisEntity.getVirtualTransFlowId())
                            && CharSequenceUtil.equals(obj.getBatchNo(), hisEntity.getBatchNo())
                            && obj.getDate().isEqual(LocalDate.now())
            ).findFirst().map(VirtualInventoryHisEntity::getId).orElse("");
            hisEntity.setId(id);
        }
        this.saveOrUpdateBatch(virtualInventoryHisList);
    }

    @Override
    public VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO getHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        return baseMapper.getHisInventoryAgeDetail(dto);
    }

    /**
     * 根据流水id查询
     * @author will
     * @date 2024/12/10 10:57
     * @param virtualTransFlowIdList
     * @param batchNoList
     * @return List<VirtualInventoryHisEntity>
     */
    private List<VirtualInventoryHisEntity> listByVirtualTransFlowIdList (List<String> virtualTransFlowIdList,List<String> batchNoList,LocalDate date) {
        return lambdaQuery()
                .in(VirtualInventoryHisEntity::getVirtualTransFlowId, virtualTransFlowIdList)
                .in(VirtualInventoryHisEntity::getBatchNo, batchNoList)
                .eq(VirtualInventoryHisEntity::getDate,date)
                .list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryHisEntity virtualInventoryHisEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
