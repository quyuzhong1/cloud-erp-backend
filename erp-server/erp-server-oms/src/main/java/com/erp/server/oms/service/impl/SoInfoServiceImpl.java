package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.B2BSoImportExcelDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductSaleEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.MachineTypeEnum;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.erp.model.wms.enums.WorkTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.SoInfoConverter;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.listener.B2BSoImportExcelListener;
import com.erp.server.oms.mapper.SoInfoMapper;
import com.erp.server.oms.service.*;
import com.erp.server.oms.utils.SoUtils;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO;

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
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;


    @Resource
    private KingdeeFeign kingdeeFeign;


    @Value("${so.contract.company}")
    private String company;

    @Value("${so.contract.companyTaxpayerId}")
    private String companyTaxpayerId;

    @Value("${so.contract.companyAddress}")
    private String companyAddress;

    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private BankAccountService bankAccountService;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private CustomerInvoiceService customerInvoiceService;

    @Resource
    private MachineInfoFeign machineInfoFeign;

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WmsVirtualWarehouseFeign wmsVirtualWarehouseFeign;
    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private FileTemplateFeign fileTemplateFeign;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    @Resource
    private OmsPushMsgService omsPushMsgService;

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
        //要货日期
        LocalDate requireDate = dto.getRequireDate();
        //单据日期
        LocalDate billDate = dto.getBillDate();
        //同步金蝶的时候要货日期要大于单据日期
        if (requireDate != null && billDate != null) {
            if (requireDate.compareTo(billDate) < 0) {
                throw new ServiceException(ApiError.ERROR_92059);
            }
        }

        SoInfoEntity addEntity = new SoInfoEntity();
        BeanMapper.copy(dto, addEntity);
        addEntity.setId(id);
        if (StringUtils.isBlank(code)) {
            //生成单号
//            code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
            code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSD);
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
        //报关费
        if (!addEntity.getIsDeclare()) {
            addEntity.setCustomsFee(BigDecimal.ZERO);
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
        //获取虚拟仓库
        handleVirtualWarehouse(addEntity);

        //保存成功
        Boolean addResult = this.saveOrUpdate(addEntity);
        if (addResult) {
            // 此处调整为明细的币制取主单的币制
            if (StrUtils.isNotEmpty(dto.getCurrency()) && CollUtil.isNotEmpty(dto.getDetailList())) {
                dto.getDetailList().stream().forEach(detail -> detail.setCurrency(dto.getCurrency()));
            }
            //添加明细
            soDetailService.addSoDetail(id, dto.getIsTax(), dto.getDetailList());

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
     * 查询虚拟仓库
     * @author will
     * @date 2024/6/12 19:14
     * @param entity
     */
    private void handleVirtualWarehouse (SoInfoEntity entity) {
        //查询客户信息
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        //查询虚拟仓信息
        VirtualWarehouseChannelDTO.PlatformDTO platformDTO = new VirtualWarehouseChannelDTO.PlatformDTO();
        platformDTO.setDictPlatform(customerInfoEntity.getPlatformType());
        platformDTO.setWarehouseIdList(Arrays.asList(entity.getWarehouseId()));
        platformDTO.setRelationId("");
        List<VirtualWarehouseRelationEntity> virtualWarehouseList = wmsVirtualWarehouseFeign.getVirtualWarehouse(platformDTO);
        if (CollectionUtils.isEmpty(virtualWarehouseList)) {
            entity.setVirtualWarehouseId("");
          return;
        }
        entity.setVirtualWarehouseId(virtualWarehouseList.get(0).getVirtualWarehouseId());
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoInfoEntity> list = this.listByIds(ids);
        long invalidCount = list.stream().filter(s -> s.getInvalidStatus()).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_INVALID_TO_SUBMIT);
        }

        //售后订单
        String afterSaleOrder = BillTypeEnum.AFTER_SALES.getCode();
        //检查的销售订单
        List<SoInfoEntity> checkSoList = list.stream().filter(s -> !afterSaleOrder.equals(s.getOrderType())).collect(Collectors.toList());
        //收款日期为空的
        List<String> isNullReceiveDateList = checkSoList.stream().filter(s -> Objects.isNull(s.getReceiveDate())).map(SoInfoEntity::getCode).
                collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(isNullReceiveDateList)) {
            String isNullReceiveDateCode = isNullReceiveDateList.stream().collect(Collectors.joining(","));
            throw new ServiceException(isNullReceiveDateCode + " 销售订单 收款日期不能为空");
        }
        BigDecimal zeroFlag = BigDecimal.ZERO;

        //收款金额为空的
        List<String> isNullReceiveAmountList = checkSoList.stream().filter(s -> Objects.isNull(s.getReceiveAmount()) || zeroFlag.compareTo(s.getReceiveAmount()) == 0).map(SoInfoEntity::getCode).
                collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(isNullReceiveAmountList)) {
            String isNullReceiveAmountCode = isNullReceiveAmountList.stream().collect(Collectors.joining(","));
            throw new ServiceException(isNullReceiveAmountCode + " 销售订单 收款金额不能为空或者为零");
        }
        List<String> checkSoIdList = checkSoList.stream().map(SoInfoEntity::getId).collect(Collectors.toList());
        List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainIdList(checkSoIdList);
        //这个是 单价为空的集合
        List<SoDetailEntity> isNullPriceList = soDetailList.stream().filter(s -> !s.getIsGift() && !s.getIsReissue() && zeroFlag.compareTo(s.getPrice()) == 0).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(isNullPriceList)) {
            List<String> soIdList = isNullPriceList.stream().map(SoDetailEntity::getMainId).collect(Collectors.toList());
            String isNullPriceCode = list.stream().filter(s -> soIdList.contains(s.getId())).
                    map(SoInfoEntity::getCode).distinct().collect(Collectors.joining(","));
            throw new ServiceException(isNullPriceCode + " 销售订单 销售单价不能为空或者为零");
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

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(rejectStatus)).
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
        ValidList<ProcessManagementDTO.StartDTO> resultList = new ValidList<>();
        list.forEach(obj -> {
            ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
            startDTO.setBusinessId(obj.getId());
            startDTO.setBusinessCode(obj.getCode());
            startDTO.setBusinessKey(SourceTypeEnum.SO_INFO.getCode());
            startDTO.setBusinessName(obj.getCode());
            startDTO.setUserId(obj.getSellerId());
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
    @GlobalTransactional(rollbackFor = Exception.class)
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

            //国家
            String countryId = customerInfo.getCountryId();
            view.setCountryId(countryId);
            // 国家
            List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
            //国家
            if (CollectionUtils.isNotEmpty(countryList)) {
                String countryName = countryList.stream().filter(obj -> obj.getId().equals(view.getCountryId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                view.setCountryName(countryName);
            }
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
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList)) {
            String receiveMethodName = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), view.getReceiveMethod())).map(DictBasicEntity::getName).findFirst().orElse(null);
            view.setReceiveMethodName(receiveMethodName);
        }
        // 收款条件
        List<KingdeeReceiptConditionEntity> receiveConditionList = kingdeeReceiptConditionService.list();
        if (CollectionUtils.isNotEmpty(receiveConditionList)) {
            String receiveConditionName = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getId(), view.getReceiveCondition())).map(KingdeeReceiptConditionEntity::getName).findFirst().orElse(null);
            view.setReceiveConditionName(receiveConditionName);
        }
        // 收款账号
        if (StrUtils.isNotEmpty(view.getReceiveAccount())) {
            List<BankAccountEntity> bankAccountList = bankAccountService.findByOrgIdAndAccountNo(view.getSalesOrgId(), view.getReceiveAccount());
            if (CollUtil.isNotEmpty(bankAccountList)) {
                view.setReceiveAccountName(bankAccountList.get(0).getAccountName());
            }
        }

        List<SoDetailDTO.ViewDTO> detailList = soDetailService.listByMainId(id, warehouseId);
        List<String> skuIds = detailList.stream().map(SoDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        //查询第三方SKU信息
        List<ListingInfoWithSkuMappingDTO> listingWithSkuMappingDTOList = skuMappingService.listByErpSkuIdAndType(skuIds,"",soInfo.getWarehouseId(),"");
        for (SoDetailDTO.ViewDTO viewDTO : detailList) {
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(skuVO)) {
                viewDTO.setWarehouseLocation(skuVO.getWarehouseLocationLarge());
            }
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingWithSkuMappingDTOList.stream().filter(v->v.getProductSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(new ListingInfoWithSkuMappingDTO());
            viewDTO.setThirdWarehouseSku(listingInfoWithSkuMappingDTO.getPlatformSkuNo());
        }
        view.setDetailList(detailList);
        return view;
    }

    /**
     * 打印拣货单
     *
     * @param id
     * @return com.erp.model.oms.dto.SoInfoDTO.ViewDTO
     * @author yl
     * @date 2023-05-16 15:01
     */
    @Override
    public SoInfoDTO.ViewDTO printPickingView(String id) {
        SoInfoDTO.ViewDTO view = view(id);
        // 仓位排序
        view.getDetailList().sort((s1, s2) -> {
            if (StringUtils.isBlank(s1.getWarehouseLocation()) && !StringUtils.isBlank(s2.getWarehouseLocation())) {
                return 1;
            } else if (!StringUtils.isBlank(s1.getWarehouseLocation()) && StringUtils.isBlank(s2.getWarehouseLocation())) {
                return -1;
            } else if (StringUtils.isBlank(s1.getWarehouseLocation()) && StringUtils.isBlank(s2.getWarehouseLocation())) {
                return 0;
            } else {
                return s1.getWarehouseLocation().compareTo(s2.getWarehouseLocation());
            }
        });
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
        List<String> fieldList = CollectionUtils.isEmpty(params.getAdvanceQueryDTOList()) ? new ArrayList<>() :  params.getAdvanceQueryDTOList().stream().map(AdvanceQueryDTO::getField).collect(Collectors.toList());
        params.setFieldList(fieldList);

        //是否虚拟仓缺货
        List<AdvanceQueryDTO> advanceQueryDTOList = dto.getParams().getAdvanceQueryDTOList();
        Boolean isVirtualOutStock = (Boolean)advanceQueryDTOList.stream().filter(v->v.getField().equals("isVirtualOutStock")).findAny().orElse(new AdvanceQueryDTO()).getValue();
        if(Objects.nonNull(isVirtualOutStock)){
            //查询全部数据，过滤出有缺货
            Page query = new Page(1,Integer.MAX_VALUE,false);
            IPage pageData = baseMapper.paging(query, params);
            List<SoInfoDTO.PagingViewDTO> list = pageData.getRecords();
            if (CollectionUtils.isEmpty(list)) {
                return new PagingVO<>(pageData);
            }
            fillPagingDb(list);
            list = list.stream().filter(v -> v.getIsVirtualScarce()!= null && v.getIsVirtualScarce().equals(isVirtualOutStock)).collect(Collectors.toList());
            Page result = new Page(dto.getCurrPage(), dto.getPageSize(),list.size());
            list = com.common.business.utils.CollectionUtils.paginateList(list,dto.getPageSize(),dto.getCurrPage());
            result.setRecords(list);
            return new PagingVO<>(result);
        }else {
            Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
            IPage pageData = baseMapper.paging(query, params);
            List<SoInfoDTO.PagingViewDTO> list = pageData.getRecords();
            if (CollectionUtils.isEmpty(list)) {
                return new PagingVO<>(pageData);
            }
            fillPagingDb(list);
            return new PagingVO<>(pageData);
        }
    }

    /**
     * 填充分页数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-10-08 18:00
     */
    private void fillPagingDb(List<SoInfoDTO.PagingViewDTO> list) {
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
        // 国家
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        Map<String,String> countryMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(countryList)){
            countryMap = countryList.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        }

        //有效发货通知单
        List<String> sodIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getDetailId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(sodIdList);
        List<String> skuIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getWarehouseId).collect(Collectors.toList());

        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        if (CollectionUtils.isNotEmpty(bomChildrenList)) {
            List<String> bomSkuIdList = bomChildrenList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
            skuIdList.addAll(bomSkuIdList);
        }

        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);

        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);
        //客户id
        List<String> customerIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getCustomerId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerList = CollectionUtils.isNotEmpty(customerIdList) ? customerInfoService.listByIds(customerIdList) : Collections.emptyList();
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(soIdList);
        // 忽略库存计算SKU
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if (CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }

        //虚拟仓库存
        List<String> virtualWarehouseIdList = list.stream().map(SoInfoDTO.PagingViewDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        paramDTO.setDictInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        paramDTO.setSkuIdList(skuIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryFeign.listInventoryQty(paramDTO);

        //实体仓
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);
        //虚拟仓
        List<VirtualWarehouseEntity> virtualWarehouseList = FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //销售出库单列表
        List<SoOutstockEntity> soOutstockList = soOutstockFeign.listBySoIds(soIdList);
        for (SoInfoDTO.PagingViewDTO item : list) {
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(item.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String userName = StringUtils.join(curApproveName, ",");
            item.setApproveUserName(userName);
            String warehouseId = item.getWarehouseId();
            String salesDeptId = item.getSalesDeptId();
            String deptName = departmentList.stream().filter(d -> d.getId().equals(salesDeptId)).
                    map(SysDepartmentEntity::getName).findFirst().orElse("");
            item.setSalesDeptName(deptName);
            BillApproveStatusEnum billApproveStatus = item.getApproveStatus();
            item.setApproveStatusName(billApproveStatus.getName());
            String type = item.getOrderType();
            item.setOrderTypeName(BillTypeEnum.getName(type));
            String soId = item.getId();
            //运单号集合
            List<String> trackNoList = soOutstockList.stream().filter(s -> s.getSoId().equals(soId)
                            && StringUtils.isNotBlank(s.getTrackNo())).
                    map(SoOutstockEntity::getTrackNo).collect(Collectors.toList());
            item.setTrackNoList(trackNoList);
            item.setTrackNoStr(trackNoList.stream().collect(Collectors.joining(",")));
            //国家
            item.setCountryName(countryMap.get(item.getCountryId()));
            //实体仓名称
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), item.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            item.setWarehouseName(warehouseName);

            //存在虚拟仓库则判断是否缺货
            if (StrUtil.isNotBlank(item.getVirtualWarehouseId())) {

                Integer approveNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), item.getDetailId())
                ).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

                //虚拟仓名称
                String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), item.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
                item.setVirtualWarehouseName(virtualWarehouseName);

                //虚拟仓缺货按bom处理
                SoInfoDTO.VirtuaParamScarceDTO virtuaParamScarceDTO = new SoInfoDTO.VirtuaParamScarceDTO();
                BeanMapperUtils.copy(item,virtuaParamScarceDTO);
                handleVirtualBomScarce(bomChildrenList, virtualInventoryList, virtuaParamScarceDTO,approveNoticeQty);
                item.setVirtualUsableQty(virtuaParamScarceDTO.getVirtualUsableQty());
                item.setChildScarceList(virtuaParamScarceDTO.getChildScarceList());
                item.setIsVirtualScarce(virtuaParamScarceDTO.getIsVirtualScarce());
                item.setVirtualScarceQty(virtuaParamScarceDTO.getVirtualScarceQty());
            }

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

            /**
             * 可出数量
             * 根据可用即时库存计算可出数量，
             * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
             * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
             */
            Integer availableQty = 0;
            if (!isGre) {
                //缺货数量=销售数量-发货通知单审核通过数量 -可用即时库存数量；
                Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceDetailId().equals(item.getDetailId())).
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

            //发货通知数量
            if (CollectionUtils.isNotEmpty(soDeliveryNoticeDetailList)) {
                Integer effectiveNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(item.getDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                item.setEffectiveNoticeQty(effectiveNoticeQty);
            }


            //缺货标识
            item.setIsScarce(scarceQty > 0);
            /**
             * 已出库数量
             * 新增时默认为0
             * 编辑时根据关联出库单
             * 总共已发货数量同步
             *
             */
            Integer deliveryQty = item.getDeliveryQty();

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
            //发货状态
            String deliveryStatus = DeliveryStatusEnum.UN_SHIPPED.getCode();
            //表示发货完毕
            if (waitQty == 0) {
                deliveryStatus = DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode();
            } else {
                if (!qty.equals(waitQty)) {
                    deliveryStatus = DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode();
                }
            }
            item.setDeliveryStatus(deliveryStatus);
            String deliveryStatusName = DeliveryStatusEnum.getName(deliveryStatus);
            item.setDeliveryStatusName(deliveryStatusName);
            //税率
            BigDecimal taxRate = item.getTaxRate();

            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //销售单价
            BigDecimal price = item.getPrice();

            //销售单价(本位币)
            item.setPriceLc(MathUtil.multiply(price, exchangeRate));

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            item.setTaxPrice(taxPrice);
            //含税单价(本位币)
            item.setTaxPriceLc(MathUtil.multiply(taxPrice, exchangeRate));

            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(item.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(e.getType())).count();
                if (count > 0) {
                    item.setIsConstitute(Boolean.TRUE);
                }
            }

            if (ignoreInventorySkuIds.contains(skuId)) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库，不做库存验证", skuId, item.getSkuNo());
                item.setIsScarce(Boolean.FALSE);
                item.setScarceQty(0);
            }
        }


    }

    /**
     * 是否虚拟仓缺货
     * @author will
     * @date 2024/7/22 10:52
     * @param bomChildrenList
     * @param virtualInventoryList
     * @param item
     * @return Boolean
     */
    private void handleVirtualBomScarce(List<BomChildrenSkuDTO> bomChildrenList, List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList
            , SoInfoDTO.VirtuaParamScarceDTO item, Integer approveNoticeQty) {
        //返回信息
        List<SoInfoDTO.VirtualChildScarceDTO> childScarceList = new ArrayList<>();

        //虚拟仓可用库存（单品赋值）
        Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), item.getSkuId())
                        && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), item.getVirtualWarehouseId())
                        && CharSequenceUtil.equals(obj.getWarehouseId(), item.getWarehouseId())
                        && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())
                )
                .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                .findFirst().orElse(MathUtil.ZERO);
        item.setVirtualUsableQty(virtualUsableQty);

        /**
         *缺货数量：
         *
         * 缺货数量 = [ 销售数量 - 已下推发货通知单（确认状态“未作废”）的数量 - 锁定数量 ]  - 当前虚拟仓可用库存
         * 若＜0则表示不缺货则结果显示“0”
         * 若＞0则表示缺货，列表显示缺货标识
         */
        //判断是否是组合品
        Boolean isCombination = Boolean.FALSE;
        //销售套装bom需要判断子件库存是否够使用
        List<BomChildrenSkuDTO> childList = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(item.getSkuId())
                        && BomTypeEnum.COMBINATION.getType().equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(childList)) {
            isCombination = Boolean.TRUE;
        }
        item.setIsCombination(isCombination);
        //虚拟仓是否缺货
        Boolean isVirtualScarce = Boolean.FALSE;
        //费销售套装bom判断父级SKU是否够使用
        if (!isCombination) {
            //缺货数量 = [ 销售数量 - 已下推发货通知单（确认状态“未作废”）的通过数量 - 锁定数量 ]  - 当前虚拟仓可用库存
            Integer virtualScarceQty = item.getQty() - approveNoticeQty -  item.getFrozenQty() - item.getVirtualUsableQty();

            item.setIsVirtualScarce(virtualScarceQty > MathUtil.ZERO);
            item.setVirtualScarceQty(virtualScarceQty > MathUtil.ZERO ? virtualScarceQty : MathUtil.ZERO);
            item.setChildScarceList(childScarceList);
            return;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : childList) {
            SoInfoDTO.VirtualChildScarceDTO scarceDTO = new SoInfoDTO.VirtualChildScarceDTO();
            //用量
            Integer quantity = childrenSkuDTO.getQuantity();

            //虚拟仓是否缺货
            Integer childVirtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), childrenSkuDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), item.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), item.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())
                    )
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty)
                    .findFirst().orElse(MathUtil.ZERO);
            //已缺货的无需标记
            if (!isVirtualScarce && (item.getQty() * quantity - item.getFrozenQty() * quantity -  approveNoticeQty * quantity > childVirtualUsableQty)) {
                isVirtualScarce = Boolean.TRUE;
            }
            scarceDTO.setSkuNo(childrenSkuDTO.getSkuNo());
            //针对父级可用数量
            double floor = Math.floor((double) childVirtualUsableQty / quantity);
            Integer parentUsableQty = (int) floor;
            scarceDTO.setParentUsableQty(parentUsableQty);
            //缺货数量
            Integer virtualScarceQty = (item.getQty() * quantity - item.getFrozenQty() * quantity - approveNoticeQty * quantity) - childVirtualUsableQty;
            scarceDTO.setChildUsableQty(childVirtualUsableQty);
            scarceDTO.setVirtualScarceQty(MathUtil.compareTo(virtualScarceQty,MathUtil.ZERO) >= MathUtil.ZERO ? virtualScarceQty : MathUtil.ZERO);
            scarceDTO.setSkuId(childrenSkuDTO.getSkuId());
            scarceDTO.setQuantity(childrenSkuDTO.getQuantity());
            scarceDTO.setBomVersion(childrenSkuDTO.getBomVersion());
            childScarceList.add(scarceDTO);
        }
        item.setIsVirtualScarce(isVirtualScarce);
        item.setChildScarceList(childScarceList);
        if (CollectionUtils.isNotEmpty(childScarceList)) {
            //bom最小可用数
            Integer bomUsableQty = childScarceList.stream().min(Comparator.comparing(SoInfoDTO.VirtualChildScarceDTO::getParentUsableQty)).map(SoInfoDTO.VirtualChildScarceDTO::getParentUsableQty).get();
            item.setVirtualUsableQty(bomUsableQty);
        }
    }

    @Override
    public SoInfoDTO.PagingTotalDTO pagingTotal(SoInfoDTO.PagingParamDTO dto) {
        List<String> fieldList = CollectionUtils.isEmpty(dto.getAdvanceQueryDTOList()) ? new ArrayList<>() :  dto.getAdvanceQueryDTOList().stream().map(AdvanceQueryDTO::getField).collect(Collectors.toList());
        dto.setFieldList(fieldList);
        SoInfoDTO.PagingTotalDTO pagingTotalDTO = baseMapper.pagingTotal(dto);
        //总发货数量
        Integer totalDeliveryQty = pagingTotalDTO.getTotalDeliveryQty();
        //总数量
        Integer totalQty = pagingTotalDTO.getTotalQty();
        //待发货数量
        Integer waitQty = totalQty > totalDeliveryQty ? totalQty - totalDeliveryQty : 0;
        pagingTotalDTO.setTotalWaitQty(waitQty);
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
        String currency = dto.getCurrency();
        if (StringUtils.isBlank(currency)) {
            throw new ServiceException("币别不能空");
        }
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
        //报关费
        if (!draftEntity.getIsDeclare()) {
            draftEntity.setCustomsFee(BigDecimal.ZERO);
        }
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
        //获取虚拟仓库
        handleVirtualWarehouse(draftEntity);
        //保存成功
        Boolean draftResult = this.saveOrUpdate(draftEntity);
        if (draftResult) {
            // 此处调整为明细的币制取主单的币制
            if (StrUtils.isNotEmpty(dto.getCurrency()) && CollUtil.isNotEmpty(dto.getDetailList())) {
                dto.getDetailList().stream().forEach(detail -> detail.setCurrency(dto.getCurrency()));
            }
            //添加明细
            soDetailService.addSoDetail(id, dto.getIsTax(), dto.getDetailList());

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
    @GlobalTransactional(rollbackFor = Exception.class)
    public String updateSo(SoInfoDTO.UpdateDTO dto) {
        String id = dto.getId();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        String customerId = dto.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            String oldCustomerId = soInfo.getCustomerId();
            if(StringUtils.isNotBlank(oldCustomerId) && !customerId.equals(oldCustomerId)) {
                //判断是否已下推发货通知单，是则客户不允许修改
                String soId = soInfo.getId();
                Map<String, Long> pushDownMap = soDeliveryNoticeFeign.getPushDownDeliveryNoticeCnt(Lists.newArrayList(soId));
                if (CollUtil.isNotEmpty(pushDownMap)
                        && pushDownMap.containsKey(soId)
                        && pushDownMap.get(soId) > 0) {
                    throw new ServiceException("已下推发货通知单冻结库存，客户不允许修改，请删除发货通知单后修改");
                }
            }
            customerInfoService.quoteCustomer(Arrays.asList(customerId));
        }
        String code = soInfo.getCode();
        //旧的
        SoInfoEntity old = new SoInfoEntity();
        BeanMapper.copy(soInfo, old);

        BeanMapper.copy(dto, soInfo);
        soInfo.setCode(code);


        //要货日期
        LocalDate requireDate = dto.getRequireDate();
        //单据日期
        LocalDate billDate = dto.getBillDate();
        //同步金蝶的时候要货日期要大于单据日期
        if (requireDate != null && billDate != null) {
            if (requireDate.compareTo(billDate) < 0) {
                throw new ServiceException(ApiError.ERROR_92059);
            }
        }

        //报关费
        if (!soInfo.getIsDeclare()) {
            soInfo.setCustomsFee(BigDecimal.ZERO);
        }

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
        //获取虚拟仓库
        handleVirtualWarehouse(soInfo);

        Boolean updateResult = this.updateById(soInfo);
        if (updateResult) {

            // 保存附件
            TableName tableName = SoInfoEntity.class.getDeclaredAnnotation(TableName.class);
            omsAttachmentService.batchSave(dto.getAttachUrlList(), dto.getAttachNameList(), tableName.value(), id);

            /**
             * 添加修改日志
             */
            operateLogService.addModuleOperateLogByObj(old, soInfo, ModuleTypeEnum.SO.getCode(), id, "", "");
            // 此处调整为明细的币制取主单的币制
            if (StrUtils.isNotEmpty(dto.getCurrency()) && CollUtil.isNotEmpty(dto.getDetailList())) {
                dto.getDetailList().stream().forEach(detail -> detail.setCurrency(dto.getCurrency()));
            }
            //修改 订单详情
            soDetailService.updateSoDetail(id, dto.getIsTax(), dto.getDetailList(),old);
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(BaseApproveParamDTO dto, SoInfoEntity entity) {
        List<SoInfoEntity> list = Arrays.asList(entity);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        if(!ingStatus.equals(entity.getApproveStatus().getStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        //审核流程
        approveProcess(list, dto);
        //添加日志
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售订单", ApproveTypeEnum.getName(dto.getType())).concat("【%s】").concat(StringUtils.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO.getCode(), pairList, "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public Boolean approveEnd(BaseApproveParamDTO dto, List<SoInfoEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        List<String> ids = list.stream().map(SoInfoEntity::getId).collect(Collectors.toList());
        //意见
        String userName = UserContext.getDefaultLoginUser().getUserName();
        String approveStatus = "";
        List<DmpPushTaskEntity> pushTaskList = new ArrayList<>();
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approveStatus = ApproveStatusEnum.APPROVE.getStatus();

            //推送同步中台dmp任务
            list.forEach(obj -> syncKingdeeSoService.syncOrderToDmp(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));

            //发送金蝶
            sendPushTask(list,SyncOperateEnum.OPERATE_APPROVE.getCode());

            //同步数帝云
            list.forEach(obj -> sdyFieldOrderHandler(obj.getId(), SyncOperateEnum.OPERATE_APPROVE.getCode()));
        } else {
            //审核不通过
            approveStatus = ApproveStatusEnum.REJECT.getStatus();
        }
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(approveStatus), userName);

        if (dto.getType().equals(ApproveType.PASS)) {
            // 填入首批上市时间
            setFirstListingTime(ids);
        }
        return result;
    }

    private void setFirstListingTime(List<String> ids) {
        //填入首批上市时间
        List<ListingTimeDTO> listingTimeList = baseMapper.listFirstListingTime(ids);
        List<String> skuIds = listingTimeList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductSaleEntity> productSaleEntities = plmTaskFeign.listProductSaleBySkuId(skuIds);
        //获取到没有设置首批下单时间的sku
        List<String> skuIdList = productSaleEntities.stream().filter(req -> req.getListingTime() == null).map(req -> req.getSkuId()).collect(Collectors.toList());
        List<ProductSaleEntity> ProductSaleEntityList = new ArrayList<>();
        for (String skuId : skuIdList) {
            ListingTimeDTO firstListingTimeDTO = listingTimeList.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(firstListingTimeDTO)) {
                ProductSaleEntity productSaleEntity = new ProductSaleEntity();
                productSaleEntity.setSkuId(skuId);
                productSaleEntity.setListingTime(firstListingTimeDTO.getSoDate());
                ProductSaleEntityList.add(productSaleEntity);
            }
        }
        plmTaskFeign.updateProductSaleListingTimeBatch(ProductSaleEntityList);
    }

    /**
     * 反审核
     *
     * @param dto
     * @param entity
     * @param soChangeEntityList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 16:48
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(BaseIdsDTO.IdsDTO dto, SoInfoEntity entity, List<SoChangeEntity> soChangeEntityList) {
        List<String> ids = Arrays.asList(entity.getId());
        List<SoInfoEntity> list = Arrays.asList(entity);
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        if(!BillApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }

        //检查关联单据
        checkRefBill(ids);
        //有销售变更的也不能反审核
        List<SoChangeEntity> soChangeList = soChangeEntityList.stream().filter(v->v.getSoId().equals(entity.getId())).collect(Collectors.toList());
        long soChangeCount = soChangeList.stream().filter(s -> !s.getInvalidStatus()).count();
        //表示 有变更中的销售变更单
        if (soChangeCount > 0) {
            throw new ServiceException(ApiError.ERROR_92047);
        }

        List<Pair<String, String>> rejectPairList = list.stream().filter(s -> s.getApproveStatus().getStatus().equals(approveStatus)).
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        Boolean result = this.updateApproveStatus(list, BillApproveStatusEnum.getByStatus(waitSubmitStatus), "");

        //反审核
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.APPROVE.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), rejectPairList, "状态变更");
            // TODO 收款字段需补

            //推送同步中台dmp任务
            list.forEach(obj -> syncKingdeeSoService.syncOrderToDmp(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));

            //发送金蝶
            sendPushTask(list,SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

            //同步数帝云
            list.forEach(obj -> sdyFieldOrderHandler(obj.getId(), SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        }
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
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
        List<MachineRefSoEntity> machineRefSoList = machineInfoFeign.listBySoIdList(soIds);
        if (CollectionUtils.isNotEmpty(machineRefSoList)) {
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
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
        //需要同步的数据
        List<SoInfoEntity> syncList = list.stream().filter(obj -> !BillApproveStatusEnum.DRAFT.equals(obj.getApproveStatus())).collect(Collectors.toList());

        //删除释放冻结库存
        ids.stream().forEach(obj -> unLockVirtualInventory(obj));

        Boolean result = this.removeByIds(ids);

        if (result) {
            //添加日志
            String content = "删除销售订单[%s]";
            List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "删除");

            //同步数帝云
            list.forEach(obj -> sdyFieldOrderHandler(obj.getId(), SyncOperateEnum.OPERATE_DELETE.getCode()));

            //删除明细
            soDetailService.removeByMainIdList(ids);

            //推送金蝶
            if (CollectionUtils.isNotEmpty(syncList)) {

                //推送同步中台dmp任务
                syncList.forEach(obj -> syncKingdeeSoService.syncOrderToDmp(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));

                //发送金蝶
                sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
            }
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
    @GlobalTransactional(rollbackFor = Exception.class)
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

        //作废释放冻结库存
        ids.stream().forEach(obj -> unLockVirtualInventory(obj));

        lambdaUpdate().in(SoInfoEntity::getId, ids).
                set(SoInfoEntity::getInvalidStatus, Boolean.TRUE).update();
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        String content = "作废了一个销售订单【%s】,作废原因: ".concat(remark);
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.SO.getCode(), pairList, "作废");

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        return Boolean.TRUE;
    }


    /**
     * 导出数据
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-17 18:02
     */
    @Override
    public Boolean exportExcel(SoInfoDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("销售订单", EXPORT_OMS_SO.getCode(), dto);
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
        String countryId = "";
        String mailAddress = "";
        if (customerInfo != null) {
            customerName = customerInfo.getName();
            countryId = customerInfo.getCountryId();
            customer.setCustomerSellerId(customerInfo.getSellerId());
//            mailAddress = customerInfo.getMailAddress();
        }
        //客户开票信息
        List<InvoiceDTO.ViewDTO> invoiceList = StringUtils.isNotEmpty(customerId) ? customerInvoiceService.listByMainId(customerId) : null;
        if (CollectionUtils.isNotEmpty(invoiceList)){
            InvoiceDTO.ViewDTO viewDTO = invoiceList.stream().filter(InvoiceDTO.ViewDTO::getIsDefault).findFirst().orElse(invoiceList.get(0));
            mailAddress = viewDTO.getInvoiceAddress();
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
        customer.setMailAddress(mailAddress);
        String deliveryMode = customer.getDeliveryMode();
        String deliveryModeName = DeliveryModeEnum.getName(deliveryMode);
        customer.setDeliveryModeName(deliveryModeName);
        String addressType = soInfo.getAddressType();
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
        customer.setCountryId(countryId);
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
    public SoInfoDTO.ExportPdfDTO listSoContractPdf(String id) {
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
        result.setContactAddress(customer.getMailAddress());

        result.setCurrency(customer.getCurrency());
        result.setFirstSignDate(customer.getCreateTime().toLocalDate());
        result.setSecondSignDate(customer.getCreateTime().toLocalDate());

        result.setCompany(customer.getSalesOrgName());
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
        BigDecimal totalTaxAmount = details.stream().map(SoDetailDTO.ExportPdfDTO::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalTaxAmount(totalTaxAmount);
        result.setDetails(details);
        String chineseAmount = Convert.digitToChinese(totalTaxAmount);
        result.setChineseAmount(chineseAmount);
        return result;
    }

    @Override
    public void exportSoContractPdf(String id,HttpServletResponse response) {
        //销售合同订单
        SoInfoDTO.ExportPdfDTO result = listSoContractPdf(id);
        if (ObjectUtil.isEmpty(result)) {
            throw new ServiceException("未发现销售合同订单数据");
        }

        List<String> base64List = new ArrayList<>();
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.SO_CONTRACT_PDF);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
        if (inputStream == null) {
            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
            return;
        }
        Map<String, Object> map = BeanUtil.beanToMap(result);
        JRBeanCollectionDataSource detail = new JRBeanCollectionDataSource(result.getDetails());
        map.put("detail", detail);
        //JasperHelperUtil.export(FileTypeEnum.PDF.getCode(), "pfd", inputStream, map, result.getDetails());

        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map, Arrays.asList(result));
        String base = Base64.getEncoder().encodeToString(bytes);
        base64List.add("data:application/pdf;base64," + base);
        PdfUtil.exportBase64ForPdf(response,base64List);
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
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
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
                    Integer deliveryNoticeQty = soDeliveryNoticeList.stream().filter(f -> f.getSourceDetailId().equals(viewDTO.getSourceDetailId())).
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
            CustomerInfoEntity customerInfoEntity = customerList.stream().filter(c -> c.getId().equals(customerId)).findFirst().orElse(new CustomerInfoEntity());
            item.setCustomerName(Optional.ofNullable(customerInfoEntity.getName()).orElse(""));
            String type = item.getOrderType();
            item.setCountryId(customerInfoEntity.getCountryId());
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
        List<SoInfoDTO.GenerateDeliveryView> resultList = new ArrayList<>();
        for (SoInfoDTO.GenerateDeliveryView view : viewList) {
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
            view.setDeliveryQty(view.getSalesQty() - view.getAlreadyDeliveryQty());
            view.setPlanDeliveryDate(view.getRequireDate());
            String customerName = customerList.stream().filter(c -> c.getId().equals(view.getCustomerId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            view.setCustomerName(customerName);
            if(view.getDeliveryQty()>0){
                resultList.add(view);
            }
        }
        if(CollectionUtils.isEmpty(resultList)){
            throw new ServiceException("没有待发货明细");
        }
        return resultList;
    }

    /**
     * 下推退货单预览
     * @param detailIds detailIds 订单详情id
     */
    @Override
    public List<SoInfoDTO.GenerateSoReturnView> generateSoReturnView(List<String> detailIds) {
        List<SoDetailEntity> soDetailEntities = soDetailService.listByIds(detailIds);
        //获取订单主表id
        List<String> mainIds = soDetailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());


        checkIfPushDown(mainIds);
        List<SoInfoDTO.GenerateSoReturnView> viewList = baseMapper.generateSoReturnView(detailIds);
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
     * @param syncKingdeeId
     * @return
     * @author yl
     * @date 2023-05-31 14:20
     */
    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        try {
            if (StringUtils.isEmpty(id)) {
                return Boolean.TRUE;
            }

            KingdeeDTO dto = new KingdeeDTO();
            dto.setId(syncKingdeeId);
            dto.setNumber("");
            dto.setKingdeePushModuleCode(KingdeePushModuleEnum.SAL_SALEORDER.getCode());
            JSONObject soJson = dmpTaskFeign.getByKingdeeId(dto);
            List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(id);
            List<SoDetailEntity> updateList = new ArrayList<>(10);
            if (soJson != null) {
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) soJson.get("SaleOrderEntry");
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
            }
            if (updateList.size() > 0) {
                soDetailService.updateBatchById(updateList);
            }

            return this.lambdaUpdate()
                    .eq(SoInfoEntity::getId, id)
                    .set(StringUtils.isNotBlank(syncKingdeeId), SoInfoEntity::getSyncKingdeeId, syncKingdeeId)
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
        return this.lambdaQuery().in(SoInfoEntity::getReceiveAddressId, addressIds).count();
    }


    /**
     * 导出销售订单发票信息
     *
     * @param id
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
        Boolean invalidStatus = soInfo.getInvalidStatus();
        if (invalidStatus != null && invalidStatus) {
            throw new ServiceException(ApiError.ERROR_92022);
        }
        String approveStatus = soInfo.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        String waitSubmit = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = Arrays.asList(approve, approveIng, waitSubmit);
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

        String receiveConditionStr="";

        //收款条件
        if (StringUtils.isNotBlank(receiveCondition)) {
            KingdeeReceiptConditionEntity receiveConditionEntity = kingdeeReceiptConditionService.getById(receiveCondition);
            if (Objects.nonNull(receiveConditionEntity)) {
                receiveConditionStr = receiveConditionEntity.getName();
            }
        }
        soPi.setReceiveConditionStr(receiveConditionStr);
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
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIdList);
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
            viewPi.setTaxAmount(item.getTaxAmount());
            viewPi.setPriceStr(symbol + price);
            viewPi.setAmountStr(symbol + amount);
            String model = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().map(SkuVO::getDeclareModel).orElse("");
            viewPi.setModel(model);
            viewPi.setDesc("");
            i++;
            viewPiList.add(viewPi);

        }

        //总金额
        BigDecimal totalAmount = viewPiList.stream().map(SoDetailDTO.ViewPiDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        //总含税金额
        BigDecimal totalTaxAmount = viewPiList.stream().map(SoDetailDTO.ViewPiDTO::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        //总数量
        Integer totalQty = viewPiList.stream().mapToInt(SoDetailDTO.ViewPiDTO::getQty).sum();
        soPi.setTotalAmountStr(currencySymbol + totalTaxAmount);
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
        if (Objects.isNull(costParam.getDiscountAmount())) {
            costParam.setDiscountAmount(BigDecimal.ZERO);
            ;
        }
        skuCostProfitResult.setTaxAmountBefore(costParam.getTaxAmount());
        skuCostProfitResult.setTaxAmount(costParam.getTaxAmount());
        skuCostProfitResult.setSaleAmount(costParam.getSaleAmount());
        // 销售金额需要减去折扣金额
        if (Objects.nonNull(costParam.getDiscountAmount()) && costParam.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saleAmountAfter = costParam.getSaleAmount().subtract(costParam.getDiscountAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
            costParam.setSaleAmount(saleAmountAfter);
            skuCostProfitResult.setSaleAmount(saleAmountAfter);

            BigDecimal taxAmountAfter = costParam.getTaxAmount().subtract(costParam.getDiscountAmount()).setScale(4, BigDecimal.ROUND_HALF_UP);
            costParam.setTaxAmount(taxAmountAfter);
            skuCostProfitResult.setTaxAmount(taxAmountAfter);
        }

        // 销售金额转换
        if (Objects.equals(costParam.getCurrency(), "CNY")) {
            skuCostProfitResult.setAmountLocalCurrency(skuCostProfitResult.getSaleAmount());
            skuCostProfitResult.setAllAmountLocalCurrency(skuCostProfitResult.getTaxAmount());
            skuCostProfitResult.setExchangeRate(BigDecimal.ONE);
        }
        BigDecimal saleRate;
        if (Objects.nonNull(costParam.getSaleAmount()) &&
                costParam.getSaleAmount().compareTo(BigDecimal.ZERO) > 0 &&
                !Objects.equals(costParam.getCurrency(), "CNY")) {
            saleRate = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), costParam.getCurrency());
            log.info("提交的币制：{}，转换后汇率：{}", costParam.getCurrency(), saleRate);
            if (Objects.isNull(saleRate) || saleRate.compareTo(BigDecimal.ZERO) <= 0) {
                costParam.setSaleAmount(BigDecimal.ZERO);
                costParam.setTaxAmount(BigDecimal.ZERO);
            } else {
                skuCostProfitResult.setExchangeRate(saleRate);
                // 转换成人民币销售金额
                costParam.setSaleAmount(saleRate.multiply(costParam.getSaleAmount()).setScale(4, BigDecimal.ROUND_HALF_UP));
                skuCostProfitResult.setAllAmountLocalCurrency(saleRate.multiply(costParam.getTaxAmount()).setScale(4, BigDecimal.ROUND_HALF_UP));
            }
            skuCostProfitResult.setExchangeRate(saleRate);
            skuCostProfitResult.setAmountLocalCurrency(costParam.getSaleAmount());
        }

        BigDecimal purchasePrice = BigDecimal.ZERO;
        // 获取SKU对应的一级供应商
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(Lists.newArrayList(costParam.getSkuId()));
        // 获取采购单价
//        String supplierId = skuList.stream().filter(r -> Objects.equals(r.getSkuId(), costParam.getSkuId())).findFirst().map(SkuVO::getSupplierId).orElse(null);
//        List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//        if (StrUtil.isNotEmpty(supplierId)) {
//            purchasePriceList = scmTaskFeign.listSupplierSkuPrice(Lists.newArrayList(supplierId));
//        }
        SkuVO skuVO = null;
        String currency = CurrencyEnum.CNY.getCurrencyCode();
        if (CollUtil.isNotEmpty(skuList)) {
            skuVO = skuList.stream().filter(r -> Objects.equals(r.getSkuId(), costParam.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
//                purchasePrice = skuVO.getActualTaxCost();
//                BigDecimal purchaseTaxRate = supplierSkuPrice.getTaxRate();
                //不含税单价（不含税价格=含税价格/（1+增值税税率））
                purchasePrice = skuVO.getNotTaxCostPrice();

                skuCostProfitResult.setPurchasePrice(purchasePrice);
            }
        }
        log.info("提交的币制：{}", costParam.getCurrency());

        BigDecimal rate = BigDecimal.ZERO;
//        if (Objects.nonNull(supplierSkuPrice)) {
            LocalDate purchaseDate = costParam.getBillDate();
            if (Objects.nonNull(purchaseDate)) {
                String purchaseDateStr = purchaseDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                rate = dmpTaskFeign.getRate(purchaseDateStr, currency);
            }

//        }
        // 最新的采购单价币制转换（非人民币）--dmp采购成本是人民币币种 这里注释不用计算
//        if (Objects.nonNull(skuCostProfitResult.getPurchasePrice()) &&
//                skuCostProfitResult.getPurchasePrice().compareTo(BigDecimal.ZERO) == 1 &&
//                !Objects.equals(supplierSkuPrice.getCurrency(), "CNY")) {
//
//            log.info("找到的最新的采购价目的币制：{}，含税单价：【{}】, 销售订单日期：{}，转换后汇率：{}", supplierSkuPrice.getCurrency(), supplierSkuPrice.getTaxPrice(), rate);
//            // 未找到汇率直接返回
//            if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
//                skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
//                return skuCostProfitResult;
//            } else {
//                // skuCostProfitResult.setExchangeRate(rate);
//                // 转换成人民币采购单价
//                purchasePrice = rate.multiply(skuCostProfitResult.getPurchasePrice()).setScale(4, BigDecimal.ROUND_HALF_UP);
//            }
//        }
        costParam.setAmountLocalCurrency(skuCostProfitResult.getAmountLocalCurrency());
        // 计算成本毛利信息
        skuCostProfitResult = SoUtils.calCostProfit(purchasePrice, costParam, skuCostProfitResult);
        return skuCostProfitResult;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void brushCostData(LocalDate startDate, LocalDate endDate) {
        // 查询需要重刷数据的创建时间范围
        List<SoInfoEntity> soList = lambdaQuery().ge(SoInfoEntity::getCreateTime, startDate).le(SoInfoEntity::getCreateTime, endDate.plusDays(1)).list();
        if (CollUtil.isEmpty(soList)) {
            return;
        }
        log.warn("共查询到销售订单数据{}条", soList.size());
        for (SoInfoEntity soInfoEntity : soList) {
            List<SoDetailEntity> detailList = soDetailService.listBaseByMainId(soInfoEntity.getId());
            if (CollUtil.isEmpty(detailList)) {
                continue;
            }
            List<String> skuIdList = detailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
            List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
            // 供应商id集合
//            List<String> supplierIds = skuList.stream().filter(r -> StrUtil.isNotEmpty(r.getSupplierId())).map(SkuVO::getSupplierId).distinct().collect(Collectors.toList());
//            List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//            if (CollUtil.isNotEmpty(supplierIds)) {
//                purchasePriceList = scmTaskFeign.listSupplierSkuPrice(supplierIds);
//            }
            log.warn("开始计算销售订单【{}】成本毛利数据", soInfoEntity.getCode());
            for (SoDetailEntity item : detailList) {
                // 计算毛利成本
                soDetailService.calCost(skuList, soInfoEntity.getBillDate(), item, Boolean.TRUE);
                soDetailService.updateCost(item.getId(), item);
            }

        }
    }

    @Override
    public void brushCostData(String id) {
        SoInfoEntity soInfoEntity = super.getById(id);
        if(null == soInfoEntity){
            throw new ServiceException(ApiError.ERROR_92016);
        }
        List<SoDetailEntity> detailList = soDetailService.listBaseByMainId(soInfoEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        List<String> skuIdList = detailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        // 供应商id集合
//        List<String> supplierIds = skuList.stream().filter(r -> StrUtil.isNotEmpty(r.getSupplierId())).map(SkuVO::getSupplierId).distinct().collect(Collectors.toList());
//        List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//        if (CollUtil.isNotEmpty(supplierIds)) {
//            purchasePriceList = scmTaskFeign.listSupplierSkuPrice(supplierIds);
//        }
        for (SoDetailEntity item : detailList) {
            // 计算毛利成本
            soDetailService.calCost(skuList, soInfoEntity.getBillDate(), item, Boolean.TRUE);
            soDetailService.updateCost(item.getId(), item);
        }
    }

    private void checkDict(SoInfoEntity soInfoEntity) {
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList) && StrUtils.isNotEmpty(soInfoEntity.getReceiveMethod())) {
            DictBasicEntity dictBasicEntity = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), soInfoEntity.getReceiveMethod())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(dictBasicEntity), () -> new ServiceException("收款方式错误"));
        }
        // 收款条件
        List<KingdeeReceiptConditionEntity> receiveConditionList = kingdeeReceiptConditionService.list();
        if (CollectionUtils.isNotEmpty(receiveConditionList) && StrUtils.isNotEmpty(soInfoEntity.getReceiveCondition())) {
            KingdeeReceiptConditionEntity receiptCondition = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getId(), soInfoEntity.getReceiveCondition())).findFirst().orElse(null);
            ValidatorUtil.isTrue(Objects.nonNull(receiptCondition), () -> new ServiceException("收款条件错误"));
        }
        // 收款账号
        if (StrUtils.isNotEmpty(soInfoEntity.getReceiveAccount())) {
            List<BankAccountEntity> bankAccountList = bankAccountService.findByOrgIdAndAccountNo(soInfoEntity.getSalesOrgId(), soInfoEntity.getReceiveAccount());
            ValidatorUtil.isTrue(CollUtil.isNotEmpty(bankAccountList), () -> new ServiceException("收款账号错误"));
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
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);

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


    /**
     * 导出合同的excel
     *
     * @param id
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-17 17:39
     */
    @Override
    public Boolean exportSoContractExcel(String id, HttpServletResponse response) {
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
        result.setContactAddress(customer.getMailAddress());

        result.setCurrency(customer.getCurrency());
        result.setFirstSignDate(customer.getCreateTime().toLocalDate());
        result.setSecondSignDate(customer.getCreateTime().toLocalDate());

        result.setCompany(customer.getSalesOrgName());
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
        BigDecimal totalTaxAmount = details.stream().map(SoDetailDTO.ExportPdfDTO::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        result.setTotalAmount(totalAmount);
        result.setTotalTaxAmount(totalTaxAmount);
        result.setDetails(details);
        String chineseAmount = Convert.digitToChinese(totalTaxAmount);
        result.setChineseAmount(chineseAmount);
        int i = 1;
        for (SoDetailDTO.ExportPdfDTO item : details) {
            item.setNo(i);
            i++;
        }

        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SoContract.xlsx";
        String name = "销售合同信息";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(details, result, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售合同信息导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    @Override
    public List<String> temporaryUpdate() {
        List<SoInfoEntity> soList = this.list();
        List<String> errorList = Lists.newArrayList();
        for (SoInfoEntity so : soList) {
            try {
                String soId = so.getId();
                Boolean isTax = so.getIsTax();
                //总折扣额
                BigDecimal discountAmount = so.getDiscountAmount();
                List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(soId);
                // 金额折扣处理
                SoUtils.handleDetailAmount(isTax, discountAmount, soDetailList);
                soDetailService.updateBatchById(soDetailList);
            } catch (Exception e) {
                errorList.add(so.getId());
            }
        }

        return errorList;
    }

    @Override
    public Boolean updateDetailRemark(BaseIdsDTO.RemarkDTO dto) {
        soDetailService.updateRemarkByIds(dto.getIds(), dto.getRemark());
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateRemark(BaseIdsDTO.RemarkDTO dto) {
        List<SoInfoEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        lambdaUpdate().in(SoInfoEntity::getId, dto.getIds())
                .set(SoInfoEntity::getRemark, dto.getRemark())
                .update(new SoInfoEntity());
        return Boolean.TRUE;
    }

    @Override
    public Boolean checkSoPushDeliveryNotice(String id) {
        SoInfoEntity soInfoEntity = this.getById(id);
        if (Objects.isNull(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        String soId = soInfoEntity.getId();
        Map<String, Long> pushDownMap = soDeliveryNoticeFeign.getPushDownDeliveryNoticeCnt(Lists.newArrayList(soId));
        if (CollUtil.isNotEmpty(pushDownMap) && pushDownMap.containsKey(soId)
                && pushDownMap.get(soId) > 0) {
            log.warn("销售订单【{}】已下推过有效发货通知单【{}】个", soId, pushDownMap.get(soId));
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public List<SoDetailDTO.CalDetailResultDTO> calSkuCostProfit(SoInfoDTO.CalCostProfitDTO calCostProfitDTO) {
        List<SoDetailDTO.CalDetailResultDTO> resultList = Lists.newArrayList();
        List<SoDetailDTO.CalDetailDTO> detailList = calCostProfitDTO.getDetailList();
        List<String> skuIdList = detailList.stream().map(SoDetailDTO.CalDetailDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuCostByIds(skuIdList);
        // 供应商id集合
//        List<String> supplierIds = skuList.stream().filter(r -> StrUtil.isNotEmpty(r.getSupplierId())).map(SkuVO::getSupplierId).distinct().collect(Collectors.toList());
//        List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//        if (CollUtil.isNotEmpty(supplierIds)) {
//            purchasePriceList = scmTaskFeign.listSupplierSkuPrice(supplierIds);
//        }
        List<SoDetailEntity> soDetailList = BeanMapper.copyList(detailList, SoDetailEntity.class);
        //是否含税
        long count = soDetailList.stream().filter(s -> Objects.isNull(s.getTaxRate()) || (Objects.nonNull(s.getTaxRate()) &&
                s.getTaxRate().compareTo(BigDecimal.ZERO) == 0)).count();

        // 金额折扣处理
        SoUtils.handleDetailAmount(count > 0, calCostProfitDTO.getDiscountAmount(), soDetailList);
        for (int i = 0; i < soDetailList.size(); i++) {
            SoDetailEntity item = soDetailList.get(i);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(item.getPrice(), multiplyTax);
            item.setTaxPrice(taxPrice);

            // 计算毛利成本
            soDetailService.calCost(skuList, calCostProfitDTO.getBillDate(), item, Boolean.FALSE);
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }
            SoDetailDTO.CalDetailResultDTO result = new SoDetailDTO.CalDetailResultDTO();
            BeanMapper.copy(item, result);
            result.setTaxPriceLc(MathUtil.multiply(taxPrice, exchangeRate));
            resultList.add(result);
        }
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAddress(String soId, String receiveAddressId, String addressType, String receiverName, String telNumber) {
        lambdaUpdate().set(SoInfoEntity::getReceiveAddressId, receiveAddressId).set(SoInfoEntity::getAddressType, addressType)
                .set(SoInfoEntity::getReceiverName, receiverName).set(SoInfoEntity::getTelNumber, telNumber)
                .eq(SoInfoEntity::getId, soId)
                .update();
    }


    /**
     * 获取到折扣额大于0的历史数据
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoInfoDTO.ListDTO>
     * @author yl
     * @date 2023-09-28 10:31
     */
    @Override
    public List<SoInfoDTO.ListDTO> listRepairHistoryDb() {
        return baseMapper.listRepairHistoryDb();
    }


    /**
     * 导出国内的spi数据
     *
     * @param id
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-10-12 14:43
     */
    @Override
    public Boolean exportSoDomesticPI(String id, HttpServletResponse response) {
        SoInfoDTO.SoPIDTO soPi = new SoInfoDTO.SoPIDTO();
        SoInfoEntity soInfo = this.getById(id);
        if (Objects.isNull(soInfo)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        Boolean invalidStatus = soInfo.getInvalidStatus();
        if (invalidStatus != null && invalidStatus) {
            throw new ServiceException(ApiError.ERROR_92022);
        }
        String approveStatus = soInfo.getApproveStatus().getStatus();
        String approve = ApproveStatusEnum.APPROVE.getStatus();
        String approveIng = ApproveStatusEnum.APPROVE_ING.getStatus();
        String waitSubmit = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = Arrays.asList(approve, approveIng, waitSubmit);
        if (!statusList.contains(approveStatus)) {
            throw new ServiceException(ApiError.ERROR_92022);
        }


        soPi.setCode(soInfo.getCode());
        soPi.setBillDate(soInfo.getCreateTime().toLocalDate());

        soPi.setRemark(soInfo.getRemark());
        //客户id
        String customerId = soInfo.getCustomerId();
        String receiveCondition = soInfo.getReceiveCondition();
        //销售员 id
        String sellerId = soInfo.getSellerId();
        FindUserDTO user = sysUserFeign.getUserByUserId(sellerId);
        if (Objects.nonNull(user)) {
            soPi.setSellerName(user.getUserName());
            soPi.setSellerEmail(user.getEmail());
            soPi.setSellerMobile(user.getMobile());
        }

        //销售组织
        soPi.setSalesOrgName(soInfo.getSalesOrgName());

        //收款条件
        KingdeeReceiptConditionEntity receiptConditionEntity = kingdeeReceiptConditionService.getById(receiveCondition);
        if (Objects.isNull(receiptConditionEntity)) {
            soPi.setReceiveConditionStr("");
        } else {
            soPi.setReceiveConditionStr(receiptConditionEntity.getName());

        }

        CustomerInfoEntity customerInfo = StringUtils.isNotEmpty(customerId) ? customerInfoService.getById(customerId) : null;
        String customerName = "";
        if (customerInfo != null) {
            customerName = customerInfo.getName();
        }
        soPi.setCustomerName(customerName);

        List<CustomerAddressDTO.ViewDTO> addressList = customerAddressService.listByMainId(customerId);
        if (CollectionUtils.isNotEmpty(addressList)) {
            CustomerAddressDTO.ViewDTO address = addressList.get(0);
            soPi.setAddress(address.getAddress());
            soPi.setEmail(address.getEmail());
            soPi.setTelNumber(address.getTelNumber());
        }


        List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(id);
        List<SoDetailDTO.ViewPiDTO> viewPiList = new ArrayList<>(soDetailList.size());
        int i = 1;
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuLogisticsByIds(skuIdList);
        //本位币
        String symbol = "¥";
        for (SoDetailEntity item : soDetailList) {
            SoDetailDTO.ViewPiDTO viewPi = new SoDetailDTO.ViewPiDTO();
            viewPi.setNo(i);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            //汇率
            BigDecimal exchangeRate = item.getExchangeRate();
            if (Objects.isNull(exchangeRate)) {
                exchangeRate = MathUtil.BigDecimal_1;
            }

            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //销售单价
            BigDecimal price = item.getPrice();

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = MathUtil.multiply(price, multiplyTax);
            taxPrice = MathUtil.multiply(taxPrice, exchangeRate);
            item.setTaxPrice(taxPrice);
            viewPi.setSkuNo(item.getSkuNo());
            Integer qty = item.getQty();
            viewPi.setQty(qty);
            BigDecimal taxAmountBefore = MathUtil.multiply(taxPrice, qty);
            viewPi.setAmount(taxAmountBefore);
            viewPi.setTaxPriceStr(symbol + taxPrice);
            viewPi.setAmountStr(symbol + taxAmountBefore);
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                viewPi.setModel(skuVO.getDeclareModel());
                viewPi.setMaterials(skuVO.getMaterials());
                viewPi.setProductName(skuVO.getSkuName());
            } else {
                viewPi.setModel("");
                viewPi.setProductName("");
                viewPi.setMaterials("");
            }
            viewPi.setImageUrl("");
            i++;
            viewPiList.add(viewPi);

        }

        //总金额
        BigDecimal totalAmount = viewPiList.stream().map(SoDetailDTO.ViewPiDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //总数量
        Integer totalQty = viewPiList.stream().mapToInt(SoDetailDTO.ViewPiDTO::getQty).sum();
        soPi.setTotalAmountStr(symbol + totalAmount);
        soPi.setTotalQty(totalQty);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/PIDomestic.xlsx";
        String name = "销售单国内发票信息";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(viewPiList, soPi, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("销售单国内发票导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }


    /**
     * 下载b2b 导入模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-10-17 10:26
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/b2bsoExport.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("b2b 销售订单导入 downloadTemplate  出错了 e>>>>>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入销售订单
     *
     * @param excelFile
     * @param response
     * @return void
     * @author yl
     * @date 2023-10-17 10:34
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        B2BSoImportExcelListener excelListenerUtil = new B2BSoImportExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), B2BSoImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<B2BSoImportExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
            if (CollectionUtils.isEmpty(excelDateList)) {
                throw new ServiceException(ApiError.ERROR_95123);
            }
            //错误的
            List<B2BSoImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            //数据验证
            List<B2BSoImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            //处理验证成功数据
            handleImportSuccessList(successList, errorList);
            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/b2bsoExportError.xlsx";
                String name = "B2BSo";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
                return Boolean.FALSE;
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        return Boolean.TRUE;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean generateMachineInfo(List<String> ids) {
        List<SoDetailEntity> soDetailEntityList = soDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(soDetailEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //主表信息
        List<String> mainIds = soDetailEntityList.stream().map(SoDetailEntity::getMainId).collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = this.listByIds(mainIds);
        if (CollectionUtils.isEmpty(soInfoEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        String codes = soInfoEntityList.stream().filter(obj -> !BillApproveStatusEnum.APPROVE.equals(obj.getApproveStatus())).map(SoInfoEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(codes)) {
            throw new ServiceException(ApiError.ERROR_SO_PUSH_APPROVE_STATUS,codes);
        }

        //非组合品不能下推加工单
        List<String> skuIds = soDetailEntityList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<CfgRulePickingStagingEntity> warehouseStagingList = FeignQuery.list(CfgRulePickingStagingEntity.class);
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        Boolean isHasAdd = Boolean.FALSE;
        //生成加工单
        for (SoInfoEntity entry : soInfoEntityList) {
            MachineInfoDTO.AddDTO addDTO = new MachineInfoDTO.AddDTO();
            addDTO.setType(MachineTypeEnum.ORDINARY.getCode());
            addDTO.setBillDate(LocalDate.now());

            addDTO.setReceiverId(userInfo.getUid());
            addDTO.setWarehouseKeeperId(userInfo.getUid());
            addDTO.setWarehouseId(entry.getWarehouseId());
            addDTO.setWorkType(WorkTypeEnum.ASSEMBLE.getCode());
            addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
            addDTO.setSourceId(entry.getId());
            addDTO.setSourceCode(entry.getCode());
            //明细信息
            List<SoDetailEntity> soDetailList = soDetailEntityList.stream().filter(obj -> entry.getId().equals(obj.getMainId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(soDetailList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            List<MachineDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SoDetailEntity soDetailEntity : soDetailList) {
                MachineDetailDTO.AddDTO addDetailDTO = new MachineDetailDTO.AddDTO();
                addDetailDTO.setSkuId(soDetailEntity.getSkuId());
                addDetailDTO.setSkuNo(soDetailEntity.getSkuNo());
                addDetailDTO.setQty(soDetailEntity.getQty());
                // 获取仓库暂存区默认配置
                CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                        .filter(staging -> PickingBillTypeEnum.B2B.getCode().equals(staging.getBillType()))
                        .filter(staging -> staging.getWarehouseId().equals(entry.getWarehouseId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                addDetailDTO.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                List<BomChildrenSkuDTO> bomList = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(soDetailEntity.getSkuId()) && BomTypeEnum.COMBINATION.getType().equals(obj.getType())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(bomList)) {
                    continue;
                }
                addDetailDTO.setReferenceVersion(bomList.get(0).getBomVersion());
                addDetailDTO.setRefCode(entry.getCode());
                addDetailDTO.setRefId(entry.getId());
                addDetailDTO.setRefDetailId(soDetailEntity.getId());
                List<MachineSubComponentsDTO.AddDTO> subComponentsList = new ArrayList<>();
                for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
                    MachineSubComponentsDTO.AddDTO subComponentsDTO = new MachineSubComponentsDTO.AddDTO();
                    subComponentsDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    subComponentsDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    subComponentsDTO.setWarehouseId(entry.getWarehouseId());
                    subComponentsDTO.setQty(soDetailEntity.getQty() * bomChildrenSkuDTO.getQuantity());
                    subComponentsDTO.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                    subComponentsList.add(subComponentsDTO);
                }
                addDetailDTO.setSubComponentsList(subComponentsList);
                detailList.add(addDetailDTO);
            }
            //如果没有明细则跳过无需新增
            if (CollectionUtils.isEmpty(detailList)) {
                continue;
            }
            addDTO.setDetailList(detailList);
            machineInfoFeign.addMachineInfo(addDTO);
            isHasAdd = Boolean.TRUE;
        }
        if (!isHasAdd) {
            throw new ServiceException(ApiError.ERROR_SO_INFO_PUSH_MACHINE_NOT_EXIST_DATA);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoInfoDTO.GenerateSoOutView> generateSoOutView(List<String> ids) {
        List<SoDetailEntity> soDetailEntityList = soDetailService.listSoDetailByIds(ids);
        List<String> mainIds = soDetailEntityList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntityList = this.listByIds(mainIds);
        List<SoOutstockDetailDTO.DeliveryQtyDTO> allDeliveryQtyDTOList = soOutstockFeign.listDetailBySoDetailIds(ids);
        List<String> customerIds = soInfoEntityList.stream().map(SoInfoEntity::getCustomerId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.listByIds(customerIds);
        List<SoInfoDTO.GenerateSoOutView> viewList = new ArrayList<>();
        //根据ids查询sku信息
        List<String> skuIdList = soDetailEntityList.stream().map(SoDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (SoDetailEntity soDetailEntity : soDetailEntityList) {
            SoInfoEntity soInfoEntity = soInfoEntityList.stream().filter(v->v.getId().equals(soDetailEntity.getMainId())).findFirst().orElse(null);
            ProductDetailEntity productDetailEntity  = productDetailEntityList.stream().filter(v->v.getId().equals(soDetailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            if(Objects.isNull(soInfoEntity)){
                throw new ServiceException("销售订单为空"+soDetailEntity.getId());
            }
            if(!soInfoEntity.getApproveStatus().equals(BillApproveStatusEnum.APPROVE)){
                throw new ServiceException( CharSequenceUtil.format("只有已审核的单据可以下推销售出库单:{}",soInfoEntity.getCode()));
            }
            CustomerInfoEntity customerInfo = customerInfoEntities.stream().filter(v->v.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            List<SoOutstockDetailDTO.DeliveryQtyDTO> deliveryQtyDTOList = allDeliveryQtyDTOList.stream().filter(v->v.getSoDetailId().equals(soDetailEntity.getId())).collect(Collectors.toList());
            SoInfoDTO.GenerateSoOutView soOutView = SoInfoConverter.INSTANCE.soDetailToGenerateSoOutView(soDetailEntity,soInfoEntity,customerInfo);
            Integer actualDeliveryQty = deliveryQtyDTOList.stream().mapToInt(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).sum();
            soOutView.setWaitDeliveryQty(soDetailEntity.getQty() - actualDeliveryQty);
            soOutView.setProductName(productDetailEntity.getName());
            viewList.add(soOutView);
        }
        return viewList;
    }

    @Override
    public List<BatchResultDTO> generateSoOut(List<SoInfoDTO.GenerateSoOutView> generateSoOutViewList) {
        Map<String,List<SoInfoDTO.GenerateSoOutView>> groupMap = generateSoOutViewList.stream().collect(Collectors.groupingBy(SoInfoDTO.GenerateSoOutView::getCode));
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        groupMap.forEach((key,val)->{
            List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateSoOutstockViewDTOList = new ArrayList<>();
            for (SoInfoDTO.GenerateSoOutView soOutView : val) {
                if(soOutView.getActualDeliveryQty() > soOutView.getWaitDeliveryQty()){
                    batchResultDTOList.add(BatchResultDTO.fail(key,key, CharSequenceUtil.format("{}实发数量不能大于待发数量",soOutView.getSkuNo())));
                    return;
                }
                SoOutstockDTO.GenerateSoOutstockViewDTO generateB2cDTO = SoInfoConverter.INSTANCE.soOutViewToGenerateSoOut(soOutView);
                generateSoOutstockViewDTOList.add(generateB2cDTO);
            }
            try {
                soOutstockFeign.addB2bPushDownNo(generateSoOutstockViewDTOList);
            }catch (Exception e){
                batchResultDTOList.add(BatchResultDTO.fail(key,key, CharSequenceUtil.format("生成销售出库单失败:{}",e.getMessage())));
            }
        });

        return batchResultDTOList;
    }

    @Override
    public SoInfoDTO.LockVirtualInventoryDTO viewLockVirtualInventory(String id) {
        SoInfoEntity soInfoEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        if (soInfoEntity.getInvalidStatus()) {
            throw new ServiceException( CharSequenceUtil.format("销售订单【{}】已作废不支持锁定库存",soInfoEntity.getCode()));
        }
        SoInfoDTO.LockVirtualInventoryDTO lockVirtualInventoryDTO = handleLockVirtualInventory(soInfoEntity);
        return lockVirtualInventoryDTO;
    }

    @Override
    public List<SoInfoDTO.BatchLockVirtualInventoryDTO> viewBatchLockVirtualInventory(List<String> detailIdList) {
        List<SoDetailEntity> soDetailList = soDetailService.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<SoInfoDTO.BatchLockVirtualInventoryDTO> resuleList = handleBatchLockVirtualInventory(detailIdList,soDetailList);
        return resuleList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unLockVirtualInventory(String id) {
        SoInfoEntity soInfoEntity = getById(id);
        if (ObjectUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92016);
        }
        if (soInfoEntity.getInvalidStatus()) {
            throw new ServiceException( CharSequenceUtil.format("销售订单【{}】已作废不支持释放库存",soInfoEntity.getCode()));
        }
        List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(id);
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        List<String> detailIdList = soDetailList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        BatchResultDTO resultDTO = soDetailService.batchUnLockVirtualInventory(detailIdList, null);
        if (!resultDTO.getSuccess()) {
            throw new ServiceException(resultDTO.getMsg());
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoInfoDTO.PagingViewDTO> exportSo(PagingDTO<SoInfoDTO.ExportDTO> dto) {

        //获取导出数据
        List<String> fieldList = CollectionUtils.isEmpty(dto.getParams().getAdvanceQueryDTOList()) ? new ArrayList<>() :  dto.getParams().getAdvanceQueryDTOList().stream().map(AdvanceQueryDTO::getField).collect(Collectors.toList());
        dto.getParams().setFieldList(fieldList);
        Page<SoInfoDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        //填充分页列表
        fillPagingDb(page.getRecords());
        return new PagingVO<>(page);
    }


    /**
     * 处理导入数据
     *
     * @param successList
     * @param errorList
     * @return void
     * @author yl
     * @date 2023-10-26 11:53
     */
    private void handleImportSuccessList(List<B2BSoImportExcelDTO> successList, List<B2BSoImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //销售员标识
        String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();
        //币别
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();

        //核算组织
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Lists.newArrayList());
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //金蝶业务员列表
        List<KingdeeOperatorRefPostDTO.OperatorDTO> kingdeeBusinessOperatorList = kingdeeFeign.listBusinessOperatorByUserIdList(new ArrayList<>());
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
        //收款账号
        List<String> receiveAccountList = successList.stream().map(B2BSoImportExcelDTO::getReceiveAccount).distinct().collect(Collectors.toList());
        //根据收款账号获取数据
        List<BankAccountEntity> bankAccountList = bankAccountService.listByAccountNameList(receiveAccountList);
        List<String> keyList = new ArrayList<>(5);
        //收款方式
        keyList.add(DictBasicTypeEnum.RECEIVE_METHOD.getType());

        //交货方式
        keyList.add(DictBasicTypeEnum.DELIVERY_MODE.getType());
        //贸易条款
        keyList.add(DictBasicTypeEnum.TRADE_TERM.getType());
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(keyList);
        List<String> customerNameList = successList.stream().map(B2BSoImportExcelDTO::getCustomerName).distinct().collect(Collectors.toList());
        //客户列表
        List<CustomerInfoEntity> customerList = customerInfoService.listByNameList(customerNameList);
        //收货地址
        List<String> customerIdList = customerList.stream().map(CustomerInfoEntity::getId).collect(Collectors.toList());
        List<CustomerAddressEntity> customerAddressList = customerAddressService.listByMainIdList(customerIdList);
        //sku
        List<String> skuNoList = successList.stream().map(B2BSoImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);

        // 供应商id集合
//        List<String> supplierIds = skuList.stream().filter(r -> StrUtil.isNotEmpty(r.getSupplierId())).map(SkuVO::getSupplierId).distinct().collect(Collectors.toList());
//        List<PurchasePriceDTO.SupplierSkuPrice> purchasePriceList = Lists.newArrayList();
//        if (CollUtil.isNotEmpty(supplierIds)) {
//            purchasePriceList = scmTaskFeign.listSupplierSkuPrice(supplierIds);
//        }

        //收款条件
        List<KingdeeReceiptConditionEntity> receiptConditionList = kingdeeReceiptConditionService.list();

        //以序号分组
        Map<String, List<B2BSoImportExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(B2BSoImportExcelDTO::getNo));

        for (Map.Entry<String, List<B2BSoImportExcelDTO>> entry : map.entrySet()) {
            String no = entry.getKey();
            List<B2BSoImportExcelDTO> list = entry.getValue();
            SoInfoEntity addSo = new SoInfoEntity();
            String mainId = IdWorker.getIdStr();
            addSo.setId(mainId);
            List<String> errorMsgList = new ArrayList<>();
            B2BSoImportExcelDTO mainInfo = list.get(0);
            //要货日期
            String requireDateStr = mainInfo.getRequireDate();
            LocalDate requireDate = LocalDateUtil.parseStrToLocalDate(requireDateStr);
            if (Objects.isNull(requireDate)) {
                errorMsgList.add("要货日期不能为空");
            }
            addSo.setRequireDate(requireDate);
            //单据日期
            String billDateStr = mainInfo.getBillDate();
            LocalDate billDate = LocalDateUtil.parseStrToLocalDate(billDateStr);
            if (Objects.isNull(billDate)) {
                errorMsgList.add("单据日期不能为空");
            }
            addSo.setBillDate(billDate);
            if (Objects.nonNull(requireDate) && Objects.nonNull(billDate) && requireDate.compareTo(billDate) < 0) {
                errorMsgList.add("要货日期必须大于单据日期");
            }
            //单据类型
            String orderTypeStr = mainInfo.getOrderTypeStr();
            String orderType = BillTypeEnum.getCodeByName(orderTypeStr);
            if (StringUtils.isBlank(orderType)) {
                errorMsgList.add("单据类型不存在");
            }
            //生成单号
//            String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSD, BusinessNoTypeEnum.CODE_XSD.getCode()));
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSD);
            addSo.setCode(code);

            addSo.setOrderType(orderType);
            //销售组织
            String salesOrgName = mainInfo.getSalesOrgName();
            BaseIdDTO.CodeDTO salesOrg = orgList.stream().filter(o -> o.getName().equals(salesOrgName)).findFirst().
                    orElse(null);
            if (Objects.isNull(salesOrg)) {
                errorMsgList.add("销售组织不存在");
            }
            String salesOrgId = "";
            String salesOrgCode = "";
            if (Objects.nonNull(salesOrg)) {
                salesOrgId = salesOrg.getId();
                salesOrgCode = salesOrg.getCode();
            }
            addSo.setSalesOrgId(salesOrgId);
            addSo.setSalesOrgName(salesOrgName);
            //销售部门
            String salesDeptName = mainInfo.getSalesDeptName();
            String salesDeptId = deptList.stream().filter(d -> d.getName().equals(salesDeptName)).findFirst().
                    map(SysDepartmentDTO::getId).orElse("");
            addSo.setSalesDeptId(salesDeptId);
            if (StringUtils.isBlank(salesDeptId)) {
                errorMsgList.add("销售部门不存在");
            }
            //销售员
            String sellerName = mainInfo.getSellerName();
            String sellerId = userList.stream().filter(u -> u.getUserName().equals(sellerName)).findFirst().
                    map(FindUserDTO::getUserId).orElse("");
            addSo.setSellerId(sellerId);
            addSo.setSellerName(sellerName);
            if (StringUtils.isBlank(sellerId)) {
                errorMsgList.add("销售员不存在");
            }
            String finalSalesOrgId1 = salesOrgId;
            KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeBusinessOperatorList.stream().filter(k -> k.getUserId().equals(sellerId) &&
                    k.getOrgId().equals(finalSalesOrgId1) &&
                    xsyCode.equals(k.getTypeCode())
            ).findFirst().orElse(null);
            if (Objects.isNull(businessOperator)) {
                errorMsgList.add("金蝶未存在该销售员");
            }
            //是否收取运费
            String isCollectShippingFeeStr = mainInfo.getIsCollectShippingFee();
            Boolean isCollectShippingFee = Boolean.FALSE;
            if (StringUtils.isNotBlank(isCollectShippingFeeStr)) {
                isCollectShippingFee = isCollectShippingFeeStr.equals("是");
            }
            addSo.setIsCollectShippingFee(isCollectShippingFee);
            //仓库
            String warehouseName = mainInfo.getWarehouseName();
            WarehouseDTO.UpdateDTO warehouse = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).findFirst().
                    orElse(null);
            String warehouseId = "";
            String warehouseOrgId = "";
            if (Objects.isNull(warehouse)) {
                errorMsgList.add("仓库不存在");
            } else {
                warehouseId = warehouse.getId();
                warehouseOrgId = warehouse.getOrgId();
            }
            addSo.setWarehouseId(warehouseId);
            addSo.setWarehouseOrgId(warehouseOrgId);
            String finalWarehouseOrgId = warehouseOrgId;
            String warehouseOrgName = orgList.stream().filter(o -> o.getId().equals(finalWarehouseOrgId)).findFirst().
                    map(BaseIdDTO.CodeDTO::getName).orElse("");
            addSo.setWarehouseOrgName(warehouseOrgName);
            //收款账号
            String receiveAccountStr = mainInfo.getReceiveAccount();
            String receiveAccount = "";
            String finalSalesOrgId = salesOrgId;
            BankAccountEntity bankAccount = bankAccountList.stream().filter(b -> b.getAccountName().equals(receiveAccountStr) &&
                    finalSalesOrgId.equals(b.getOrgId())).findFirst().orElse(null);
            if (Objects.isNull(bankAccount)) {
                errorMsgList.add("收款账号不存在");
            } else {
                receiveAccount = bankAccount.getId();
            }
            addSo.setReceiveAccount(receiveAccount);

            //收款方式
            String receiveMethodStr = mainInfo.getReceiveMethod();
            String receiveMethod = dictBasicList.stream().filter(d -> d.getName().equals(receiveMethodStr)).findFirst().
                    map(DictBasicEntity::getValue).orElse("");
            if (StringUtils.isBlank(receiveMethod)) {
                errorMsgList.add("收款方式不存在");
            }
            addSo.setReceiveMethod(receiveMethod);
            //收款日期
            String receiveDateStr = mainInfo.getReceiveDate();
            if (StringUtils.isNotBlank(receiveDateStr)) {
                addSo.setReceiveDate(LocalDateUtil.parseStrToLocalDate(receiveDateStr));
            }
            //贸易条款
            String tradeTermStr = mainInfo.getTradeTerm();
            String tradeTerm = "";
            if (StringUtils.isNotBlank(tradeTermStr)) {
                tradeTerm = dictBasicList.stream().filter(d -> d.getName().equals(tradeTermStr)).findFirst().
                        map(DictBasicEntity::getValue).orElse("");
                if (StringUtils.isBlank(tradeTerm)) {
                    errorMsgList.add("贸易条款不存在");
                }
            }
            addSo.setTradeTerm(tradeTerm);
            //客户
            String customerName = mainInfo.getCustomerName();
            String customerId = customerList.stream().filter(c -> c.getName().equals(customerName)).map(CustomerInfoEntity::getId).
                    findFirst().orElse("");
            if (StringUtils.isBlank(customerId)) {
                errorMsgList.add("客户不存在");
            }
            addSo.setCustomerId(customerId);
            //收货人
            String receiverName = mainInfo.getReceiverName();
            addSo.setReceiverName(receiverName);

            //联系电话
            String telNumber = mainInfo.getTelNumber();
            addSo.setTelNumber(telNumber);
            //收货地址
            String receiveAddress = mainInfo.getReceiveAddress();
            String customerAddressId = customerAddressList.stream().filter(c -> c.getAddress().equals(receiveAddress)).
                    findFirst().map(CustomerAddressEntity::getId).orElse("");
            if (StringUtils.isBlank(customerAddressId)) {
                errorMsgList.add("联系地址不存在");
            }
            addSo.setReceiveAddressId(customerAddressId);
            //交货方式
            String deliveryModeStr = mainInfo.getDeliveryMode();
            String deliveryMode = dictBasicList.stream().filter(d -> d.getName().equals(deliveryModeStr)).findFirst().
                    map(DictBasicEntity::getValue).orElse("");
            if (StringUtils.isBlank(deliveryMode)) {
                errorMsgList.add("交货方式不存在");
            }
            addSo.setDeliveryMode(deliveryMode);

            //地址类型
            String addressTypeStr = mainInfo.getAddressType();
            String addressType = AddressTypeEnum.getCodeByName(addressTypeStr);
            if (StringUtils.isBlank(addressType)) {
                errorMsgList.add("地址类型不存在");
            }
            addSo.setAddressType(addressType);
            //币别
            String currencyStr = mainInfo.getCurrency();
            DictCurrencyEntity currencyEntity = currencyList.stream().filter(c -> c.getName().equals(currencyStr)).
                    findFirst().orElse(null);
            String symbol = "";
            String currency = "";
            if (Objects.isNull(currencyEntity)) {
                errorMsgList.add("币种不存在");
            } else {
                symbol = currencyEntity.getSymbol();
                currency = currencyEntity.getId();
            }
            addSo.setCurrencySymbol(symbol);
            addSo.setCurrency(currency);
            //是否含税
            String isTaxStr = mainInfo.getIsTax();
            Boolean isTax = "是".equals(isTaxStr);
            addSo.setIsTax(isTax);

            String receiveConditionStr = mainInfo.getReceiveCondition();
            String receiveCondition = receiptConditionList.stream().filter(d -> d.getName().equals(receiveConditionStr)).findFirst().
                    map(KingdeeReceiptConditionEntity::getId).orElse("");
            if (StringUtils.isBlank(receiveCondition)) {
                errorMsgList.add("收款条件不存在");
            }
            addSo.setReceiveCondition(receiveCondition);
            String remark = mainInfo.getRemark();
            addSo.setRemark(remark);
            //银行手续费
            String bankServiceFeeStr = mainInfo.getBankServiceFee();
            BigDecimal bankServiceFee = MathUtil.getBigDecimalByStr(bankServiceFeeStr);
            //运费
            String shippingFeeStr = mainInfo.getShippingFee();
            BigDecimal shippingFee = MathUtil.getBigDecimalByStr(shippingFeeStr);

            //收款金额
            String receiveAmountStr = mainInfo.getReceiveAmount();
            BigDecimal receiveAmount = MathUtil.getBigDecimalByStr(receiveAmountStr);

            //报关费
            String customsFeeStr = mainInfo.getCustomsFee();
            BigDecimal customsFee = MathUtil.getBigDecimalByStr(customsFeeStr);

            //折扣总额
            String discountAmountStr = mainInfo.getDiscountAmount();
            BigDecimal discountAmount = MathUtil.getBigDecimalByStr(discountAmountStr);
            addSo.setBankServiceFee(bankServiceFee);
            addSo.setShippingFee(shippingFee);
            addSo.setReceiveAmount(receiveAmount);
            addSo.setCustomsFee(customsFee);
            addSo.setDiscountAmount(discountAmount);
            Boolean isAdd = Boolean.TRUE;
            List<SoDetailEntity> soDetailList = new ArrayList<>(list.size());
            for (B2BSoImportExcelDTO item : list) {
                SoDetailEntity addDetail = new SoDetailEntity();
                addDetail.setMainId(mainId);
                //是否赠品
                String isGiftStr = item.getIsGift();
                addDetail.setIsGift("是".equals(isGiftStr));
                //是否补发
                String isReissueStr = item.getIsReissue();
                addDetail.setIsReissue("是".equals(isReissueStr));
                //是否关闭
                String isCloseStr = item.getIsClose();
                addDetail.setIsClose("是".equals(isCloseStr));
                addDetail.setRemark(item.getDetailRemark());
                //sku no
                String skuNo = item.getSkuNo();
                String skuId = "";
                SkuVO skuVO = skuList.stream().filter(s -> s.getSkuNo().equals(skuNo)).findFirst().orElse(null);
                if (Objects.isNull(skuVO)) {
                    errorMsgList.add("sku不存在");
                } else {
                    skuId = skuVO.getSkuId();
                }
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    isAdd = Boolean.FALSE;
                    errorMsgList = errorMsgList.stream().distinct().collect(Collectors.toList());
                    item.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    errorList.add(item);
                }
                addDetail.setSkuId(skuId);
                addDetail.setSkuNo(skuNo);
                //币种
                addDetail.setCurrency(currency);
                //数量
                String qtyStr = item.getQty();
                Integer qty = StringUtils.isNotBlank(qtyStr) ? Integer.valueOf(qtyStr) : 0;
                addDetail.setQty(qty);
                //销售单价
                String priceStr = item.getPrice();
                BigDecimal price = MathUtil.getBigDecimalByStr(priceStr);
                addDetail.setPrice(price);

                //税率
                String taxRateStr = item.getTaxRate();
                BigDecimal taxRate = MathUtil.getBigDecimalByStr(taxRateStr);
                addDetail.setTaxRate(taxRate);
                soDetailList.add(addDetail);
            }

            try {
                //错误的编号集合
                List<String> errorNoList = errorList.stream().map(B2BSoImportExcelDTO::getNo).distinct().collect(Collectors.toList());

                //表示可以添加
                if (isAdd && !errorNoList.contains(no)) {
                    // 金额折扣处理
                    SoUtils.handleDetailAmount(isTax, discountAmount, soDetailList);
                    for (int i = 0; i < soDetailList.size(); i++) {
                        SoDetailEntity item = soDetailList.get(i);
                        // 计算毛利成本
                        soDetailService.calCost(skuList, addSo.getBillDate(), item, Boolean.FALSE);
                    }
                    BigDecimal allAmountLc = soDetailList.stream().map(SoDetailEntity::getAllAmountLocalCurrency).reduce(BigDecimal.ZERO, BigDecimal::add);
                    addSo.setAllAmountLc(allAmountLc);
                    this.save(addSo);
                    soDetailService.saveBatch(soDetailList);
                } else {
                    continue;
                }
            } catch (Exception e) {
                errorMsgList.add(e.getMessage());
            }

            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                errorMsgList = errorMsgList.stream().distinct().collect(Collectors.toList());
                isAdd = Boolean.FALSE;
                list.get(0).setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.addAll(list);
            }

        }
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<SoInfoEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeSoService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    /**
     * 批量锁库存数据处理
     * @author will
     * @date 2024/7/15 15:54
     * @param soDetailList
     * @return List<BatchLockVirtualInventoryDTO>
     */
    private List<SoInfoDTO.BatchLockVirtualInventoryDTO> handleBatchLockVirtualInventory (List<String> detailIdList,List<SoDetailEntity> soDetailList) {
        List<SoInfoDTO.BatchLockVirtualInventoryDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(soDetailList)) {
            return resultList;
        }
        //主表信息
        List<String> mainIdList = soDetailList.stream().map(SoDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoList = this.listByIds(mainIdList);

        //客户信息
        List<String> customerIdList = soInfoList.stream().map(SoInfoEntity::getCustomerId).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = customerInfoService.listByIds(customerIdList);
        if (CollectionUtils.isEmpty(customerInfoList)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        //仓库
        List<String> warehouseIdList = soInfoList.stream().map(SoInfoEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.getByIds(WarehouseEntity.class, warehouseIdList);

        //虚拟仓库
        List<String> virtualWarehouseIdList = soInfoList.stream().map(SoInfoEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = FeignQuery.getByIds(VirtualWarehouseEntity.class, virtualWarehouseIdList);

        //SKu
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).distinct().collect(Collectors.toList());


        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        if (CollectionUtils.isNotEmpty(bomChildrenList)) {
            List<String> bomSkuIdList = bomChildrenList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
            skuIdList.addAll(bomSkuIdList);
        }

        //产品信息
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //查询虚拟库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyList = virtualInventoryFeign.listInventoryQty(paramDTO);

        //发货通知单
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).distinct().collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(soDetailIdList);

        //销售出库信息
        List<SoOutstockDetailDTO.DeliveryQtyDTO> deliveryQtyList = soOutstockFeign.listDetailBySoDetailIds(soDetailIdList);


        for (String soDetailId :detailIdList) {
            //销售订单明细
            SoDetailEntity soDetailEntity = soDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailId)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }

            SoInfoDTO.BatchLockVirtualInventoryDTO batchLockDTO = new SoInfoDTO.BatchLockVirtualInventoryDTO();
            //主表信息
            SoInfoEntity soInfoEntity = soInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92016);
            }
            batchLockDTO.setId(soInfoEntity.getId());
            batchLockDTO.setDetailId(soDetailEntity.getId());
            batchLockDTO.setCode(soInfoEntity.getCode());
            batchLockDTO.setRemark(soInfoEntity.getRemark());
            //客户信息
            String customerName = customerInfoList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soInfoEntity.getCustomerId())).map(CustomerInfoEntity::getName).findFirst().orElse("");
            batchLockDTO.setCustomerName(customerName);
            batchLockDTO.setSellerName(soInfoEntity.getSellerName());
            batchLockDTO.setRequireDate(soInfoEntity.getRequireDate());
            //仓库信息
            String warehouseName = warehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soInfoEntity.getWarehouseId())).map(WarehouseEntity::getName).findFirst().orElse("");
            batchLockDTO.setWarehouseName(warehouseName);
            //虚拟仓库信息
            String virtualWarehouseName = virtualWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soInfoEntity.getVirtualWarehouseId())).map(VirtualWarehouseEntity::getName).findFirst().orElse("");
            batchLockDTO.setVirtualWarehouseName(virtualWarehouseName);
            batchLockDTO.setSkuNo(soDetailEntity.getSkuNo());
            //产品名称
            String productName = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            batchLockDTO.setProductName(productName);
            batchLockDTO.setQty(soDetailEntity.getQty());
            batchLockDTO.setFrozenQty(soDetailEntity.getFrozenQty());
            //虚拟可用库存
            Integer virtualUsableQty = virtualInventoryQtyList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getVirtualWarehouseId(), soInfoEntity.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getSkuId(), soDetailEntity.getSkuId())
                            && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())
            ).map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).findFirst().orElse(MathUtil.ZERO);
            batchLockDTO.setVirtualUsableQty(virtualUsableQty);


            //销售通知单
            Integer totalNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getSkuId(),soDetailEntity.getSkuId()))
                    .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            batchLockDTO.setTotalNoticeQty(totalNoticeQty);

            Integer effectiveNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getSkuId(),soDetailEntity.getSkuId())
                    && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
            ).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            batchLockDTO.setEffectiveNoticeQty(effectiveNoticeQty);

            //入参
            SoInfoDTO.VirtuaParamScarceDTO paramScarceDTO = new SoInfoDTO.VirtuaParamScarceDTO();
            BeanMapperUtils.copy(soDetailEntity,paramScarceDTO);
            paramScarceDTO.setWarehouseId(soInfoEntity.getWarehouseId());
            paramScarceDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
            //虚拟仓bom库存
            handleVirtualBomScarce(bomChildrenList,virtualInventoryQtyList,paramScarceDTO,totalNoticeQty);
            batchLockDTO.setVirtualUsableQty(paramScarceDTO.getVirtualUsableQty());
            batchLockDTO.setChildScarceList(paramScarceDTO.getChildScarceList());
            batchLockDTO.setIsCombination(paramScarceDTO.getIsCombination());

            //Min 【（销售数量 - 发货通知单数量 - 当前锁定数量），虚拟仓可用库存】
            Integer toFrozenQty = soDetailEntity.getQty() - totalNoticeQty - soDetailEntity.getFrozenQty();
            batchLockDTO.setToFrozenQty(virtualUsableQty > toFrozenQty ? toFrozenQty : virtualUsableQty);
            batchLockDTO.setVirtualScarceQty(paramScarceDTO.getVirtualScarceQty());

            //销售出库单
            Integer outstockQty = deliveryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
            ).map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            batchLockDTO.setOutstockQty(outstockQty);
            batchLockDTO.setUnOutstockQty(soDetailEntity.getDeliveryQty() - outstockQty);
            batchLockDTO.setDetailRemark(soDetailEntity.getRemark());
            resultList.add(batchLockDTO);
        }
        return resultList;
    }

    /**
     * 单个锁定数据处理
     * @author will
     * @date 2024/7/15 15:00
     * @param soInfoEntity
     * @return LockVirtualInventoryDTO
     */
    private SoInfoDTO.LockVirtualInventoryDTO handleLockVirtualInventory (SoInfoEntity soInfoEntity) {
        //对象转换
        SoInfoDTO.LockVirtualInventoryDTO dto =  BeanMapperUtils.map(SoInfoDTO.LockVirtualInventoryDTO.class,soInfoEntity);
        //客户信息
        CustomerInfoEntity customerInfoEntity = customerInfoService.getCustomerById(soInfoEntity.getCustomerId());
        if (ObjectUtil.isEmpty(customerInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92011);
        }
        dto.setCustomerName(customerInfoEntity.getName());
        //订单类型名称
        dto.setOrderTypeName(BillTypeEnum.getName(soInfoEntity.getOrderType()));
        //部门名称
        List<SysDepartmentEntity> sysDepartmentEntityList = sysUserFeign.listDeptByIds(Arrays.asList(soInfoEntity.getSalesDeptId()));
        if (CollectionUtils.isEmpty(sysDepartmentEntityList)) {
            throw new ServiceException(ApiError.ERROR_9029);
        }
        dto.setSalesDeptName(sysDepartmentEntityList.get(0).getName());
        //仓库
        WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class, soInfoEntity.getWarehouseId());
        if (ObjectUtil.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        dto.setWarehouseName(warehouseEntity.getName());
        //虚拟仓库
        VirtualWarehouseEntity virtualWarehouseEntity = FeignQuery.getById(VirtualWarehouseEntity.class, soInfoEntity.getVirtualWarehouseId());
        if (ObjectUtil.isNotEmpty(virtualWarehouseEntity)) {
            dto.setVirtualWarehouseName(virtualWarehouseEntity.getName());
        }
        //明细信息
        List<SoDetailEntity> soDetailList = soDetailService.listBaseByMainId(soInfoEntity.getId());
        if (CollectionUtils.isEmpty(soDetailList)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        //产品信息
        List<String> skuIdList = soDetailList.stream().map(SoDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);

        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        if (CollectionUtils.isNotEmpty(bomChildrenList)) {
            List<String> bomSkuIdList = bomChildrenList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
            skuIdList.addAll(bomSkuIdList);
        }

        //查询虚拟库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(Arrays.asList(soInfoEntity.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(Arrays.asList(soInfoEntity.getVirtualWarehouseId()));
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyList = virtualInventoryFeign.listInventoryQty(paramDTO);

        //发货通知单
        List<String> soDetailIdList = soDetailList.stream().map(SoDetailEntity::getId).distinct().collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = soDeliveryNoticeFeign.listDetailBySourceDetailIds(soDetailIdList);

        //销售出库信息
        List<SoOutstockDetailDTO.DeliveryQtyDTO> deliveryQtyList = soOutstockFeign.listDetailBySoDetailIds(soDetailIdList);

        List<SoInfoDTO.LockVirtualInventoryDetailDTO> detailList = new ArrayList<>();
        for (SoDetailEntity soDetailEntity : soDetailList) {
            //明细
            SoInfoDTO.LockVirtualInventoryDetailDTO detailDTO = BeanMapperUtils.map(SoInfoDTO.LockVirtualInventoryDetailDTO.class, soDetailEntity);
            //明细id
            detailDTO.setDetailId(soDetailEntity.getId());

            String productName = productDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soDetailEntity.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            detailDTO.setProductName(productName);

            detailDTO.setFrozenQty(soDetailEntity.getFrozenQty());

            //销售通知单
            Integer totalNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getSkuId(),soDetailEntity.getSkuId()))
                    .map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            detailDTO.setTotalNoticeQty(totalNoticeQty);

            Integer effectiveNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getSkuId(),soDetailEntity.getSkuId())
                    && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
            ).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            detailDTO.setEffectiveNoticeQty(effectiveNoticeQty);

            //入参
            SoInfoDTO.VirtuaParamScarceDTO paramScarceDTO = new SoInfoDTO.VirtuaParamScarceDTO();
            BeanMapperUtils.copy(detailDTO,paramScarceDTO);
            paramScarceDTO.setWarehouseId(soInfoEntity.getWarehouseId());
            paramScarceDTO.setVirtualWarehouseId(soInfoEntity.getVirtualWarehouseId());
            //虚拟仓bom库存
            handleVirtualBomScarce(bomChildrenList,virtualInventoryQtyList,paramScarceDTO,totalNoticeQty);
            detailDTO.setVirtualUsableQty(paramScarceDTO.getVirtualUsableQty());
            detailDTO.setChildScarceList(paramScarceDTO.getChildScarceList());
            detailDTO.setIsCombination(paramScarceDTO.getIsCombination());

            //Min 【（销售数量 - 发货通知单数量 - 当前锁定数量），虚拟仓可用库存】
            Integer toFrozenQty = soDetailEntity.getQty() - totalNoticeQty - soDetailEntity.getFrozenQty();
            detailDTO.setToFrozenQty(detailDTO.getVirtualUsableQty() > toFrozenQty ? toFrozenQty : detailDTO.getVirtualUsableQty());
            detailDTO.setVirtualScarceQty(paramScarceDTO.getVirtualScarceQty());

            //销售出库单
            Integer outstockQty = deliveryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), soDetailEntity.getId())
                    && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
            ).map(SoOutstockDetailDTO.DeliveryQtyDTO::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            detailDTO.setOutstockQty(outstockQty);
            detailDTO.setUnOutstockQty(soDetailEntity.getDeliveryQty() - outstockQty);
            detailList.add(detailDTO);
        }
        dto.setDetailList(detailList);
        return dto;
    }


    /**
     * 同步速递云B2B订单
     * @param soId
     * @param operateEnum
     */
    public void sdyFieldOrderHandler(String soId, String operateEnum) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        SoInfoDTO.ViewDTO view = this.view(soId);

        List<String> skuNos = view.getDetailList().stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        List<String> skuIds = view.getDetailList().stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);

        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(view.getCurrency()));
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                .in(ProductDetailEntity::getId, parentSkuId)
                .list();

        //查询变更单
        List<String> soDetailIds = view.getDetailList().stream().map(req -> req.getId()).collect(Collectors.toList());
        List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

        //获取取消的订单
        List<String> cancelSoDetailIds = soChangeDetailEntities.stream()
                .filter(req -> SoChangeTypeEnum.TERMINATE.getCode().equals(req.getChangeType().getCode())
                        || SoChangeTypeEnum.DELETE.getCode().equals(req.getChangeType().getCode())
                ).map(req -> req.getSoDetailId()).collect(Collectors.toList());

        BigDecimal totalCanceledGoodsAmount = BigDecimal.ZERO;
        Integer totalCanceledGoodsQty = 0;
        List<SoDetailDTO.ViewDTO> cancelSoDetailList = view.getDetailList().stream().filter(req -> cancelSoDetailIds.contains(req.getId())).collect(Collectors.toList());
        for (SoDetailDTO.ViewDTO viewDTO : cancelSoDetailList) {
            totalCanceledGoodsAmount = totalCanceledGoodsAmount.add(viewDTO.getTaxAmountBefore());
            totalCanceledGoodsQty = totalCanceledGoodsQty + viewDTO.getQty();
        }


        //组织信息
        CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, view.getCustomerId());
        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), view.getSalesOrgId()));

        for (int i = 0; i < view.getDetailList().size(); i++) {
            SoDetailDTO.ViewDTO soDetailEntity = view.getDetailList().get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

            shudiyunB2cOrderDTO.setBiz_uni_key(view.getId()+soDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(view.getCode());
            shudiyunB2cOrderDTO.setBiz_time(localDate.format(view.getBillDate()));
            //默认线下订单
            shudiyunB2cOrderDTO.setTransaction_type("100.30");
            shudiyunB2cOrderDTO.setTransaction_sub_type(view.getTransactionSubType());
            shudiyunB2cOrderDTO.setBiz_status(shudiyunB2cOrderDTO.sdyStatusHandle(operateEnum));

            BigDecimal taxAmountBefore = view.getDetailList().stream().map(req -> req.getTaxAmountBefore()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(taxAmountBefore);

            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(view.getDiscountAmount());

            Integer totalQty = view.getDetailList().stream().mapToInt(SoDetailDTO.ViewDTO::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty);

            //取消金额、数量
            shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(totalCanceledGoodsAmount);

            // 取消商品数量（合计）
            shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalCanceledGoodsQty);

            shudiyunB2cOrderDTO.setBuyer_actual_payment(view.getReceiveAmount());
            shudiyunB2cOrderDTO.setTotal_freight(view.getShippingFee());
            shudiyunB2cOrderDTO.setSales_company_code(view.getSalesOrgId());

            //组织信息
            if (ObjectUtil.isNotEmpty(customerInfo)) {
                String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(view.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
                BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                    shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                    shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                }

                if (customerInfo.getCurrency() == null) {
                    shudiyunB2cOrderDTO.setSettlement_currency_code("");
                } else {
                    shudiyunB2cOrderDTO.setSettlement_currency_code(customerInfo.getCurrency());
                }

                if (customerInfo.getTradeCurrency() == null) {
                    shudiyunB2cOrderDTO.setSettlement_currency_code("");
                } else {
                    shudiyunB2cOrderDTO.setTransaction_currency_code(customerInfo.getTradeCurrency());
                }

                shudiyunB2cOrderDTO.setPlatform_id(customerInfo.getPlatformType());
                shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.checkAndGetByCode(customerInfo.getPlatformType()).getName());
            }


            shudiyunB2cOrderDTO.setShop_no(view.getCustomerId());
            shudiyunB2cOrderDTO.setShop_name(view.getCustomerName());
            shudiyunB2cOrderDTO.setRoot_node_no(view.getCode());
            if (view.getReceiveDate() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDate.format(view.getReceiveDate()));
            }
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(view.getUpdateTime()));
            shudiyunB2cOrderDTO.setGoods_no(soDetailEntity.getSkuNo());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
            if (soDetailEntity.getIsGift()) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soDetailEntity.getSkuId())).findFirst().orElse(null);
            shudiyunB2cOrderDTO.setIs_comb(0);

            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
                if (BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
                    shudiyunB2cOrderDTO.setIs_comb(1);
                    shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                    ProductDetailEntity productDetailEntity = parentSkuList.stream().filter(req -> req.getId().equals(bomChildrenSkuDTO.getParentSkuId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                        shudiyunB2cOrderDTO.setSuite_name(productDetailEntity.getName());
                    }
                }
            }

            shudiyunB2cOrderDTO.setRemark(soDetailEntity.getRemark());
            shudiyunB2cOrderDTO.setGoods_status("10.20");
            // 商品状态
            if (DeliveryStatusEnum.COMPLETE_SHIPMENT.getCode().equals(soDetailEntity.getDeliveryStatus())) {
                shudiyunB2cOrderDTO.setGoods_status("10.10");
            }

            SoDetailDTO.ViewDTO viewDTO = cancelSoDetailList.stream().filter(req -> req.getId().equals(view.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(viewDTO)) {
                shudiyunB2cOrderDTO.setGoods_status("10.20");
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(soDetailEntity.getQty());
            shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());
            shudiyunB2cOrderDTO.setPrice(soDetailEntity.getTaxPrice().subtract(soDetailEntity.getDiscountAmount().divide(MathUtil.valueOf(soDetailEntity.getQty()), 4, RoundingMode.HALF_UP)));
            shudiyunB2cOrderDTO.setGoods_transaction_amount(soDetailEntity.getTaxAmountBefore().subtract(soDetailEntity.getDiscountAmount()));
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
            if (CollectionUtils.isNotEmpty(currencyList)) {
                shudiyunB2cOrderDTO.setTransaction_currency(currencyList.get(0).getName());
                shudiyunB2cOrderDTO.setTransaction_currency_code(currencyList.get(0).getId());
            }

            shudiyunB2cOrderDTO.setPost_amount(view.getShippingFee());
            shudiyunB2cOrderDTO.setMsku_code(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSpuName());
            shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(view.getCode());

            //同步B2B订单
            OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
            omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_OFFLINE_ORDER.getCode());
            omsPushMsgEntity.setSourceId(soDetailEntity.getId());
            omsPushMsgEntity.setSourceCode(view.getCode());
            omsPushMsgEntity.setSyncOperate(operateEnum);
            omsPushMsgEntity.setPushData(JSON.toJSONString(shudiyunB2cOrderDTO));
            omsPushMsgService.save(omsPushMsgEntity);
        }
    }



}
