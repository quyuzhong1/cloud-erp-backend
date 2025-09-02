package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsDeclareBillDetailDTO;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.server.tms.mapper.TmsDeclareBillDetailMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsDeclareBillDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 报关单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Slf4j
@Service
public class TmsDeclareBillDetailServiceImpl extends SuperServiceImpl<TmsDeclareBillDetailMapper, TmsDeclareBillDetailEntity> implements TmsDeclareBillDetailService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(TmsDeclareBillEntity tmsDeclareBillEntity, List<TmsDeclareBillDetailEntity> detailEntityList) {
        detailEntityList.forEach(v->v.setMainId(tmsDeclareBillEntity.getId()));
        return super.saveBatch(detailEntityList);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsDeclareBillDetailDTO.UpdateDTO updateDTO) {
        TmsDeclareBillDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "报关单明细"));
        TmsDeclareBillDetailEntity tmsDeclareBillDetailEntity =  BeanMapperUtils.map(TmsDeclareBillDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsDeclareBillDetailEntity);
        log.info("编辑 开始修改报关单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsDeclareBillDetailEntity);
        if(!save) {
            throw new ServiceException("报关单明细保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录报关单明细日志数据，id：【{}】", tmsDeclareBillDetailEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), tmsDeclareBillDetailEntity.getId(), "报关单明细");
        
        operateLogService.addModuleOperateLogByObj(old, tmsDeclareBillDetailEntity, null, tmsDeclareBillDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TmsDeclareBillDetailEntity> listByMainIds(List<String> mainIds) {

        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsDeclareBillDetailEntity::getMainId, mainIds).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsDeclareBillDetailEntity tmsDeclareBillDetailEntity) {
    
    }
}
