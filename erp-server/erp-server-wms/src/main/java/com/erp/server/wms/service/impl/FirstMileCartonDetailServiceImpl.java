package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.FirstMileCartonDTO;
import com.erp.model.wms.entity.FirstMileCartonDetailEntity;
import com.erp.server.wms.mapper.FirstMileCartonDetailMapper;
import com.erp.server.wms.service.FirstMileCartonDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileCartonDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 发货单箱子信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class FirstMileCartonDetailServiceImpl extends SuperServiceImpl<FirstMileCartonDetailMapper, FirstMileCartonDetailEntity> implements FirstMileCartonDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCartonDTO.AddDTO addDTO, String mainId) {
        FirstMileCartonDetailEntity firstMileCartonDetailEntity = new FirstMileCartonDetailEntity();
        BeanMapperUtils.copy(addDTO, firstMileCartonDetailEntity);

        // 数据处理
        handleData(firstMileCartonDetailEntity);

        log.info("开始新增发货单箱子信息单");
        boolean save = super.save(firstMileCartonDetailEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        return new BaseResultDTO.AddDTO(firstMileCartonDetailEntity.getId(), firstMileCartonDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileCartonDTO.UpdateDTO updateDTO, String mainId) {
        FirstMileCartonDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发货单箱子信息单"));
        FirstMileCartonDetailEntity firstMileCartonDetailEntity =  BeanMapperUtils.map(FirstMileCartonDetailEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCartonDetailEntity);
        log.info("编辑 开始修改发货单箱子信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCartonDetailEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCartonDetailEntity firstMileCartonDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
