package com.erp.server.oms.kingdee.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerContactEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerContactService;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerService;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerContactService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.CustomerInvoiceService;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.KingdeeReceiptConditionService;
import com.erp.server.oms.service.OmsPushMsgService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 同步客户到金蝶
 *
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerServiceImpl implements SyncKingdeeCustomerService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerInvoiceService customerInvoiceService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private CustomerContactService customerContactService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;

    @Resource
    private  KingdeeReceiptConditionService kingdeeReceiptConditionService;
    
    @Resource
    private OmsPushMsgService omsPushMsgService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<DmpPushTaskEntity> syncDataToKingdee(CustomerInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户编号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            DmpPushTaskEntity pushTaskEntity = saveTask(entity, operate, resultMap);
            return Arrays.asList(pushTaskEntity);
        }

        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getUseOrgId()));
        String useOrgCode = accountingCompanyList.stream().filter(req -> req.getId().equals(entity.getUseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
        //使用组织
        resultMap.put("useOrgCode", useOrgCode);
        List<DictBasicDTO.ViewDTO> customerCompanyCategory = dictBasicService.getByKey("customerCompanyCategory");
        if (CollectionUtils.isNotEmpty(customerCompanyCategory)) {
            DictBasicDTO.ViewDTO viewDTO = customerCompanyCategory.stream().filter(req -> req.getValue().equals(entity.getCompanyCategoryDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            //公司类型
            resultMap.put("companyCategory", viewDTO.getRemark());
        }

        //客户名称
        resultMap.put("name", entity.getName());
        //客户分组
        resultMap.put("groupName", entity.getGroupName());
        //简称
        resultMap.put("shortName", entity.getShortName());
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(entity.getCountryId());
        //国家
        resultMap.put("countryCode", countryEntity.getKingdeeCode());

        if (StringUtils.isNotBlank(entity.getProvinceId())) {
            DictCityEntity province = sysUserFeign.getCityById(entity.getProvinceId());
            if (ObjectUtil.isNotEmpty(province)) {
                resultMap.put("province", province.getKingdeeCode());
            }
        }

        if (StringUtils.isNotBlank(entity.getCityId())) {
            DictCityEntity city = sysUserFeign.getCityById(entity.getCityId());
            if (ObjectUtil.isNotEmpty(city)) {
                resultMap.put("city", city.getKingdeeCode());
            }
        }
        //结算方
        if (StringUtils.isNotBlank(entity.getSettleCode())) {
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getSettleCode());
            resultMap.put("settleCode", customerInfoEntity.getCode());
        }
        //付款方
        if (StringUtils.isNotBlank(entity.getPayCode())) {
            //金蝶只能录入单个付款方，这里默认取第一条
            String[] split = entity.getPayCode().split(",");
            List<CustomerInfoEntity> customerInfoEntities = customerInfoService.listByIds(Arrays.asList(split));
            resultMap.put("payCode", customerInfoEntities.get(0).getCode());
        }

        List<InvoiceDTO.ViewDTO> viewDTOList = customerInvoiceService.listByMainId(entity.getId());
        List<InvoiceDTO.ViewDTO> viewDTOS = viewDTOList.stream().sorted(Comparator.comparing(InvoiceDTO.ViewDTO::getIsDefault).reversed()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(viewDTOS)) {
            InvoiceDTO.ViewDTO viewDTO = viewDTOS.get(MathUtil.ZERO);
            //发票抬头
            resultMap.put("head", viewDTO.getHead());
            resultMap.put("bankName", viewDTO.getBankName());
            resultMap.put("bankAccount", viewDTO.getBankAccount());
            resultMap.put("FInvoiceType", viewDTO.getType());
        }
        List<CurrencyDTO.ViewDTO> viewDTOS1 = sysUserFeign.listByCurrency(Arrays.asList(entity.getCurrency()));

        resultMap.put("currency", viewDTOS1.get(MathUtil.ZERO).getKingdeeCode());
        resultMap.put("remark", entity.getRemark());

        //销售员
        String sellerId = entity.getSellerId();
        String deptCode = "";



        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(useOrgCode);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerUserCode", kingSellerInfo.getUserPostCode());
                deptCode=kingSellerInfo.getDeptCode();
            }
        }

        resultMap.put("sellerDeptCode", deptCode);
        List<DictBasicDTO.ViewDTO> settleModeList = dictBasicService.getByKey("settleMode");
        DictBasicDTO.ViewDTO settleMode = settleModeList.stream().filter(req -> req.getValue().equals(entity.getSettleDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
        resultMap.put("settleModeCode", settleMode.getRemark());
        List<KingdeeReceiptConditionEntity> collectionTermsList = kingdeeReceiptConditionService.list();
        KingdeeReceiptConditionEntity collectionTerms = collectionTermsList.stream().filter(req -> req.getId().equals(entity.getConditionDict())).findFirst().orElse(new KingdeeReceiptConditionEntity());
        resultMap.put("collectionTermsCode", collectionTerms.getCode());
        List<CustomerContactEntity> customerContactList = customerContactService.listEntityByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(customerContactList)) {
            List<CustomerContactEntity> collect = customerContactList.stream().sorted(Comparator.comparing(CustomerContactEntity::getIsDefault).reversed()).collect(Collectors.toList());
            resultMap.put("defaultContact", collect.get(MathUtil.ZERO).getCode());
        }
        resultMap.put("customerContactList", customerContactList);
        resultMap.put("invoiceList", viewDTOS);
        List<CustomerAddressDTO.ViewDTO> customerAddressList = customerAddressService.listByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(customerAddressList)) {
            List<CustomerAddressDTO.ViewDTO> collect = customerAddressList.stream().sorted(Comparator.comparing(CustomerAddressDTO.ViewDTO::getIsDefault).reversed()).collect(Collectors.toList());
            resultMap.put("telNumber", collect.get(MathUtil.ZERO).getTelNumber());
            resultMap.put("person", collect.get(MathUtil.ZERO).getPerson());
            resultMap.put("address", collect.get(MathUtil.ZERO).getAddress());
        }
        for (CustomerAddressDTO.ViewDTO viewDTO : customerAddressList) {
            viewDTO.setDisabled(viewDTO.getDisabled() ? Boolean.FALSE : Boolean.TRUE);
        }
        resultMap.put("customerAddressList", customerAddressList);
        String platformType = entity.getPlatformType();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SALES_PLATFORM.getType(), platformType);
        String platformTypeKingdeeCode = "";
        if (dictBasic != null) {
            platformTypeKingdeeCode = dictBasic.getRemark();
        }
        resultMap.put("platformType", platformTypeKingdeeCode);
        String regionCode = countryEntity.getRegionCode();
        if (StrUtil.isNotBlank(regionCode)) {
            DictGlobalAreaEntity globalAreaEntity = sysUserFeign.getGlobalAreaById(regionCode);
            if (ObjectUtil.isNotEmpty(globalAreaEntity)) {
                resultMap.put("globalAreaCode", globalAreaEntity.getKingdeeCode());
            }
        }
        resultMap.put("disabled", entity.getDisabled());
        resultMap.put("operate", operate);
        //同步好客户信息后再同步客户联系人
        List<CustomerContactEntity> contactEntities = customerContactService.listEntityByMainId(entity.getId());
        resultMap.put("customerList", contactEntities);
        List<DmpPushTaskEntity> contractPushEnityLsit = new ArrayList<>();
        //生成任务
        DmpPushTaskEntity pushTaskEntity = saveTask(entity, operate, resultMap);
        contractPushEnityLsit.add(pushTaskEntity);
        //审核通过联系人发送金蝶
        contactEntities.forEach(obj -> {
            DmpPushTaskEntity taskEntity = syncKingdeeCustomerContactService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode());
            contractPushEnityLsit.add(taskEntity);
        });
        return contractPushEnityLsit;
    }


    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (CustomerInfoEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.CUSTOMER_INFO.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
        	//添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(entity.getId());
            taskFeignDTO.setSourceCode(entity.getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.CUSTOMER_INFO.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_CUSTOMER_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }
    	
    	OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_INFO.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
        
        return null;
    }
}
