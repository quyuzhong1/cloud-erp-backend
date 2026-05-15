package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.AfterSalePackStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;
import com.erp.model.wms.entity.AfterSalePackEntity;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.mapper.AfterSalePackDetailMapper;
import com.erp.server.wms.service.AfterSalePackDetailService;
import com.erp.server.wms.service.AfterSalePackService;
import com.erp.server.wms.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 售后装箱明细表 服务实现类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@Service
public class AfterSalePackDetailServiceImpl extends SuperServiceImpl<AfterSalePackDetailMapper, AfterSalePackDetailEntity> implements AfterSalePackDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private AfterSalePackService afterSalePackService;

    @Resource
    private ProductDetailFeign productDetailFeign;

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSalePackDetailDTO.UpdateDTO addOrUpdateDTO) {
        AfterSalePackDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细单"));
        AfterSalePackEntity afterSalePackEntity = afterSalePackService.getById(old.getMainId());
        if (afterSalePackEntity == null) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单");
        }
        // 箱唛状态不等于已封箱，不可以操作
        if (!AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(afterSalePackEntity.getPackStatus())) {
            throw new ServiceException("箱唛状态不等于已封箱，不可以操作");
        }
        // 操作类型不为空，表示移除sku操作
        if (StringUtils.isNotBlank(addOrUpdateDTO.getOperation())) {
            super.removeById(old.getId());
        } else {
            if (addOrUpdateDTO.getUpdateQty() == null || addOrUpdateDTO.getUpdateQty() == 0) {
                throw new ServiceException("新增或者减少数量时，更新数量必填且不能为0");
            }
            log.info("编辑 开始修改售后装箱明细单数据，id：【{}】", old.getId());
            old.setPackQty(old.getPackQty() + addOrUpdateDTO.getUpdateQty());
            boolean save = super.updateById(old);
            if (!save) {
                throw new ServiceException("售后装箱明细单保存失败");
            }
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱明细单日志数据，id：【{}】", old.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getId(), "售后装箱明细单");
        operateLogService.addModuleOperateLogByObj(old, old, null, old.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public AfterSalePackDetailDTO.ViewDTO view(String id) {
        AfterSalePackDetailEntity afterSalePackDetailEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱明细单数据"));
        return BeanMapperUtils.map(AfterSalePackDetailDTO.ViewDTO.class, afterSalePackDetailEntity);
    }

    @Override
    public List<AfterSalePackDetailDTO.ViewDTO> listByCode(String code) {
        AfterSalePackDTO.ViewDTO afterSalePackViewDTO = afterSalePackService.viewByCode(code);
        List<AfterSalePackDetailEntity> list = lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, afterSalePackViewDTO.getId())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException("未找到售后装箱明细数据");
        }
        // 取出所有不为空的skuId数据
        List<String> skuIdList = list.stream()
                .map(AfterSalePackDetailEntity::getSkuId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        return Collections.emptyList();
    }

}
