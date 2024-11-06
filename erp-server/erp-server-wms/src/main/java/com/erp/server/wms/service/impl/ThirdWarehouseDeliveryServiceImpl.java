package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 三方仓发货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryMapper, ThirdWarehouseDeliveryEntity> implements ThirdWarehouseDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private ThirdWarehouseDeliveryDetailService detailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ThirdWarehouseDeliveryEntity entity) {

        // 生成单号
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }
        entity.getDetailEntityList().forEach(v->{
            v.setMainId(entity.getId());
        });
        detailService.saveBatch(entity.getDetailEntityList());
        return entity.getId();
    }

}
