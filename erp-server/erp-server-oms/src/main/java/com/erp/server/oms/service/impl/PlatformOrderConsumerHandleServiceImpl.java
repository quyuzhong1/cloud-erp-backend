package com.erp.server.oms.service.impl;

import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 平台消费处理接口
 * @param
 * @author Jim
 * @date 2023/12/18 16:42
 * @Return
 */
@Slf4j
@Service
public class PlatformOrderConsumerHandleServiceImpl implements PlatformOrderConsumerHandleService {

    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoB2cFinanceService soB2cFinanceService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CustomerB2cService customerB2cService;
    @Resource
    private CustomerB2cAddressService customerB2cAddressService;
    @Resource
    private CustomerB2cContactService customerB2cContactService;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SysDictFeign sysDictFeign;

    @Override
    public void handleAll(PlatformOrderDTO dto) {
        SoB2cEntity mainEntity = this.checkAndSaveAll(dto);
        //订单状态
        String billStatus = mainEntity.getBillStatus();
        //已发货
        String shippedCode= SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String id=mainEntity.getId();
        //自动匹配订单规则
//        if (SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equalsIgnoreCase(billStatus)) {
//            //是否是平台仓订单 true 是
//            Boolean isPlatformWarehouseOrder= mainEntity.hasPlatformWarehouseOrder();
//            Map<String,Object> map=soB2cService.handleMatchJson(id,detailList,new HashMap<>());
//            if(isPlatformWarehouseOrder){
//                soB2cService.platformWarehouseOrderHandle(id,map);
//            }else{
//                //拉取订单正常处理
//                soB2cService.pullOrderHandle(id,detailList,map);
//            }
//        }
//        //已发货就要生成销售出库单
//        if(shippedCode.equalsIgnoreCase(billStatus)){
//            try {
//                soOutstockFeign.generateB2cSoOutstock(id);
//            }catch (Exception e){
//                log.error("B2C订单【{}】 更改状态为已发货， 生成销售出库单失败{}",mainEntity.getCode(),e.getMessage());
//            }
//
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public SoB2cEntity checkAndSaveAll(PlatformOrderDTO dto) {
        // 查询关联关系
        List<String> platformSkuList = dto.getDetails()
                .stream()
                .map(PlatformOrderDetailDTO::getPlatformSkuNo)
                .distinct().collect(Collectors.toList());
        Map<String, ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingDTOMap = soB2cDetailService.mapListingByPlatformSkuNo(platformSkuList, dto.getDictPlatform(), dto.getShopId());

        // 查询当前店铺信息
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (null == shopInfo) {
            throw new ServiceException("未找到订单的店铺" + dto.getShopId());
        }
        // 查询国家信息
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(shopInfo.getDictCountryCode()));

        // 主表更新或保存
        SoB2cEntity mainEntity = soB2cService.saveOrUpdateEntity(dto);
        // 详情更新或保存
        List<SoB2cDetailEntity> detailList = soB2cDetailService.saveOrUpdateEntity(dto, mainEntity, listingInfoWithSkuMappingDTOMap, shopInfo);
        //物流信息更新保存
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.saveOrUpdateEntity(dto, mainEntity);
        //买家信息更新保存
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.saveOrUpdateEntity(dto, mainEntity);

        //财务信息更新保存
        soB2cFinanceService.saveOrUpdateEntity(dto, mainEntity, logisticsEntity, detailList);

        //客户信息
        // 根据平台和名称判断
        CustomerB2cEntity customerB2cEntity = customerB2cService.findByPlatformAndName(dto.getDictPlatform(), receiverEntity.getName(), SourceTypeEnum.SO_B2C.getCode());

        customerB2cEntity = customerB2cService.saveOrUpdateEntity(customerB2cEntity, dto, mainEntity, receiverEntity, shopInfo.getDictCountryCode(), countryList);

        customerB2cAddressService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        customerB2cContactService.saveOrUpdateEntity(dto, customerB2cEntity, receiverEntity);

        if (StringUtils.isBlank(receiverEntity.getId())){
            receiverEntity.setCustomerId(customerB2cEntity.getId());
            if (!soB2cReceiverService.save(receiverEntity)){
                throw new ServiceException("[SoB2cReceiverEntity] 保存失败");
            }
        }
        return mainEntity;
    }
}
