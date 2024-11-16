package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WmsDataCompareImportDTO;
import com.erp.model.wms.entity.WmsDataCompareImportEntity;
import com.erp.server.wms.mapper.WmsDataCompareImportMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsDataCompareImportService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 数据对比导入文件信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@Service
public class WmsDataCompareImportServiceImpl extends SuperServiceImpl<WmsDataCompareImportMapper, WmsDataCompareImportEntity> implements WmsDataCompareImportService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsDataCompareImportDTO.AddDTO addDTO) {
        WmsDataCompareImportEntity wmsDataCompareImportEntity = new WmsDataCompareImportEntity();
        BeanMapperUtils.copy(addDTO, wmsDataCompareImportEntity);

        // 数据处理
        handleData(wmsDataCompareImportEntity);

        log.info("开始新增数据对比导入文件信息");
        boolean save = super.save(wmsDataCompareImportEntity);
        if(!save) {
            throw new ServiceException("数据对比导入文件信息保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "数据对比导入文件信息" , wmsDataCompareImportEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsDataCompareImportEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsDataCompareImportEntity.getId(), wmsDataCompareImportEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsDataCompareImportDTO.UpdateDTO updateDTO) {
        WmsDataCompareImportEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比导入文件信息");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "数据对比导入文件信息"));
        WmsDataCompareImportEntity wmsDataCompareImportEntity =  BeanMapperUtils.map(WmsDataCompareImportEntity.class, updateDTO);

        // 数据处理
        handleData(wmsDataCompareImportEntity);
        log.info("编辑 开始修改数据对比导入文件信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsDataCompareImportEntity);
        if(!save) {
            throw new ServiceException("数据对比导入文件信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录数据对比导入文件信息日志数据，id：【{}】", wmsDataCompareImportEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsDataCompareImportEntity.getId(), "数据对比导入文件信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsDataCompareImportEntity, null, wmsDataCompareImportEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsDataCompareImportEntity wmsDataCompareImportEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
