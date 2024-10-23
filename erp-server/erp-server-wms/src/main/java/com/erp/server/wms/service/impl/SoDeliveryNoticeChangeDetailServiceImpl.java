package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeChangeDetailEntity;
import com.erp.server.wms.mapper.SoDeliveryNoticeChangeDetailMapper;
import com.erp.server.wms.service.SoDeliveryNoticeChangeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoDeliveryNoticeChangeDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货通知变更单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
 */
@Slf4j
@Service
public class SoDeliveryNoticeChangeDetailServiceImpl extends SuperServiceImpl<SoDeliveryNoticeChangeDetailMapper, SoDeliveryNoticeChangeDetailEntity> implements SoDeliveryNoticeChangeDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoDeliveryNoticeChangeDetailDTO.AddDTO addDTO) {
        SoDeliveryNoticeChangeDetailEntity soDeliveryNoticeChangeDetailEntity = new SoDeliveryNoticeChangeDetailEntity();
        BeanMapperUtils.copy(addDTO, soDeliveryNoticeChangeDetailEntity);

        // 数据处理
        handleData(soDeliveryNoticeChangeDetailEntity);

        log.info("开始新增发货通知变更单明细");
        boolean save = super.save(soDeliveryNoticeChangeDetailEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货通知变更单明细" , soDeliveryNoticeChangeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soDeliveryNoticeChangeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soDeliveryNoticeChangeDetailEntity.getId(), soDeliveryNoticeChangeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoDeliveryNoticeChangeDetailDTO.UpdateDTO updateDTO) {
        SoDeliveryNoticeChangeDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货通知变更单明细"));
        SoDeliveryNoticeChangeDetailEntity soDeliveryNoticeChangeDetailEntity =  BeanMapperUtils.map(SoDeliveryNoticeChangeDetailEntity.class, updateDTO);

        // 数据处理
        handleData(soDeliveryNoticeChangeDetailEntity);
        log.info("编辑 开始修改发货通知变更单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soDeliveryNoticeChangeDetailEntity);
        if(!save) {
            throw new ServiceException("发货通知变更单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录发货通知变更单明细日志数据，id：【{}】", soDeliveryNoticeChangeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soDeliveryNoticeChangeDetailEntity.getId(), "发货通知变更单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soDeliveryNoticeChangeDetailEntity, null, soDeliveryNoticeChangeDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoDeliveryNoticeChangeDetailEntity soDeliveryNoticeChangeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
