package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售订单信息 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class SoInfoServiceImpl extends SuperServiceImpl<SoInfoMapper, SoInfoEntity> implements SoInfoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    @Override
    public String add(SoInfoDTO.AddDTO dto) {
        //id
        String id = IdWorker.getIdStr();
        SoInfoEntity addEntity = new SoInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
        addEntity.setCode(code);
        //销售组织
        String salesOrgId = dto.getSalesOrgId();
        //销售员
        String sellerId = dto.getSellerId();
        //用户信息
        FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
        if (userInfo != null) {
            addEntity.setSellerName(userInfo.getUserName());
        }

        //仓库id
        String warehouseId = dto.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String warehouseOrgId = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            warehouseOrgId = warehouseList.get(0).getOrgId();
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        addEntity.setWarehouseOrgId(warehouseOrgId);
        addEntity.setWarehouseOrgName(warehouseOrgName);
        //保存成功
        Boolean addResult = this.save(addEntity);
        if (addResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());
            //添加日志
            String content = String.format("新增了一个{%s}-销售单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), id, "新增操作");
            return id;

        }


        return "";
    }


    /**
     * 提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:41
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoInfoEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.CUSTOMER.getCode(), rejectPairList, "状态变更");
        }
        return result;

    }


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-16 14:49
     */
    @Override
    public Boolean addAndSubmit(SoInfoDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 销售订单详情
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     * @author yl
     * @date 2023-05-16 15:01
     */
    @Override
    public SoInfoDTO.ViewDTO view(String id) {
        SoInfoDTO.ViewDTO view = new SoInfoDTO.ViewDTO();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        BeanMapper.copy(soInfo, view);
        String warehouseId = view.getWarehouseId();
        ApproveStatusEnum approveStatus = view.getApproveStatus();
        view.setApproveStatusName(approveStatus.getName());
        List<SoDetailDTO.ViewDTO> detailList = soDetailService.listByMainId(id, warehouseId);
        view.setDetailList(detailList);
        return view;
    }


    /**
     * 分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoInfoDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-17 10:03
     */
    @Override
    public PagingVO<SoInfoDTO.PagingViewDTO> paging(PagingDTO<SoInfoDTO.PagingParamDTO> dto) {
        SoInfoDTO.PagingParamDTO params = dto.getParams();
        params.setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        List<String> paramDetailIds = soDetailService.listParamDetailIdsBySearchType(params.getSearchType());
        if (Objects.isNull(paramDetailIds)) {
            paramDetailIds = Collections.emptyList();
        } else {
            if (paramDetailIds.size() == 0) {
                return new PagingVO<>(new Page<>());
            }
        }
        IPage pageData = baseMapper.paging(query, params, paramDetailIds);
        List<SoInfoDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //详情id
        List<String> detailIds = list.stream().map(SoInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailList = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<String> skuIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        String warehouseId = list.get(0).getWarehouseId();
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(warehouseId);
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        //客户id
        List<String> customerIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> flagList = new ArrayList<>();

        for (SoInfoDTO.PagingViewDTO item : list) {
            boolean contains = flagList.contains(item.getId());
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String type = item.getType();
            item.setTypeName(BillTypeEnum.getName(type));
            //发货状态
            Boolean deliveryStatus = item.getDeliveryStatus();
            String deliveryStatusName = deliveryStatus != null && deliveryStatus ? "已发货" : "未发货";
            item.setDeliveryStatusName(deliveryStatusName);
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && deliveryStatus ? "已作废" : "未作废";
            item.setInvalidStatusName(invalidStatusName);
            String customerName = customerList.stream().filter(c -> c.getId().equals(item.getCustomerId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCustomerName(customerName);
            String skuId = item.getSkuId();
            //销售数量
            Integer qty = item.getQty();

            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            /**
             * 缺货数量
             * 当可用即时库存数量小于销售数量时，
             * 缺货数量=销售数量-可用即时库存数量；
             * 当可用即时库存数量大于销售数量时，缺货数量为0
             */
            Integer scarceQty = 0;
            Boolean isGre = curInventoryQty > qty;

            Boolean isScarce = !isGre;

            item.setIsScarce(isScarce);
            /**
             * 可出数量
             * 根据可用即时库存计算可出数量，
             * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
             * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
             */
            Integer availableQty = 0;
            if (!isGre) {
                scarceQty = qty;
                availableQty = curInventoryQty;

            } else {
                availableQty = qty;
            }
            item.setScarceQty(scarceQty);
            item.setAvailableQty(availableQty);
            /**
             * 已出库数量
             * 新增时默认为0
             * 编辑时根据关联出库单
             * 总共已发货数量同步
             *
             */
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSourceDetailId().equals(item.getDetailId())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            item.setDeliveryQty(deliveryQty);
            /**
             * 剩余数量
             * 销售数量-已出库数量
             */
            Integer waitQty = qty > deliveryQty ? qty - deliveryQty : 0;
            item.setWaitQty(waitQty);
            SkuVO sku = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (sku != null) {
                item.setProductName(sku.getSkuName());
                item.setUnit(sku.getUnitName());
            }

            if (contains) {
                item.setCode("");
                item.setTypeName("");
                item.setApproveStatusName("");
                item.setInvalidStatusName("");
                item.setCustomerName("");
                item.setSalesOrgName("");
                item.setSellerName("");
                item.setCreateTime(null);
                item.setCreateUserName("");
                item.setApproveUserName("");
                item.setRequireDate(null);
            }
            flagList.add(item.getId());
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    private Boolean updateApproveStatus(List<SoInfoEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }
}
