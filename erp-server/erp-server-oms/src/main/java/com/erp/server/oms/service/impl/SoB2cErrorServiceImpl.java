package com.erp.server.oms.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.mapper.SoB2cErrorMapper;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单异常表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@Service
public class SoB2cErrorServiceImpl extends ServiceImpl<SoB2cErrorMapper, SoB2cErrorEntity> implements SoB2cErrorService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(SoB2cErrorDTO.AddDTO addDTO) {
        //记录是否已存在
        SoB2cErrorEntity soB2cErrorEntity = this.getByMainIdAndType(addDTO.getMainId(),addDTO.getType());
        if (Objects.nonNull(soB2cErrorEntity)){
            soB2cErrorEntity.setParamJson(addDTO.getParamJson());
            soB2cErrorEntity.setMessage(addDTO.getMessage());
        }else {
            soB2cErrorEntity = new SoB2cErrorEntity();
            soB2cErrorEntity.setParamJson(addDTO.getParamJson());
            soB2cErrorEntity.setMessage(addDTO.getMessage());
            soB2cErrorEntity.setMainId(addDTO.getMainId());
            soB2cErrorEntity.setType(addDTO.getType());
        }
        boolean save = super.saveOrUpdate(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        soB2cService.addSignError(soB2cErrorEntity.getMainId(),soB2cErrorEntity.getType());
        return true;
    }




    /** 
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-20 11:24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(SoB2cErrorDTO.DeleteDTO dto) {
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    @Override
    public void generateErrorOrder(String mainId, String type, String message, String paramJson, String returnJson) {
        SoB2cErrorEntity soB2cErrorEntity = new SoB2cErrorEntity();
        soB2cErrorEntity.setMainId(mainId);
        soB2cErrorEntity.setType(type);
        soB2cErrorEntity.setMessage(message);
        soB2cErrorEntity.setParamJson(paramJson);
        soB2cErrorEntity.setReturnJson(returnJson);
        this.save(soB2cErrorEntity);
        soB2cService.addSignError(mainId,type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeErrorOrder(String mainId, String type) {
        SoB2cErrorDTO.DeleteDTO dto=new SoB2cErrorDTO.DeleteDTO();
        dto.setType(type);
        dto.setMainId(mainId);
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    /**
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2023-12-22 9:08
     */
    @Override
    public SoB2cErrorDTO.ViewDTO info(SoB2cErrorDTO.InfoDTO dto) {
        SoB2cErrorDTO.ViewDTO viewDTO=new SoB2cErrorDTO.ViewDTO();
        SoB2cErrorEntity entity = this.getByMainIdAndType(dto.getId(),dto.getType());
        if(Objects.nonNull(entity)){
             BeanMapperUtils.copy(entity,viewDTO);
        }
        return viewDTO;
    }



    @Override
    public SoB2cErrorEntity getByMainIdAndType(String mainId, String type) {
       return this.lambdaQuery().eq(SoB2cErrorEntity::getMainId, mainId).
                eq(SoB2cErrorEntity::getType,type).
               orderByDesc(SoB2cErrorEntity::getCreateTime).
               last("LIMIT 1").
               one();
    }

    @Override
    public void deleteByCodeAndType(String soCode, String type) {
         baseMapper.deleteByCodeAndType(soCode,type);
    }

    @Override
    public void deleteErrorByMainIds(SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO) {
        if (Objects.isNull(batchDeleteDTO) || CollectionUtils.isEmpty(batchDeleteDTO.getMainIds())){
            return;
        }
        Boolean result = baseMapper.batchDeleteB2cError(batchDeleteDTO);
        if(result){
            soB2cService.batchRemoveSignError(batchDeleteDTO.getMainIds(),batchDeleteDTO.getType());
        }
    }

    @Override
    public void batchAddSoB2cError(SoB2cErrorDTO.BatchAdd batchAdd) {
        if (Objects.isNull(batchAdd) || CollectionUtils.isEmpty(batchAdd.getMainIds()) || StringUtil.isEmpty(batchAdd.getType())){
            return;
        }
        batchAdd.getMainIds().forEach(mainId ->{
            SoB2cErrorEntity soB2cErrorEntity = this.getByMainIdAndType(mainId,batchAdd.getType());
            if (Objects.nonNull(soB2cErrorEntity)){
                soB2cErrorEntity.setParamJson(batchAdd.getParamJson());
                soB2cErrorEntity.setMessage(batchAdd.getMessage());
                this.updateById(soB2cErrorEntity);
            }else {
                soB2cErrorEntity = new SoB2cErrorEntity();
                soB2cErrorEntity.setParamJson(batchAdd.getParamJson());
                soB2cErrorEntity.setMessage(batchAdd.getMessage());
                soB2cErrorEntity.setMainId(mainId);
                soB2cErrorEntity.setType(batchAdd.getType());
                this.save(soB2cErrorEntity);
            }
            soB2cService.addSignError(mainId,batchAdd.getType());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO retrySoOutStock(String soBcId) {
        SoB2cEntity soB2cEntity = soB2cService.getById(soBcId);
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        SoB2cErrorEntity errorEntity = this.getByMainIdAndType(soB2cEntity.getId(), SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
        if (null == errorEntity){
            return BatchResultDTO.success(soBcId, soB2cEntity.getCode(), "重试成功");
        }
        // TODO
//        soOutstockFeign.generateB2cSoOutstock();
        //删除订单异常记录
        SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
        deleteDTO.setMainId(soB2cEntity.getId());
        deleteDTO.setType(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
        this.delete(deleteDTO);

        return BatchResultDTO.success(soBcId, soB2cEntity.getCode(), "重试执行成功");
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
