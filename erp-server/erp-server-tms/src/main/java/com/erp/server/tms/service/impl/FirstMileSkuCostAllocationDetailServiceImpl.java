package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.FirstMileSkuCostAllocationDetailEntity;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.server.tms.mapper.FirstMileSkuCostAllocationDetailMapper;
import com.erp.server.tms.service.FirstMileSkuCostAllocationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileSkuCostAllocationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 头程费用SKU分摊明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileSkuCostAllocationDetailServiceImpl extends SuperServiceImpl<FirstMileSkuCostAllocationDetailMapper, FirstMileSkuCostAllocationDetailEntity> implements FirstMileSkuCostAllocationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileSkuCostAllocationDetailDTO.AddDTO addDTO) {
        FirstMileSkuCostAllocationDetailEntity firstMileSkuCostAllocationDetailEntity = new FirstMileSkuCostAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, firstMileSkuCostAllocationDetailEntity);

        // 数据处理
        handleData(firstMileSkuCostAllocationDetailEntity);

        log.info("开始新增头程费用SKU分摊明细");
        boolean save = super.save(firstMileSkuCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程费用SKU分摊明细" , firstMileSkuCostAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileSkuCostAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileSkuCostAllocationDetailEntity.getId(), firstMileSkuCostAllocationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileSkuCostAllocationDetailDTO.UpdateDTO updateDTO) {
        FirstMileSkuCostAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程费用SKU分摊明细"));
        FirstMileSkuCostAllocationDetailEntity firstMileSkuCostAllocationDetailEntity =  BeanMapperUtils.map(FirstMileSkuCostAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileSkuCostAllocationDetailEntity);
        log.info("编辑 开始修改头程费用SKU分摊明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileSkuCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("头程费用SKU分摊明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程费用SKU分摊明细日志数据，id：【{}】", firstMileSkuCostAllocationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileSkuCostAllocationDetailEntity.getId(), "头程费用SKU分摊明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileSkuCostAllocationDetailEntity, null, firstMileSkuCostAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void removeByMainId(String id) {
        if (!StrUtil.isBlank(id)) {
            this.lambdaUpdate().eq(FirstMileSkuCostAllocationDetailEntity::getMainId, id).remove();
        }
    }

    @Override
    public List<FirstMileSkuCostAllocationDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)){
            return Collections.emptyList();
        }
        return baseMapper.listByMainIds(mainIds);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileSkuCostAllocationDetailEntity firstMileSkuCostAllocationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
