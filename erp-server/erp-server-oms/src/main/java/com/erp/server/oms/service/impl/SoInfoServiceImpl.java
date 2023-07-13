package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
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
import com.common.business.enums.*;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.CustomerAddressTypeEnum;
import com.erp.model.oms.enums.DeliveryModeEnum;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SoDeliveryNoticeDetailDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.*;
import com.erp.server.oms.utils.SoUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Slf4j
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
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private CommonService commonService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;


    @Value("${so.contract.company}")
    private String company;

    @Value("${so.contract.companyTaxpayerId}")
    private String companyTaxpayerId;

    @Value("${so.contract.companyAddress}")
    private String companyAddress;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private OmsAttachmentService omsAttachmentService;

    @Autowired
    private CustomerInvoiceService customerInvoiceService;

    /**
     * 添加销售订单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 16:28
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
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
        String currency = dto.getCurrency();
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(Arrays.asList(currency));
        String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
        addEntity.setCurrencySymbol(symbol);
        addEntity.setTradeTerm(dto.getTradeTerm());
        // 验证字典值
        checkDict(addEntity);

        //保存成功
        Boolean addResult = this.saveOrUpdate(addEntity);
        if (addResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());

            // 保存附件
            TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), tableName.value(), id);

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
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoInfoEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }
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

        //启动审核流程
        startProcess(list);

        List<Pair<String, String>> pairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(waitSubmitStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());

        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(ingStatus), "");
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
     * 启动流程
     *
     * @param list list
     * @return void
     * @Author Luo_WG
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(List<SoInfoEntity> list) {
        LoginUser userInfo = commonService.getUserInfo();
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SO_INFO.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(userInfo.getUid());
            startDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(startDTO);
        });
        ApiResult<List<ProcessManagementDTO.StartResultDTO>> listApiResult = workflowFeign.batchStartProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
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
    @Transactional(rollbackFor = Exception.class)
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
        String customerId = soInfo.getCustomerId();
        String customerName = "";
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            customerName = customerInfo.getName();
        }
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(Arrays.asList(soInfo.getId()));

        List<ProcessTaskManagementEntity> collect = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(soInfo.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE)).collect(Collectors.toList());
        List<String> curApproveName = collect.stream().map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
        String userName = StringUtils.join(curApproveName, ",");
        view.setApproveUserName(userName);
        if (CollectionUtils.isNotEmpty(collect)) {
            view.setApproveTime(collect.get(MathUtil.ZERO).getApproveTime());
        }
        view.setCustomerName(customerName);
        String warehouseId = view.getWarehouseId();
        BillApproveStatusEnum approveStatus = view.getApproveStatus();
        view.setApproveStatusName(approveStatus.getName());

        List<OmsAttachmentDTO.UpdateDTO> attachmentList = omsAttachmentService.getByBusinessIds(Arrays.asList(id));
        List<String> attachmentUrlList = attachmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        view.setAttachUrlList(attachmentUrlList);
        view.setAttachNameList(attachmentNameList);

        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicEnum.RECEIVE_METHOD.getType(), DictBasicEnum.COLLECTION_TERMS.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList)) {
            String receiveMethodName = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), view.getReceiveMethod())).map(DictBasicEntity::getName).findFirst().orElse(null);
            view.setReceiveMethodName(receiveMethodName);
        }
        // 收款条件
        List<DictBasicEntity> receiveConditionList = dictBasicMap.get(DictBasicEnum.COLLECTION_TERMS.getType());
        if (CollectionUtils.isNotEmpty(receiveConditionList)) {
            String receiveConditionName = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getValue(), view.getReceiveCondition())).map(DictBasicEntity::getName).findFirst().orElse(null);
            view.setReceiveConditionName(receiveConditionName);
        }
        // 收款账号
        if (StrUtils.isNotEmpty(view.getReceiveAccount())) {
            BankAccountEntity bankAccountEntity = bankAccountService.findByAccountNo(view.getReceiveAccount());
            view.setReceiveAccountName(Optional.ofNullable(bankAccountEntity).map(BankAccountEntity::getAccountName).orElse(""));
        }

        List<SoDetailDTO.ViewDTO> detailList = soDetailService.listByMainId(id, warehouseId);
        List<String> skuIds = detailList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        for (SoDetailDTO.ViewDTO viewDTO : detailList) {
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(skuVO)) {
                viewDTO.setWarehouseLocation(skuVO.getWarehouseLocation());
            }
        }
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
        params.setPermissionSql(dto.getPermissionSql());
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
        //销售部门id
        List<String> salesDeptIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(salesDeptIdList);
        //销售订单id集合
        List<String> soIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getId).collect(Collectors.toList());
        //发货通知单的
        List<SoDeliveryNoticeDetailDTO.ListDTO> soDeliveryNoticeList = soDeliveryNoticeFeign.listBySourceIdList(soIdList);
        //发货通知单的详情id
        List<String> deliveryNoticeDetailIdList = soDeliveryNoticeList.stream().map(SoDeliveryNoticeDetailDTO.ListDTO::getDetailId).collect(Collectors.toList());
        //详情id
        List<String> detailIds = list.stream().map(SoInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryNoticeDetailIdList)) {
            detailIds.addAll(deliveryNoticeDetailIdList);
        }

        //出库
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockFeign.listDetailBySoDetailIds(detailIds);

        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        soOutstockDetailList = soOutstockDetailList.stream().filter(s -> s.getApproveStatus().equals(approveStatus)).collect(Collectors.toList());
        List<String> skuIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);

        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);
        //客户id
        List<String> customerIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        List<String> flagList = new ArrayList<>();

        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(soIdList);
        for (SoInfoDTO.PagingViewDTO item : list) {
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(item.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String userName = StringUtils.join(curApproveName, ",");
            item.setApproveUserName(userName);
            boolean contains = flagList.contains(item.getId());
            String warehouseId = item.getWarehouseId();
            String salesDeptId = item.getSalesDeptId();
            String deptName = departmentList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(deptName);
            BillApproveStatusEnum billApproveStatus = item.getApproveStatus();
            item.setApproveStatusName(billApproveStatus.getName());
            String type = item.getOrderType();
            item.setOrderTypeName(BillTypeEnum.getName(type));
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
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(
                    s -> s.getSkuId().equals(skuId) &&
                            s.getWarehouseId().equals(warehouseId)
            ).mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
            /**
             * 缺货数量
             * 当可用即时库存数量小于销售数量时，缺货数量=销售数量-发货通知单审核通过数量 -可用即时库存数量；
             *
             * 当可用即时库存数量大于销售数量时，缺货数量为0
             */
            Integer scarceQty = 0;
            //是否大于销售数量
            Boolean isGre = curInventoryQty >= qty;

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
                //缺货数量=销售数量-发货通知单审核通过数量 -可用即时库存数量；
                Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceId().equals(item.getId())).
                        mapToInt(SoDeliveryNoticeDetailDTO.ListDTO::getDeliveryQty).sum();
                scarceQty = curInventoryQty - (qty - deliveryNoticeQty);
                //当为正数的时候不缺货
                scarceQty = scarceQty > 0 ? 0 : Math.abs(scarceQty);
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
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSoDetailId().equals(item.getDetailId())).map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
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
            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //销售单价
            BigDecimal price=item.getPrice();
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);

            if (contains) {
                item.setCode("");
                item.setOrderTypeName("");
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

    @Override
    public SoInfoDTO.PagingTotalDTO pagingTotal(SoInfoDTO.PagingParamDTO dto) {

        List<String> paramDetailIds = soDetailService.listParamDetailIdsBySearchType(dto.getSearchType());

        if (Objects.isNull(paramDetailIds)) {
            paramDetailIds = Collections.emptyList();
        } else {
            if (paramDetailIds.size() == 0) {
                SoInfoDTO.PagingTotalDTO pagingTotalDTO = new SoInfoDTO.PagingTotalDTO(MathUtil.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
                return pagingTotalDTO;
            }
        }
        SoInfoDTO.PagingTotalDTO pagingTotalDTO = baseMapper.pagingTotal(dto, paramDetailIds);
        return pagingTotalDTO;
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
    @Transactional(rollbackFor = Exception.class)
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
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
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
        draftEntity.setTradeTerm(dto.getTradeTerm());
        // 验证字典值
        checkDict(draftEntity);

        //保存成功
        Boolean draftResult = this.saveOrUpdate(draftEntity);
        if (draftResult) {
            //添加明细
            soDetailService.addSoDetail(id, dto.getDetailList());

            // 保存附件
            TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), tableName.value(), id);

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
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId, warehouseOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(salesOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soInfo.setSalesOrgName(salesOrgName);
        String finalWarehouseOrgId = warehouseOrgId;
        String warehouseOrgName = orgList.stream().filter(d -> d.getId().equals(finalWarehouseOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soInfo.setWarehouseOrgId(warehouseOrgId);
        soInfo.setWarehouseOrgName(warehouseOrgName);
        String currency = dto.getCurrency();
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(Arrays.asList(currency));
        String symbol = currencyViewList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
        soInfo.setCurrencySymbol(symbol);
        soInfo.setTradeTerm(dto.getTradeTerm());
        // 验证字典值
        checkDict(soInfo);

        Boolean updateResult = this.updateById(soInfo);
        if (updateResult) {

            // 保存附件
            TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), tableName.value(), id);

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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SoInfoEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //审核流程
        approveProcess(list, dto);

        //添加日志
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售订单", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    /**
     * 流程审核
     *
     * @param list
     * @param dto
     * @return void
     * @Author Luo_WG
     * @Date 2023/7/4 10:18
     **/
    private void approveProcess(List<SoInfoEntity> list, BaseApproveParamDTO dto) {
        ValidList<ProcessManagementDTO.ApproveDTO> resultList = new ValidList<>();
        LoginUser userInfo = commonService.getUserInfo();
        list.forEach(obj -> {
            ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
            approveDTO.setBusinessId(obj.getId());
            approveDTO.setBusinessKey(SourceTypeEnum.SO_INFO.getCode());
            approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
            approveDTO.setComment(dto.getComment());
            approveDTO.setUserId(userInfo.getUid());
            approveDTO.setVariablesMap(BeanUtil.beanToMap(obj));
            resultList.add(approveDTO);
        });
        ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> listApiResult = workflowFeign.batchApproveProcess(resultList);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<ProcessManagementDTO.ApproveResultDTO> data = listApiResult.getData();
        List<String> businessIdList = data.stream()
                .filter(obj -> ObjectUtils.isEmpty(obj.getIsExistProcess()) || !obj.getIsExistProcess())
                .map(ProcessManagementDTO.ApproveResultDTO::getBusinessId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(businessIdList)) {
            List<SoInfoEntity> businessSoInfoList = list.stream().filter(obj -> businessIdList.contains(obj.getId())).collect(Collectors.toList());
            approveEnd(dto, businessSoInfoList);
        }
    }

    /**
     * 结束审核
     *
     * @param dto
     * @param list
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/4 10:55
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<SoInfoEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        //意见
        String userName = commonService.getUserInfo().getUserName();
        String approveStatus = "";
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            //审核通过发送金蝶
            list.forEach(obj -> syncKingdeeSoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT.getStatus();
        }
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(approveStatus), userName);
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean disApprove(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<SoInfoEntity> list = this.listByIds(ids);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        //检查关联单据
        checkRefBill(ids);
        //有销售变更的也不能反审核
        List<SoChangeEntity> soChangeList = soChangeService.listBySoIds(ids);

        long soChangeCount = soChangeList.stream().filter(s -> !s.getInvalidStatus()).count();
        //表示 有变更中的销售变更单
        if (soChangeCount > 0) {
            throw new ServiceException(ApiError.ERROR_92047);
        }
        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(approveStatus))).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(waitSubmitStatus), "");
        //反审核
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");
            // TODO 收款字段需补
            list.forEach(obj -> syncKingdeeSoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return result;
    }


    /**
     * 检查关联单据
     *
     * @param soIds
     * @return void
     * @author yl
     * @date 2023-05-29 16:08
     */
    private void checkRefBill(List<String> soIds) {
        Integer wmsCount = wmsTaskFeign.getPushDownBySourceIds(soIds);
        if (wmsCount > 0) {
            throw new ServiceException(ApiError.ERROR_92040);
        }
        Integer omsCount = soReturnService.getPushDownBySourceIds(soIds);
        if (omsCount > 0) {
            throw new ServiceException(ApiError.ERROR_92040);
        }
        Integer scmCount = scmTaskFeign.getPushDownBySourceIds(soIds);
        if (scmCount > 0) {
            throw new ServiceException(ApiError.ERROR_92040);
        }
    }

    private void checkRemove(List<String> soIds) {

        Integer wmsCount = wmsTaskFeign.getPushDownBySourceIds(soIds);
        if (wmsCount > 0) {
            throw new ServiceException(ApiError.ERROR_92018);
        }
        Integer omsCount = soReturnService.getPushDownBySourceIds(soIds);
        if (omsCount > 0) {
            throw new ServiceException(ApiError.ERROR_92018);
        }
        Integer scmCount = scmTaskFeign.getPushDownBySourceIds(soIds);
        if (scmCount > 0) {
            throw new ServiceException(ApiError.ERROR_92018);
        }

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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        List<SoInfoEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        LoginUser userInfo = commonService.getUserInfo();
        ids.forEach(obj ->{
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.SO_INFO.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(waitSubmitStatus), "");
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
    @Transactional(rollbackFor = Exception.class)
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
        //检查能否删除
        checkRemove(ids);
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
    @Transactional(rollbackFor = Exception.class)
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
            throw new ServiceException(ApiError.ERROR_98012);
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
        list.forEach(obj -> syncKingdeeSoService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_INVALID.getCode()));

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
        //销售订单id集合
        List<String> soIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getId).collect(Collectors.toList());

        //发货通知单的
        List<SoDeliveryNoticeDetailDTO.ListDTO> soDeliveryNoticeList = soDeliveryNoticeFeign.listBySourceIdList(soIdList);

        //发货通知单的详情id
        List<String> deliveryNoticeDetailIdList = soDeliveryNoticeList.stream().map(SoDeliveryNoticeDetailDTO.ListDTO::getDetailId).collect(Collectors.toList());
        //详情id
        List<String> detailIds = list.stream().map(SoInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryNoticeDetailIdList)) {
            detailIds.addAll(deliveryNoticeDetailIdList);
        }
        List<SoOutstockDetailDTO.DeliveryQtyDTO> soOutstockDetailList = soOutstockFeign.listDetailBySoDetailIds(detailIds);
        List<String> skuIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSkuId).collect(Collectors.toList());
        List<String> warehouseIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);

        //销售部门id
        List<String> salesDeptIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSalesDeptId).distinct().collect(Collectors.toList());
        List<SysDepartmentEntity> departmentList = sysUserFeign.listDeptByIds(salesDeptIdList);

        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);
        //客户id
        List<String> customerIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoInfoDTO.PagingViewDTO item : list) {
            BillApproveStatusEnum approveStatus = item.getApproveStatus();
            item.setApproveStatusName(approveStatus.getName());
            String warehouseId = item.getWarehouseId();
            String type = item.getOrderType();
            item.setOrderTypeName(BillTypeEnum.getName(type));
            String deliveryStatus = item.getDeliveryStatus();
            String deliveryStatusName = DeliveryStatusEnum.getName(deliveryStatus);
            item.setDeliveryStatusName(deliveryStatusName);

            //部门名称
            String deptName = departmentList.stream().filter(d -> d.getId().equals(item.getSalesDeptId())).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(deptName);

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
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(
                    s -> s.getSkuId().equals(skuId) &&
                            s.getWarehouseId().equals(warehouseId)
            ).mapToInt(InventoryQtyDTO.SkuInventoryTotalDTO::getInventoryTotal).sum();
            /**
             * 缺货数量
             * 当可用即时库存数量小于销售数量时， 缺货数量=可用即时库存数量-(销售数量-发货通知单数量)；
             *
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
                Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceId().equals(item.getId())).
                        mapToInt(SoDeliveryNoticeDetailDTO.ListDTO::getDeliveryQty).sum();
                scarceQty = curInventoryQty - (qty - deliveryNoticeQty);
                //当为正数的时候不缺货
                scarceQty = scarceQty > 0 ? 0 : Math.abs(scarceQty);
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
            Integer deliveryQty = soOutstockDetailList.stream().filter(req -> req.getSourceDetailId().equals(item.getDetailId())).map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
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
                eq(SoInfoEntity::getApproveStatus, ApproveStatusEnum.getByStatus(approveStatus)).
                orderByDesc(SoInfoEntity::getCreateTime).
                list();
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
        customer.setSoRemark(soInfo.getRemark());
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
        List<InvoiceDTO.ViewDTO> viewDTOS = customerInvoiceService.listByMainId(customerId);
        if (CollectionUtils.isNotEmpty(viewDTOS)) {
            List<InvoiceDTO.ViewDTO> collect = viewDTOS.stream().sorted(Comparator.comparing(InvoiceDTO.ViewDTO::getIsDefault).reversed()).collect(Collectors.toList());
            customer.setTaxRegisterCode(collect.get(MathUtil.ZERO).getTaxRegisterCode());
        }
        customer.setReceiveAddress(receiverAddressName);
        customer.setCustomerName(customerName);
        String deliveryMode = customer.getDeliveryMode();
        String deliveryModeName = DeliveryModeEnum.getName(deliveryMode);
        customer.setDeliveryModeName(deliveryModeName);
        String addressType = customer.getAddressType();
        String addressTypeName = CustomerAddressTypeEnum.getName(addressType);
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
        String type = soInfo.getOrderType();
        customer.setOrderTypeName(BillTypeEnum.getName(type));
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(soInfo.getWarehouseId()));
        //仓库
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            String warehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(soInfo.getWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse("");
            customer.setWarehouseName(warehouseName);
        }
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

        Boolean invalidStatus = customer.getInvalidStatus();
        if (invalidStatus != null && invalidStatus) {
            throw new ServiceException(ApiError.ERROR_92022);
        }


        String approveStatus = customer.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        String waitSubmit = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = Arrays.asList(approve, approveIng, waitSubmit);
        if (!statusList.contains(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_92022);
        }
        result.setCode(customer.getCode());
        result.setCustomerName(customer.getCustomerName());
        result.setTaxpayerId(customer.getTaxRegisterCode());
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
        String chineseAmount = Convert.digitToChinese(totalAmount);
        result.setChineseAmount(chineseAmount);
        return result;
    }

    @Override
    public List<SoInfoDTO.ViewGenerateSalesDemandDTO> viewGenerateSalesDemand(List<String> ids) {
        checkIfPushDown(ids);
        List<SoInfoDTO.ViewGenerateSalesDemandDTO> list = baseMapper.viewGenerateSalesDemand(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        //未审核完成不支持下推备货申请单
        SoInfoDTO.ViewGenerateSalesDemandDTO viewGenerateSalesDemandDTO = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(viewGenerateSalesDemandDTO)) {
            throw new ServiceException(ApiError.ERROR_92025.code, String.format(ApiError.ERROR_92025.msg, viewGenerateSalesDemandDTO.getSourceCode()));
        }

        List<String> skuIds = list.stream().map(SoInfoDTO.ViewGenerateSalesDemandDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<String> soIdList = list.stream().map(SoInfoDTO.ViewGenerateSalesDemandDTO::getSourceId).collect(Collectors.toList());
        //发货通知单的
        List<SoDeliveryNoticeDetailDTO.ListDTO> soDeliveryNoticeList = soDeliveryNoticeFeign.listBySourceIdList(soIdList);
        Map<String, List<SoInfoDTO.ViewGenerateSalesDemandDTO>> map = list.stream().collect(Collectors.groupingBy(SoInfoDTO.ViewGenerateSalesDemandDTO::getSourceId));

        for (Map.Entry<String, List<SoInfoDTO.ViewGenerateSalesDemandDTO>> entry : map.entrySet()) {

            List<SoInfoDTO.ViewGenerateSalesDemandDTO> value = entry.getValue();
            InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
            paramDTO.setSkuIds(skuIds);
            paramDTO.setWarehouseId(value.get(0).getWarehouseId());
            paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
            //从wms 获取到sku 的即时库存信息
            List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
            List<String> flagList = new ArrayList<>();
            for (SoInfoDTO.ViewGenerateSalesDemandDTO viewDTO : value) {
                //即时库存
                Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(viewDTO.getSkuId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);

                //产品名称
                String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDTO.setProductName(productName);

                //来源类型
                viewDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());

                //销售数量
                Integer qty = viewDTO.getQty();

                /**
                 * 缺货数量
                 * 当可用即时库存数量小于销售数量时， 缺货数量=可用即时库存数量-(销售数量-发货通知单数量)；
                 * 缺货数量=销售数量-可用即时库存数量；
                 * 当可用即时库存数量大于销售数量时，缺货数量为0
                 */
                Integer scarceQty = 0;
                Boolean isGre = curInventoryQty >= qty;
                if (!isGre) {
                    Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceId().equals(viewDTO.getSourceId())).
                            mapToInt(SoDeliveryNoticeDetailDTO.ListDTO::getDeliveryQty).sum();
                    scarceQty = curInventoryQty - (qty - deliveryNoticeQty);
                    //当为正数的时候不缺货
                    scarceQty = scarceQty > 0 ? 0 : Math.abs(scarceQty);

                }
                viewDTO.setScarceQty(scarceQty);

                viewDTO.setFlag(Boolean.TRUE);

                boolean contains = flagList.contains(viewDTO.getSourceId());
                if (contains) {
                    viewDTO.setFlag(Boolean.FALSE);
                    continue;
                }
                flagList.add(viewDTO.getSourceId());
            }
        }

        return list;
    }


    /**
     * 检查能否下推
     *
     * @param ids
     * @return void
     * @author yl
     * @date 2023-05-30 17:20
     */
    private void checkIfPushDown(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<SoInfoEntity> soInfoList = this.listByIds(ids);
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        long unApprove = soInfoList.stream().filter(s -> !s.getApproveStatus().getStatus().
                equals(approveStatus)).count();
        if (unApprove > 0) {
            throw new ServiceException(ApiError.ERROR_92042);
        }
        long invalidCount = soInfoList.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_92043);
        }

        List<SoChangeEntity> soChangeList = soChangeService.listBySoIds(ids);
        String waitSubmit = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>();
        statusList.add(waitSubmit);
        statusList.add(approveIng);
        long count = soChangeList.stream().filter(s -> statusList.contains(s.getApproveStatus().getStatus())).count();
        //表示 有变更中的销售变更单
        if (count > 0) {
            //但是 如果 有作废的数据 也可以下推
            long invalidNum = soChangeList.stream().filter(s -> statusList.contains(s.getApproveStatus().getStatus()) && s.getInvalidStatus()).count();
            if (invalidNum != count) {
                throw new ServiceException(ApiError.ERROR_92041);
            }

        }


    }


    /**
     * 方法说明
     *
     * @param soIdList
     * @return com.erp.model.oms.dto.SoInfoDTO.CustomerDTO
     * @author yl
     * @date 2023-05-22 10:44
     */
    @Override
    public List<SoInfoDTO.CustomerDTO> listSoCustomerByIds(List<String> soIdList) {
        if (CollectionUtils.isEmpty(soIdList)) {
            return Collections.emptyList();
        }
        List<SoInfoEntity> soList = this.listByIds(soIdList);
        List<SoInfoDTO.CustomerDTO> resultList = BeanMapper.copyList(soList, SoInfoDTO.CustomerDTO.class);
        List<String> customerIds = resultList.stream().map(SoInfoDTO.CustomerDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIds) ? customerInfoService.listByIds(customerIds) : Collections.emptyList();
        for (SoInfoDTO.CustomerDTO item : resultList) {
            String customerId = item.getCustomerId();
            String customerName = customerList.stream().filter(c -> c.getId().equals(customerId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCustomerName(customerName);
            String type = item.getOrderType();
            item.setOrderTypeName(BillTypeEnum.getName(type));
        }
        return resultList;
    }


    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }


    private Boolean updateApproveStatus(List<SoInfoEntity> list, BillApproveStatusEnum statusEnum, String approveUserName) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (SoInfoEntity item : list) {
                item.setApproveStatus(statusEnum);
                item.setApproveUserName(approveUserName);
                if (StringUtils.isNotBlank(approveUserName)) {
                    item.setApproveTime(LocalDateTime.now());
                } else {
                    item.setApproveTime(null);
                }
            }
            return this.updateBatchById(list);
        }
        return true;
    }

    @Override
    public List<SoInfoDTO.GenerateDeliveryView> generateDeliveryView(List<String> ids) {
        checkIfPushDown(ids);
        List<SoInfoDTO.GenerateDeliveryView> viewList = baseMapper.generateDeliveryView(ids);
        long closeCount = viewList.stream().filter(s -> s.getIsClose()).count();
        if (closeCount > 0) {
            throw new ServiceException(ApiError.ERROR_98068);
        }
        //获取sku的id集合
        List<String> skuIdList = viewList.stream().map(SoInfoDTO.GenerateDeliveryView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<String> customerIds = viewList.stream().map(SoInfoDTO.GenerateDeliveryView::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIds) ? customerInfoService.listByIds(customerIds) : Collections.emptyList();
        for (SoInfoDTO.GenerateDeliveryView view : viewList) {
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            view.setDeliveryQty(view.getSalesQty());
            view.setPlanDeliveryDate(view.getRequireDate());
            String customerName = customerList.stream().filter(c -> c.getId().equals(view.getCustomerId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            view.setCustomerName(customerName);
        }
        return viewList;
    }

    @Override
    public List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(List<String> ids) {
        checkIfPushDown(ids);
        List<SoInfoDTO.GenerateSoReturnView> viewList = baseMapper.generateSoReturnView(ids);
        //获取sku的id集合
        List<String> skuIdList = viewList.stream().map(SoInfoDTO.GenerateSoReturnView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取出库详情
        List<String> soIds = viewList.stream().map(SoInfoDTO.GenerateSoReturnView::getSoId).distinct().collect(Collectors.toList());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        for (SoInfoDTO.GenerateSoReturnView view : viewList) {
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            view.setReturnQty(view.getSalesQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> view.getSoId().equals(detail.getSoId()) && detail.getSkuId().equals(view.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            view.setDeliveryQty(actualQty);
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(view.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            view.setCustomerName(customerInfoEntity.getName());
            view.setReturnDate(LocalDate.now());
        }
        return viewList;
    }


    /**
     * 更改销售订单金蝶推送的状态
     *
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate) {
        try {
            if (StringUtils.isEmpty(id)) {
                return Boolean.TRUE;
            }
            //同步成功
            if (syncKingdeeStatus.equals(SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode())) {
                KingdeeDTO dto = new KingdeeDTO();
                dto.setId(syncKingdeeId);
                dto.setNumber("");
                dto.setKingdeePushModuleCode(KingdeePushModuleEnum.SAL_SALEORDER.getCode());
                JSONObject soJson = dmpTaskFeign.getByKingdeeId(dto);
                List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(id);
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) soJson.get("SaleOrderEntry");
                List<SoDetailEntity> updateList = new ArrayList<>(10);
                if (CollectionUtils.isNotEmpty(resultList)) {
                    for (int i = 0; i < resultList.size(); i++) {
                        Map<String, Object> item = resultList.get(i);
                        String KingdeeId = item.get("Id").toString();
                        Map<String, Object> materialMap = (Map<String, Object>) item.get("MaterialId");
                        String skuNo = materialMap.get("Number").toString();
                        if (soDetailList.size() >= resultList.size()) {
                            SoDetailEntity soDetail = soDetailList.get(i);
                            if (soDetail.getSkuNo().equals(skuNo)) {
                                soDetail.setKingdeeDetailId(KingdeeId);
                                updateList.add(soDetail);
                            }
                        }
                    }
                }
                if (updateList.size() > 0) {
                    soDetailService.updateBatchById(updateList);
                }
            }

            return this.lambdaUpdate()
                    .eq(SoInfoEntity::getId, id)
                    .set(StringUtils.isNotBlank(syncKingdeeStatus), SoInfoEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                    .set(StringUtils.isNotBlank(syncKingdeeStatus), SoInfoEntity::getSyncKingdeeTime, LocalDateTime.now())
                    .set(StringUtils.isNotBlank(syncKingdeeId), SoInfoEntity::getSyncKingdeeId, syncKingdeeId)
                    .set(StringUtils.isNotBlank(syncOperate), SoInfoEntity::getSyncOperate, syncOperate)
                    .update();
        } catch (Exception e) {
            log.error("同步状态出错>>>>{}", e);
        }
        return Boolean.TRUE;
    }


    /**
     * 查看 客户是有使用
     *
     * @param customerIds
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-31 18:17
     */
    @Override
    public Boolean getIsUseCustomer(List<String> customerIds) {
        if (CollectionUtils.isEmpty(customerIds)) {
            return Boolean.FALSE;
        }
        long count = this.lambdaQuery().in(SoInfoEntity::getCustomerId, customerIds).count();
        return count > 0;
    }


    /**
     * 根据地址id 获取到对应 销售订单是否引用
     *
     * @param addressIds
     * @return int
     * @author yl
     * @date 2023-06-06 11:13
     */
    @Override
    public int getCountByAddressIds(List<String> addressIds) {
        if (CollectionUtils.isEmpty(addressIds)) {
            return 0;
        }
        return this.lambdaQuery().in(SoInfoEntity::getReceiveAddressId,addressIds).count();
    }


    /**
     * 导出销售订单发票信息
     *
     * @param id
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-04 14:48
     */
    @Override
    public Boolean exportSoPI(String id, HttpServletResponse response) {
        SoInfoDTO.SoPIDTO soPi = new SoInfoDTO.SoPIDTO();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        Boolean invalidStatus=soInfo.getInvalidStatus();
        if(invalidStatus!=null&&invalidStatus){
            throw new ServiceException(ApiError.ERROR_92022);
        }
        String approveStatus = soInfo.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        String waitSubmit = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = Arrays.asList(approve, approveIng,waitSubmit);
        if (!statusList.contains(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_92022);
        }


        soPi.setCode(soInfo.getCode());
        soPi.setBillDate(soInfo.getCreateTime().toLocalDate());
        //币种符号
        String currencySymbol = soInfo.getCurrencySymbol();
        //海运
        BigDecimal shippingFee = soInfo.getShippingFee();
        String shippingFeeStr = currencySymbol + shippingFee;
        soPi.setShippingFeeStr(shippingFeeStr);
        soPi.setShippingFee(shippingFee);
        //客户id
        String customerId = soInfo.getCustomerId();
        String receiveCondition = soInfo.getReceiveCondition();


        //收款条件
        String receiveMethodType = DictBasicEnum.RECEIVE_METHOD.getType();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(receiveMethodType, receiveCondition);
        if (Objects.isNull(dictBasic)) {
            soPi.setReceiveConditionStr("");
        } else {
            soPi.setReceiveConditionStr(dictBasic.getName());

        }

        CustomerInfoEntity customerInfo = StringUtils.isNotEmpty(customerId) ? customerInfoService.getById(customerId) : null;
        String customerName = "";
        if (customerInfo != null) {
            customerName = customerInfo.getName();
        }
        soPi.setCustomerName(customerName);

        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(customerId);
        CustomerAddressDTO.ViewDTO address = addressList.stream().filter(a -> a.getIsDefault()).findFirst().orElse(null);
        if (!Objects.isNull(address)) {
            soPi.setAddress(address.getAddress());
            soPi.setEmail(address.getEmail());
            soPi.setTelNumber(address.getTelNumber());
        }


        List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(id);
        List<SoDetailDTO.ViewPiDTO> viewPiList = new ArrayList<>(soDetailList.size());
        int i = 1;
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        for (SoDetailEntity item : soDetailList) {
            String symbol = item.getCurrencySymbol();
            SoDetailDTO.ViewPiDTO viewPi = new SoDetailDTO.ViewPiDTO();
            viewPi.setNo(i);
            BigDecimal amount = item.getAmount();
            viewPi.setAmount(amount);
            viewPi.setCurrencySymbol(symbol);
            viewPi.setSkuNo(item.getSkuNo());
            Integer qty = item.getQty();
            viewPi.setQty(qty);
            BigDecimal price = item.getPrice();
            viewPi.setPrice(price);
            viewPi.setPriceStr(symbol + price);
            viewPi.setAmountStr(symbol + amount);
            String model = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().map(SkuVO::getDeclareModel).orElse("");
            viewPi.setModel(model);
            i++;
            viewPiList.add(viewPi);

        }

        //总金额
        BigDecimal totalAmount = viewPiList.stream().map(SoDetailDTO.ViewPiDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        //总数量
        Integer totalQty = viewPiList.stream().mapToInt(SoDetailDTO.ViewPiDTO::getQty).sum();
        soPi.setTotalAmountStr(currencySymbol + totalAmount);
        soPi.setTotalQty(totalQty);
        //总费用
        BigDecimal totalFee = totalAmount.add(shippingFee);
        soPi.setTotalFeeStr(currencySymbol + totalFee);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SalesContract.xlsx";
        String name = "销售单发票信息";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(viewPiList, soPi, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售单发票导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public SkuCostProfitDTO.SkuCostProfitResult getSkuCostProfit(SkuCostProfitDTO.SkuCostProfitParam costParam) {
        if (Objects.isNull(costParam.getQty()) || costParam.getQty() < 0) {
            costParam.setQty(0);
        }
        SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult = new SkuCostProfitDTO.SkuCostProfitResult();
        skuCostProfitResult.setSkuId(costParam.getSkuId());
        skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
        skuCostProfitResult.setSaleCost(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfit(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfitRate(BigDecimal.ZERO);
        BigDecimal purchasePrice = BigDecimal.ZERO;
        // 获取采购单价
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.getLatest(Arrays.asList(costParam.getSkuId()));
        PurchaseOrderDetailEntity purchaseOrderDetailEntity = CollUtil.isNotEmpty(purchaseOrderDetailEntityList) ? purchaseOrderDetailEntityList.get(0) : null;
        if(Objects.nonNull(purchaseOrderDetailEntity)) {
            purchasePrice = purchaseOrderDetailEntity.getTaxPrice();
            skuCostProfitResult.setPurchasePrice(purchasePrice);
        }
        log.info("提交的币制：{}", costParam.getCurrency());
        // 最新的采购单价币制转换（非人民币）
        if(Objects.nonNull(skuCostProfitResult.getPurchasePrice()) &&
                skuCostProfitResult.getPurchasePrice().compareTo(BigDecimal.ZERO) == 1 &&
                !Objects.equals(purchaseOrderDetailEntity.getCurrency(), "CNY")) {
            String purchaseDate = purchaseOrderDetailEntity.getPurchaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate =  dmpTaskFeign.getRate(purchaseDate, purchaseOrderDetailEntity.getCurrency());
            log.info("找到的最新的采购订单:{} 的币制：{}，采购订单日期：{}，转换后汇率：{}", purchaseOrderDetailEntity.getPurchaseOrderId(), purchaseOrderDetailEntity.getCurrency(), rate);
            // 未找到汇率直接返回
            if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
                skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
                return skuCostProfitResult;
            } else {
                // 转换成人民币采购单价
                purchasePrice = rate.multiply(skuCostProfitResult.getPurchasePrice()).setScale(4, BigDecimal.ROUND_HALF_UP);
            }
        }
        // 销售金额转换
        if(Objects.nonNull(costParam.getSaleAmount()) &&
                costParam.getSaleAmount().compareTo(BigDecimal.ZERO) == 1 &&
                !Objects.equals(costParam.getCurrency(), "CNY")) {
            BigDecimal rate =  dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), costParam.getCurrency());
            log.info("提交的币制：{}，转换后汇率：{}", costParam.getCurrency(), rate);
            if(Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
                costParam.setSaleAmount(BigDecimal.ZERO);
            } else {
                // 转换成人民币销售金额
                costParam.setSaleAmount(rate.multiply(costParam.getSaleAmount()).setScale(4, BigDecimal.ROUND_HALF_UP));
            }
        }
        // 计算成本毛利信息
        skuCostProfitResult = SoUtils.calCostProfit(purchasePrice , costParam, skuCostProfitResult);
        return skuCostProfitResult;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void brushCostData(LocalDate startDate, LocalDate endDate) {
       // 查询需要重刷数据的创建时间范围
        List<SoInfoEntity> soList =  lambdaQuery().ge(SoInfoEntity::getCreateTime, startDate).le(SoInfoEntity::getCreateTime,endDate.plusDays(1)).list();
        if(CollUtil.isEmpty(soList)) {
            return;
        }
        for(SoInfoEntity soInfoEntity : soList) {
            List<SoDetailEntity>  detailList = soDetailService.listBaseByMainId(soInfoEntity.getId());
            if(CollUtil.isEmpty(detailList)) {
                continue;
            }
            List<String> skuIdList = detailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.getLatest(skuIdList);
            Map<String, List<PurchaseOrderDetailEntity>> purchaseOrderDetailMap = Maps.newHashMap();
            if(CollUtil.isNotEmpty(purchaseOrderDetailEntityList)) {
                purchaseOrderDetailMap = purchaseOrderDetailEntityList.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getSkuId));
            }
            for (SoDetailEntity item : detailList) {
                // 计算毛利成本
                soDetailService.calCost(purchaseOrderDetailMap, item, Boolean.TRUE);
                soDetailService.updateCost(item.getId(), item);
            }

        }
    }

    @Override
    public void brushCostData(String id) {
        SoInfoEntity soInfoEntity = super.getById(id);
        Optional.ofNullable(soInfoEntity).orElseThrow(()->new ServiceException(ApiError.ERROR_92016));
        List<SoDetailEntity>  detailList = soDetailService.listBaseByMainId(soInfoEntity.getId());
        if(CollUtil.isEmpty(detailList)) {
            return;
        }
        List<String> skuIdList = detailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = scmTaskFeign.getLatest(skuIdList);
        Map<String, List<PurchaseOrderDetailEntity>> purchaseOrderDetailMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(purchaseOrderDetailEntityList)) {
            purchaseOrderDetailMap = purchaseOrderDetailEntityList.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getSkuId));
        }
        for (SoDetailEntity item : detailList) {
            // 计算毛利成本
            soDetailService.calCost(purchaseOrderDetailMap, item, Boolean.TRUE);
                soDetailService.updateCost(item.getId(), item);
            }
    }

    private void checkDict(SoInfoEntity soInfoEntity) {
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicEnum.RECEIVE_METHOD.getType(), DictBasicEnum.COLLECTION_TERMS.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList) && StrUtils.isNotEmpty(soInfoEntity.getReceiveMethod())) {
            DictBasicEntity dictBasicEntity = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), soInfoEntity.getReceiveMethod())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(dictBasicEntity), () -> new ServiceException("收款方式错误"));
        }
        // 收款条件
        List<DictBasicEntity> receiveConditionList = dictBasicMap.get(DictBasicEnum.COLLECTION_TERMS.getType());
        if (CollectionUtils.isNotEmpty(receiveConditionList) && StrUtils.isNotEmpty(soInfoEntity.getReceiveCondition())) {
            DictBasicEntity dictBasicEntity = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getValue(), soInfoEntity.getReceiveCondition())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(dictBasicEntity), () -> new ServiceException("收款条件错误"));
        }
        // 收款账号
        if (StrUtils.isNotEmpty(soInfoEntity.getReceiveAccount())) {
            BankAccountEntity bankAccountEntity = bankAccountService.findByAccountNo(soInfoEntity.getReceiveAccount());
            ValidatorUtil.isTrue(Objects.nonNull(bankAccountEntity), () -> new ServiceException("收款账号错误"));
        }
    }

    @Override
    public List<SoInfoDTO.PrintDTO> print(List<String> ids) {
        List<SoInfoDTO.PrintDTO> printDTOList = new ArrayList<>();
        List<SoInfoEntity> soInfoEntities = this.listByIds(ids);
        if (CollectionUtils.isEmpty(soInfoEntities)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //获取客户id集合
        List<String> customerIds = soInfoEntities.stream().map(SoInfoEntity::getCustomerId).distinct().collect(Collectors.toList());
        //根据客户id集合查询客户信息
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.listByIds(customerIds);
        //获取销售单详情
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByMainIds(ids);
        //获取sku的id集合
        List<String> skuIdList = soDetailEntities.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        //根据skuId查询sku信息
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        List<String> receiveAddressId = soInfoEntities.stream().map(SoInfoEntity::getReceiveAddressId).collect(Collectors.toList());
        List<CustomerAddressEntity> customerAddressEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receiveAddressId)) {
            customerAddressEntities.addAll(customerAddressService.listByIds(receiveAddressId));
        }
        for (SoInfoEntity soInfoEntity : soInfoEntities) {
            //根据客户id获取客户信息
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            SoInfoDTO.PrintDTO printDTO = new SoInfoDTO.PrintDTO();
            printDTO.setCustomerName(customerInfoEntity.getName());
            printDTO.setSellerName(soInfoEntity.getSellerName());
            CustomerAddressEntity customerAddressEntity = customerAddressEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getReceiveAddressId())).findFirst().orElse(null);
            printDTO.setReceiveAddress(customerAddressEntity.getAddress());
            printDTO.setTelNumber(soInfoEntity.getTelNumber());
            List<SoDetailEntity> soDetailEntityList = soDetailEntities.stream().filter(req -> req.getMainId().equals(soInfoEntity.getId())).collect(Collectors.toList());
            printDTO.setSumNumber(soDetailEntityList.stream().mapToInt(SoDetailEntity::getQty).sum());
            List<SoInfoDTO.PrintDetailDTO> printDetailDTOList = new ArrayList<>();
            for (SoDetailEntity soDetailEntity : soDetailEntityList) {
                SoInfoDTO.PrintDetailDTO printDetailDTO = new SoInfoDTO.PrintDetailDTO();
                printDetailDTO.setPlatformSkuNo(soDetailEntity.getPlatformSkuNo());
                printDetailDTO.setProductSkuNo(soDetailEntity.getSkuNo());
                SkuVO skuVO = skuList.stream().filter(req -> req.getSkuId().equals(soDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                printDetailDTO.setProductName(skuVO.getSkuName());
                printDetailDTO.setRemark(soDetailEntity.getRemark());
                printDetailDTO.setQty(soDetailEntity.getQty());
                printDetailDTOList.add(printDetailDTO);
            }
            printDTO.setPrintDetailList(printDetailDTOList);
            printDTOList.add(printDTO);
        }
        return printDTOList;
    }
}
