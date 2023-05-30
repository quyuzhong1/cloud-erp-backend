package com.erp.server.oms.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.InvoiceTypeEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerService;
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
    private CustomerSellerService customerSellerService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public void syncDataToKingdee(CustomerInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //仓库名称
        resultMap.put("name", entity.getName());
        //客户编号
        resultMap.put("code", entity.getCode());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getUseOrgId()));
        String useOrgCode = accountingCompanyList.stream().filter(req -> req.getId().equals(entity.getUseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
        //使用组织
        resultMap.put("useOrgCode", useOrgCode);
        //客户名称
        resultMap.put("name", entity.getName());
        //简称
        resultMap.put("shortName", entity.getShortName());
        DictCountryEntity countryEntity = sysUserFeign.getCountryById(entity.getCountryId());
        //国家
        resultMap.put("countryCode", countryEntity.getKingdeeCode());

        List<InvoiceDTO.ViewDTO> viewDTOS = customerInvoiceService.listByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(viewDTOS)) {
            InvoiceDTO.ViewDTO viewDTO = viewDTOS.get(MathUtil.ZERO);
            //发票抬头
            resultMap.put("head", viewDTO.getHead());
            resultMap.put("bankName", viewDTO.getBankName());
            resultMap.put("bankAccount", viewDTO.getBankAccount());
            if (InvoiceTypeEnum.INVOICE.getCode().equals(viewDTO.getType())) {
                resultMap.put("FInvoiceType", InvoiceTypeEnum.INVOICE.getName());
                resultMap.put("FInvoiceType", 1);
            } else {
                resultMap.put("FInvoiceType", "增值税专用发票");
                resultMap.put("FInvoiceType", 2);
            }
        }
        List<CurrencyDTO.ViewDTO> viewDTOS1 = sysUserFeign.listByCurrency(Arrays.asList(entity.getCurrency()));

        resultMap.put("currency", viewDTOS1.get(MathUtil.ZERO).getKingdeeCode());
        resultMap.put("remark",entity.getRemark());
        List<SellerDTO.ViewDTO> sellerList = customerSellerService.listByMainId(entity.getId());
        if (CollectionUtils.isNotEmpty(sellerList)) {
            SellerDTO.ViewDTO viewDTO = sellerList.get(MathUtil.ZERO);
            String deptId = viewDTO.getDeptId();
            if (StringUtils.isNotBlank(deptId)) {
                SysDepartmentDTO dept = sysUserFeign.getUserDeptById(deptId);
                if (dept != null) {
                    resultMap.put("sellerDeptCode",dept.getCode());
                }
            }
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(viewDTO.getSellerId());
            resultMap.put("sellerUserCode",findUserDTO.getCode());
        }
        List<DictBasicDTO.ViewDTO> settleModeList = dictBasicService.getByKey("settleMode");
        DictBasicDTO.ViewDTO settleMode = settleModeList.stream().filter(req -> req.getValue().equals(entity.getSettleDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
        resultMap.put("settleModeCode",settleMode.getRemark());
        List<DictBasicDTO.ViewDTO> collectionTermsList = dictBasicService.getByKey("collectionTerms");
        DictBasicDTO.ViewDTO collectionTerms = collectionTermsList.stream().filter(req -> req.getValue().equals(entity.getConditionDict())).findFirst().orElse(new DictBasicDTO.ViewDTO());
        resultMap.put("collectionTermsCode",collectionTerms.getRemark());

        resultMap.put("invoiceList", viewDTOS);
        List<CustomerAddressDTO.ViewDTO> customerAddressList = customerAddressService.listByMainId(entity.getId());
        resultMap.put("customerAddressList", customerAddressList);
        resultMap.put("platformType", entity.getPlatformType().getKingdeeCode());

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_CUSTOMER_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return customerInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"", operate);
            }
            return Boolean.TRUE;
        });
    }
}
