package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleRecipientDetailEntity;
import com.erp.server.wms.mapper.SampleRecipientDetailMapper;
import com.erp.server.wms.service.SampleRecipientDetailService;
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
import com.erp.model.wms.dto.SampleRecipientDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品领用单明细 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleRecipientDetailServiceImpl extends SuperServiceImpl<SampleRecipientDetailMapper, SampleRecipientDetailEntity> implements SampleRecipientDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleRecipientDetailDTO.AddDTO addDTO) {
        SampleRecipientDetailEntity sampleRecipientDetailEntity = new SampleRecipientDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleRecipientDetailEntity);

        // 数据处理
        handleData(sampleRecipientDetailEntity);

        log.info("开始新增样品领用单明细");
        boolean save = super.save(sampleRecipientDetailEntity);
        if(!save) {
            throw new ServiceException("样品领用单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品领用单明细" , sampleRecipientDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleRecipientDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleRecipientDetailEntity.getId(), sampleRecipientDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleRecipientDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleRecipientDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品领用单明细"));
        SampleRecipientDetailEntity sampleRecipientDetailEntity =  BeanMapperUtils.map(SampleRecipientDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleRecipientDetailEntity);
        log.info("编辑 开始修改样品领用单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleRecipientDetailEntity);
        if(!save) {
            throw new ServiceException("样品领用单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品领用单明细日志数据，id：【{}】", sampleRecipientDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleRecipientDetailEntity.getId(), "样品领用单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleRecipientDetailEntity, null, sampleRecipientDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleRecipientDetailEntity sampleRecipientDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
