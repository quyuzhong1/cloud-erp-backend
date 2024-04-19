package com.erp.server.oms.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.mapper.SoB2cErrorMapper;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

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
            soB2cErrorEntity.setDetailId(StringUtils.isNotBlank(addDTO.getDetailId()) ? addDTO.getDetailId() : "");
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
    public Boolean deleteDetail(SoB2cErrorDTO.DeleteDetailDTO dto) {
        Boolean result = baseMapper.deleteB2cErrorByDetailId(dto);
        if (result){
            // 检查历史明细是否存在
            Integer count = this.lambdaQuery()
                    .eq(SoB2cErrorEntity::getMainId, dto.getMainId())
                    .eq(SoB2cErrorEntity::getType, dto.getType())
                    .count();
            if(0 == count){
                soB2cService.removeSignError(dto.getMainId(),dto.getType());
            }
        }
        return true;
    }

    @Override
    public void deleteAndAddErrorBatch(SoB2cErrorDTO.AddAndDeleteDTO addAndDeleteDTO) {
        for (SoB2cErrorDTO.DeleteDTO deleteDTO : addAndDeleteDTO.getDeleteDTOList()) {
            this.delete(deleteDTO);
        }

        for (SoB2cErrorDTO.AddDTO addDTO : addAndDeleteDTO.getAddDTOList()) {
            this.add(addDTO);
        }
        //给订单赋值第三方平台发货单号
        soB2cService.updateShippingOrderNo(addAndDeleteDTO.getShippingOrderDTO());

    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
