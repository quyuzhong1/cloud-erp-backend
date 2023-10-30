package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.entity.CustomerB2cContactEntity;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerB2cContactService;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerB2cService;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步客户到金蝶
 *
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeCustomerB2cServiceImpl implements SyncKingdeeCustomerB2cService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerB2cService customerB2cService;

    @Resource
    private CustomerB2cInvoiceService customerB2cInvoiceService;

    @Resource
    private CustomerB2cSellerService customerB2cSellerService;

    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    @Resource
    private CustomerB2cContactService customerB2cContactService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private SyncKingdeeCustomerB2cContactService syncKingdeeCustomerB2cContactService;

    @Override
    public void syncDataToKingdee(CustomerB2cEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();

        //更新同步状态为待同步
        customerB2cService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(),"",operate);

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户编号
        resultMap.put("code", entity.getCode());
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
            CustomerB2cEntity customerInfoEntity = customerB2cService.getById(entity.getSettleCode());
            resultMap.put("settleCode", customerInfoEntity.getCode());
        }
        //付款方
        if (StringUtils.isNotBlank(entity.getPayCode())) {
            //金蝶只能录入单个付款方，这里默认取第一条
            String[] split = entity.getPayCode().split(",");
            List<CustomerB2cEntity> customerInfoEntities = customerB2cService.listByIds(Arrays.asList(split));
            resultMap.put("payCode", customerInfoEntities.get(0).getCode());
        }

        List<InvoiceDTO.ViewDTO> viewDTOList = customerB2cInvoiceService.listByMainId(entity.getId());
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
/*        List<SellerDTO.ViewDTO> sellerList = customerSellerService.listByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(sellerList)) {
            SellerDTO.ViewDTO viewDTO = sellerList.get(MathUtil.ZERO);
            String deptId = viewDTO.getDeptId();
            if (StringUtils.isNotBlank(deptId)) {
                SysDepartmentDTO dept = sysUserFeign.getUserDeptById(deptId);
                if (dept != null) {
                    resultMap.put("sellerDeptCode", dept.getCode());
                }
            }
            List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoDTOS = sysUserFeign.listUserKingdeePostByUserIds(Collections.singletonList(viewDTO.getSellerId()));
            if (CollectionUtils.isNotEmpty(userKingdeePostInfoDTOS)) {
                resultMap.put("sellerUserCode", userKingdeePostInfoDTOS.get(MathUtil.ZERO).getKingdeePostCode());
            }
        }*/
        //销售员
        String sellerId = entity.getSellerId();
        String deptCode = "";

        //当为空的时候 就取岗位表的
        KingdeePostDTO.FindUserKingdeePostInfoDTO findUserPostKingdee = new KingdeePostDTO.FindUserKingdeePostInfoDTO();
        findUserPostKingdee.setUserId(sellerId);
        findUserPostKingdee.setOrgCode("100");
        KingdeePostDTO.UserKingdeePostInfoDTO kingdeePost = kingdeeFeign.getUserKingdeePost(findUserPostKingdee);
        if (kingdeePost != null) {
            deptCode = kingdeePost.getKingdeeDeptCode();
        }
        resultMap.put("sellerDeptCode", deptCode);
        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode("100");
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeBusinessOperatorEntity kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerUserCode", kingSellerInfo.getKingdeePostCode());
            }
        }


        List<DictBasicDTO.ViewDTO> settleModeList = dictBasicService.getByKey("settleMode");
        DictBasicDTO.ViewDTO settleMode = settleModeList.stream().filter(req -> req.getValue().equals(entity.getSettleDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
        resultMap.put("settleModeCode", settleMode.getRemark());
        List<DictBasicDTO.ViewDTO> collectionTermsList = dictBasicService.getByKey("collectionTerms");
        DictBasicDTO.ViewDTO collectionTerms = collectionTermsList.stream().filter(req -> req.getValue().equals(entity.getConditionDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
        resultMap.put("collectionTermsCode", collectionTerms.getRemark());
        List<CustomerB2cContactEntity> customerContactList = customerB2cContactService.listEntityByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(customerContactList)) {
            List<CustomerB2cContactEntity> collect = customerContactList.stream().sorted(Comparator.comparing(CustomerB2cContactEntity::getIsDefault).reversed()).collect(Collectors.toList());
            resultMap.put("defaultContact", collect.get(MathUtil.ZERO).getCode());
        }
        resultMap.put("customerContactList", customerContactList);
        resultMap.put("invoiceList", viewDTOS);
        List<CustomerAddressDTO.ViewDTO> customerAddressList = customerB2cAddressService.listByMainId(entity.getId());
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
        List<CustomerB2cContactEntity> contactEntities = customerB2cContactService.listEntityByMainId(entity.getId());
        resultMap.put("customerList", contactEntities);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CUSTOMER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //审核通过发送金蝶
                contactEntities.forEach(obj -> syncKingdeeCustomerB2cContactService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
                //mq发送成更新业务表状态及时间
                return customerB2cService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.IN_SYNC.getCode(), "", operate);
            }
            return Boolean.TRUE;
        });
    }
}
