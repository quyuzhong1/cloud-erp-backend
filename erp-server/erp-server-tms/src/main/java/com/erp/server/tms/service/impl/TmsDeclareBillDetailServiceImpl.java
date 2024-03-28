package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.erp.server.tms.mapper.TmsDeclareBillDetailMapper;
import com.erp.server.tms.service.TmsDeclareBillDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsDeclareBillDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
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
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录报关单明细日志数据，id：【{}】", tmsDeclareBillDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsDeclareBillDetailEntity.getId(), "报关单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsDeclareBillDetailEntity, null, tmsDeclareBillDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsDeclareBillDetailEntity tmsDeclareBillDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
