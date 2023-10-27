package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步销售退货单到金蝶
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeSoReturnServiceImpl implements SyncKingdeeSoReturnService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(SoReturnEntity entity, String operate) {

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
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }

        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerInfoService.listByIds(Arrays.asList(entity.getCustomerId()));
        //退货单
        SoReturnEntity soReturnEntity = soReturnService.getById(entity.getId());
        //退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnDetailService.listDetailByMainId(entity.getId());
        //销售单
        SoInfoEntity soInfoEntity = soInfoService.getById(entity.getSourceId());
        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByMainIds(Arrays.asList(soInfoEntity.getId()));
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getWarehouseId()));
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        orgIdList.add(entity.getSalesOrgId());
        orgIdList.add(entity.getInventoryOrgId());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soInfoEntity.getCurrency()));
        //单据日期
        resultMap.put("billDate", entity.getBillDate());
        //组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //销售组织
            resultMap.put("salesOrgCode", salesOrgCode);
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //库存组织
            resultMap.put("inventoryOrgCode", inventoryOrgCode);
        }
     /*   //销售部门
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
            List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoDTOS = sysUserFeign.listUserKingdeePostByUserIds(Collections.singletonList(viewDTO.getSellerId()));
            if (CollectionUtils.isNotEmpty(userKingdeePostInfoDTOS)) {
                resultMap.put("sellerUserCode",userKingdeePostInfoDTOS.get(MathUtil.ZERO).getKingdeePostCode());
            }
        }*/

        //销售员
        String sellerId = entity.getSellerId();
        String deptCode = "";
        String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //当为空的时候 就取岗位表的
        KingdeePostDTO.FindUserKingdeePostInfoDTO findUserPostKingdee = new KingdeePostDTO.FindUserKingdeePostInfoDTO();
        findUserPostKingdee.setUserId(sellerId);
        findUserPostKingdee.setOrgCode(salesOrgCode);
        KingdeePostDTO.UserKingdeePostInfoDTO kingdeePost = kingdeeFeign.getUserKingdeePost(findUserPostKingdee);
        if (kingdeePost != null) {
            deptCode = kingdeePost.getKingdeeDeptCode();
        }
        resultMap.put("sellerDeptCode", deptCode);
        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(salesOrgCode);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeBusinessOperatorEntity kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerUserCode", kingSellerInfo.getKingdeePostCode());
            }
        }

        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            //收款条件
            List<DictBasicDTO.ViewDTO> collectionTermsList = dictBasicService.getByKey("collectionTerms");
            DictBasicDTO.ViewDTO viewDTO = collectionTermsList.stream().filter(req -> req.getValue().equals(customerInfoEntity.getCode())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            resultMap.put("collectionTerms", viewDTO.getRemark());
        }
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soReturnEntity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soReturnEntity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());

        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        //金蝶 FEntity:物料信息
        List<Map<String,Object>> list = new ArrayList<>();
        for (SoReturnDetailEntity detailEntity : returnDetailEntityList) {
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Map<String,Object> map = new HashMap<>();
            //退货原因
            if (StringUtils.isNotBlank(detailEntity.getReturnReasonDict())) {
                resultMap.put("returnReason", ReturnReasonEnum.getEnum(detailEntity.getReturnReasonDict()).getKingdeeCode());
            }
            //销售订单金蝶id
            map.put("soSyncKingdeeId", soInfoEntity.getSyncKingdeeId());
            //物料编码
            map.put("skuNo", detailEntity.getSkuNo());
            //退货数量
            map.put("returnQty", detailEntity.getReturnQty());
            map.put("salesQty", soDetailEntity.getQty());
            //单价
            map.put("price", soDetailEntity.getPrice());
            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(soDetailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(soDetailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            //是否赠品
            map.put("isGift", soDetailEntity.getIsGift());
            //金额
            map.put("amount", soDetailEntity.getAmount());
//            //税率
//            map.put("isGift", flagTaxRate);
            //退货类型
            map.put("returnType", detailEntity.getReturnTypeDict());
            //货主
            map.put("salesOrgCode", resultMap.get("salesOrgCode"));
            //仓库
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).map(WarehouseDTO.UpdateDTO::getKingdeeWarehouseCode).findFirst().orElse("");
                //仓库
                map.put("warehouseCode", warehouseCode);
            }
            //退货日期
            map.put("billDate", LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            //备注
            map.put("remark", detailEntity.getRemark());
            if (entity.getSourceType().equals(SourceTypeEnum.SO_INFO.getCode())) {
                //原单类型
                map.put("FSrcBillTypeID", "SAL_SaleOrder");
                //原单编号
                map.put("FSrcBillNo", soInfoEntity.getCode());
            }
            list.add(map);
        }
        resultMap.put("FEntityList", list);

        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (SoReturnEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_RETURN.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_RETURN_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        taskFeignDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
