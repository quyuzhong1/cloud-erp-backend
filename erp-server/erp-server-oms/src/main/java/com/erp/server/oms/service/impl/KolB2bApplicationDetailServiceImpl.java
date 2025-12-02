package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.KolB2bApplicationDetailDTO;
import com.erp.model.oms.entity.KolB2bApplicationDetailEntity;
import com.erp.server.oms.mapper.KolB2bApplicationDetailMapper;
import com.erp.server.oms.service.KolB2bApplicationDetailService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * B2B寄养申请明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolB2bApplicationDetailServiceImpl extends SuperServiceImpl<KolB2bApplicationDetailMapper, KolB2bApplicationDetailEntity> implements KolB2bApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<KolB2bApplicationDetailDTO.AddDTO> detailList,String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1041, "B2B寄样申请明细单");
        }
        List<KolB2bApplicationDetailEntity> list = BeanMapperUtils.copyList(KolB2bApplicationDetailEntity.class, detailList);
        // 数据处理
        handleData(list);

        log.info("开始新增B2B寄养申请明细单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("B2B寄养申请明细单保存失败");
        }
        return save;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<KolB2bApplicationDetailDTO.UpdateDTO> detailList,String mainId) {
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_1041, "B2B寄样申请明细单");
        }
        List<KolB2bApplicationDetailEntity> list = BeanMapperUtils.copyList(KolB2bApplicationDetailEntity.class, detailList);

        // 数据处理
        handleData(list);
        log.info("编辑 开始修改B2B寄养申请明细单数据，mainId：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("B2B寄养申请明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteByMainId(String mainId) {
        return lambdaUpdate().eq(KolB2bApplicationDetailEntity::getMainId,mainId).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<KolB2bApplicationDetailEntity> list ) {
    // TODO 验证数据 & 数据赋值
    }
}
