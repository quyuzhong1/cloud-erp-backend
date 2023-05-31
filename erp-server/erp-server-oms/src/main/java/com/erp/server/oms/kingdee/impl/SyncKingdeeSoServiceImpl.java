package com.erp.server.oms.kingdee.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Description TODO
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeSoServiceImpl implements SyncKingdeeSoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;


    /**
     * 销售订单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    public void syncDataToKingdee(SoInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //编码
        resultMap.put("code", entity.getCode());
        //要货日期
        resultMap.put("requireDate", entity.getRequireDate());
        //交货方式
        resultMap.put("deliveryMode", entity.getDeliveryMode());
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //创建日期
        resultMap.put("createDate", entity.getCreateTime().toLocalDate());
        String salesDeptId = entity.getSalesDeptId();
        //销售员
        String sellerId = entity.getSellerId();
        //获取部门id
        if (StringUtils.isNotBlank(salesDeptId)) {
            SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(salesDeptId);
            //销售部门
            if (!Objects.isNull(departmentDTO)) {
                resultMap.put("deptCode", departmentDTO.getCode());
            }
        }
        //获取员工
        if (StringUtils.isNotBlank(sellerId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(sellerId);
            //销售员
            if (!Objects.isNull(userDTO)) {
                resultMap.put("sellerCode", userDTO.getCode());
            }
        }
        //销售组织
        String salesOrgId = entity.getSalesOrgId();
        if (StringUtils.isNotBlank(salesOrgId)) {
            List<BaseIdDTO.CodeDTO> salesOrgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
            if (CollectionUtils.isNotEmpty(salesOrgList)) {
                resultMap.put("salesOrgCode", salesOrgList.get(0).getCode());
            }
        }
        //客户
        String customerId = entity.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (customerInfo != null) {
                resultMap.put("customerCode", customerInfo.getCode());
            }
        }
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //收货人
        resultMap.put("receiverName", entity.getReceiverName());
        String receiveAddressId = entity.getReceiveAddressId();
        CustomerAddressEntity addressEntity = customerAddressService.getById(receiveAddressId);
        String receiveAddress = addressEntity != null ? addressEntity.getAddress() : "";
        //收货地址
        resultMap.put("receiveAddress", receiveAddress);


    }
}
