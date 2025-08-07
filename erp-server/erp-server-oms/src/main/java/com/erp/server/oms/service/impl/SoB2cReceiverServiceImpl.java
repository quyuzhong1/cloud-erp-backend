package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderReceiverDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.ReflectUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.dto.excel.B2CCustomerImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cNfeStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysPartitionFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.listener.B2CCustomerImportExcelListener;
import com.erp.server.oms.mapper.SoB2cReceiverMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
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

    @Resource
    private SysPartitionFeign sysPartitionFeign;

    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Override
    public Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, SoB2cEntity soB2cEntity) {
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity, soB2cEntity.getId());
        if(StringUtils.isNotBlank(soB2cEntity.getShopId())){
            ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
            this.buildPartitionId(entity, shopInfoEntity);
        }else{
            this.buildPartitionId(entity, null);
        }
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, SoB2cEntity soB2cEntity) {
        SoB2cReceiverEntity old = super.getById(receiverDTO.getId());
        if(null == old){
           throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单买家信息表");
        }
        SoB2cReceiverEntity entity = new SoB2cReceiverEntity();
        BeanMapperUtils.copy(receiverDTO,entity);
        //处理买家信息
        handleSoB2cReceiver(entity, soB2cEntity.getId());
        //封装军区
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        this.buildPartitionId(entity,shopInfoEntity);
        boolean update = this.updateById(entity);
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
    public SoB2cReceiverEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<DictCountryEntity> countryList ,boolean notUpdateAddress) {
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
//                .filter(e -> CharSequenceUtil.isNotBlank(e.getCustomerId()))
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
                if(notUpdateAddress){
                    ReflectUtils.updateSpecifiedFieldsIfNotValue(newReceiverEntity, entity, SoB2cReceiverEntity.fieldsExistNotUpdate());
                }

                this.updateById(newReceiverEntity);
//                if (!this.updateById(entity2)){
//                    throw new ServiceException("[SoB2cReceiverEntity] 更新失败");
//                }
                return newReceiverEntity;
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

    @Override
    public void buildPartitionId(SoB2cReceiverEntity receiverEntity, ShopInfoEntity shopInfoEntity) {
        String country = receiverEntity.getCountry();
        if(Objects.nonNull(shopInfoEntity)){
            if(StringUtils.isNotBlank(shopInfoEntity.getDictCountryCode())&& !shopInfoEntity.getDictCountryCode().equals(DictValueEnum.ALL.getCode())){
                country = shopInfoEntity.getDictCountryCode();
            }else if (StringUtils.isNotBlank(shopInfoEntity.getCustomerId())){
                CustomerInfoEntity customerInfo = customerInfoService.getById(shopInfoEntity.getCustomerId());
                if(Objects.nonNull(customerInfo) && StringUtils.isNotBlank(customerInfo.getCountryId()) && !customerInfo.getCountryId().equals(DictValueEnum.ALL.getCode())){
                    country = customerInfo.getCountryId();
                }
            }
        }
        if(StringUtils.isBlank(country)){
            return;
        }
        receiverEntity.setPartitionId(sysPartitionFeign.getPartitionByCountry(country));
    }

    @Override
    public IPage<SoB2cReceiverEntity> pagePartitionIsNull(Page query) {
        return baseMapper.pagePartitionIsNull(query);
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


    @Override
    public void importB2cCustomerFile(MultipartFile excelFile, HttpServletResponse response) {
        B2CCustomerImportExcelListener excelListenerUtil = new B2CCustomerImportExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), B2CCustomerImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<B2CCustomerImportExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
            if (CollectionUtils.isEmpty(excelDateList)) {
                throw new ServiceException(ApiError.ERROR_95123);
            }
            //错误的
            List<B2CCustomerImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            //数据验证
            List<B2CCustomerImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            //处理验证成功数据
            handleImportSuccessList(successList, errorList);
            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/b2cCustomerUpdateError.xlsx";
                String name = "B2CCustomer";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
    }

    @Override
    public void updateInvoiceAddress(String soId, String invoiceAddress) {
        if (CharSequenceUtil.isNotBlank(soId)){
            this.lambdaUpdate().eq(SoB2cReceiverEntity::getMainId,soId).set(SoB2cReceiverEntity::getInvoiceAddress,invoiceAddress).update();
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新订单开票地址为：{}", invoiceAddress), ModuleTypeEnum.SO_B2C.getCode(), soId,"更新开票地址");
        }
    }

    private void handleImportSuccessList(List<B2CCustomerImportExcelDTO> successList, List<B2CCustomerImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //销售单号和平台订单号不能同时为空，其余字段非必填
        //销售单号和平台订单号同时存在时，以销售单号为准导入
        List<String> codeList = new ArrayList<>();
        List<String> platformCodeList = new ArrayList<>();
        for (B2CCustomerImportExcelDTO excelDTO : successList) {
            if (StringUtils.isNotBlank(excelDTO.getCode())) {
                codeList.add(excelDTO.getCode());
            }else if (StringUtils.isNotBlank(excelDTO.getPlatformCode())) {
                platformCodeList.add(excelDTO.getPlatformCode());
            }
        }
        if(CollectionUtils.isEmpty(codeList) && CollectionUtils.isEmpty(platformCodeList)){
            successList.forEach(e -> {
                e.setErrorMsg("销售单号和平台订单号不能同时为空");
                errorList.add(e);
            });
            return;
        }
        // 构建动态查询条件
        LambdaQueryChainWrapper<SoB2cEntity> soB2cEntityLambdaQueryChainWrapper = soB2cService.lambdaQuery();
        if (CollectionUtils.isNotEmpty(codeList)) {
            soB2cEntityLambdaQueryChainWrapper.in(SoB2cEntity::getCode, codeList);
        }
        if (CollectionUtils.isNotEmpty(platformCodeList)) {
            if (CollectionUtils.isNotEmpty(codeList)) {
                soB2cEntityLambdaQueryChainWrapper.or();
            }
            soB2cEntityLambdaQueryChainWrapper.in(SoB2cEntity::getPlatformCode, platformCodeList);
        }
        List<SoB2cEntity> list = soB2cEntityLambdaQueryChainWrapper.list();
        if(CollectionUtils.isEmpty(list)){
            successList.forEach(e -> {
                e.setErrorMsg("销售订单不存在");
                errorList.add(e);
            });
            return;
        }
        Map<String, SoB2cEntity> codeMap = list.stream().collect(Collectors.toMap(SoB2cEntity::getCode, t -> t, (oldValue, newValue) -> oldValue));
//        Map<String, SoB2cEntity> platformCodeMap = list.stream().collect(Collectors.toMap(SoB2cEntity::getPlatformCode, t -> t, (oldValue, newValue) -> oldValue));

        // 订单id集合
        List<String> idList = list.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
        List<SoB2cReceiverEntity> soB2cReceiverList = this.listByMainIds(idList);
        //b2c销售订单买家信息
        Map<String, SoB2cReceiverEntity> soB2cReceiverMap = soB2cReceiverList.stream().collect(Collectors.toMap(SoB2cReceiverEntity::getMainId, t -> t, (oldValue, newValue) -> oldValue));

        //国家字段只能填入国家二字码或三字码，且与系统基础数据做匹配校验，系统不存在时提示：国家信息不存在
        List<String> countrys = successList.stream().map(B2CCustomerImportExcelDTO::getCountry).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countrys);
        Map<String, String> countryMap = countryList.stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getNameCn, (oldValue, newValue) -> oldValue));

        //买家信息字段导入时，以表格导入字段为准，但是如果导入表格字段为空时，则该字段不做更新
        for (B2CCustomerImportExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            List<SoB2cEntity> soB2cList = new ArrayList<>();
            if(StringUtils.isNotBlank(excelDTO.getCode())){
                if(codeMap.containsKey(excelDTO.getCode())){
                    SoB2cEntity soB2cEntity = codeMap.get(excelDTO.getCode());
                    soB2cList.add(soB2cEntity);
                }else {
                    excelDTO.setErrorMsg("销售单号不存在");
                    errorList.add(excelDTO);
                    continue;
                }
            }else if(StringUtils.isNotBlank(excelDTO.getPlatformCode())){
                soB2cList = list.stream().filter(e -> e.getPlatformCode().equals(excelDTO.getPlatformCode())).collect(Collectors.toList());
                if(CollUtil.isEmpty(soB2cList)){
                    excelDTO.setErrorMsg("平台订单号不存在");
                    errorList.add(excelDTO);
                    continue;
                }
            }else {
                excelDTO.setErrorMsg("销售单号和平台订单号不能同时为空");
                errorList.add(excelDTO);
                continue;
            }

            //国家
            if(StringUtils.isNotBlank(excelDTO.getCountry())){
                if(!countryMap.containsKey(excelDTO.getCountry())){
                    errorMsgList.add("国家信息不存在");
                }else {
                    excelDTO.setCountryName(countryMap.get(excelDTO.getCountry()));
                }
            }
            for (SoB2cEntity soB2cEntity : soB2cList) {
                //订单只有待提交、审核不通过时允许导入更新
                if(!soB2cEntity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT) && !soB2cEntity.getApproveStatus().equals(ApproveStatusEnum.REJECT)){
                    errorMsgList.add("【"+soB2cEntity.getCode() + "】订单只有待提交、审核不通过时允许导入更新");
                }else{
                    //如果买家名为空，则使用收件人名
                    if (StringUtils.isBlank(excelDTO.getCustomerName())) {
                        excelDTO.setCustomerName(excelDTO.getReceiverName());
                    }

                    SoB2cReceiverEntity b2cReceiverEntity = soB2cReceiverMap.get(soB2cEntity.getId());
                    if (ObjectUtils.isEmpty(b2cReceiverEntity)) {
                        //新增买家信息
                        b2cReceiverEntity = new SoB2cReceiverEntity();
                        BeanMapper.copy(excelDTO, b2cReceiverEntity);
                        b2cReceiverEntity.setName(excelDTO.getCustomerName());

                        String customerId = saveB2cCustomer(excelDTO, soB2cEntity);
                        b2cReceiverEntity.setCustomerId(customerId);
                        b2cReceiverEntity.setMainId(soB2cEntity.getId());
                        this.save(b2cReceiverEntity);
                    } else {
                        //如果存在买家信息，则更新
                        BeanMapper.copy(excelDTO, b2cReceiverEntity);
                        //获取客户表id ，如果客户id为空则新增客户
                        if (StringUtils.isBlank(b2cReceiverEntity.getCustomerId())
                                || !b2cReceiverEntity.getName().equals(excelDTO.getCustomerName())
                        ) {
                            String customerId = saveB2cCustomer(excelDTO, soB2cEntity);
                            b2cReceiverEntity.setCustomerId(customerId);
                        }
                        b2cReceiverEntity.setName(excelDTO.getCustomerName());
                        this.updateById(b2cReceiverEntity);

                        // 记录主单操作日志
                        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
                        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
                        operateLogService.addModuleOperateLogByObj(soB2cReceiverMap.get(soB2cEntity.getId()), b2cReceiverEntity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                errorList.add(excelDTO);
            }
        }
    }

    private String saveB2cCustomer(B2CCustomerImportExcelDTO excelDTO, SoB2cEntity soB2cEntity) {
        CustomerB2cEntity dto = new CustomerB2cEntity();
        String customerId = IdWorker.getIdStr();
        dto.setId(customerId);
        dto.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CUSTC));
        dto.setPlatformType(soB2cEntity.getDictPlatform());
        dto.setName(excelDTO.getCustomerName());
        dto.setCountryId(excelDTO.getCountry());
        dto.setCurrency(soB2cEntity.getCurrency());
        dto.setSourceId(soB2cEntity.getId());
        dto.setSourceType("soB2c");
        dto.setApproveStatus(ApproveStatusEnum.APPROVE);
        customerB2cService.save(dto);
        return customerId;
    }
}
