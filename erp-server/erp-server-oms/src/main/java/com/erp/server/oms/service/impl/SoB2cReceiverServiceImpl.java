package com.erp.server.oms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderReceiverDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ReflectUtils;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.mapper.SoB2cReceiverMapper;
import com.erp.server.oms.service.CustomerB2cService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cReceiverService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单买家信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cReceiverServiceImpl extends SuperServiceImpl<SoB2cReceiverMapper, SoB2cReceiverEntity> implements SoB2cReceiverService {

    @Resource
    private CustomerB2cService customerB2cService;


    @Resource
    private OperateLogService operateLogService;

    @Lazy
    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity,mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, String mainId) {
        SoB2cReceiverEntity old = super.getById(receiverDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单买家信息表"));
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity,mainId);
        boolean update = this.updateById(entity);
        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg =  CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return update;
    }

    @Override
    public SoB2cReceiverEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cReceiverEntity::getMainId,mainId).one();
    }

    private List<SoB2cReceiverEntity> getListByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cReceiverEntity::getMainId,mainId).list();
    }
    @Override
    public List<SoB2cReceiverEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoB2cReceiverEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cReceiverEntity::getMainId,mainIds).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cReceiverEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<DictCountryEntity> countryList) {
        PlatformOrderReceiverDTO receiverDTO = dto.getReceiver();
        // 当前国家
        // 匹配来源三字码
        DictCountryEntity dictCountryEntity = countryList.stream()
                .filter(e-> (Objects.nonNull(receiverDTO) && e.getId().equalsIgnoreCase(receiverDTO.getCountry()))
                        || (Objects.nonNull(receiverDTO)
                            && StringUtils.isNotBlank(receiverDTO.getCountry())
                            && StringUtils.isNotBlank(e.getAlpha3())
                            && e.getAlpha3().equalsIgnoreCase(receiverDTO.getCountry())
                        ))
                .findFirst()
                .orElse(null);

        if (null == receiverDTO){
            //获取主表下物流记录
            SoB2cReceiverEntity oldEntity = getByMainId(mainEntity.getId());
            if( null == oldEntity){
                SoB2cReceiverEntity entity = B2cOrderConsumerConverter.INSTANCE.convertNewReceiver(null, mainEntity.getId());
                if (null != dictCountryEntity){
                    entity.setCountryName(dictCountryEntity.getNameCn());
                    entity.setCountry(dictCountryEntity.getId());
                }
                //处理买家信息
//                handleSoB2cReceiver(entity, mainEntity.getId());
                return entity;
            } else {
                return oldEntity;
            }
        }

        //获取主表下物流记录
        List<SoB2cReceiverEntity> listByMainId = getListByMainId(mainEntity.getId());
        //转map 比较是否存在记录 不存在则删除 存在则更新
        Map<String, SoB2cReceiverEntity> map = listByMainId.stream()
//                .filter(e -> StrUtil.isNotBlank(e.getCustomerId()))
                .collect(Collectors.toMap(SoB2cReceiverEntity::getMainId, Function.identity()));
            SoB2cReceiverEntity entity = map.get(mainEntity.getId());
            if (Objects.isNull(entity)){
                entity = B2cOrderConsumerConverter.INSTANCE.convertNewReceiver(receiverDTO, mainEntity.getId());
                entity.setTelNumber(receiverDTO.getTelNumber());
                entity.setReceiverTelNumber(receiverDTO.getReceiverTelNumber());
                //处理买家信息
//                handleSoB2cReceiver(entity, mainEntity.getId());
                if (null != dictCountryEntity){
                    entity.setCountryName(dictCountryEntity.getNameCn());
                    // 检查修正国家3字码为2字码
                    entity.checkAndSetCountry(dto.getReceiver().getCountry(), dictCountryEntity.getId());
                }
                if (StringUtils.isBlank(receiverDTO.getName())){
                    receiverDTO.setEmail(StringUtils.isBlank(receiverDTO.getEmail()) ? "" : receiverDTO.getEmail());
                }
                return entity;
            }else {
                SoB2cReceiverEntity newReceiverEntity = B2cOrderConsumerConverter.INSTANCE.convertNewReceiver(receiverDTO, mainEntity.getId());
                if (null != dictCountryEntity && StringUtils.isBlank(entity.getCountryName())){
                    entity.setCountryName(dictCountryEntity.getNameCn());
                    // 检查修正国家3字码为2字码
                    entity.checkAndSetCountry(dto.getReceiver().getCountry(), dictCountryEntity.getId());
                }
                newReceiverEntity.setId(entity.getId());
                // 指定有值不更新
                ReflectUtils.updateSpecifiedFieldsIfNotValue(newReceiverEntity, entity, SoB2cReceiverEntity.fieldsExistNotUpdate());

                this.updateById(newReceiverEntity);
//                if (!this.updateById(entity2)){
//                    throw new ServiceException("[SoB2cReceiverEntity] 更新失败");
//                }
                return entity;
            }

    }

    @Override
    public void updateFieldById(SoB2cReceiverEntity receiver) {
        if (Objects.isNull(receiver) || StringUtils.isBlank(receiver.getId())){
            return;
        }
        lambdaUpdate()
                .set(SoB2cReceiverEntity::getCountry,receiver.getCountry())
                .set(SoB2cReceiverEntity::getCountryName,receiver.getCountryName())
                .set(SoB2cReceiverEntity::getProvinceName,receiver.getProvinceName())
                .set(SoB2cReceiverEntity::getCityName,receiver.getCityName())
                .set(SoB2cReceiverEntity::getPostCode,receiver.getPostCode())
                .set(SoB2cReceiverEntity::getReceiverName,receiver.getReceiverName())
                .set(SoB2cReceiverEntity::getReceiverTelNumber,receiver.getReceiverTelNumber())
                .set(SoB2cReceiverEntity::getReceiverTaxNo,receiver.getReceiverTaxNo())
                .set(SoB2cReceiverEntity::getFirstAddress,receiver.getFirstAddress())
                .set(SoB2cReceiverEntity::getSecondAddress,receiver.getSecondAddress())
                .set(SoB2cReceiverEntity::getFullAddress,receiver.getFullAddress())
                .eq(SoB2cReceiverEntity::getId, receiver.getId()).eq(SoB2cReceiverEntity::getMainId,receiver.getMainId()).update();
    }

    /**
     * @description: 
     * @author Will
     * @date: 2023/8/31 9:56
     * @param entity
     
     */
    private void handleSoB2cReceiver (SoB2cReceiverEntity entity,String mainId) {
        //验证地址信息
        if (StringUtils.isBlank(entity.getFirstAddress()) && StringUtils.isBlank(entity.getSecondAddress())
                && StringUtils.isBlank(entity.getFullAddress())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_ADDRESS_NOT_NULL);
        }

        CustomerB2cEntity customerB2cEntity = customerB2cService.getById(entity.getCustomerId());
        if (ObjectUtils.isNotEmpty(customerB2cEntity)) {
            entity.setName(customerB2cEntity.getName());
        }
        String country=entity.getCountry();
        if(StringUtils.isNotBlank(country)){
            DictCountryEntity countryEntity=sysUserFeign.getCountryById(country);
            if(Objects.nonNull(countryEntity)){
                entity.setCountryName(countryEntity.getNameCn());
            }
        }
        entity.setMainId(mainId);
    }
}
