package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.FirstMileCartonEntity;
import com.erp.server.wms.mapper.FirstMileCartonMapper;
import com.erp.server.wms.service.FirstMileCartonDetailService;
import com.erp.server.wms.service.FirstMileCartonService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import java.util.*;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(FirstMileCartonDTO.AddDTO addDTO) {
        FirstMileCartonEntity firstMileCartonEntity = new FirstMileCartonEntity();
        BeanMapperUtils.copy(addDTO, firstMileCartonEntity);

        // 数据处理
        handleData(firstMileCartonEntity);

        log.info("开始新增发货单箱规信息");
        boolean save = super.save(firstMileCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        //新增详情信息
        firstMileCartonDetailService.add(addDTO, firstMileCartonEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(FirstMileCartonDTO.UpdateDTO updateDTO) {
        FirstMileCartonEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货单箱规信息"));
        FirstMileCartonEntity firstMileCartonEntity =  BeanMapperUtils.map(FirstMileCartonEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCartonEntity);
        log.info("编辑 开始修改发货单箱规信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        //新增详情信息
        firstMileCartonDetailService.update(updateDTO, firstMileCartonEntity.getId());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCartonEntity firstMileCartonEntity) {

    }
}
