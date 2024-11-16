package com.erp.server.wms.service.impl;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WmsDataCompareTempDTO;
import com.erp.model.wms.entity.WmsDataCompareTempEntity;
import com.erp.server.wms.mapper.WmsDataCompareTempMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsDataCompareTempService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * <p>
 * 数据对比对比加工临时表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@Service
public class WmsDataCompareTempServiceImpl extends SuperServiceImpl<WmsDataCompareTempMapper, WmsDataCompareTempEntity> implements WmsDataCompareTempService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsDataCompareTempDTO.AddDTO addDTO) {
        WmsDataCompareTempEntity wmsDataCompareTempEntity = new WmsDataCompareTempEntity();
        BeanMapperUtils.copy(addDTO, wmsDataCompareTempEntity);

        // 数据处理
        handleData(wmsDataCompareTempEntity);

        log.info("开始新增数据对比对比加工临时单");
        boolean save = super.save(wmsDataCompareTempEntity);
        if(!save) {
            throw new ServiceException("数据对比对比加工临时单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "数据对比对比加工临时单" , wmsDataCompareTempEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsDataCompareTempEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsDataCompareTempEntity.getId(), wmsDataCompareTempEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDataCompareTempDTO.UpdateDTO updateDTO) {
        WmsDataCompareTempEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比对比加工临时单");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比对比加工临时单"));
        WmsDataCompareTempEntity wmsDataCompareTempEntity =  BeanMapperUtils.map(WmsDataCompareTempEntity.class, updateDTO);

        // 数据处理
        handleData(wmsDataCompareTempEntity);
        log.info("编辑 开始修改数据对比对比加工临时单数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsDataCompareTempEntity);
        if(!save) {
            throw new ServiceException("数据对比对比加工临时单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录数据对比对比加工临时单日志数据，id：【{}】", wmsDataCompareTempEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsDataCompareTempEntity.getId(), "数据对比对比加工临时单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsDataCompareTempEntity, null, wmsDataCompareTempEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsDataCompareTempEntity wmsDataCompareTempEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
	public void batchInsertWmsDataCompareTemp(List<WmsDataCompareTempEntity> list) {
    	baseMapper.batchInsertWmsDataCompareTemp(list);
	}
    
	@Override
	public void deleteData(String taskId) {
		baseMapper.deleteData(taskId);
	}

}
