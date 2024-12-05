package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationMainEntity;
import com.erp.server.tms.mapper.SmallBagCostAllocationMainMapper;
import com.erp.server.tms.service.SmallBagCostAllocationMainService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.SmallBagCostAllocationMainDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 小包费用分摊主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-05
 */
@Slf4j
@Service
public class SmallBagCostAllocationMainServiceImpl extends SuperServiceImpl<SmallBagCostAllocationMainMapper, SmallBagCostAllocationMainEntity> implements SmallBagCostAllocationMainService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SmallBagCostAllocationMainDTO.AddDTO addDTO) {
        SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity = new SmallBagCostAllocationMainEntity();
        BeanMapperUtils.copy(addDTO, smallBagCostAllocationMainEntity);

        // 数据处理
        handleData(smallBagCostAllocationMainEntity);

        log.info("开始新增小包费用分摊主单");
        boolean save = super.save(smallBagCostAllocationMainEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "小包费用分摊主单" , smallBagCostAllocationMainEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, smallBagCostAllocationMainEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(smallBagCostAllocationMainEntity.getId(), smallBagCostAllocationMainEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SmallBagCostAllocationMainDTO.UpdateDTO updateDTO) {
        SmallBagCostAllocationMainEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "小包费用分摊主单"));
        SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity =  BeanMapperUtils.map(SmallBagCostAllocationMainEntity.class, updateDTO);

        // 数据处理
        handleData(smallBagCostAllocationMainEntity);
        log.info("编辑 开始修改小包费用分摊主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(smallBagCostAllocationMainEntity);
        if(!save) {
            throw new ServiceException("小包费用分摊主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录小包费用分摊主单日志数据，id：【{}】", smallBagCostAllocationMainEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), smallBagCostAllocationMainEntity.getId(), "小包费用分摊主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, smallBagCostAllocationMainEntity, null, smallBagCostAllocationMainEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SmallBagCostAllocationMainEntity smallBagCostAllocationMainEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
