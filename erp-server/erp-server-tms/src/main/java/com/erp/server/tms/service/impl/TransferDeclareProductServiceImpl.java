package com.erp.server.tms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TransferDeclareProductDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.mapper.TransferDeclareProductMapper;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TransferDeclareProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 中转报关产品 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@Service
public class TransferDeclareProductServiceImpl extends SuperServiceImpl<TransferDeclareProductMapper, TransferDeclareProductEntity> implements TransferDeclareProductService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private SoB2cFeign soB2cFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareProductDTO.AddDTO addDTO) {
        TransferDeclareProductEntity transferDeclareProductEntity = new TransferDeclareProductEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareProductEntity);

        // 数据处理
        handleData(transferDeclareProductEntity);

        log.info("开始新增中转报关产品");
        boolean save = super.save(transferDeclareProductEntity);
        if(!save) {
            throw new ServiceException("中转报关产品保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转报关产品" , transferDeclareProductEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, transferDeclareProductEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(transferDeclareProductEntity.getId(), transferDeclareProductEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareProductDTO.UpdateDTO updateDTO) {
        TransferDeclareProductEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关产品"));
        TransferDeclareProductEntity transferDeclareProductEntity =  BeanMapperUtils.map(TransferDeclareProductEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareProductEntity);
        log.info("编辑 开始修改中转报关产品数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareProductEntity);
        if(!save) {
            throw new ServiceException("中转报关产品保存失败");
        }
        

        // 记录主单操作日志
            log.info("编辑 开始记录中转报关产品日志数据，id：【{}】", transferDeclareProductEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareProductEntity.getId(), "中转报关产品");
        
        operateLogService.addModuleOperateLogByObj(old, transferDeclareProductEntity, null, transferDeclareProductEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferDeclareProductEntity> listByDeclareDetailIds(List<String> declareDetailIds) {
        if (CollectionUtils.isEmpty(declareDetailIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferDeclareProductEntity::getDeclareDetailId, declareDetailIds).list();
    }

    @Override
    public List<TransferDeclareProductEntity> listByDeclareIds(List<String> declareIds) {
        if (CollectionUtils.isEmpty(declareIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(TransferDeclareProductEntity::getDeclareId, declareIds).list();
    }

    @Override
    public void saveOrUpdateTransferDeclareProducts(List<TransferDeclareDetailEntity> transferDeclareDetailEntities) {
        List<String> soIds = transferDeclareDetailEntities.stream().map(TransferDeclareDetailEntity::getSoId).collect(Collectors.toList());
        Map<String, TransferDeclareDetailEntity> detailEntityMap = transferDeclareDetailEntities.stream().collect(Collectors.toMap(TransferDeclareDetailEntity::getSoId, Function.identity()));
        List<com.erp.model.oms.dto.TransferDeclareProductDTO> productDTOS = soB2cFeign.getTransferDeclareProductBySoIds(soIds);
        if (CollectionUtils.isEmpty(productDTOS)){
            return;
        }
        productDTOS.forEach(transferDeclareProductDTO -> {
            TransferDeclareProductEntity entity = new TransferDeclareProductEntity();
            BeanUtil.copyProperties(transferDeclareProductDTO, entity);
            entity.setFromDeclarePrice(transferDeclareProductDTO.getDeclarePrice());
            entity.setFromCurrency(transferDeclareProductDTO.getDeclareCurrency());
            entity.setToDeclarePrice(transferDeclareProductDTO.getDestDeclarePrice());
            entity.setToCurrency(transferDeclareProductDTO.getDestCurrency());
            TransferDeclareDetailEntity transferDeclareDetailEntity = detailEntityMap.get(transferDeclareProductDTO.getSoId());
            if (Objects.nonNull(transferDeclareDetailEntity)){
                entity.setDeclareId(transferDeclareDetailEntity.getMainId());
                entity.setDeclareDetailId(transferDeclareDetailEntity.getId());
            }
            //检查记录是否已存在
            saveOrUpdateProduct(entity);
        });
    }

    @Override
    public void removeByDeclareDetailIds(List<String> deleteIds) {
        if (CollectionUtils.isNotEmpty(deleteIds)){
            lambdaUpdate().in(TransferDeclareProductEntity::getDeclareDetailId,deleteIds).remove();
        }
    }

    private void saveOrUpdateProduct(TransferDeclareProductEntity entity) {
        //检查数据是否已存在
        if (CharSequenceUtil.isNotEmpty(entity.getDeclareId()) && CharSequenceUtil.isNotEmpty(entity.getDeclareDetailId())
                && CharSequenceUtil.isNotEmpty(entity.getSoDetailId()) && CharSequenceUtil.isNotEmpty(entity.getSkuNo())){
            List<TransferDeclareProductEntity> list = lambdaQuery().eq(TransferDeclareProductEntity::getDeclareId, entity.getDeclareId())
                    .eq(TransferDeclareProductEntity::getDeclareDetailId, entity.getDeclareDetailId())
                    .eq(TransferDeclareProductEntity::getSoDetailId, entity.getSoDetailId())
                    .eq(TransferDeclareProductEntity::getSkuNo, entity.getSkuNo()).list();
            if (CollectionUtils.isNotEmpty(list)){
                entity.setId(list.get(0).getId());
            }
        }
        this.saveOrUpdate(entity);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareProductEntity transferDeclareProductEntity) {
    
    }
}
