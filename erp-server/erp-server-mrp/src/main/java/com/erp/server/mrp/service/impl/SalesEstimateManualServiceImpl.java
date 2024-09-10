package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.SalesEstimateManualDTO;
import com.erp.model.mrp.entity.SalesEstimateManualEntity;
import com.erp.server.mrp.mapper.SalesEstimateManualMapper;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.SalesEstimateManualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 运营销量预估 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-05
 */
@Slf4j
@Service
public class SalesEstimateManualServiceImpl extends SuperServiceImpl<SalesEstimateManualMapper, SalesEstimateManualEntity> implements SalesEstimateManualService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SalesEstimateManualDTO.UpdateDTO updateDTO,String replenishmentId) {
        SalesEstimateManualEntity salesEstimateManualEntity =  BeanMapperUtils.map(SalesEstimateManualEntity.class, updateDTO);

        SalesEstimateManualEntity old = this.getByReplenishmentId(replenishmentId);
        if (ObjectUtil.isNotEmpty(old)) {
            salesEstimateManualEntity.setId(old.getId());
        }
        // 数据处理
        handleData(salesEstimateManualEntity);
        log.info("编辑 开始修改运营销量预估数据");
        boolean save = super.saveOrUpdate(salesEstimateManualEntity);
        if(!save) {
            throw new ServiceException("运营销量预估保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录运营销量预估日志数据，id：【{}】", salesEstimateManualEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), salesEstimateManualEntity.getId(), "运营销量预估");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, salesEstimateManualEntity, null, salesEstimateManualEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public List<SalesEstimateManualEntity> listByReplenishmentDetailIds(List<String> detailIds) {
        return list(Wrappers.<SalesEstimateManualEntity>lambdaQuery().in(SalesEstimateManualEntity::getReplenishmentId, detailIds));
    }

    /**
     * 根据补货建议id查询
     * @author will
     * @date 2024/9/5 16:05
     * @param replenishmentId
     * @return SalesEstimateManualEntity
     */
    private SalesEstimateManualEntity getByReplenishmentId (String replenishmentId) {
       return lambdaQuery().eq(SalesEstimateManualEntity::getReplenishmentId,replenishmentId).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SalesEstimateManualEntity salesEstimateManualEntity) {
        LocalDate now = LocalDate.now();
        //本月天数
        Integer thisDays = now.getDayOfMonth();
        //本月总天数
        Integer thisMonthDays = now.getMonth().maxLength();
        //计算当月销量剩余预估
        BigDecimal currentMonthSalesQty = ObjectUtil.isEmpty(salesEstimateManualEntity.getCurrentMonthSalesQty()) ? BigDecimal.ZERO : salesEstimateManualEntity.getCurrentMonthSalesQty();

        /**
         * 当月剩余有效预估 = 当月初始整月预估值 / 当月的天数 * 剩余天数
         */
        Integer days = thisMonthDays - thisDays;
        BigDecimal currentMonthSurplusSalesQty = MathUtil.multiply(MathUtil.divide(currentMonthSalesQty,new BigDecimal(thisMonthDays)),new BigDecimal(days));
        salesEstimateManualEntity.setCurrentMonthSurplusSalesQty(currentMonthSurplusSalesQty);
    }

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        System.out.println(now.getMonth().maxLength());
    }
}
