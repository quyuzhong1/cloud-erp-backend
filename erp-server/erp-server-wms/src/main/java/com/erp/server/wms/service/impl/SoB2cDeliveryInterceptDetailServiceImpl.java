package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptDetailEntity;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptDetailMapper;
import com.erp.server.wms.service.SoB2cDeliveryInterceptDetailService;
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
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货拦截单详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryInterceptDetailServiceImpl extends SuperServiceImpl<SoB2cDeliveryInterceptDetailMapper, SoB2cDeliveryInterceptDetailEntity> implements SoB2cDeliveryInterceptDetailService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SoB2cDeliveryInterceptDTO.AddDTO addDTO, String mainId) {
        List<SoB2cDeliveryInterceptDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), SoB2cDeliveryInterceptDetailEntity.class);

        // 数据处理
        handleData(detailEntityList, mainId);

        log.info("开始新增b2c发货拦截单详情");
        boolean save = super.saveBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("b2c发货拦截单详情保存失败");
        }
    }

    @Override
    public List<SoB2cDeliveryInterceptDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cDeliveryInterceptDetailEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<SoB2cDeliveryInterceptDetailEntity> detailList, String mainId) {
        for (SoB2cDeliveryInterceptDetailEntity detailEntity : detailList) {
            detailEntity.setMainId(mainId);
        }
    // TODO 验证数据 & 数据赋值
    }
}
