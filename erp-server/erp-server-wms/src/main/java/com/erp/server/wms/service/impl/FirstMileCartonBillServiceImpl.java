package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.wms.entity.FirstMileCartonBillEntity;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.erp.server.wms.mapper.FirstMileCartonBillMapper;
import com.erp.server.wms.service.FirstMileCartonBillService;
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
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱子信息明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonBillServiceImpl extends SuperServiceImpl<FirstMileCartonBillMapper, FirstMileCartonBillEntity> implements FirstMileCartonBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCartonBillDTO.AddDTO addDTO) {
        FirstMileCartonBillEntity firstMileCartonBillEntity = new FirstMileCartonBillEntity();
        BeanMapperUtils.copy(addDTO, firstMileCartonBillEntity);

        // 数据处理
        handleData(firstMileCartonBillEntity);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.save(firstMileCartonBillEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        return new BaseResultDTO.AddDTO(firstMileCartonBillEntity.getId(), firstMileCartonBillEntity.getId());
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(FirstMileCartonBillEntity::getCartonId, cartonIds).remove();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(FirstMileCartonBillEntity::getMainId, mainIds).remove();
    }

    @Override
    public List<FirstMileCartonBillEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileCartonBillEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCartonBillEntity firstMileCartonBillEntity) {

    }
}
