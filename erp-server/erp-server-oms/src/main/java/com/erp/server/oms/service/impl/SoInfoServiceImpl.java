package com.erp.server.oms.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.AddressTypeEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
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
    private CustomerAddressService customerAddressService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private CommonService commonService;

    @Value("${so.contract.company}")
    private String company;

    @Value("${so.contract.companyTaxpayerId}")
    private String companyTaxpayerId;

    @Value("${so.contract.companyAddress}")
    private String companyAddress;

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
        String id = dto.getId();
        String customerId = dto.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            customerInfoService.quoteCustomer(Arrays.asList(customerId));
        }
        String code = "";
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        } else {
            SoInfoEntity so = this.getById(id);
            if (Objects.isNull(so)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            code = so.getCode();
        }

        SoInfoEntity addEntity = new SoInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        if (StringUtils.isBlank(code)) {
            //生成单号
            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
        }
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
        String waitSubmitStatus = BillApproveStatusEnum.WAIT_SUBMIT.getStatus();
        addEntity.setApproveStatus(BillApproveStatusEnum.getByStatus(waitSubmitStatus));
        //保存成功
        Boolean addResult = this.saveOrUpdate(addEntity);
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
        String userName = commonService.getUserInfo().getUserName();


        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(ingStatus),userName);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", BillApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");
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
            throw new ServiceException(ApiError.ERROR_92016);
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
            String deliveryStatus = item.getDeliveryStatus();
            String deliveryStatusName = DeliveryStatusEnum.getName(deliveryStatus);
            item.setDeliveryStatusName(deliveryStatusName);
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && invalidStatus ? "已作废" : "未作废";
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
     * 暂存数据
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:00
     */
    @Override
    public String draft(SoInfoDTO.AddDTO dto) {
        //id
        String id = dto.getId();
        Boolean isFirst = false;
        if (StringUtils.isNotBlank(id)) {
            SoInfoEntity soInfo = this.getById(id);
            if (Objects.isNull(soInfo)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
        } else {
            isFirst = true;
            id = IdWorker.getIdStr();
        }
        String customerId = dto.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            customerInfoService.quoteCustomer(Arrays.asList(customerId));
        }
        SoInfoEntity draftEntity = new SoInfoEntity();
        BeanMapper.copy(dto, draftEntity);
        draftEntity.setId(id);
        //销售组织
        String salesOrgId = dto.getSalesOrgId() == null ? "" : dto.getSalesOrgId();
        //销售员
        String sellerId = dto.getSellerId();
        if (StringUtils.isNotBlank(sellerId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
            if (userInfo != null) {
                draftEntity.setSellerName(userInfo.getUserName());
            }
        }
        String warehouseOrgId = "";
        //仓库id
        String warehouseId = dto.getWarehouseId();
        if (StringUtils.isNotBlank(warehouseId)) {
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                warehouseOrgId = warehouseList.get(0).getOrgId();
            }
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        draftEntity.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        draftEntity.setWarehouseOrgId(warehouseOrgId);
        draftEntity.setWarehouseOrgName(warehouseOrgName);
        String draftStatus = BillApproveStatusEnum.DRAFT.getStatus();
        draftEntity.setApproveStatus(BillApproveStatusEnum.getByStatus(draftStatus));
        //保存成功
        Boolean draftResult = this.saveOrUpdate(draftEntity);
        if (draftResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());
            if (isFirst) {
                //添加日志
                String content = String.format("新增了一个{%s}-销售单", BillApproveStatusEnum.DRAFT.getName());
                addModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), id, "新增操作");
            }
            return id;
        }
        return "";

    }


    /**
     * 修改 销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-17 15:42
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSo(SoInfoDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        String customerId = dto.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            customerInfoService.quoteCustomer(Arrays.asList(customerId));
        }
        String code = soInfo.getCode();
        //旧的
        SoInfoEntity old = new SoInfoEntity();
        BeanMapper.copy(soInfo, old);

        BeanMapper.copy(dto, soInfo);
        soInfo.setCode(code);

        //销售组织
        String salesOrgId = dto.getSalesOrgId();
        //销售员
        String sellerId = dto.getSellerId();
        //用户信息
        FindUserDTO userInfo = sysUserFeign.getUserByUserId(sellerId);
        if (userInfo != null) {
            soInfo.setSellerName(userInfo.getUserName());
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
        soInfo.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soInfo.setWarehouseOrgId(warehouseOrgId);
        soInfo.setWarehouseOrgName(warehouseOrgName);

        Boolean updateResult = this.updateById(soInfo);
        if (updateResult) {
            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, soInfo, ModuleTypeEnum.SO.getCode(), id, "", "");
            //修改 订单详情
            soDetailService.updateSoDetail(id, dto.getDetailList());
            return id;
        }
        return "";
    }


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SoInfoDTO.UpdateDTO dto) {
        String id = this.updateSo(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }

    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:46
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoInfoEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        String userName = commonService.getUserInfo().getUserName();

        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(approveStatus),userName);
            content = String.format("状态由[%s]变更为[%s] , 意见:%s", ingStatusName, ApproveStatusEnum.APPROVE.getName(), comment);
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(rejectStatus),userName);
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
        }

        return result;
    }

    /**
     * 反审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:48
     */
    @Override
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<SoInfoEntity> list = this.listByIds(ids);
        //审核中
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveIngStatus);
        statusList.add(approveStatus);
        String userName = commonService.getUserInfo().getUserName();

        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveIngStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(waitSubmitStatus),userName);
        //反审核
        if (result) {
            //添加日志
            String ingContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(ingContent, ModuleTypeEnum.SO.getCode(), pairList, "状态变更");
            //审核通过
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");

        }
        return result;
    }


    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:51
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SoInfoEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        String userName = commonService.getUserInfo().getUserName();
        //TODO 撤销流程
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(waitSubmitStatus),userName);
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("销售订单【%s】取消流程", ModuleTypeEnum.SO.getCode(), pairList, "取消流程操作");
        return result;
    }


    /**
     * 批量删除
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:53
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        List<SoInfoEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String draftStatus = BillApproveStatusEnum.DRAFT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(waitSubmitStatus);
        statusList.add(draftStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92017);
        }
        //占用状态
        long occupyCount = list.stream().filter(s -> s.getOccupyStatus()).count();
        if (occupyCount > 0) {
            throw new ServiceException(ApiError.ERROR_92018);
        }

        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "删除");
            //删除明细
            soDetailService.removeByMainIdList(ids);

        }

        return result;
    }

    /**
     * 作废
     *
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 17:14
     */
    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<SoInfoEntity> list = this.listByIds(ids);
        String waitSubmitStatus = BillApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String draftStatus = BillApproveStatusEnum.DRAFT.getStatus();
        String rejectStatus = BillApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(3);
        statusList.add(waitSubmitStatus);
        statusList.add(draftStatus);
        statusList.add(rejectStatus);
        long invalidCount = list.stream().filter(d -> !d.getInvalidStatus()).count();
        if (invalidCount != list.size()) {
            throw new ServiceException(ApiError.ERROR_98061);
        }
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92019);
        }
        lambdaUpdate().in(SoInfoEntity::getId, ids).
                set(SoInfoEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售订单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "作废");

        return Boolean.TRUE;
    }


    /**
     * 导出数据
     *
     * @param dto
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 18:02
     */
    @Override
    public Boolean exportExcel(SoInfoDTO.ExportDTO dto, HttpServletResponse response) {
        List<String> paramDetailIds = soDetailService.listParamDetailIdsBySearchType(dto.getSearchType());
        if (Objects.isNull(paramDetailIds)) {
            paramDetailIds = Collections.emptyList();
        } else {
            if (paramDetailIds.size() == 0) {
                throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
            }
        }
        //获取导出数据
        List<SoInfoDTO.PagingViewDTO> list = baseMapper.listExport(dto, paramDetailIds);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
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
        for (SoInfoDTO.PagingViewDTO item : list) {
            ApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String type = item.getType();
            item.setTypeName(BillTypeEnum.getName(type));
            String deliveryStatus = item.getDeliveryStatus();
            String deliveryStatusName = DeliveryStatusEnum.getName(deliveryStatus);
            item.setDeliveryStatusName(deliveryStatusName);
            //作废状态
            Boolean invalidStatus = item.getInvalidStatus();
            String invalidStatusName = invalidStatus != null && invalidStatus ? "已作废" : "未作废";
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
                String unit = sku.getUnitName();
                item.setUnit(StringUtils.isNotBlank(unit) ? unit : "");
            }
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SoInfo.xlsx";
        String name = "销售订单列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售订单列表导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 获取到已审核的销售订单列表
     *
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     * @author yl
     * @date 2023-05-17 18:59
     */
    @Override
    public List<BaseIdDTO.CodeDTO> listSo() {
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        List<SoInfoEntity> list = this.lambdaQuery().
                eq(SoInfoEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus)).list();
        return BeanMapper.copyList(list, BaseIdDTO.CodeDTO.class);
    }


    /**
     * 根据销售单id
     * 获取到销售订单客户信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-17 19:10
     */
    @Override
    public SoInfoDTO.CustomerDTO getSoCustomer(String id) {
        SoInfoDTO.CustomerDTO customer = new SoInfoDTO.CustomerDTO();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        BeanMapper.copy(soInfo, customer);

        String customerId = customer.getCustomerId();
        CustomerInfoEntity customerInfo = StringUtils.isNotEmpty(customerId) ? customerInfoService.getById(customerId) : null;
        String customerName = "";
        if (customerInfo != null) {
            customerName = customerInfo.getName();
        }
        //收货地址id
        String receiverAddressId = customer.getReceiveAddressId();

        String receiverAddressName = "";
        if (StringUtils.isNotBlank(receiverAddressId)) {
            CustomerAddressEntity addressEntity = customerAddressService.getById(receiverAddressId);
            if (addressEntity != null) {
                receiverAddressName = addressEntity.getAddress();
            }
        }

        customer.setReceiveAddress(receiverAddressName);
        customer.setCustomerName(customerName);
        String deliveryMode = customer.getDeliveryMode();
        String deliveryModeName = DeliveryModeEnum.getName(deliveryMode);
        customer.setDeliveryModeName(deliveryModeName);
        String addressType = customer.getAddressType();
        String addressTypeName = AddressTypeEnum.getName(addressType);
        customer.setAddressTypeName(addressTypeName);
        //销售部门id
        String salesDeptId = soInfo.getSalesDeptId();
        String salesDeptName = "";
        if (StringUtils.isNotBlank(salesDeptId)) {
            SysDepartmentDTO dept = sysUserFeign.getUserDeptById(salesDeptId);
            if (dept != null) {
                salesDeptName = dept.getName();
            }
        }
        customer.setSalesDeptName(salesDeptName);
        BillTypeEnum type = soInfo.getType();
        customer.setTypeName(type.getName());
        return customer;
    }


    /**
     * 获取到合同信息
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ExportPdfDTO
     * @author yl
     * @date 2023-05-18 14:12
     */
    @Override
    public SoInfoDTO.ExportPdfDTO exportSoContractPdf(String id) {
        SoInfoDTO.ExportPdfDTO result = new SoInfoDTO.ExportPdfDTO();
        SoInfoDTO.CustomerDTO customer = this.getSoCustomer(id);
        String approveStatus = customer.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = Arrays.asList(approve, approveIng);
        if (!statusList.contains(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_92022);
        }
        result.setCode(customer.getCode());
        result.setCustomerName(customer.getCustomerName());
        result.setTaxpayerId("");
        result.setContactPerson(customer.getReceiverName());
        result.setContactTelNumber(customer.getTelNumber());
        result.setContactAddress(customer.getReceiveAddress());

        result.setCurrency(customer.getCurrency());
        result.setFirstSignDate(customer.getCreateTime().toLocalDate());
        result.setSecondSignDate(customer.getCreateTime().toLocalDate());

        result.setCompany(company);
        result.setCompanyTaxpayerId(companyTaxpayerId);
        result.setCompanyAddress(companyAddress);
        result.setSellerName(customer.getSellerName());
        String sellerId = customer.getSellerId();
        String sellerTelNumber = "";
        if (StringUtils.isNotBlank(sellerId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(sellerId);
            if (userDTO != null) {
                sellerTelNumber = userDTO.getMobile();
            }
        }
        result.setSellerTelNumber(sellerTelNumber);
        List<SoDetailDTO.ExportPdfDTO> details = soDetailService.listExportPdf(id);
        Integer totalQty = details.stream().mapToInt(SoDetailDTO.ExportPdfDTO::getQty).sum();
        result.setTotalQty(totalQty);
        BigDecimal totalAmount = details.stream().map(SoDetailDTO.ExportPdfDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalAmount(totalAmount);
        result.setDetails(details);
        String  chineseAmount=  Convert.digitToChinese(totalAmount);
        result.setChineseAmount(chineseAmount);
        return result;
    }

    @Override
    public List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids) {
        List<SoInfoDTO.ViewGenerateSalesDemandDTO> list = baseMapper.viewGenerateSalesDemand(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        //未审核完成不支持下推备货申请单
        SoInfoDTO.ViewGenerateSalesDemandDTO viewGenerateSalesDemandDTO = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(viewGenerateSalesDemandDTO)) {
            throw new ServiceException(ApiError.ERROR_92025.code,String.format(ApiError.ERROR_92025.msg,viewGenerateSalesDemandDTO.getSourceCode()));
        }

        List<String> skuIds = list.stream().map(SoInfoDTO.ViewGenerateSalesDemandDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        Map<String, List<SoInfoDTO.ViewGenerateSalesDemandDTO>> map = list.stream().collect(Collectors.groupingBy(SoInfoDTO.ViewGenerateSalesDemandDTO::getSourceId));

        for (Map.Entry<String, List<SoInfoDTO.ViewGenerateSalesDemandDTO>> entry : map.entrySet()) {

            List<SoInfoDTO.ViewGenerateSalesDemandDTO> value = entry.getValue();
            InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
            paramDTO.setSkuIds(skuIds);
            paramDTO.setWarehouseId(value.get(0).getWarehouseId());
            paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //从wms 获取到sku 的即时库存信息
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);

            for (SoInfoDTO.ViewGenerateSalesDemandDTO viewDTO : value) {
                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(viewDTO.getSkuId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);

                //产品名称
                String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDTO.setProductName(productName);

                //销售数量
                Integer qty = viewDTO.getQty();

                /**
                 * 缺货数量
                 * 当可用即时库存数量小于销售数量时，
                 * 缺货数量=销售数量-可用即时库存数量；
                 * 当可用即时库存数量大于销售数量时，缺货数量为0
                 */
                Integer scarceQty = 0;
                Boolean isGre = curInventoryQty > qty;
                if (!isGre) {
                    scarceQty = qty;
                }
                viewDTO.setScarceQty(scarceQty);
            }
        }

        return list;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    private Boolean updateApproveStatus(List<SoInfoEntity> list, BillApproveStatusEnum statusEnum,String  approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for(SoInfoEntity item:list){
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
            }
            return this.updateBatchById(list);
        }
        return true;
    }
}
