package com.erp.server.tms.schedule;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.TransferLogisticsAuthStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.tms.service.ProductRegistrationService;
import com.erp.server.tms.service.TransferLogisticsAuthService;
import com.erp.server.tms.service.TransferLogisticsService;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname ProductRegistrationJob
 * @Description 备案产品 job
 * @Date 2024-01-19 12:22
 * @Created by yl
 */
@Component
@Slf4j
@EnableScheduling
public class ProductRegistrationJob {

    @Resource
    private ProductRegistrationService productRegistrationService;

    @Resource
    private TransferLogisticsService transferLogisticsService;

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    @Resource
    private PlmTaskFeign plmTaskFeign;


    /**
     * 同步产品备案信息
     */
    @XxlJob("syncProductRegistration")
    public void syncProductRegistrationInfo() {
        //所有的备案信息
        List<ProductRegistrationEntity> productRegistrationList = productRegistrationService.list();
        List<TransferLogisticsSupplierDTO.AuthDTO> authList = transferLogisticsSupplierService.listAllAuth();
        String already = TransferLogisticsAuthStatusEnum.ALREADY.getCode();
        List<TransferLogisticsSupplierDTO.AuthDTO> alreadyAuthList = authList.stream().
                filter(a -> !a.getDisabled() && already.equals(a.getAuthStatus())).collect(Collectors.toList());
        List<ProductRegistrationEntity> saveOrUpdateList = new ArrayList<>(20);
        for (TransferLogisticsSupplierDTO.AuthDTO item : alreadyAuthList) {
            try {
                String authId = item.getAuthId();
                ApiResult<List<ProductRegistrationEntity>> result = transferLogisticsService.getAllProductInfo(authId);
                Boolean isSuccess = result.isSuccess();
                if (!isSuccess) {
                    continue;
                }
                List<ProductRegistrationEntity> findProductRegistrationList = result.getData();
                for (ProductRegistrationEntity entity : findProductRegistrationList) {
                    String skuNo = entity.getSkuNo();
                    String platform = entity.getDeclarePlatform();
                    String status = entity.getStatus();
                    ProductRegistrationEntity dbEntity = productRegistrationList.stream().
                            filter(
                                    p -> p.getSkuNo().equals(skuNo) && p.getDeclarePlatform().equals(platform)).
                            findFirst().orElse(null);
                    if (dbEntity != null) {
                        //状态是否一致
                        String dbStatus = dbEntity.getStatus();
                        //不一致修改
                        if (!status.equals(dbStatus)) {
                            dbEntity.setStatus(status);
                            saveOrUpdateList.add(dbEntity);
                        }

                    } else {
                        ProductRegistrationEntity addEntity = new ProductRegistrationEntity();
                        addEntity.setSkuNo(skuNo);
                        addEntity.setStatus(status);
                        addEntity.setDeclarePlatform(platform);
                        saveOrUpdateList.add(addEntity);
                    }

                }
            } catch (Exception e) {
                log.error("同步产品备案信息失败 {}", e.getMessage());
            }
        }
        List<ProductRegistrationEntity> addList = saveOrUpdateList.stream().
                filter(s -> StringUtils.isBlank(s.getSkuId())).collect(Collectors.toList());
        List<String> addSkuNoList = addList.stream().map(ProductRegistrationEntity::getSkuNo).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addSkuNoList)) {
            List<ProductDetailEntity> skuList = plmTaskFeign.listBySkuNos(addSkuNoList);
            for (ProductRegistrationEntity item : addList) {
                String skuNo = item.getSkuNo();
                ProductDetailEntity detailEntity = skuList.stream().filter(s -> skuNo.equals(s.getSkuNo())).
                        findFirst().orElse(null);
                if (detailEntity != null) {
                    item.setSkuId(detailEntity.getId());
                    item.setProductName(detailEntity.getName());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(saveOrUpdateList)) {
            productRegistrationService.saveOrUpdateBatch(saveOrUpdateList);
        }

    }
}
