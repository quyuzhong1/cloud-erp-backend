package com.erp.server.wms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.FirstMileCartonEntity;
import com.erp.server.wms.mapper.FirstMileCartonMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱规信息 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonServiceImpl extends SuperServiceImpl<FirstMileCartonMapper, FirstMileCartonEntity> implements FirstMileCartonService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private FirstMileCartonDetailService firstMileCartonDetailService;
    @Autowired
    private FirstMileCartonBillService firstMileCartonBillService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FirstMileCartonDTO.AddDTO addDTO, String mainId) {
        FirstMileCartonEntity firstMileCartonEntity = new FirstMileCartonEntity();
        BeanMapperUtils.copy(addDTO, firstMileCartonEntity);

        // 数据处理
        handleData(firstMileCartonEntity, mainId);

        log.info("开始新增发货单箱规信息");
        boolean save = super.saveOrUpdate(firstMileCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        //新增详情信息
        firstMileCartonDetailService.add(addDTO, firstMileCartonEntity.getId(), mainId);
    }

    @Override
    public List<FirstMileCartonEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FirstMileCartonEntity::getMainId, mainIds)
                .orderByAsc(FirstMileCartonEntity::getCreateTime, FirstMileCartonEntity::getId)
                .list();
    }

    @Override
    public List<FirstMileCartonDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo) {
        return baseMapper.listPackingQtyByMainId(mainId, boxSpecNo);
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(FirstMileCartonEntity::getMainId, mainIds).remove();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCartonEntity firstMileCartonEntity, String mainId) {
        firstMileCartonEntity.setMainId(mainId);
    }
}
