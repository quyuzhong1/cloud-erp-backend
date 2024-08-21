package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.WmsLocalPushMessageEntity;
import com.erp.server.wms.mapper.WmsLocalPushMessageMapper;
import com.erp.server.wms.service.WmsLocalPushMessageService;
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
import com.erp.model.wms.dto.WmsLocalPushMessageDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-21
 */
@Slf4j
@Service
public class WmsLocalPushMessageServiceImpl extends SuperServiceImpl<WmsLocalPushMessageMapper, WmsLocalPushMessageEntity> implements WmsLocalPushMessageService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsLocalPushMessageDTO.AddDTO addDTO) {
        WmsLocalPushMessageEntity wmsLocalPushMessageEntity = new WmsLocalPushMessageEntity();
        BeanMapperUtils.copy(addDTO, wmsLocalPushMessageEntity);

        // 数据处理
        handleData(wmsLocalPushMessageEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(wmsLocalPushMessageEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "本地推送消息单" , wmsLocalPushMessageEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsLocalPushMessageEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsLocalPushMessageEntity.getId(), wmsLocalPushMessageEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsLocalPushMessageDTO.UpdateDTO updateDTO) {
        WmsLocalPushMessageEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单"));
        WmsLocalPushMessageEntity wmsLocalPushMessageEntity =  BeanMapperUtils.map(WmsLocalPushMessageEntity.class, updateDTO);

        // 数据处理
        handleData(wmsLocalPushMessageEntity);
        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsLocalPushMessageEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", wmsLocalPushMessageEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsLocalPushMessageEntity.getId(), "本地推送消息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsLocalPushMessageEntity, null, wmsLocalPushMessageEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsLocalPushMessageEntity wmsLocalPushMessageEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
