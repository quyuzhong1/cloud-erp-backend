package com.erp.server.oms.dht.impl;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.server.oms.dht.DhtService;
import com.erp.server.oms.dht.SyncDhtService;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OmsPushMsgService;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.DhtCommonQueryReq;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAccountResp;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.service.DhtCommonService;
import com.sdk.oms.dht.service.DhtCustomerAccountService;
import com.sdk.oms.dht.service.DhtCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DhtServiceImpl implements DhtService {

    @Resource
    private DhtCustomerService dhtCustomerService;
    @Resource
    private DhtCommonService dhtCommonService;
    @Resource
    private DhtCustomerAccountService dhtCustomerAccountService;

    @Resource
    private DictBasicService dictBasicService;

    @Value("${dht.defaultUserMobile}")
    private String defaultUserMobile;

    @Override
    public CustomerDTO.ThirdCustomerAccountDTO queryCustomerAccountByCustomerCode(CustomerInfoEntity customerInfoEntity) {
        //先查询订货通客户id
        DhtCommonQueryReq customerReq = new DhtCommonQueryReq();
        DhtUserResp userResp = dhtCommonService.getUserByMobile(defaultUserMobile);
        customerReq.setCurrentOpenUserId(userResp.getEmpList().get(0).getOpenUserId());
        DhtCommonQueryReq.DataDTO customerDataDTO = DhtCommonQueryReq.DataDTO.builder()
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("erp_number__c")
                                        .fieldValues(Arrays.asList(customerInfoEntity.getCode()))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","erp_number__c","account_no", "name", "object_describe_api_name", "record_type", "customer_status__c","account_status"))
                        .build())
                .build();
        customerReq.setData(customerDataDTO);
        DhtBaseResp<DhtQueryCustomerResp> resp = dhtCustomerService.queryCustomer(customerReq);
        if(!resp.getErrorCode().equals(0)){
            throw new ServiceException("调用订货通查询客户接口失败，错误信息："+ JSON.toJSONString(resp));
        }
        if(CollectionUtils.isEmpty(resp.getData().getDataList())){
            throw new ServiceException("订货通客户信息为空，客户编码："+customerInfoEntity.getCode());
        }
        //通过客户id查询客户账户
        DhtCommonQueryReq customerAccountResp = new DhtCommonQueryReq();
        customerAccountResp.setCurrentOpenUserId(userResp.getEmpList().get(0).getOpenUserId());
        DhtCommonQueryReq.DataDTO customerAccountDataDTO = DhtCommonQueryReq.DataDTO.builder()
                .findExplicitTotalNum(false)
                .searchQueryInfo(DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.builder()
                        .offset(0)
                        .limit(10)
                        .fieldProjection(null)
                        .filters(null)
                        .orders(null)
                        .filters(Arrays.asList(
                                DhtCommonQueryReq.DataDTO.SearchQueryInfoDTO.FiltersDTO.builder()
                                        .fieldName("customer_id")
                                        .fieldValues(Arrays.asList(resp.getData().getDataList().get(0).getId()))
                                        .operator("EQ")
                                        .build()
                        ))
                        .fieldProjection(Arrays.asList("_id","account_balance", "name", "occupied_amount", "fund_account_id","customer_id","record_type","life_status"))
                        .build())
                .build();
        customerAccountResp.setData(customerAccountDataDTO);

        DhtBaseResp<DhtQueryCustomerAccountResp> accountRespDhtBaseResp = dhtCustomerAccountService.queryCustomerAccount(customerAccountResp);
        if(!accountRespDhtBaseResp.getErrorCode().equals(0)){
            throw new ServiceException("调用订货通查询客户账户接口失败，错误信息："+ JSON.toJSONString(accountRespDhtBaseResp));
        }
        List<DhtQueryCustomerAccountResp.DataListDTO> dataListDTOList = accountRespDhtBaseResp.getData().getDataList();
        dataListDTOList = dataListDTOList.stream().filter(v->v.getLifeStatus().equals("normal")).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(dataListDTOList)){
            throw new ServiceException("订货通客户账户信息为空，客户编码："+customerInfoEntity.getCode());
        }
        //查询账户Id配置
        List<DictBasicDTO.ViewDTO> viewDTOList = dictBasicService.getByKey("dhtAccountType");
        String accountId = viewDTOList.stream().filter(v->v.getName().equals("现金账户")).findFirst().get().getValue();
        String rebateAccountId = viewDTOList.stream().filter(v->v.getName().equals("返利账户")).findFirst().get().getValue();
        String creditAccountId = viewDTOList.stream().filter(v->v.getName().equals("授信账户")).findFirst().get().getValue();

        CustomerDTO.ThirdCustomerAccountDTO thirdCustomerAccountDTO = new CustomerDTO.ThirdCustomerAccountDTO();
        BigDecimal amount = dataListDTOList.stream().filter(v->v.getFundAccountId().equals(accountId)).map(DhtQueryCustomerAccountResp.DataListDTO::getAccountBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rebateAmount = dataListDTOList.stream().filter(v->v.getFundAccountId().equals(rebateAccountId)).map(DhtQueryCustomerAccountResp.DataListDTO::getAccountBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditAmount = dataListDTOList.stream().filter(v->v.getFundAccountId().equals(creditAccountId)).map(DhtQueryCustomerAccountResp.DataListDTO::getAccountBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        thirdCustomerAccountDTO.setAmount(amount);
        thirdCustomerAccountDTO.setRebateAmount(rebateAmount);
        thirdCustomerAccountDTO.setCreditAmount(creditAmount);
        return thirdCustomerAccountDTO;
    }
}
