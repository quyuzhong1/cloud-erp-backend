package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertDetailEntity;
import com.erp.server.plm.mapper.CfgMoldReturnAlertDetailMapper;
import com.erp.server.plm.service.CfgMoldReturnAlertDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.CfgMoldReturnAlertDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 模具返回策略明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@Service
public class CfgMoldReturnAlertDetailServiceImpl extends SuperServiceImpl<CfgMoldReturnAlertDetailMapper, CfgMoldReturnAlertDetailEntity> implements CfgMoldReturnAlertDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgMoldReturnAlertDetailDTO.AddDTO addDTO) {
        CfgMoldReturnAlertDetailEntity cfgMoldReturnAlertDetailEntity = new CfgMoldReturnAlertDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgMoldReturnAlertDetailEntity);

        // 数据处理
        handleData(cfgMoldReturnAlertDetailEntity);

        log.info("开始新增模具返回策略明细");
        boolean save = super.save(cfgMoldReturnAlertDetailEntity);
        if(!save) {
            throw new ServiceException("模具返回策略明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "模具返回策略明细" , cfgMoldReturnAlertDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogBySave(msg, "", cfgMoldReturnAlertDetailEntity.getId(), "");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgMoldReturnAlertDetailEntity.getId(), cfgMoldReturnAlertDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgMoldReturnAlertDetailDTO.UpdateDTO addOrUpdateDTO) {
        CfgMoldReturnAlertDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具返回策略明细"));
        CfgMoldReturnAlertDetailEntity cfgMoldReturnAlertDetailEntity =  BeanMapperUtils.map(CfgMoldReturnAlertDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgMoldReturnAlertDetailEntity);
        log.info("编辑 开始修改模具返回策略明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgMoldReturnAlertDetailEntity);
        if(!save) {
            throw new ServiceException("模具返回策略明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录模具返回策略明细日志数据，id：【{}】", cfgMoldReturnAlertDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgMoldReturnAlertDetailEntity.getId(), "模具返回策略明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addSysLogByUpdate(old,cfgMoldReturnAlertDetailEntity,String.valueOf(CfgMoldReturnAlertDetailEntity.class), old.getId(), "", msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgMoldReturnAlertDetailEntity cfgMoldReturnAlertDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
