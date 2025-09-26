package com.erp.server.srm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.srm.dto.PayableDetailDTO;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.erp.server.srm.mapper.PayableDetailMapper;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.PayableDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2025-09-24
 */
@Slf4j
@Service
public class PayableDetailServiceImpl extends SuperServiceImpl<PayableDetailMapper, PayableDetailEntity> implements PayableDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PayableDetailDTO.AddDTO addDTO) {
        PayableDetailEntity payableDetailEntity = new PayableDetailEntity();
        BeanMapperUtils.copy(addDTO, payableDetailEntity);

        // 数据处理
        handleData(payableDetailEntity);

        log.info("开始新增");
        boolean save = super.save(payableDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , payableDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, payableDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(payableDetailEntity.getId(), payableDetailEntity.getId());
    }

    @Override
    public List<PayableDetailEntity> listMainIdList(List<String> mainIdList) {
        if(CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(PayableDetailEntity::getMainId,mainIdList).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PayableDetailEntity payableDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
