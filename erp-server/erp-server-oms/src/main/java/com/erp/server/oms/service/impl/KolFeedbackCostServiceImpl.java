package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.erp.server.oms.mapper.KolFeedbackCostMapper;
import com.erp.server.oms.service.KolFeedbackCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolFeedbackCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * KOL回片费用表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolFeedbackCostServiceImpl extends SuperServiceImpl<KolFeedbackCostMapper, KolFeedbackCostEntity> implements KolFeedbackCostService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackCostDTO.AddDTO addDTO) {
        KolFeedbackCostEntity kolFeedbackCostEntity = new KolFeedbackCostEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackCostEntity);

        // 数据处理
        handleData(kolFeedbackCostEntity);

        log.info("开始新增KOL回片费用单");
        boolean save = super.save(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片费用单" , kolFeedbackCostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolFeedbackCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolFeedbackCostEntity.getId(), kolFeedbackCostEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolFeedbackCostDTO.UpdateDTO addOrUpdateDTO) {
        KolFeedbackCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "KOL回片费用单"));
        KolFeedbackCostEntity kolFeedbackCostEntity =  BeanMapperUtils.map(KolFeedbackCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackCostEntity);
        log.info("编辑 开始修改KOL回片费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录KOL回片费用单日志数据，id：【{}】", kolFeedbackCostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackCostEntity.getId(), "KOL回片费用单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackCostEntity, null, kolFeedbackCostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackCostEntity kolFeedbackCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
