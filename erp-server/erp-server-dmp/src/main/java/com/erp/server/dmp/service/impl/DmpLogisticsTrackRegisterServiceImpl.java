package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpLogisticsTrackRegisterEntity;
import com.erp.server.dmp.convert.DmpTrackConverter;
import com.erp.server.dmp.mapper.DmpLogisticsTrackRegisterMapper;
import com.erp.server.dmp.service.DmpLogisticsTrackRegisterService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpLogisticsTrackRegisterDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流注册表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-11-12
 */
@Slf4j
@Service
public class DmpLogisticsTrackRegisterServiceImpl extends SuperServiceImpl<DmpLogisticsTrackRegisterMapper, DmpLogisticsTrackRegisterEntity> implements DmpLogisticsTrackRegisterService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpLogisticsTrackRegisterDTO.AddDTO addDTO) {
        DmpLogisticsTrackRegisterEntity dmpLogisticsTrackRegisterEntity = new DmpLogisticsTrackRegisterEntity();
        BeanMapperUtils.copy(addDTO, dmpLogisticsTrackRegisterEntity);

        // 数据处理
        handleData(dmpLogisticsTrackRegisterEntity);

        log.info("开始新增物流注册单");
        boolean save = super.save(dmpLogisticsTrackRegisterEntity);
        if(!save) {
            throw new ServiceException("物流注册单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流注册单" , dmpLogisticsTrackRegisterEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpLogisticsTrackRegisterEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpLogisticsTrackRegisterEntity.getId(), dmpLogisticsTrackRegisterEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpLogisticsTrackRegisterDTO.UpdateDTO updateDTO) {
        DmpLogisticsTrackRegisterEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流注册单"));
        DmpLogisticsTrackRegisterEntity dmpLogisticsTrackRegisterEntity =  BeanMapperUtils.map(DmpLogisticsTrackRegisterEntity.class, updateDTO);

        // 数据处理
        handleData(dmpLogisticsTrackRegisterEntity);
        log.info("编辑 开始修改物流注册单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpLogisticsTrackRegisterEntity);
        if(!save) {
            throw new ServiceException("物流注册单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流注册单日志数据，id：【{}】", dmpLogisticsTrackRegisterEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpLogisticsTrackRegisterEntity.getId(), "物流注册单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpLogisticsTrackRegisterEntity, null, dmpLogisticsTrackRegisterEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void batchAdd(List<DmpLogisticsTrackRegisterDTO.AddDTO> addDTOList) {
        if (CollectionUtil.isEmpty(addDTOList)){
            return;
        }
        List<DmpLogisticsTrackRegisterEntity> entityList = DmpTrackConverter.INSTANCE.converterRegisterToEntity(addDTOList);
        entityList.forEach(e ->{
            e.setId(getRecordByTrackNoAndTransportNo(e.getTrackNo(),e.getTransportNo()));
        });
        this.saveOrUpdateBatch(entityList);
    }

    private String getRecordByTrackNoAndTransportNo(String trackNo, String transportNo) {
        if (StrUtil.isBlank(trackNo) && StrUtil.isBlank(transportNo)){
            return null;
        }
        DmpLogisticsTrackRegisterEntity one = this.lambdaQuery().select(DmpLogisticsTrackRegisterEntity::getId).eq(DmpLogisticsTrackRegisterEntity::getTrackNo, trackNo)
                .eq(DmpLogisticsTrackRegisterEntity::getTransportNo, transportNo).last("limit 1").one();
        if (Objects.isNull(one)){
            return null;
        }
        return one.getId();

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpLogisticsTrackRegisterEntity dmpLogisticsTrackRegisterEntity) {
    // TODO 验证数据 & 数据赋值
        //数据是否存在

    }
}
