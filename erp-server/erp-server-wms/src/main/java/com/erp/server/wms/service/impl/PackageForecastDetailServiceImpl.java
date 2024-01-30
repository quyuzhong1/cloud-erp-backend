package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.server.wms.mapper.PackageForecastDetailMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackageForecastDetailDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 组包预报详情 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@Service
public class PackageForecastDetailServiceImpl extends SuperServiceImpl<PackageForecastDetailMapper, PackageForecastDetailEntity> implements PackageForecastDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(String mainId, List<PackageForecastDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<PackageForecastDetailEntity> detailEntityList = BeanMapperUtils.copyList(PackageForecastDetailEntity.class, detailList);
        handleDataList(mainId,detailEntityList);
        this.saveBatch(detailEntityList);
    }

    /**
     * 处理数据
     * @param mainId
     * @param detailEntityList
     */
    private void handleDataList(String mainId, List<PackageForecastDetailEntity> detailEntityList) {
        for (PackageForecastDetailEntity item : detailEntityList) {
            item.setMainId(mainId);
        }
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackageForecastDetailDTO.UpdateDTO updateDTO) {
        PackageForecastDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报详情"));
        PackageForecastDetailEntity packageForecastDetailEntity = BeanMapperUtils.map(PackageForecastDetailEntity.class, updateDTO);

        // 数据处理
        handleData(packageForecastDetailEntity);
        log.info("编辑 开始修改组包预报详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(packageForecastDetailEntity);
        if (!save) {
            throw new ServiceException("组包预报详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录组包预报详情日志数据，id：【{}】", packageForecastDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), packageForecastDetailEntity.getId(), "组包预报详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packageForecastDetailEntity, null, packageForecastDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackageForecastDetailEntity packageForecastDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
