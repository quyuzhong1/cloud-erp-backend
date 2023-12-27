package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.SearchType;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessBusinessEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.convert.B2cOrderConsumerConverter;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * B2C销售订单表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cService {

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;


    @Autowired
    private InventoryFeign inventoryFeign;

    @Autowired
    private SoB2cDetailService soB2cDetailService;

    @Autowired
    private SoB2cLogisticsService soB2cLogisticsService;

    @Autowired
    private SoB2cReceiverService soB2cReceiverService;

    @Autowired
    private SoB2cRefCategoryService soB2cRefCategoryService;

    @Autowired
    private ShopInfoService shopInfoService;

    @Autowired
    private SysDictFeign sysDictFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private WmsTaskFeign wmsTaskFeign;

    @Autowired
    private SoB2cRefService soB2cRefService;

    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private OrderCategoryDetailService orderCategoryDetailService;

    @Autowired
    private SkuMappingService skuMappingService;

    @Autowired
    private ShopCostService shopCostService;

    @Autowired
    private RuleOrderApprovalService ruleOrderApprovalService;

    @Autowired
    private RuleDeliveryWarehouseService ruleDeliveryWarehouseService;

    @Autowired
    private RuleLogisticsService ruleLogisticsService;

    @Autowired
    private SoB2cFinanceService soB2cFinanceService;

    @Autowired
    private LogisticsBillFeign logisticsBillFeign;

    @Autowired
    private LogisticsFeign logisticsFeign;


    @Autowired
    private ListingInfoService listingInfoService;

    @Autowired
    private ShopAuthService shopAuthService;

    @Autowired
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;


    @Autowired
    private ThirdWarehouseFeign thirdWarehouseFeign;

    @Autowired
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Autowired
    private SoB2cErrorService soB2cErrorService;

    @Autowired
    private SoB2cDeliveryInterceptFeign soB2cDeliveryInterceptFeign;


    @Override
    public PagingVO<SoB2cDTO.ListDTO> paging(PagingDTO<SoB2cDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //列表Tab查询状态处理
        handleTableParam(pagingParamDTO.getParams());
        IPage<SoB2cDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoB2cDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoB2cTabEnum[] values = SoB2cTabEnum.values();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        for (SoB2cTabEnum item : values) {
            SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
            searchParamDTO.setPermissionSql(param.getPermissionSql());
            SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
            //搜索类型
            searchParamDTO.setTabFlag(item.getCode());
            //列表Tab查询状态处理
            Boolean isFlag = handleTableParam(searchParamDTO);
            Integer count = MathUtil.ZERO;
            if (isFlag) {
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SoB2cEntity add(SoB2cDTO.AddDTO addDTO, String code) {
        SoB2cEntity soB2cEntity = new SoB2cEntity();
        BeanMapperUtils.copy(addDTO, soB2cEntity);

        // 数据处理
        handleData(soB2cEntity, true, true);
        //创建时间
        soB2cEntity.setCreateTime(ObjectUtils.isEmpty(addDTO.getCreateTime()) ? LocalDateTime.now() : addDTO.getCreateTime());
        soB2cEntity.setCode(code);
        log.info("开始新增B2C销售订单表");
        if (StrUtil.isBlank(code)) {
            // 生成单号
            String businessNo = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SO_B2C);
            soB2cEntity.setCode(businessNo);
        }
        boolean save = super.save(soB2cEntity);
        if (!save) {
            throw new ServiceException("B2C销售订单表保存失败");
        }

        //新增物流信息
        soB2cLogisticsService.add(addDTO.getLogisticsDTO(), soB2cEntity.getId());
        //新增买家信息
        soB2cReceiverService.add(addDTO.getReceiverDTO(), soB2cEntity.getId());
        //新增明细
        soB2cDetailService.add(addDTO.getDetailList(), soB2cEntity.getId());
        //新增财务信息
        addSoB2cFinance(soB2cEntity);
        //新增订单分类
        if (CollectionUtils.isNotEmpty(addDTO.getCategoryIdList())) {
            List<SoB2cRefCategoryDTO.AddDTO> addList = addDTO.getCategoryIdList().stream().map(obj -> new SoB2cRefCategoryDTO.AddDTO(soB2cEntity.getId(), obj)).collect(Collectors.toList());
            soB2cRefCategoryService.add(addList, soB2cEntity.getId());
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "B2C销售订单表", soB2cEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "新增操作");
        Map<String, Object> map = new HashMap<>();
        //明细信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        String id = soB2cEntity.getId();
        //自动匹配订单规则
        Boolean isSuccess = approveRule(id, detailList, map);
        return soB2cEntity;
    }


    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO update(SoB2cDTO.UpdateDTO updateDTO) {
        SoB2cEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        //未付款数据不能编辑
        if (ObjectUtil.isEmpty(old.getPayStatus()) || SoB2cPayStatusEnum.ENUM_PAYMENT.getCode().equals(old.getPayStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PAYMENT_NOT_UPDATE, old.getCode());
        }

        SoB2cEntity soB2cEntity = BeanMapperUtils.map(SoB2cEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cEntity, true, true);

        log.info("编辑 开始修改B2C销售订单表数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soB2cEntity);
        if (!save) {
            throw new ServiceException("B2C销售订单表保存失败");
        }

        //修改物流信息
        soB2cLogisticsService.update(updateDTO.getLogisticsDTO(), soB2cEntity.getId());
        //修改买家信息
        soB2cReceiverService.update(updateDTO.getReceiverDTO(), soB2cEntity.getId());
        //修改明细
        soB2cDetailService.update(updateDTO.getDetailList(), soB2cEntity.getId());
        //修改订单分类
        soB2cRefCategoryService.update(updateDTO.getCategoryIdList(), soB2cEntity.getId());


        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        //店铺信息
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(Arrays.asList(old.getShopId(), soB2cEntity.getShopId()));
        if (CollectionUtils.isNotEmpty(shopList)) {
            String oldShopName = shopList.stream().filter(obj -> obj.getId().equals(old.getShopId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            old.setShopName(oldShopName);
            String newShopName = shopList.stream().filter(obj -> obj.getId().equals(soB2cEntity.getShopId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            soB2cEntity.setShopName(newShopName);
        }
        //明细信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        Map<String, Object> map = new HashMap<>();
        //自动匹配订单规则
        Boolean isSuccess = approveRule(soB2cEntity.getId(), detailList, map);

        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), old.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, soB2cEntity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return BatchResultDTO.success(soB2cEntity.getId(), soB2cEntity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id, Boolean isProcess) {
        SoB2cEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2C销售订单表数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2C销售订单表状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2C销售订单表流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }

        // 记录操作日志
        log.info("提交 开始记录B2C销售订单表日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SoB2cEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(SoB2cEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        String userId = userInfo.getUid();
        if (StringUtils.isBlank(userId)) {
            userId = "0";
        }
        approveDTO.setUserId(userId);
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }


    /**
     * 作废
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        // 待提交或审核不通过并且未作废允许作废
        if (SoB2cInvalidTypeEnum.ENUM_MANUAL.equals(soB2cInvalidTypeEnum)) {
            if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
                throw new ServiceException(ApiError.ERROR_98005);
            }
        }
        log.info("作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SoB2cEntity::getInvalidRemark, remark)
                .set(SoB2cInvalidTypeEnum.ENUM_AUTOMATIC.equals(soB2cInvalidTypeEnum), SoB2cEntity::getRemark, remark)
                .set(SoB2cEntity::getInvalidType, soB2cInvalidTypeEnum.getCode())
                .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO unInvalid(String id, SoB2cInvalidTypeEnum soB2cInvalidTypeEnum) {
        SoB2cEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        if (InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_INVALID);
        }
        if (!soB2cInvalidTypeEnum.getCode().equals(entity.getInvalidType())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_INVALID, entity.getCode(), soB2cInvalidTypeEnum.getName());
        }
        log.info("反作废 开始修改B2C销售订单表状态数据，id：【{}】", id);
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .update();

        log.info("反作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反作废操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "反作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UN_INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoB2cEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        //审核通过进行匹配规则
        if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
            //明细信息
            List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(entity.getId());
            Map<String, Object> map = new HashMap<>();
            //匹配审核规则
            handleMatchJson(entity.getId(), detailList, map);
            //自动匹配配货规则
            distributionRule(entity.getId(), detailList, map);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateRemark(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_REMARK);
        }

        this.lambdaUpdate().eq(SoB2cEntity::getId, id).set(SoB2cEntity::getRemark, remark).update(new SoB2cEntity());
        String msg = StrUtil.format("订单备注由{}变更为{}", entity.getRemark(), remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "修改订单备注");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新订单备注");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateCategory(String id, SoB2cCategoryTypeEnum typeEnum, List<String> categoryIdList) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_CATEGORY);
        }
        //原分类
        String oldCategoryName = "";
        List<SoB2cRefCategoryEntity> list = soB2cRefCategoryService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(list)) {
            oldCategoryName = list.stream().map(SoB2cRefCategoryEntity::getCategoryName).collect(Collectors.joining(","));
        }
        //添加分类
        String newCategoryName = "";
        List<OrderCategoryDetailEntity> categoryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(categoryIdList)) {
            categoryList = orderCategoryDetailService.listByIds(categoryIdList);
            newCategoryName = categoryList.stream().map(obj -> obj.getName()).collect(Collectors.joining(","));
        }

        String msg = "";
        if (SoB2cCategoryTypeEnum.ENUM_ADD.equals(typeEnum)) {
            addCategory(categoryIdList, id, categoryList);
            msg = "原分类：【{}】，新增分类：【{}】。";
        }
        if (SoB2cCategoryTypeEnum.ENUM_UPDATE.equals(typeEnum)) {
            updateCategory(categoryIdList, id, categoryList);
            msg = "原分类：【{}】，更新分类：【{}】。";
        }
        if (SoB2cCategoryTypeEnum.ENUM_DELETE.equals(typeEnum)) {
            deleteSelectCategory(id, categoryIdList);
            msg = "原分类：【{}】，删除分类。";
        }
        // 记录操作日志
        log.info("更新分类 开始记录B2C销售订单表日志数据，id：【{}】", id);
        operateLogService.addModuleOperateLog(StrUtil.format(msg, oldCategoryName, newCategoryName), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "修改订单备注");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新订单分类");
    }

    @Override
    public List<SoB2cDTO.ViewSoB2cDistributionDTO> viewSoB2cDistribution(BaseIdsDTO.IdsDTO dto) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(dto.getIds());
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //明细信息
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        List<SoB2cDTO.ViewSoB2cDistributionDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : list) {
            SoB2cDTO.ViewSoB2cDistributionDTO viewDTO = new SoB2cDTO.ViewSoB2cDistributionDTO();
            viewDTO.setId(soB2cEntity.getId());
            viewDTO.setCode(soB2cEntity.getCode());
            viewDTO.setSourceAmount(soB2cEntity.getAmount());
            viewDTO.setSourceCurrency(soB2cEntity.getCurrency());
            viewDTO.setAmount(MathUtil.multiply(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            viewDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //物流信息
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsList.stream().filter(obj -> obj.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            viewDTO.setWeight(soB2cLogisticsEntity.getWeight());
            viewDTO.setLogisticsCode(soB2cLogisticsEntity.getCode());
            viewDTO.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());

            //明细信息
            String warehouseNames = soB2cDetailList.stream().filter(obj -> obj.getMainId().equals(soB2cEntity.getId())).map(SoB2cDetailEntity::getWarehouseName).distinct().collect(Collectors.joining(","));
            viewDTO.setWarehouseNames(warehouseNames);
            viewDTO.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
            resultList.add(viewDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO saveSoB2cDistribution(String id, SoB2cDTO.SaveSoB2cDistributionDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待配货和配货中订单允许配货
        if (!SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(entity.getBillStatus())
                && !SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_DISTRIBUTION, entity.getCode());
        }
        //只有已审核数据支持配货
        if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_APPROVE_NOT_DISTRIBUTION, entity.getCode());
        }

        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //存在的物流渠道
        String existChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        //存在的物流单 code
        String code = soB2cLogisticsEntity.getCode();
        /**
         * 是否覆盖
         * 是：按照新选择的物流渠道和仓库下推配货中；如果物流方式跟订单已有的物流不一致，清空物流单号信息，且更新明细仓库
         * 否：新选择的物流渠道和仓库只添加到物流方式和仓库为空的订单，已存在物流方式和仓库的订单不做更改
         */
        Boolean isCover = dto.getIsCover();
        String logisticsChannelId = dto.getLogisticsChannelId();
        if (Boolean.TRUE.equals(isCover)) {
            //如果有物流单号 就要去取消
            if (StringUtils.isNotBlank(code)) {
                //取消物流单
                LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                        channelId(existChannelId).trackNo(code).referenceNumber(entity.getId()).build();
                logisticsBillFeign.cancelBill(cancelBillDTO);
            }
            soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
        } else {
            //当为空就覆盖
            if (StringUtils.isBlank(existChannelId)) {
                soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
            }
        }
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(logisticsChannelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_NOT_EXIST);
        }
        soB2cLogisticsEntity.setLogisticsChannelName(logisticsChannel.getName());
        //物流信息更新
        soB2cLogisticsService.updateById(soB2cLogisticsEntity);
        //明细仓库更新
        if (Boolean.TRUE.equals(isCover)) {
            soB2cDetailService.updateWarehouseIdByMainId(id, dto.getWarehouseId());
        }
        //配货中
        String billStatus = SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode();

        Boolean isPlatformWarehouseOrder = entity.hasPlatformWarehouseOrder();
        //是
        if (isPlatformWarehouseOrder) {
            billStatus = SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode();
        }
        //销售订单更新
        entity.setBillStatus(billStatus);
        entity.setAbnormalType("");
        this.updateById(entity);
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(dto.getWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //操作日志
        String msg = "B2C销售订单配货,物流方式【{}】,仓库【{}】";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, soB2cLogisticsEntity.getName(), warehouseList.get(0).getName()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "手动配货");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动配货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO getLogisticsCode(String id, Boolean isDelivery) {
        String message = "";
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        String paramJson = "";
        String returnJson = "";
        try {
            if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_LOGISTICS_CODE, entity.getCode());
            }
            //只有已审核数据支持配货
            if (!ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_APPROVE_NOT_DISTRIBUTION, entity.getCode());
            }

            //物流信息
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
            if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
            }
            if (StringUtils.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())
                    || StringUtils.isNotBlank(soB2cLogisticsEntity.getCode())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CODE, entity.getCode());
            }
            LogisticsBillDTO.GenerateBillDTO generateBillDTO = makeGenerateBillDTO(entity, soB2cLogisticsEntity);
            paramJson = JSONObject.toJSONString(generateBillDTO);
            //货取物流单号
            LogisticsBillDTO.GenerateBillResultDTO resultDTO = logisticsBillFeign.generateBill(generateBillDTO);
            if (Objects.isNull(resultDTO)) {
                throw new ServiceException("下物流单失败");
            }
            String trackNo = resultDTO.getTrackNoList().stream().collect(Collectors.joining(","));
            String transportNo = resultDTO.getTransportNo();
            soB2cLogisticsService.updateLogisticsCode(id, transportNo, trackNo);
            if (Boolean.TRUE.equals(isDelivery)) {
                //提交发货
                submitDelivery(id);
            }
            //更新销售订单异常信息
            entity.setAbnormalType("");
            this.updateById(entity);
            //操作日志
            String msg = "获取物流单号【{}】";
            operateLogService.addModuleOperateLog(StrUtil.format(msg, transportNo), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "获取物流单号");
            soB2cErrorService.removeErrorOrder(id, SoB2ErrorTypeEnum.GET_LOGISTICS_CODE.getCode());
            return BatchResultDTO.success(entity.getId(), transportNo, "获取物流单号");
        } catch (Exception e) {
            String type = SoB2ErrorTypeEnum.GET_LOGISTICS_CODE.getCode();
            message = e.getMessage();
            //添加异常信息
            soB2cErrorService.generateErrorOrder(id, type, message, paramJson, returnJson);
            log.error("销售订单【{}】 获取物流单失败，异常信息{}", entity.getCode(), message);
        }
        return BatchResultDTO.fail(entity.getId(), "", message);
    }

    /**
     * 组装生成物流单数据
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-27
     */
    private LogisticsBillDTO.GenerateBillDTO makeGenerateBillDTO(SoB2cEntity entity, SoB2cLogisticsEntity soB2cLogisticsEntity) {
        LogisticsBillDTO.GenerateBillDTO result = new LogisticsBillDTO.GenerateBillDTO();
        String id = entity.getId();
        result.setCurrency(entity.getCurrency());
        result.setOrderTime(entity.getBillDate().atStartOfDay());
        result.setChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        result.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        result.setOrderId(id);
        result.setOrderCode(entity.getCode());
        result.setOrderType(OrderTypeEnum.B2C.getCode());
        String shopId = entity.getShopId();
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        result.setShopId(shopId);
        result.setShopName(entity.getShopName());
        result.setIossTaxNo(shopInfoEntity.getIossTaxNo());
        result.setSalesPlatform(entity.getDictPlatform());
        ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(shopAuth)) {
            throw new ServiceException(ApiError.SHOP_NOT_AUTH_ERROR);
        }
        result.setToken(shopAuth.getToken());
        //买家 收货人信息
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(id);
        if (Objects.nonNull(receiverEntity)) {
            LogisticsBillDTO.ReceiverDTO receiverDTO = B2cOrderConverter.INSTANCE.convertReceiver(receiverEntity);
            result.setReceiver(receiverDTO);
        }
        LogisticsBillDTO.PackageDTO packageDTO = B2cOrderConverter.INSTANCE.convertPackage(soB2cLogisticsEntity);
        packageDTO.setCurrency(entity.getCurrency());
        result.setPackageInfo(packageDTO);

        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(id);

        List<LogisticsBillDTO.SkuDTO> skuList = B2cOrderConverter.INSTANCE.convertSku(detailList);
        result.setSkuList(skuList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submitDelivery(String id) {

        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (!SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SUBMIT_DELIVERY, entity.getCode());
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (Objects.isNull(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //物流渠道
        String logisticsChannelId = logisticsEntity.getLogisticsChannelId();
        //物流单号
        String code = logisticsEntity.getCode();
        /**
         * 未获取物流单号或获取物流单号失败的订单不允许提交发货
         * 缺货订单不允许提交发货
         * 存在多发货仓库的，不允许提交发货
         */
        //B2C销售订单明细信息
        List<SoB2cDetailEntity> list = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //发货仓库id 集合
        List<String> deliveryWarehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        if (deliveryWarehouseIdList.size() > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DELIVERY_WAREHOUSE_COMPLEX);
        }
        //检测是否是API 对接的仓库
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(deliveryWarehouseIdList);
        Boolean isApi = CollectionUtils.isNotEmpty(overseasWarehouseList);
        //必须要有物流渠道，没有物流单号可以提交发货
        if (isApi) {
            if (StringUtils.isBlank(logisticsChannelId)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_NOT_NULL, entity.getCode());
            }
        } else {
            //必须要有物流渠道和物流单号后才可以提交发货
            if (StringUtils.isBlank(logisticsChannelId) || StringUtils.isBlank(code)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_AND_CODE_NOT_NULL, entity.getCode());
            }
        }
        //skuId集合
        List<String> skuIdList = list.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //查询可用库存
        InventoryQtyDTO.SkuInventoryParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryParamDTO();
        skuInventoryDTO.setWarehouseIdList(deliveryWarehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        skuInventoryDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<InventoryQtyDTO.SkuInventoryTotalDTO> inventoryList = inventoryFeign.listSkuInventoryByParam(skuInventoryDTO);
        if (CollectionUtils.isEmpty(inventoryList)) {
            log.error("B2C销售订单【{}】未找到可用库存，skuIdList = {}，warehouseIdList = {}", entity.getCode(), skuIdList, deliveryWarehouseIdList);
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_INVENTORY, entity.getCode());
        }
        for (SoB2cDetailEntity detailEntity : list) {
            //验证是否存在可用库存
            Integer useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId()) && obj.getWarehouseId().equals(detailEntity.getWarehouseId()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(MathUtil.ZERO);
            if (MathUtil.compareTo(detailEntity.getQty(), useableQty) > MathUtil.ZERO) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SKU_NOT_INVENTORY, entity.getCode(), detailEntity.getSkuNo(), detailEntity.getWarehouseName());
            }
        }
        entity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        /**
         * 如果是API 对接的仓库
         * 下出库单的命令
         */
        if (isApi) {
            try {
                //下出库单命令
                thirdWarehouseCreateOutStock(id, entity.getCode(), logisticsChannelId, overseasWarehouseList.get(0), list);
            } catch (Exception e) {
                log.error("B2C订单【{}】下出库单异常>>>{}", entity.getCode(), e.getMessage());
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
        } else {
            //生成发货单
            generateSoB2cDeliveryBill(entity, list,logisticsEntity);
        }
        this.updateById(entity);
        //操作日志
        String msg = "B2C销售订单【{}】提交发货";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "提交发货");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "提交发货");
    }

    /**
     * 生成b2c 发货单
     * 如果SKU是销售套装BOM，需要按照子件+数量生成发货单明细
     * 数量=父件销售数量*BOM用量
     *
     * @param entity
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-26 19:48
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void generateSoB2cDeliveryBill(SoB2cEntity entity, List<SoB2cDetailEntity> list,SoB2cLogisticsEntity soB2cLogisticsEntity) {
        SoB2cDeliveryDTO.AddDTO soB2cDelivery = B2cOrderConverter.INSTANCE.convertDelivery(entity);
        soB2cDelivery.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        soB2cDelivery.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        soB2cDelivery.setTransportNo(soB2cLogisticsEntity.getCode());
        List<String> parentSkuIdList = list.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(parentSkuIdList);
        List<SoB2cDeliveryDetailDTO.AddDTO> deliveryDetailList = new ArrayList<>(list.size());
        //表示是对应sku是bom
        if (CollectionUtils.isNotEmpty(bomChildrenSkuList)) {
            for (SoB2cDetailEntity detailItem : list) {
                //相当于父级
                String skuId = detailItem.getSkuId();
                //相当于父级
                String skuNo = detailItem.getSkuNo();
                //数量
                Integer qty = detailItem.getQty();
                //套装的bom
                List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomChildrenSkuList.stream()
                        .filter(req -> req.getParentSkuId().equals(skuId)
                                && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                        ).collect(Collectors.toList());
                //表示有
                if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
                    for(BomChildrenSkuDTO bomSku:bomChildrenSkuDTOS){
                        //该sku 不是套装Bom
                        SoB2cDeliveryDetailDTO.AddDTO deliveryDetailDTO = new SoB2cDeliveryDetailDTO.AddDTO();
                        deliveryDetailDTO.setSkuId(bomSku.getSkuId());
                        deliveryDetailDTO.setSkuNo(bomSku.getSkuNo());
                        Integer quantity=bomSku.getQuantity();
                        deliveryDetailDTO.setDeliveryQty(qty*quantity);
                        deliveryDetailDTO.setSourceDetailId(detailItem.getId());
                        deliveryDetailList.add(deliveryDetailDTO);
                    }
                }else{
                    //该sku 不是套装Bom
                    SoB2cDeliveryDetailDTO.AddDTO deliveryDetailDTO = new SoB2cDeliveryDetailDTO.AddDTO();
                    deliveryDetailDTO.setSkuId(skuId);
                    deliveryDetailDTO.setDeliveryQty(qty);
                    deliveryDetailDTO.setSkuNo(skuNo);
                    deliveryDetailDTO.setSourceDetailId(detailItem.getId());
                    deliveryDetailList.add(deliveryDetailDTO);
                }
            }
        } else {
            //表示沒有bom
            deliveryDetailList = B2cOrderConverter.INSTANCE.convertDeliveryDetail(list);
        }
        soB2cDelivery.setDetailList(deliveryDetailList);
        soB2cDeliveryFeign.addSoB2cDelivery(soB2cDelivery);

    }


    /**
     * 第三方仓下出库单
     *
     * @param
     * @param overseasProviderWarehouse
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void thirdWarehouseCreateOutStock(String mainId, String code, String logisticsChannelId, OverseasProviderWarehouseDTO.ViewDTO overseasProviderWarehouse, List<SoB2cDetailEntity> detailList) {
        ThirdWarehouseCreateOutboundReq createOutboundReq = new ThirdWarehouseCreateOutboundReq();
        SoB2cReceiverEntity receiver = soB2cReceiverService.getByMainId(mainId);
        //转化收货人
        ThirdWarehouseCreateOutboundReq.ReceiverInfo receiverInfo = B2cOrderConverter.INSTANCE.convertThirdWarehouseReceiver(receiver);
        createOutboundReq.setReceiverInfo(receiverInfo);
        List<SkuMappingDTO.ListingSkuParamDTO> listSkuParamList = B2cOrderConverter.INSTANCE.convertFindListingSku(detailList);
        String warehouseType = RuleTypeEnum.WAREHOUSE.getCode();
        //平台
        String dictPlatform = overseasProviderWarehouse.getProviderCode();
        for (SkuMappingDTO.ListingSkuParamDTO item : listSkuParamList) {
            item.setDictPlatform(dictPlatform);
        }
        List<SkuMappingDTO.ListSkuResultDTO> platformSkuList = skuMappingService.listBySkuList(listSkuParamList, dictPlatform, warehouseType);
        List<ThirdWarehouseCreateOutboundReq.Item> itemList = new ArrayList<>(detailList.size());
        for (SoB2cDetailEntity item : detailList) {
            ThirdWarehouseCreateOutboundReq.Item outboundReqItem = new ThirdWarehouseCreateOutboundReq.Item();
            outboundReqItem.setQuantity(item.getQty());
            /**
             * 海外仓产品SKU
             */
            String platformSku = platformSkuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).
                    map(SkuMappingDTO.ListSkuResultDTO::getPlatformSkuNo).findFirst().orElse("");
            outboundReqItem.setProductSku(platformSku);
            itemList.add(outboundReqItem);
        }
        String platformWarehouseCode = overseasProviderWarehouse.getPlatformWarehouseCode();
        createOutboundReq.setWarehouseCode(platformWarehouseCode);
        createOutboundReq.setVerify(MathUtil.ONE);
        createOutboundReq.setReferenceNo(code);
        createOutboundReq.setThirdWarehouseProvideCode(overseasProviderWarehouse.getProviderCode());
        createOutboundReq.setAuthId(overseasProviderWarehouse.getMainId());
        LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(logisticsChannelId);
        createOutboundReq.setShippingMethod(Objects.isNull(channelEntity) ? "" : channelEntity.getCode());
        createOutboundReq.setItems(itemList);
        ApiResult apiResult = thirdWarehouseFeign.createOutboundOrder(createOutboundReq);
        String type = SoB2ErrorTypeEnum.SUBMIT_DELIVERY.getCode();
        if (!apiResult.isSuccess()) {
            String message = apiResult.getMsg();
            //生成异常订单信息
            soB2cErrorService.generateErrorOrder(mainId, type, message, JSONObject.toJSONString(createOutboundReq), JSONObject.toJSONString(apiResult));
            throw new ServiceException(ApiError.Default.code, message);
        }
        //删除异常订单信息
        soB2cErrorService.removeErrorOrder(mainId, type);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //TODO
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "发货拦截");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelDeliveryIntercept(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //TODO
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消发货拦截");
    }

    @Override
    public PagingVO<SoB2cDTO.MergeListDTO> mergePaging(PagingDTO<SoB2cDTO.MergePagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDTO.MergeListDTO> pageData = this.baseMapper.mergePaging(query, pagingParamDTO.getParams());
        List<SoB2cDTO.MergeListDTO> records = pageData.getRecords();
        fillMergeData(records);
        return new PagingVO(pageData);
    }

    @Override
    public Integer mergePagingCount(SoB2cDTO.MergePagingParamDTO pagingParamDTO) {
        pagingParamDTO.setPermissionSql(pagingParamDTO.getPermissionSql());
        List<Integer> list = this.baseMapper.mergePagingCount(pagingParamDTO);
        return CollectionUtils.isEmpty(list) ? MathUtil.ZERO : list.stream().reduce(MathUtil.ZERO, Integer::sum);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeSave(List<String> ids) {
        if (MathUtil.TWO.intValue() > ids.size()) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_SIZE);
        }

        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        // 销售明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        for (SoB2cEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_MERGE, entity.getCode());
            }
            if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SAVE_SPLIT_INVALID);
            }
            if (entity.getIsNotMerge()) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_IS_NOT_NEED_MERGE_EXIST, entity.getCode());
            }
            //fba订单不支持合并
            String mainLabelJson = entity.getLabelJson();
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(mainLabelJson, SoB2cDTO.LabelJsonDTO.class);
            if ("AFN".equals(labelJsonDTO.getFulfillmentChannel())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_FBA, entity.getCode());
            }
            //菜鸟官方仓订单不支持合并
            long cainiaoCount = soB2cDetailList.stream().filter(obj -> obj.getMainId().equals(entity.getId()) && obj.getLabelJson().contains("cainiaoInternationalWarehouse")).count();
            if (cainiaoCount > 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_CAINIAO, entity.getCode());
            }
            //速卖通非已税订单不支持合并
            long taxCount = soB2cDetailList.stream().filter(obj -> obj.getMainId().equals(entity.getId()) && (obj.getLabelJson().contains("U_TAXED") || obj.getLabelJson().contains("I_TAXED"))).count();
            if (taxCount > 0) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_TAX, entity.getCode());
            }
            //shopee订单不支持合并
            if (PlatformDictEnum.SHOPEE.getCode().equals(entity.getDictPlatform())) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_SHOPEE_NOT_MERGE, entity.getCode());
            }
        }

        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cLogisticsService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //买家信息
        List<SoB2cReceiverEntity> soB2cReceiverList = soB2cReceiverService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cReceiverList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }

        //检验合并数据
        checkMergeData(list, soB2cLogisticsList, soB2cReceiverList, soB2cDetailList);

        //新增合并后数据
        SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
        BeanMapperUtils.copy(list.get(0), addDTO);
        BigDecimal totalAmount = list.stream().map(SoB2cEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        addDTO.setAmount(totalAmount);
        //合并后取最小单据日期
        LocalDate billDate = list.stream().map(SoB2cEntity::getBillDate).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setBillDate(billDate);
        //合并后取最小创建日期
        LocalDateTime createTime = list.stream().map(SoB2cEntity::getCreateTime).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setCreateTime(createTime);
        //合并后取最小付款时间
        LocalDateTime payTime = list.stream().map(SoB2cEntity::getPayTime).min((x, y) -> x.compareTo(y)).orElse(null);
        addDTO.setPayTime(payTime);

        addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        addDTO.setSourceId(StrUtil.join(",", ids));
        String codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        addDTO.setSourceCode(codes);

        //平台订单号
        String platformCode = list.stream().filter(obj -> StrUtil.isNotBlank(obj.getPlatformCode())).map(SoB2cEntity::getPlatformCode).collect(Collectors.joining("*"));
        addDTO.setPlatformCode(platformCode);
        //物流信息
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsList.get(0), logisticsAddDTO);

        //查询汇率
        BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), list.get(0).getCurrency());
        if (ObjectUtils.isEmpty(rate) || MathUtil.compareTo(BigDecimal.ZERO, rate) == MathUtil.ZERO) {
            throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, billDate, list.get(0).getCurrency());
        }

        //预估运费(合并后默认转本位币)
        BigDecimal estimatedShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getEstimatedShippingCurrency()) ?
                obj.getEstimatedShippingCost() : MathUtil.multiply(obj.getEstimatedShippingCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setEstimatedShippingCost(estimatedShippingCost);
        //实际运费(合并后默认转本位币)
        BigDecimal actualShippingCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getActualShippingCurrency()) ?
                obj.getActualShippingCost() : MathUtil.multiply(obj.getActualShippingCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setActualShippingCost(actualShippingCost);
        //包装辅料费(合并后默认转本位币)
        BigDecimal accessoriesCost = soB2cLogisticsList.stream().map(obj -> CurrencyEnum.CNY.getCurrencyCode().equals(obj.getAccessoriesCostCurrency()) ?
                obj.getAccessoriesCost() : MathUtil.multiply(obj.getAccessoriesCost(), rate)).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesCost(accessoriesCost);
        //包装辅料数量
        Integer accessoriesQty = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesQty).reduce(MathUtil.ZERO, Integer::sum);
        logisticsAddDTO.setAccessoriesQty(accessoriesQty);
        //包装辅料净重
        BigDecimal accessoriesNw = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getAccessoriesNw).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setAccessoriesNw(accessoriesNw);
        //长
        BigDecimal height = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getHeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setHeight(height);
        //宽
        BigDecimal width = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWidth).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setWidth(width);
        //高
        BigDecimal weight = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        logisticsAddDTO.setWeight(weight);


        addDTO.setLogisticsDTO(logisticsAddDTO);
        //买家信息
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverList.get(0), receiverAddDTO);
        addDTO.setReceiverDTO(receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(ids);
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            addDTO.setCategoryIdList(categoryIdList);
        }

        //明细信息
        List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : soB2cDetailList) {
            SoB2cDetailDTO.AddDTO detailAddDTO = new SoB2cDetailDTO.AddDTO();
            BeanMapperUtils.copy(detailEntity, detailAddDTO);
            detailList.add(detailAddDTO);
        }
        addDTO.setRemark(StrUtil.format("订单【{}】合并新订单", codes));
        addDTO.setDetailList(detailList);
        log.info("新增合并后的B2C销售订单，addDTO = {}", addDTO);
        //新增数据
        SoB2cEntity add = this.add(addDTO, null);
        String soId = add.getId();
        //新增关联信息
        List<SoB2cRefDTO.AddDTO> refList = new ArrayList<>();
        for (String id : ids) {
            SoB2cRefDTO.AddDTO refAddDTO = new SoB2cRefDTO.AddDTO();
            refAddDTO.setType(SoB2cOptionTypeEnum.ENUM_MERGE.getCode());
            refAddDTO.setSourceId(id);
            refAddDTO.setTargetId(soId);
            refList.add(refAddDTO);
            log.info("作废原销售订单数据，id = {}", id);
            //作废
            this.invalid(id, StrUtil.format("B2C销售订单合并作废，合并后订单【{}】", add.getCode()), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        }
        log.info("新增合并后的订单关联关系，refList = {}", refList);
        //新增关联关系
        soB2cRefService.add(refList);

        //操作日志
        SoB2cEntity soB2cEntity = this.getById(soId);
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), soB2cEntity.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(StrUtil.format("合并到新订单【{}】", add.getCode()), ModuleTypeEnum.SO_B2C.getCode(), pairList, "合并订单");
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelMerge(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待提交或审核不通过允许取消
        if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_CANCEL_MERGE, entity.getCode());
        }
        //关联数据
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetId(id, SoB2cOptionTypeEnum.ENUM_MERGE);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CANCEL_MERGE_NOT_EXIST, entity.getCode());
        }
        log.info("删除销售订单数据，id = {}", id);
        //删除合并后的数据
        deleteById(Arrays.asList(id));
        //反作废合并前的数据
        for (SoB2cRefEntity refEntity : soB2cRefList) {
            log.info("作废原销售订单数据，id = {}", refEntity.getSourceId());
            unInvalid(refEntity.getSourceId(), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        }
        //操作日志
        String msg = "B2C销售订单【{}】取消合并";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消合并");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消合并");
    }


    @Override
    public SoB2cDTO.ViewSplitDTO viewSplit(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //验证拆分数据
        checkSplitData(id, entity);

        SoB2cDTO.ViewSplitDTO viewSplitDTO = new SoB2cDTO.ViewSplitDTO();
        viewSplitDTO.setId(id);
        //查询明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //产品信息
        List<String> skuIdList = soB2cDetailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        List<SoB2cDTO.ViewSplitDetailDTO> viewSplitDetailList = new ArrayList<>();
        for (SoB2cDetailEntity detailEntity : soB2cDetailList) {
            SoB2cDTO.ViewSplitDetailDTO viewSplitDetailDTO = new SoB2cDTO.ViewSplitDetailDTO();
            BeanMapperUtils.copy(detailEntity, viewSplitDetailDTO);
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            viewSplitDetailDTO.setProductName(skuVO.getSkuName());
            viewSplitDetailDTO.setSourceAmount(detailEntity.getAmount());
            viewSplitDetailDTO.setSourceCurrency(detailEntity.getCurrency());
            viewSplitDetailDTO.setAmount(MathUtil.multiply(detailEntity.getAmount(), detailEntity.getExchangeRate()));
            viewSplitDetailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            //产品包装重量 = SKU毛重 * 数量
            viewSplitDetailDTO.setWeight(MathUtil.multiply(skuVO.getGrossWeight(), detailEntity.getQty()));
            viewSplitDetailList.add(viewSplitDetailDTO);
        }
        viewSplitDTO.setDetailList(viewSplitDetailList);
        return viewSplitDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean splitSave(SoB2cDTO.SplitSaveDTO dto) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        /**
         * 拆分后金额、费用根据金额比例进行分摊
         */

        //验证拆分数据
        checkSplitData(dto.getId(), entity);
        //原单据明细
        List<SoB2cDetailEntity> oldDetailList = soB2cDetailService.listByMainId(dto.getId());
        if (CollectionUtils.isEmpty(oldDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.AddDTO logisticsAddDTO = new SoB2cLogisticsDTO.AddDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity, logisticsAddDTO);

        //买家信息
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(dto.getId());
        if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cReceiverDTO.AddDTO receiverAddDTO = new SoB2cReceiverDTO.AddDTO();
        BeanMapperUtils.copy(soB2cReceiverEntity, receiverAddDTO);

        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(dto.getId()));

        //原明细金额合计
        BigDecimal totalAmount = oldDetailList.stream().map(SoB2cDetailEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //拆分后数据
        List<SoB2cDTO.GroupSplitSaveDTO> splitList = dto.getGroupList();
        if (MathUtil.ONE >= splitList.size()) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT_SIZE);
        }
        Integer flag = MathUtil.ONE;
        for (SoB2cDTO.GroupSplitSaveDTO groupSplitSaveDTO : splitList) {
            //新建拆分后数据
            SoB2cDTO.AddDTO addDTO = new SoB2cDTO.AddDTO();
            BeanMapperUtils.copy(entity, addDTO);

            addDTO.setSourceId(entity.getId());
            addDTO.setSourceCode(entity.getCode());
            addDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
            if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
                List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).collect(Collectors.toList());
                addDTO.setCategoryIdList(categoryIdList);
            }
            addDTO.setReceiverDTO(receiverAddDTO);

            //拆分后金额合计
            BigDecimal splitTotalAmount = BigDecimal.ZERO;

            List<SoB2cDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SoB2cDTO.SplitDetailSaveDTO splitDetailSaveDTO : groupSplitSaveDTO.getDetailList()) {
                SoB2cDetailEntity detailEntity = oldDetailList.stream().filter(obj -> obj.getId().equals(splitDetailSaveDTO.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(detailEntity)) {
                    throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
                }
                if (MathUtil.compareTo(splitDetailSaveDTO.getQty(), detailEntity.getQty()) > MathUtil.ZERO) {
                    log.error("订单【{}】SKU【{}】拆分数量【{}】不能大于原数量【{}】", entity.getCode(), detailEntity.getSkuNo(), splitDetailSaveDTO.getQty(), detailEntity.getQty());
                    throw new ServiceException(ApiError.ERROR_SO_B2C_SPLIT_QTY, entity.getCode(), detailEntity.getSkuNo(), splitDetailSaveDTO.getQty(), detailEntity.getQty());
                }
                SoB2cDetailDTO.AddDTO addDetailDTO = new SoB2cDetailDTO.AddDTO();
                BeanMapperUtils.copy(detailEntity, addDetailDTO);
                addDetailDTO.setQty(splitDetailSaveDTO.getQty());
                addDetailDTO.setSourceDetailId(detailEntity.getId());
                detailList.add(addDetailDTO);
                //累加拆分金额
                splitTotalAmount = MathUtil.add(splitTotalAmount, MathUtil.multiply(detailEntity.getPrice(), splitDetailSaveDTO.getQty()));

            }
            addDTO.setDetailList(detailList);
            //拆分金额所占比例
            BigDecimal rate = MathUtil.divide(splitTotalAmount, totalAmount);
            //基本信息金额
            addDTO.setAmount(MathUtil.multiply(rate, entity.getAmount()));

            //预估费用
            logisticsAddDTO.setEstimatedShippingCost(MathUtil.multiply(rate, soB2cLogisticsEntity.getEstimatedShippingCost()));
            //实际费用
            logisticsAddDTO.setActualShippingCost(MathUtil.multiply(rate, soB2cLogisticsEntity.getActualShippingCost()));
            //包装辅料费
            logisticsAddDTO.setAccessoriesCost(MathUtil.multiply(rate, soB2cLogisticsEntity.getActualShippingCost()));
            //包装净重
            logisticsAddDTO.setAccessoriesNw(MathUtil.multiply(rate, soB2cLogisticsEntity.getAccessoriesNw()));
            //包装重量
            logisticsAddDTO.setWeight(MathUtil.multiply(rate, soB2cLogisticsEntity.getWeight()));

            addDTO.setLogisticsDTO(logisticsAddDTO);
            addDTO.setRemark(StrUtil.format("【{}】拆分订单", entity.getCode()));

            //新增拆分后订单
            String code = StrUtil.format("{}_{}", entity.getCode(), flag);
            SoB2cEntity add = this.add(addDTO, code);
            String soB2cId = add.getId();
            //新增拆分订单关联关系
            soB2cRefService.add(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode(), entity.getId(), soB2cId);
            flag++;
        }
        this.invalid(entity.getId(), StrUtil.format("【{}】被拆分作废", entity.getCode()), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);

        //操作日志
        String msg = "从【{}】拆分出新订单";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "拆分订单");
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDTO.CheckCancelSplitDTO> checkCancelSplit(List<String> ids) {
        //B2C销售订单主表信息
        List<SoB2cEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<String> codes = list.stream().map(SoB2cEntity::getCode).collect(Collectors.toList());
        List<String> sourceIdList = list.stream().map(SoB2cEntity::getSourceId).collect(Collectors.toList());
        List<SoB2cRefEntity> parentSoB2cRefList = soB2cRefService.listBySourceIds(sourceIdList, SoB2cOptionTypeEnum.ENUM_SPLIT);
        if (CollectionUtils.isEmpty(parentSoB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT, codes);
        }
        List<String> targetIdList = parentSoB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> targetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(targetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<SoB2cDTO.CheckCancelSplitDTO> resultList = new ArrayList<>();
        for (SoB2cEntity soB2cEntity : list) {
            SoB2cDTO.CheckCancelSplitDTO checkCancelSplitDTO = new SoB2cDTO.CheckCancelSplitDTO();
            checkCancelSplitDTO.setParentB2cSoCode(soB2cEntity.getSourceCode());
            //拆分后订单
            List<SoB2cEntity> splitList = targetList.stream().filter(obj -> obj.getSourceId().equals(soB2cEntity.getSourceId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(splitList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_SPLIT, soB2cEntity.getCode());
            }
            List<SoB2cDTO.CheckCancelSplitDetailDTO> detailList = new ArrayList<>();
            for (SoB2cEntity splitEntity : splitList) {
                SoB2cDTO.CheckCancelSplitDetailDTO checkCancelSplitDetailDTO = new SoB2cDTO.CheckCancelSplitDetailDTO();
                checkCancelSplitDetailDTO.setChildB2cSoCode(splitEntity.getCode());
                checkCancelSplitDetailDTO.setInvalidStatus(splitEntity.getInvalidStatus());
                checkCancelSplitDetailDTO.setBillStatus(splitEntity.getBillStatus());
                checkCancelSplitDetailDTO.setApproveStatus(splitEntity.getApproveStatus());
                detailList.add(checkCancelSplitDetailDTO);
            }
            checkCancelSplitDTO.setDetailList(detailList);
            resultList.add(checkCancelSplitDTO);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelSplit(String id) {
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //待提交或审核不通过允许取消拆分
        if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_STATE_NOT_CANCEL_SPLIT, entity.getCode());
        }
        //关联关系
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listSourceByTargetIds(Arrays.asList(id), SoB2cOptionTypeEnum.ENUM_SPLIT.getCode());
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PARENT_NOT_SPLIT, entity.getCode());
        }
        List<String> targetIdList = soB2cRefList.stream().map(SoB2cRefEntity::getTargetId).collect(Collectors.toList());
        List<SoB2cEntity> targetList = this.listByIds(targetIdList);
        if (CollectionUtils.isEmpty(targetList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_NOT_EXIST, entity.getCode());
        }
        //验证拆分后单据是否作废
        String invalidCodes = targetList.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(invalidCodes)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_HAS_INVALID, invalidCodes);
        }
        //验证拆分后单据是否审核
        String approveCodes = targetList.stream().filter(obj -> ApproveStatusEnum.APPROVE.equals(obj.getApproveStatus())).map(SoB2cEntity::getCode).collect(Collectors.joining(","));
        if (StringUtils.isNotBlank(approveCodes)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_HAS_APPROVE, approveCodes);
        }
        log.info("删除B2C销售订单数据，ids = {}", targetIdList);
        //删除拆分后的数据
        deleteById(targetIdList);
        //反作废合并前的数据
        log.info("反作废原B2C销售订单数据，id = {}", entity.getId());
        unInvalid(soB2cRefList.get(0).getSourceId(), SoB2cInvalidTypeEnum.ENUM_AUTOMATIC);
        //操作日志
        String msg = "从【{}】取消拆分";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "取消拆分");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消拆分");
    }


    /**
     * @param ids
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 14:09
     */
    private void deleteById(List<String> ids) {
        //删除物流信息
        soB2cLogisticsService.deleteByMainIds(ids);
        //删除买家信息
        soB2cReceiverService.deleteByMainIds(ids);
        //删除明细信息
        soB2cDetailService.deleteByMainIds(ids);
        //删除关联关系
        soB2cRefService.deleteByTargetIds(ids);
        //删除分类信息
        soB2cRefCategoryService.deleteByMainIds(ids);
        //删除主表信息
        this.removeByIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
    }


    @Override
    public SoB2cDTO.ViewDTO view(String id) {
        SoB2cEntity soB2cEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));
        SoB2cDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDTO.ViewDTO.class, soB2cEntity);
        //物流
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cLogisticsDTO.ViewDTO logisticsDTO = new SoB2cLogisticsDTO.ViewDTO();
        BeanMapperUtils.copy(soB2cLogisticsEntity, logisticsDTO);
        //渠道id
        String logisticsChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        String logisticsChannelName = "";
        if (StringUtils.isNotBlank(logisticsChannelId)) {
            LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(logisticsChannelId);
            if (Objects.nonNull(channelEntity)) {
                logisticsChannelName = channelEntity.getName();
            }
        }
        logisticsDTO.setLogisticsChannelName(logisticsChannelName);
        data.setLogisticsDTO(logisticsDTO);
        //买家
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cReceiverDTO.ViewDTO receiverDTO = new SoB2cReceiverDTO.ViewDTO();
        BeanMapperUtils.copy(soB2cReceiverEntity, receiverDTO);

        data.setReceiverDTO(receiverDTO);
        //订单分类
        List<SoB2cRefCategoryEntity> soB2cRefCategoryList = soB2cRefCategoryService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(soB2cRefCategoryList)) {
            List<String> categoryIdList = soB2cRefCategoryList.stream().map(SoB2cRefCategoryEntity::getCategoryId).distinct().collect(Collectors.toList());
            data.setCategoryIdList(categoryIdList);
        }

        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(id);
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        //财务信息
        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.TRUE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(soB2cLogisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfo = this.getFinancialInfo(dto, Boolean.FALSE);
        data.setFinancialInfoDTO(financialInfo);

        List<SoB2cDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(SoB2cDetailDTO.ViewDTO.class, soB2cDetailList);
        data.setDetailList(detailList);
        // 数据填充处理
        fillOne(data);
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(SoB2cEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_B2C.getCode());
        startDTO.setBusinessName(entity.getCode());
        String userId = commonService.getUserInfo().getUid();
        startDTO.setUserId(userId);
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 查询详情数据处理
     */
    private void fillOne(SoB2cDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(data.getShopId());
        if (ObjectUtils.isNotEmpty(shopInfoEntity)) {
            data.setShopName(shopInfoEntity.getName());
        }

        data.setApproveStatusName(data.getApproveStatus().getName());
        data.setBillStatusName(SoB2cBillStatusEnum.getName(data.getBillStatus()));
        //平台信息
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        if (CollectionUtils.isNotEmpty(dictList)) {
            String name = dictList.stream().filter(obj -> obj.getValue().equals(data.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setDictPlatformName(name);
        }

        //产品信息
        List<String> skuIdList = data.getDetailList().stream().map(SoB2cDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isNotEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }

        for (SoB2cDetailDTO.ViewDTO viewDTO : data.getDetailList()) {
            SkuVO skuVO = skuVOMap.get(viewDTO.getSkuId());
//            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
//            if (ObjectUtils.isEmpty(skuVO)) {
//                throw new ServiceException(ApiError.ERROR_95084);
//            }
            viewDTO.setProductName(null == skuVO ? "" : skuVO.getSkuName());
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getApproveStatus, approveStatus)
                .set(ApproveStatusEnum.REJECT.getStatus().equals(approveStatus), SoB2cEntity::getAbnormalType, SoB2cAbnormalTypeEnum.ENUM_MANUAL_REJECT.getCode())
                .update(new SoB2cEntity());
    }


    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getApproveStatus, approveStatus)
                .set(SoB2cEntity::getAbnormalType, "")
                .update(new SoB2cEntity());
    }

    /**
     * @param categoryIdList
     * @param mainId
     * @description: 新增分类
     * @author Will
     * @date: 2023/8/22 14:22
     */
    private void addCategory(List<String> categoryIdList, String mainId, List<OrderCategoryDetailEntity> categoryList) {
        List<SoB2cRefCategoryEntity> list = soB2cRefCategoryService.listByMainIds(Arrays.asList(mainId));
        List<SoB2cRefCategoryEntity> addList = new ArrayList<>();
        for (String categoryId : categoryIdList) {
            //如果已存在分类则无需新增
            if (CollectionUtils.isNotEmpty(list)) {
                long count = list.stream().filter(obj -> obj.getCategoryId().equals(categoryId)).count();
                if (count > 0) {
                    log.info("已存在分类，categoryId = {}", categoryId);
                    continue;
                }
            }
            String name = categoryList.stream().filter(obj -> obj.getId().equals(categoryId)).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            SoB2cRefCategoryEntity entry = new SoB2cRefCategoryEntity();
            entry.setCategoryId(categoryId);
            entry.setCategoryName(name);
            entry.setSoB2cId(mainId);
            addList.add(entry);
        }
        soB2cRefCategoryService.saveBatch(addList);
    }

    /**
     * @param categoryIdList
     * @param mainId
     * @description: 修改分类
     * @author Will
     * @date: 2023/8/22 14:36
     */
    private void updateCategory(List<String> categoryIdList, String mainId, List<OrderCategoryDetailEntity> categoryList) {
        //删除原有分类
        deleteCategory(mainId);
        //新增分类
        addCategory(categoryIdList, mainId, categoryList);
    }

    /**
     * @param mainId
     * @description: 删除已有分类
     * @author Will
     * @date: 2023/8/22 14:30
     */
    private void deleteCategory(String mainId) {
        soB2cRefCategoryService.deleteByMainIds(Arrays.asList(mainId));
    }

    /**
     * @param mainId
     * @description: 删除选择分类
     * @author Will
     * @date: 2023/8/22 14:30
     */
    private void deleteSelectCategory(String mainId, List<String> categoryIdList) {
        soB2cRefCategoryService.deleteByMainIdAndCategoryId(mainId, categoryIdList);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SoB2cDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //店铺
        List<String> shopIdList = list.stream().map(SoB2cDTO.ListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopIdList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }

        List<String> ids = list.stream().map(SoB2cDTO.ListDTO::getId).collect(Collectors.toList());

        List<SoB2cEntity> allList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        List<SoB2cDetailEntity> allDetailList = soB2cDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(allDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //产品信息
        List<String> skuIdList = list.stream().flatMap(obj -> Stream.of(allDetailList.stream().map(SoB2cDetailEntity::getSkuId).toArray(String[]::new))).distinct().collect(Collectors.toList());
        Map<String, SkuVO> skuVOMap = new HashMap<>();
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (!CollectionUtils.isEmpty(skuList)) {
            skuVOMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));
        }
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //仓库id
        List<String> warehouseIdList = allDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
        //获取第三方仓海外信息
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasProviderWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIdList);
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(), InventoryStatusEnum.FROZEN.getCode()));
        skuInventoryDTO.setWarehouseIdList(warehouseIdList);
        skuInventoryDTO.setSkuIdList(skuIdList);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);

        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listBySourceIdOrTargetId(ids);


        //物流信息
        List<SoB2cLogisticsEntity> logisticsEntityList = soB2cLogisticsService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(logisticsEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //财务信息
        List<SoB2cFinanceEntity> soB2cFinanceEntityList = soB2cFinanceService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(soB2cFinanceEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }

        //SKU对照表信息
        List<SkuMappingDTO.ListSkuParamDTO> listParamList = allDetailList.stream().map(obj -> new SkuMappingDTO.ListSkuParamDTO(obj.getSkuNo(), obj.getWarehouseId(), allList.stream().filter(e -> e.getId().equals(obj.getMainId())).findFirst().flatMap(e -> Optional.ofNullable(e.getDictPlatform())).orElse(""))).collect(Collectors.toList());
        ValidList<SkuMappingDTO.ListSkuParamDTO> listSkuParamList = new ValidList<>();
        listSkuParamList.setList(listParamList);
        List<SkuMappingDTO.ListSkuDTO> skuMappingList = skuMappingService.listBySkuNoList(listSkuParamList);


        //查询拦截单
        List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> interceptDTOList = soB2cDeliveryInterceptFeign.listIsIntercept(ids);

        // 属性赋值
        for (SoB2cDTO.ListDTO data : list) {
            //设置拦截标识
            SoB2cDeliveryInterceptDTO.IsInterceptDTO isInterceptDTO = interceptDTOList.stream().filter(req -> req.getId().equals(data.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(isInterceptDTO)) {
                data.setIsIntercept(isInterceptDTO.getIsIntercept());
            }

            //店铺
            ShopInfoEntity shopInfoEntity = shopInfoList.stream().filter(obj -> obj.getId().equals(data.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(shopInfoEntity)) {
                data.setShopName(shopInfoEntity.getName());
                data.setCountryName(shopInfoEntity.getCountryName());
            }

            //单据状态
            data.setStatus(data.getBillStatus());
            data.setStatusName(SoB2cBillStatusEnum.getName(data.getBillStatus()));

            //异常信息名称
            data.setAbnormalTypeName(SoB2cAbnormalTypeEnum.getName(data.getAbnormalType()));

            //标签处理
            String label = data.getLabel();
            SoB2cDTO.LabelDTO labelDTO = new SoB2cDTO.LabelDTO();
            labelDTO.setIsIntercept(data.getIsIntercept());
            labelDTO.setIsManual(data.getSourceType().equals(SourceTypeEnum.SELF_ADD.getCode()));
            if (CollectionUtils.isNotEmpty(soB2cRefList)) {
                //合并
                long mergeCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                        && SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())
                ).count();
                if (mergeCount > 0) {
                    labelDTO.setRefType(SoB2cOptionTypeEnum.ENUM_MERGE.getCode());
                    labelDTO.setMergeCount(Integer.valueOf(String.valueOf(mergeCount)));
                }
                //拆分
                long splitCount = soB2cRefList.stream().filter(obj -> (obj.getTargetId().equals(data.getId()))
                        && SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())
                        && InvalidStatusEnum.NOT_VOIDED.getStatus().equals(data.getInvalidStatus())
                ).count();
                if (splitCount > 0) {
                    labelDTO.setRefType(SoB2cOptionTypeEnum.ENUM_SPLIT.getCode());

                }
            }
            //主表标签
            if (StringUtils.isNotBlank(label)) {
                SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(label, SoB2cDTO.LabelJsonDTO.class);
                labelDTO.setAliexpressStatus(labelJsonDTO.getAliexpressStatus());
                labelDTO.setAmazonStatus(labelJsonDTO.getAmazonStatus());
                labelDTO.setFulfillmentChannel(labelJsonDTO.getFulfillmentChannel());
            }
            //明细信息
            List<SoB2cDetailEntity> detailList = allDetailList.stream().filter(obj -> obj.getMainId().equals(data.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            String warehouseId = detailList.get(0).getWarehouseId();
            long warehouseCount = overseasProviderWarehouseList.stream().filter(o -> o.getWarehouseId().equals(warehouseId)).count();
            Boolean isOverseasProviderWarehouse = warehouseCount > 0;
            data.setIsOverseasProviderWarehouse(isOverseasProviderWarehouse);
            List<SoB2cDetailDTO.ListDTO> soB2cDetailList = BeanMapperUtils.copyList(SoB2cDetailDTO.ListDTO.class, detailList);

            Boolean isCombination = Boolean.FALSE;
            for (SoB2cDetailDTO.ListDTO detailDTO : soB2cDetailList) {
                SkuVO skuVO = skuVOMap.get(detailDTO.getSkuId());
                detailDTO.setVariantProperty(null == skuVO ? "" : skuVO.getVariantProperty());
                detailDTO.setProductName(null == skuVO ? "" : skuVO.getSkuName());
                //是否是组合SKU
                if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                    long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailDTO.getSkuId())).count();
                    if (count > 0) {
                        isCombination = Boolean.TRUE;
                    }
                }

                //库存SKU
                SkuMappingDTO.ListSkuDTO warehouseListSkuDTO = skuMappingList.stream().filter(obj -> StrUtil.equals(obj.getProductSkuId(), detailDTO.getSkuId()) && StrUtil.equals(obj.getWarehouseId(), detailDTO.getWarehouseId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(warehouseListSkuDTO)) {
                    detailDTO.setVariantProperty(warehouseListSkuDTO.getVariantProperty());
                }

                //订单本位币金额
                detailDTO.setSourceAmount(detailDTO.getAmount());
                detailDTO.setSourceCurrency(detailDTO.getCurrency());

                BigDecimal amount = MathUtil.multiply(detailDTO.getSourceAmount(), detailDTO.getExchangeRate());
                detailDTO.setAmount(amount);
                detailDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());

                //标签处理
                SoB2cDetailDTO.DetailLabelDTO detailLabelDTO = new SoB2cDetailDTO.DetailLabelDTO();
                String detailLabel = detailDTO.getLabel();
                if (StringUtils.isNotBlank(detailLabel)) {
                    SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabel, SoB2cDetailDTO.LabelJsonDTO.class);
                    detailLabelDTO.setAlreadyTaxed(labelJsonDTO.getAlreadyTaxed());
                    detailLabelDTO.setLogisticsWarehouseType(labelJsonDTO.getLogisticsWarehouseType());
                    detailLabelDTO.setTagList(labelJsonDTO.getTagList());
                }
                Integer useableQty = MathUtil.ZERO;
                Integer freezeQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(inventoryList)) {
                    //可用库存
                    useableQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                    && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                    && obj.getWarehouseLocationId().equals(detailDTO.getWarehouseLocation())
                                    && InventoryStatusEnum.USABLE.getCode().equals(obj.getInventoryStatus()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                            .orElse(MathUtil.ZERO);
                    detailDTO.setUseableQty(useableQty);
                    //冻结库存
                    freezeQty = inventoryList.stream().filter(obj -> obj.getSkuId().equals(detailDTO.getSkuId())
                                    && obj.getWarehouseId().equals(detailDTO.getWarehouseId())
                                    && obj.getWarehouseLocationId().equals(detailDTO.getWarehouseLocation())
                                    && InventoryStatusEnum.FROZEN.getCode().equals(obj.getInventoryStatus()))
                            .findFirst().flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal()))
                            .orElse(MathUtil.ZERO);
                    detailDTO.setFreezeQty(freezeQty);
                }
                //缺货订单
                if ((SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(data.getBillStatus())
                        || SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(data.getBillStatus()))
                        && MathUtil.compareTo(MathUtil.ZERO, useableQty) == MathUtil.ZERO) {
                    detailLabelDTO.setIsOutStock(Boolean.TRUE);
                }
                detailDTO.setDetailLabelDTO(detailLabelDTO);
            }

            SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
            dto.setId(data.getId());
            dto.setIsCny(Boolean.FALSE);
            SoB2cEntity soB2cEntity = allList.stream().filter(obj -> obj.getId().equals(data.getId())).findFirst().orElse(new SoB2cEntity());
            dto.setSoB2cEntity(soB2cEntity);

            //物流信息
            SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).findFirst().orElse(new SoB2cFinanceEntity());
            dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
            //物流信息
            SoB2cLogisticsEntity logisticsEntity = logisticsEntityList.stream().filter(obj -> obj.getMainId().equals(data.getId())).findFirst().orElse(new SoB2cLogisticsEntity());
            dto.setSoB2cLogisticsEntity(logisticsEntity);
            dto.setSoB2cDetailList(detailList);
            SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.FALSE);
            data.setTotalProfit(financialInfoDTO.getProfit());
            data.setProfitCurrency(data.getCurrency());
            data.setProfitRate(new BigDecimal(financialInfoDTO.getProfitRate().replace("%", "")));
            data.setDetailList(soB2cDetailList);
            //明细存在一条数据时组合SKU则标识
            labelDTO.setIsCombination(isCombination);
            data.setLabelDTO(labelDTO);
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(SoB2cEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //作废和冻结不支持提交
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus()) || SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_UPDATE_SUBMIT, entity.getCode());
        }
        //未付款数据不支持提交
        if (ObjectUtil.isEmpty(entity.getPayStatus()) || SoB2cPayStatusEnum.ENUM_PAYMENT.getCode().equals(entity.getPayStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PAYMENT_NOT_SUBMIT, entity.getCode());
        }


        return;
    }

    /**
     * 新增修改处理数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleData(SoB2cEntity soB2cEntity, Boolean exchangeRateThrow, Boolean checkPayTime) {
        if (ObjectUtils.isEmpty(soB2cEntity)) {
            return;
        }
        soB2cEntity.setBillDate(ObjectUtils.isEmpty(soB2cEntity.getBillDate()) ? LocalDate.now() : soB2cEntity.getBillDate());
        if (StringUtils.isNotBlank(soB2cEntity.getCurrency())) {
            BigDecimal exchangeRate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
            if (MathUtil.compareTo(exchangeRate, MathUtil.ZERO) == MathUtil.ZERO && exchangeRateThrow) {
                throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
            }
            soB2cEntity.setExchangeRate(null == exchangeRate ? BigDecimal.ZERO : exchangeRate);
        }

        //店铺
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        if (ObjectUtils.isEmpty(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        soB2cEntity.setOrgId(shopInfoEntity.getSalesOrgId());
        soB2cEntity.setOrgName(shopInfoEntity.getSalesOrgName());

        //付款时间不为空则已付款
        if (ObjectUtils.isNotEmpty(soB2cEntity.getPayTime()) && checkPayTime) {
            soB2cEntity.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        }
    }

    /**
     * @param params
     * @return Boolean
     * @description: 列表查询数量状态处理
     * @author Will
     * @date: 2023/8/21 12:16
     */
    private Boolean handleTableParam(SoB2cDTO.PagingParamDTO params) {
        //审核状态
        List<String> approveStatusList = new ArrayList<>(1);
        //付款状态
        List<String> payStatusList = new ArrayList<>(1);
        //单据状态
        List<String> billStatusList = new ArrayList<>(1);


        // 全部
        if (SearchType.ALL.equals(params.getTabFlag())) {
            params.setInvalidStatus(Boolean.FALSE);
        }

        // 待付款
        if (SoB2cTabEnum.ENUM_PAYMENT.getCode().equals(params.getTabFlag())) {
            payStatusList.add(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //待处理
        if (SoB2cTabEnum.ENUM_PENDING.getCode().equals(params.getTabFlag())) {
            params.setAbnormalTypeList(Arrays.stream(SoB2cAbnormalTypeEnum.values()).map(SoB2cAbnormalTypeEnum::getCode).collect(Collectors.toList()));
            params.setInvalidStatus(Boolean.FALSE);
            params.setPayStatusList(Arrays.asList(SoB2cPayStatusEnum.ENUM_PAID.getCode()));
        }
        //审核中
        if (SoB2cTabEnum.ENUM_APPROVE_ING.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //待配货
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //配货中
        if (SoB2cTabEnum.ENUM_IN_DISTRIBUTION.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //待发货
        if (SoB2cTabEnum.ENUM_WAIT_SHIPPED.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //已发货
        if (SoB2cTabEnum.ENUM_SHIPPED.getCode().equals(params.getTabFlag())) {
            approveStatusList.add(ApproveStatusEnum.APPROVE.getStatus());
            billStatusList.add(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //冻结中
        if (SoB2cTabEnum.ENUM_FROZEN.getCode().equals(params.getTabFlag())) {
            billStatusList.add(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
            params.setInvalidStatus(Boolean.FALSE);
        }
        //已作废
        if (SoB2cTabEnum.ENUM_INVALID.getCode().equals(params.getTabFlag())) {
            params.setInvalidStatus(Boolean.TRUE);
        }
        //订单异常
        if (SoB2cTabEnum.ENUM_ORDER_ERROR.getCode().equals(params.getTabFlag())) {
            params.setIsOrderError(Boolean.TRUE);
        }

        if (CollectionUtils.isNotEmpty(approveStatusList)) {
            params.setApproveStatusList(approveStatusList);
        }
        if (CollectionUtils.isNotEmpty(billStatusList)) {
            params.setBillStatusList(billStatusList);
        }
        if (CollectionUtils.isNotEmpty(payStatusList)) {
            params.setPayStatusList(payStatusList);
        }
        return Boolean.TRUE;
    }

    /**
     * @param records
     * @description: 合并列表数据显示处理
     * @author Will
     * @date: 2023/8/23 9:32
     */
    private void fillMergeData(List<SoB2cDTO.MergeListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = records.stream().map(SoB2cDTO.MergeListDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopList = shopInfoService.listByIds(shopIdList);
        if (CollectionUtils.isEmpty(shopList)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        //国家信息
        List<String> countryIdList = shopList.stream().map(ShopInfoEntity::getDictCountryCode).collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = sysDictFeign.listCountryByIds(countryIdList);

        //平台
        List<String> platformList = records.stream().map(SoB2cDTO.MergeListDTO::getDictPlatform)
                .distinct().collect(Collectors.toList());
        //币别
        List<String> currencyList = records.stream().map(SoB2cDTO.MergeListDTO::getSourceCurrency)
                .distinct().collect(Collectors.toList());
        //买家名称
        List<String> buyerNameList = records.stream().map(SoB2cDTO.MergeListDTO::getBuyerName)
                .distinct().collect(Collectors.toList());
        //平台
        List<String> receiverNameList = records.stream().map(SoB2cDTO.MergeListDTO::getReceiverName)
                .distinct().collect(Collectors.toList());
        //地址1
        List<String> firstAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getFirstAddress).distinct().collect(Collectors.toList());
        //地址2
        List<String> secondAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getSecondAddress).distinct().collect(Collectors.toList());
        //详细地址
        List<String> fullAddressList = records.stream().map(SoB2cDTO.MergeListDTO::getFullAddress).distinct().collect(Collectors.toList());
        //仓库
        List<String> warehouseIdList = records.stream().map(SoB2cDTO.MergeListDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //物流方式
        List<String> logisticsChannelIdList = records.stream().map(SoB2cDTO.MergeListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());

        SoB2cDTO.MergeParamDTO mergeParamDTO = new SoB2cDTO.MergeParamDTO();
        mergeParamDTO.setPlatformList(platformList);
        mergeParamDTO.setShopIdList(shopIdList);
        mergeParamDTO.setCurrencyList(currencyList);
        mergeParamDTO.setBuyerNameList(buyerNameList);
        mergeParamDTO.setReceiverNameList(receiverNameList);
        mergeParamDTO.setFirstAddressList(firstAddressList);

        mergeParamDTO.setSecondAddressList(secondAddressList);
        mergeParamDTO.setFullAddressList(fullAddressList);
        mergeParamDTO.setWarehouseIdList(warehouseIdList);
        mergeParamDTO.setLogisticsChannelIdList(logisticsChannelIdList);
        List<SoB2cDTO.MergeMainDTO> mergeMainList = baseMapper.listMerge(mergeParamDTO);
        if (CollectionUtils.isEmpty(mergeMainList)) {
            return;
        }
        //产品详细
        List<String> skuIdList = mergeMainList.stream().map(SoB2cDTO.MergeMainDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuList)) {
            log.error("未发现产品详细，skuIdList = {}", skuIdList);
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (SoB2cDTO.MergeListDTO mergeListDTO : records) {

            //店铺信息
            ShopInfoEntity shopInfoEntity = shopList.stream().filter(obj -> obj.getId().equals(mergeListDTO.getShopId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            mergeListDTO.setShopName(shopInfoEntity.getName());
            //国家信息
            if (CollectionUtils.isNotEmpty(dictCountryList)) {
                String countryName = dictCountryList.stream().filter(obj -> obj.getId().equals(shopInfoEntity.getDictCountryCode()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
                mergeListDTO.setCountyName(countryName);
            }
            //主表数据
            if (CollectionUtils.isNotEmpty(mergeMainList)) {
                List<SoB2cDTO.MergeMainDTO> mainList = mergeMainList.stream().filter(obj -> mergeListDTO.getDictPlatform().equals(obj.getDictPlatform())
                        && mergeListDTO.getShopId().equals(obj.getShopId())
                        && mergeListDTO.getBuyerName().equals(obj.getBuyerName())
                        && mergeListDTO.getSourceCurrency().equals(obj.getSourceCurrency())
                        && mergeListDTO.getReceiverName().equals(obj.getReceiverName())
                        && mergeListDTO.getFirstAddress().equals(obj.getFirstAddress())
                        && mergeListDTO.getSecondAddress().equals(obj.getSecondAddress())
                        && mergeListDTO.getFullAddress().equals(obj.getFullAddress())
                        && mergeListDTO.getWarehouseId().equals(obj.getWarehouseId())
                        && StrUtil.equals(mergeListDTO.getLogisticsChannelId(), obj.getLogisticsChannelId())
                ).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(mainList) || mainList.size() == 1) {
                    continue;
                }

                List<String> ids = new ArrayList<>();
                for (SoB2cDTO.MergeMainDTO mergeMainDTO : mainList) {
                    if (!ids.contains(mergeMainDTO.getId())) {
                        mergeMainDTO.setIsMain(Boolean.TRUE);
                    }
                    ids.add(mergeMainDTO.getId());
                    //产品名称
                    String productName = skuList.stream().filter(obj -> obj.getSkuId().equals(mergeMainDTO.getSkuId())).findFirst()
                            .flatMap(obj -> Optional.ofNullable(obj.getSkuName())).orElse("");
                    mergeMainDTO.setProductName(productName);
                    //本位币金额
                    mergeMainDTO.setAmount(MathUtil.multiply(mergeMainDTO.getSourceAmount(), mergeMainDTO.getExchangeRate()));
                    mergeMainDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                }
                mergeListDTO.setMainList(mainList);
                //总原币金额
                BigDecimal totalSourceAmount = mainList.stream().map(SoB2cDTO.MergeMainDTO::getSourceAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setSourceAmount(totalSourceAmount);
                //总本位币金额
                BigDecimal totalAmount = mainList.stream().map(obj -> MathUtil.multiply(obj.getSourceAmount(), obj.getExchangeRate()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setAmount(totalAmount);
                mergeListDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                //总重量
                BigDecimal totalWeight = mainList.stream().map(SoB2cDTO.MergeMainDTO::getWeight)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergeListDTO.setWeight(totalWeight);
            }
        }
    }

    /**
     * @param list
     * @param soB2cLogisticsList
     * @param soB2cReceiverList
     * @param soB2cDetailList
     * @description: 检验合并数据
     * @author Will
     * @date: 2023/8/23 10:23
     */
    private void checkMergeData(List<SoB2cEntity> list, List<SoB2cLogisticsEntity> soB2cLogisticsList,
                                List<SoB2cReceiverEntity> soB2cReceiverList, List<SoB2cDetailEntity> soB2cDetailList) {
        //销售平台
        long platformCount = list.stream().map(SoB2cEntity::getDictPlatform).distinct().count();
        if (platformCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_PLATFORM_CODE_COMPLEX);
        }
        //店铺
        long shopCount = list.stream().map(SoB2cEntity::getShopId).distinct().count();
        if (shopCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOP_COMPLEX);
        }
        //币别
        long currencyCount = list.stream().map(SoB2cEntity::getCurrency).distinct().count();
        if (currencyCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CURRENCY_COMPLEX);
        }
        //物流方式
        long logisticsMethodCount = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).distinct().count();
        if (logisticsMethodCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_COMPLEX);
        }

        //买家
        long nameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getName).distinct().count();
        if (nameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_BUYER_NAME_COMPLEX);
        }
        //收货人
        long receiverNameCount = soB2cReceiverList.stream().map(SoB2cReceiverEntity::getReceiverName).distinct().count();
        if (receiverNameCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NAME_COMPLEX);
        }
        //收货地址
        long addressCount = soB2cReceiverList.stream().map(obj -> StrUtil.join(",", obj.getFirstAddress(), obj.getSecondAddress(), obj.getFullAddress())).distinct().count();
        if (addressCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_ADDRESS_COMPLEX);
        }

        //仓库
        long warehouseCount = soB2cDetailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().count();
        if (warehouseCount > MathUtil.ONE) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_WAREHOUSE_COMPLEX);
        }
    }

    /**
     * @param id
     * @param entity
     * @description: 验证拆分数据
     * @author Will
     * @date: 2023/8/23 15:12
     */
    private void checkSplitData(String id, SoB2cEntity entity) {

        if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_STATE_NOT_SPLIT, entity.getCode());
        }
        if (SoB2cBillStatusEnum.ENUM_FROZEN.getCode().equals(entity.getBillStatus()) || InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SAVE_SPLIT_INVALID);
        }
        if (PlatformDictEnum.SHOPEE.getCode().equals(entity.getDictPlatform())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_SHOPEE_NOT_SPLIT, entity.getCode());
        }

        //查询订单是否是合并订单或拆分子订单
        List<SoB2cRefEntity> soB2cRefList = soB2cRefService.listByTargetId(id, null);
        if (CollectionUtils.isEmpty(soB2cRefList)) {
            return;
        }
        long mergeCount = soB2cRefList.stream().filter(obj -> SoB2cOptionTypeEnum.ENUM_MERGE.getCode().equals(obj.getType())).count();
        if (mergeCount > 0) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_MERGE_NOT_SPLIT, entity.getCode());
        }
        long splitCount = soB2cRefList.stream().filter(obj -> SoB2cOptionTypeEnum.ENUM_SPLIT.getCode().equals(obj.getType())).count();
        if (splitCount > 0) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_CHILD_SPLIT_NOT_SPLIT, entity.getCode());
        }
    }

    /**
     * @param id
     * @return Boolean
     * @description: 匹配审核规则
     * @author Will
     * @date: 2023/8/24 15:18
     */
    @Override
    public Boolean approveRule(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        if (map.isEmpty()) {
            //匹配审核规则
            handleMatchJson(id, detailList, map);
        }
        RuleOrderApprovalDTO.RuleMatchDTO ruleOrderMatchResult = ruleOrderApprovalService.getRuleOrderMatchResult(map);
        //审核规则是否通过
        Boolean approveSuccess = ruleOrderMatchResult.getApproveSuccess();
        //匹配审核规则通过,自动提交并审核
        if (Objects.nonNull(approveSuccess) && approveSuccess) {
            //更新流转状态和分类信息
            soB2cRefCategoryService.update(ruleOrderMatchResult.getCategoryDetailIdList(), id);
            //自动提交
            BatchResultDTO submit = this.submit(id, Boolean.FALSE);
            if (!submit.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_1042);
            }
            String approveMsg = "自动审核不通过";
            //审核通过
            if (ApproveType.PASS.equals(ruleOrderMatchResult.getFlowStatus())) {
                approveMsg = "自动审核通过";
            }
            //自动审核通过
            BatchResultDTO approve = this.approve(new ApproveOneDTO(id, ruleOrderMatchResult.getFlowStatus(), approveMsg));
            if (!approve.getSuccess()) {
                throw new ServiceException(ApiError.ERROR_94006);
            }
            return Boolean.TRUE;
        }
        //标识异常并且审核不通过
        updateAbnormalTypeApprove(id, ApproveStatusEnum.REJECT, SoB2cAbnormalTypeEnum.ENUM_APPROVE_REJECT);
        return Boolean.FALSE;
    }

    /**
     * @param id
     * @param detailList
     * @return List<JSONObject>
     * @description: 审核规则匹配字段处理
     * @author Will
     * @date: 2023/11/16 15:27
     */
    @Override
    public Map<String, Object> handleMatchJson(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cEntity soB2cEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(id);
        if (ObjectUtil.isEmpty(receiverEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_RECEIVER_NOT_EXIST);
        }
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(id);
        if (ObjectUtil.isEmpty(soB2cFinanceEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        List<String> skuIdList = detailList.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<ProductDetailDTO.ProductDTO> productList = plmTaskFeign.listProductBySkuIds(skuIdList);

        //含税总成本
        BigDecimal totalTaxCost = detailList.stream().filter(obj -> MathUtil.compareTo(obj.getTaxCost(), MathUtil.ZERO) > MathUtil.ZERO)
                .map(SoB2cDetailEntity::getTaxCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.TRUE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(detailList);
        SoB2cDTO.FinancialInfoDTO financialInfo = getFinancialInfo(dto, Boolean.FALSE);
        map.put("dictPayMethod", soB2cEntity.getDictPayMethod());
        Integer goodsTotalQty = detailList.stream().mapToInt(SoB2cDetailEntity::getQty).sum();
        map.put("goodsTotalQty", goodsTotalQty);
        LocalDateTime payTime = soB2cEntity.getPayTime();
        String payTimeStr = Objects.nonNull(payTime) ? LocalDateUtil.formatTime(payTime, DateUtil.fmt) : "";
        map.put("payTime", payTimeStr);
        map.put("packageWeight", logisticsEntity.getWeight());
        map.put("packageLength", logisticsEntity.getLength());
        map.put("packageHeight", logisticsEntity.getHeight());
        map.put("shop", soB2cEntity.getShopId());
        map.put("logisticsChannelId", logisticsEntity.getLogisticsChannelId());
        map.put("actualShippingCost", logisticsEntity.getActualShippingCost());
        map.put("estimatedShippingCost", logisticsEntity.getEstimatedShippingCost());
        map.put("dictPlatform", soB2cEntity.getDictPlatform());
        //是否买家留言
        Boolean isHavebuyerRemark = !StringUtils.isBlank(soB2cEntity.getBuyerRemark());

        map.put("isHavebuyerRemark", isHavebuyerRemark);
        //主表标签处理
        String mainLabelJson = soB2cEntity.getLabelJson();
        Boolean isAmazonFBA = Boolean.FALSE;
        if (StrUtil.isNotBlank(mainLabelJson)) {
            SoB2cDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(mainLabelJson, SoB2cDTO.LabelJsonDTO.class);
            //FBA
            if ("AFN".equals(labelJsonDTO.getFulfillmentChannel())) {
                isAmazonFBA = Boolean.TRUE;
            }
        }
        map.put("isAmazonFBA", isAmazonFBA);
        map.put("packageWidth", logisticsEntity.getWidth());

        //仓库数量
        long warehouseCount = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().count();
        map.put("deliveryWarehouseQty", warehouseCount);
        map.put("buyLogisticsChannelId", logisticsEntity.getName());
        map.put("destCountry", receiverEntity.getCountry());
        map.put("destCity", receiverEntity.getCityName());
        map.put("orderTaxCost", totalTaxCost);
        map.put("amount", MathUtil.multiply(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
        map.put("orderProfitRate", financialInfo.getProfitRateFlag());

        List<Map<String, Object>> mapList = new ArrayList<>(detailList.size());
        for (SoB2cDetailEntity detailEntity : detailList) {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("detailId", detailEntity.getId());
            detailMap.put("platformSkuNo", detailEntity.getPlatformSkuNo());
            detailMap.put("skuQty", detailEntity.getQty());
            detailMap.put("skuNo", detailEntity.getSkuNo());
            detailMap.put("dictPayMethod", soB2cEntity.getDictPayMethod());
            detailMap.put("goodsTotalQty", goodsTotalQty);
            detailMap.put("payTime", payTimeStr);
            detailMap.put("packageWeight", logisticsEntity.getWeight());
            detailMap.put("packageLength", logisticsEntity.getLength());
            detailMap.put("packageHeight", logisticsEntity.getHeight());
            detailMap.put("shop", soB2cEntity.getShopId());
            detailMap.put("logisticsChannelId", logisticsEntity.getLogisticsChannelId());
            detailMap.put("actualShippingCost", logisticsEntity.getActualShippingCost());
            detailMap.put("estimatedShippingCost", logisticsEntity.getEstimatedShippingCost());
            detailMap.put("dictPlatform", soB2cEntity.getDictPlatform());
            detailMap.put("isHavebuyerRemark", isHavebuyerRemark);
            detailMap.put("deliveryWarehouseQty", warehouseCount);
            detailMap.put("buyLogisticsChannelId", logisticsEntity.getName());
            detailMap.put("destCountry", receiverEntity.getCountry());
            detailMap.put("destCity", receiverEntity.getCityName());
            detailMap.put("orderTaxCost", totalTaxCost);
            detailMap.put("amount", MathUtil.multiply(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            detailMap.put("orderProfitRate", financialInfo.getProfitRate());
            detailMap.put("isAmazonFBA", isAmazonFBA);
            detailMap.put("packageWidth", logisticsEntity.getWidth());
            detailMap.put("deliveryWarehouseQty", warehouseCount);
            detailMap.put("destCountry", receiverEntity.getCountry());
            detailMap.put("destCity", receiverEntity.getCityName());
            detailMap.put("orderTaxCost", totalTaxCost);
            detailMap.put("amount", MathUtil.multiply(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            detailMap.put("orderProfitRate", financialInfo.getProfitRateFlag());
            //明细标签处理
            String detailLabelJson = detailEntity.getLabelJson();
            if (StrUtil.isNotBlank(detailLabelJson)) {
                SoB2cDetailDTO.LabelJsonDTO labelJsonDTO = JSONUtil.toBean(detailLabelJson, SoB2cDetailDTO.LabelJsonDTO.class);
                //速卖通已税
                if ("U_TAXED".equals(labelJsonDTO.getAlreadyTaxed()) || "I_TAXED".equals(labelJsonDTO.getAlreadyTaxed())) {
                    detailMap.put("isAliExpressTaxOrder", Boolean.TRUE);
                }
                //菜鸟官方仓
                if ("cainiaoInternationalWarehouse".equals(labelJsonDTO.getLogisticsWarehouseType())) {
                    detailMap.put("isAliExpressNewbieWarehouse", Boolean.TRUE);
                }
            }
            //是否是组合SKU
            if (CollectionUtils.isNotEmpty(bomChildrenList)) {
                long count = bomChildrenList.stream().filter(e -> e.getParentSkuId().equals(detailEntity.getSkuId())).count();
                if (count > 0) {
                    detailMap.put("isCombinationOrder", Boolean.TRUE);
                }
            }
            ProductDetailDTO.ProductDTO productDTO = productList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDTO)) {
                detailMap.put("category", productDTO.getCategory());
                detailMap.put("property", productDTO.getProperty());
            }
            detailMap.put("deliveryWarehouseId", detailEntity.getWarehouseId());
            detailMap.put("deliveryWarehouseLocation", detailEntity.getWarehouseLocation());
            mapList.add(detailMap);
        }
        map.put("detailList", mapList);
        String skuNo = getByField("skuNo", mapList);
        map.put("skuNo", skuNo);

        String sellerSkuNo = getByField("sellerSkuNo", mapList);
        map.put("sellerSkuNo", sellerSkuNo);

        String platformSkuNo = getByField("platformSkuNo", mapList);
        map.put("platformSkuNo", platformSkuNo);

        String skuQty = getByField("skuQty", mapList);
        map.put("skuQty", skuQty);

        String deliveryWarehouseId = getByField("deliveryWarehouseId", mapList);
        map.put("deliveryWarehouseId", deliveryWarehouseId);

        String deliveryWarehouseLocation = getByField("deliveryWarehouseLocation", mapList);
        map.put("deliveryWarehouseLocation", deliveryWarehouseLocation);

        String category = getByField("category", mapList);
        map.put("category", category);

        String property = getByField("propertyId", mapList);
        map.put("propertyId", property);

        String isAliExpressTaxOrder = getByField("isAliExpressTaxOrder", mapList);
        map.put("isAliExpressTaxOrder", isAliExpressTaxOrder);

        String isAliExpressNewbieWarehouse = getByField("isAliExpressNewbieWarehouse", mapList);
        map.put("isAliExpressNewbieWarehouse", isAliExpressNewbieWarehouse);

        String isCombinationOrder = getByField("isCombinationOrder", mapList);
        map.put("isCombinationOrder", isCombinationOrder);


        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateLogisticsWaybill(List<SoB2cDTO.WaybillDTO> waybillDTOList) {
        for (SoB2cDTO.WaybillDTO waybillDTO : waybillDTOList) {
            if (StringUtils.isNotBlank(waybillDTO.getLogisticsBase64()) && StringUtils.isNotBlank(waybillDTO.getSoB2cId())) {
                lambdaUpdate()
                        .set(SoB2cEntity::getLogisticsWaybill, waybillDTO.getLogisticsBase64())
                        .eq(SoB2cEntity::getId, waybillDTO.getSoB2cId())
                        .update();
            }

        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateDistributeWaybill(List<SoB2cDTO.WaybillDTO> waybillDTOList) {
        for (SoB2cDTO.WaybillDTO distributeWaybillDTO : waybillDTOList) {
            if (StringUtils.isNotBlank(distributeWaybillDTO.getDistributeBase64()) && StringUtils.isNotBlank(distributeWaybillDTO.getSoB2cId())) {
                lambdaUpdate()
                        .set(SoB2cEntity::getDistributeWaybill, distributeWaybillDTO.getDistributeBase64())
                        .eq(SoB2cEntity::getId, distributeWaybillDTO.getSoB2cId())
                        .update();
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 添加异常标示
     *
     * @param id
     * @param sign
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSignError(String id, String sign) {
        this.lambdaUpdate().
                set(SoB2cEntity::getSignOrderError, sign).
                eq(SoB2cEntity::getId, id).update();
    }

    /**
     * 清空异常标示
     *
     * @param id
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-20 15:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSignError(String id, String sign) {
        SoB2cEntity soB2cEntity = this.getById(id);
        if (Objects.nonNull(soB2cEntity)) {
            String signOrderError = soB2cEntity.getSignOrderError();
            if (signOrderError.equals(sign)) {
                soB2cEntity.setSignOrderError("");
                this.updateById(soB2cEntity);
            }
        }
    }


    /**
     * 获取标记发货参数
     *
     * @param soB2cId
     * @return
     */
    @Override
    public SoB2cDTO.SignShipOrderDTO getSignShipParam(String soB2cId) {
        SoB2cDTO.SignShipOrderDTO result = baseMapper.getSignShipParam(soB2cId);
        return result;
    }

    @Override
    public Boolean checkPlatformShipOrder(String soB2cId) {
        SoB2cEntity soB2cEntity = this.getById(soB2cId);
        //如果不是手工新增订单需要同步第三方发货标识
        if (!SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
            return Boolean.TRUE;
        } else {
            //如果类型是手工单，可能是拆分或者合并的，需要查询原单是否是第三方平台单
            List<SoB2cRefEntity> soB2cRefEntities = soB2cRefService.listSourceByTargetIds(Arrays.asList(soB2cEntity.getId()), "");
            List<String> soIds = soB2cRefEntities.stream().map(req -> req.getSourceId()).collect(Collectors.toList());
            //查询原单，判断SourceType是否有平台单
            if (CollectionUtils.isNotEmpty(soIds)) {
                List<SoB2cEntity> soB2cEntityList = this.listByIds(soIds);
                List<SoB2cEntity> soB2cEntities = soB2cEntityList.stream()
                        .filter(req -> !SourceTypeEnum.SELF_ADD.getCode().equals(req.getSourceType()))
                        .collect(Collectors.toList());
                //如果包含平台单需要同步第三方发货
                if (CollectionUtils.isNotEmpty(soB2cEntities)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 根据 字段获取值
     *
     * @param fieldCode
     * @return jsonObjectList
     * @author yl
     * @date 2023-12-05 10:25
     */

    private String getByField(String fieldCode, List<Map<String, Object>> mapList) {
        Set<String> list = new HashSet<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            Object obj = map.getOrDefault(fieldCode, "");
            if (Objects.nonNull(obj)) {
                list.add(obj.toString());
            }
        }
        return list.stream().collect(Collectors.joining(","));
    }

    /**
     * @param id
     * @return Boolean
     * @description: 配货仓库规则
     * @author Will
     * @date: 2023/8/24 15:19
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean distributionRule(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {
        SoB2cEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        //仓库匹配规则结果
        RuleDeliveryWarehouseDTO.RuleMatchResultDTO ruleMatchResult = ruleDeliveryWarehouseService.getRuleOrderMatchResult(map);
        //配货规则是否通过
        Boolean distributionSuccess = Objects.nonNull(ruleMatchResult);
        if (!distributionSuccess) {
            //仓库规则不匹配,标识异常
            updateWarehouseAbnormalType(id, SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT);
            //明细设置仓库规则不匹配
            List<String> detailIdList = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).map(SoB2cDetailEntity::getId).collect(Collectors.toList());
            soB2cDetailService.updateIsMatchWarehouseRule(detailIdList);
            return Boolean.FALSE;
        }
        //更新明细仓库信息
        String warehouseId = ruleMatchResult.getWarehouseId();
        //返回了仓库则更新仓库为空的数据
        if (StrUtil.isNotBlank(warehouseId)) {
            for (SoB2cDetailEntity detailEntity : detailList) {
                if (StrUtil.isNotBlank(detailEntity.getWarehouseId())) {
                    continue;
                }
                detailEntity.setWarehouseId(warehouseId);
            }
            soB2cDetailService.updateWarehouse(detailList);
        }
        try {
            //走物流规则
            Boolean ruleLogistics = logisticsRule(id, map);
        } catch (Exception e) {
            log.error("物流规则报错>>>{}", e.getMessage());

        }

        return Boolean.TRUE;
    }

    /**
     * @param id  订单id
     * @param map 校验的map
     * @return
     * @description 物流规则
     * @author Lambda
     * @create 2023-12-14 15:11
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean logisticsRule(String id, Map<String, Object> map) {
        SoB2cEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        //规则结果
        RuleLogisticsDTO.RuleMatchResultDTO matchResult = ruleLogisticsService.getRuleOrderMatchResult(map);
        Boolean result = Objects.nonNull(matchResult);
        //表示通过
        if (result) {
            //物流商id
            String logisticsChannelId = matchResult.getLogisticsChannelId();
            String logisticsChannelName = matchResult.getLogisticsChannelName();
            Boolean autoGetTrackNo = matchResult.getAutoGetTrackNo();
            if (StringUtils.isNotBlank(logisticsChannelId)) {
                SoB2cLogisticsEntity b2cLogistics = soB2cLogisticsService.getByMainId(id);
                if (Objects.nonNull(b2cLogistics)) {
                    b2cLogistics.setLogisticsChannelId(logisticsChannelId);
                    b2cLogistics.setLogisticsChannelName(logisticsChannelName);
                    soB2cLogisticsService.updateById(b2cLogistics);
                }
            }
            //状态更新为配货中
            updateBillStatus(id, SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION);
            //获取跟踪单号
            if (ObjectUtil.isNotEmpty(autoGetTrackNo) && autoGetTrackNo) {
                try {
                    this.getLogisticsCode(id, Boolean.TRUE);
                    //自动发货(物流规则有设置则自动发货)
                    submitDelivery(id);
                } catch (Exception e) {
                    log.error("获取物流单号出错了>>>>>>>>{}", e.getMessage());
                }
            }
        } else {
            updateLogisticsAbnormalType(id, SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT);
        }
        return result;
    }

    /**
     * @param id
     * @param approveStatusEnum
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 订单审核规则不通过
     * @author Will
     * @date: 2023/8/24 15:14
     */
    private Boolean updateAbnormalTypeApprove(String id, ApproveStatusEnum approveStatusEnum, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(ObjectUtils.isNotEmpty(approveStatusEnum), SoB2cEntity::getApproveStatus, approveStatusEnum.getCode())
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .set(SoB2cEntity::getIsMatchOrderRule, Boolean.FALSE)
                .update(new SoB2cEntity());
    }

    /**
     * @param id
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 物流规则不通过
     * @author Will
     * @date: 2023/12/14 9:40
     */
    private Boolean updateLogisticsAbnormalType(String id, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .set(SoB2cEntity::getIsMatchLogisticsRule, Boolean.FALSE)
                .update(new SoB2cEntity());
    }


    /**
     * @param id
     * @param soB2cAbnormalTypeEnum
     * @return Boolean
     * @description: 仓库规则不通过
     * @author Will
     * @date: 2023/8/24 15:14
     */
    private Boolean updateWarehouseAbnormalType(String id, SoB2cAbnormalTypeEnum soB2cAbnormalTypeEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getAbnormalType, soB2cAbnormalTypeEnum.getCode())
                .update(new SoB2cEntity());
    }

    /**
     * @param id
     * @param soB2cBillStatusEnum
     * @description: 更新订单状态
     * @author Will
     * @date: 2023/8/24 14:55
     */
    private Boolean updateBillStatus(String id, SoB2cBillStatusEnum soB2cBillStatusEnum) {
        return lambdaUpdate().eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getBillStatus, soB2cBillStatusEnum.getCode())
                .update(new SoB2cEntity());
    }


    /**
     * 报表管理 销售统计
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.ReportDTO.ProductSalesPagingViewDTO>
     * @author yl
     * @date 2023-09-01 11:19
     */
    @Override
    public PagingVO<ReportDTO.ProductSalesPagingViewDTO> productSalesPaging(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        ReportDTO.ProductSalesPagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        //sku 创建时间
        List skuCreateTimeList = params.getSkuCreateTimeList();
        List<String> skuIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(skuCreateTimeList)) {
            List<ProductDetailEntity> skuList = plmTaskFeign.listByCreateTimeList(skuCreateTimeList);
            skuIdList = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(skuIdList)) {
                return new PagingVO<>(new Page<>());
            }
        }
        IPage pageData = baseMapper.productSalesPaging(query, params, skuIdList);
        List<ReportDTO.ProductSalesPagingViewDTO> list = pageData.getRecords();
        Duration between = LocalDateTimeUtil.between(params.getOrderCreateTimeList().get(0), params.getOrderCreateTimeList().get(1));
        long diffDays = between.toDays();
        if (diffDays == 0) {
            diffDays = 1;
        }
        fillProductSalesList(list, diffDays);
        return new PagingVO<>(pageData);

    }


    /**
     * 导出 销售统计
     *
     * @param params
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-09-04 16:39
     */
    @Override
    public Boolean productSalesExport(ReportDTO.ProductSalesPagingParamDTO params, HttpServletResponse response) {
        //sku 创建时间
        List skuCreateTimeList = params.getSkuCreateTimeList();
        List<String> skuIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(skuCreateTimeList)) {
            List<ProductDetailEntity> skuList = plmTaskFeign.listByCreateTimeList(skuCreateTimeList);
            skuIdList = skuList.stream().map(ProductDetailEntity::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(skuIdList)) {
                throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
            }
        }
        //获取到产品销售统计导出的数据
        List<ReportDTO.ProductSalesPagingViewDTO> list = baseMapper.listProductSalesExport(params, skuIdList);
        Duration between = LocalDateTimeUtil.between(params.getOrderCreateTimeList().get(0), params.getOrderCreateTimeList().get(1));
        long diffDays = between.toDays();
        if (diffDays == 0) {
            diffDays = 1;
        }
        fillProductSalesList(list, diffDays);
        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/ProductSalesCount.xlsx";
        String name = "产品销售统计";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("产品销售统计导出出错 >>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;


    }

    @Override
    public SoB2cDTO.FinancialInfoDTO getFinancialInfoById(SoB2cDTO.FinancialParamDTO dto) {
        //销售订单
        SoB2cEntity soB2cEntity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到B2C销售订单表数据"));

        //物流信息
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(dto.getId());
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = soB2cFinanceService.getByMainId(dto.getId());
        if (ObjectUtil.isEmpty(soB2cFinanceEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }

        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(dto.getId());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cFinanceEntity(soB2cFinanceEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.FALSE);
        return financialInfoDTO;
    }

    @Override
    public SoB2cDTO.FinancialInfoDTO getFinancialInfo(SoB2cDTO.FinancialParamDTO dto, Boolean isAdd) {

        //销售订单
        SoB2cEntity soB2cEntity = dto.getSoB2cEntity();
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = dto.getSoB2cLogisticsEntity();
        //明细信息
        List<SoB2cDetailEntity> soB2cDetailList = dto.getSoB2cDetailList();
        //财务信息
        SoB2cFinanceEntity soB2cFinanceEntity = dto.getSoB2cFinanceEntity();

        if (ObjectUtils.isEmpty(soB2cFinanceEntity) && !isAdd) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }
        //新增时用新对象
        if (isAdd) {
            soB2cFinanceEntity = new SoB2cFinanceEntity();
        }

        //店铺信息
        ShopCostEntity shopCostEntity = shopCostService.getByShopId(soB2cEntity.getShopId());
        //平台费
        BigDecimal platformCost = BigDecimal.ZERO;
        //avt 费
        BigDecimal vatCost = BigDecimal.ZERO;
        //转账费
        BigDecimal paypalCost = BigDecimal.ZERO;

        SoB2cDTO.FinancialInfoDTO financialInfoDTO = new SoB2cDTO.FinancialInfoDTO();
        BeanMapperUtils.copy(soB2cFinanceEntity, financialInfoDTO);

        //商品成本,订单SKU*数量的含税成本价汇总
        BigDecimal itemCost = soB2cDetailList.stream().map(SoB2cDetailEntity::getTaxCost).reduce(BigDecimal.ZERO, BigDecimal::add);


        //判断是否是人民币
        if (ObjectUtils.isNotEmpty(dto.getIsCny()) && dto.getIsCny()) {
            financialInfoDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            financialInfoDTO.setAmount(MathUtil.multiply(soB2cEntity.getAmount(), soB2cEntity.getExchangeRate()));
            financialInfoDTO.setItemCost(itemCost);
        } else {
            financialInfoDTO.setCurrency(soB2cEntity.getCurrency());
            financialInfoDTO.setAmount(soB2cEntity.getAmount());
            financialInfoDTO.setItemCost(MathUtil.divide(itemCost, soB2cEntity.getExchangeRate()));
        }

        String platformOption = soB2cFinanceEntity.getPlatformCostType();
        String vatOption = soB2cFinanceEntity.getVatCostType();
        String transferOption = soB2cFinanceEntity.getTransferCostType();
        BigDecimal platformRate = soB2cFinanceEntity.getPlatformRate();
        BigDecimal vatRate = soB2cFinanceEntity.getVatRate();
        BigDecimal transferRate = soB2cFinanceEntity.getTransferRate();
        if (isAdd) {
            if (ObjectUtils.isNotEmpty(shopCostEntity)) {
                platformOption = shopCostEntity.getDictPlatformOption();
                vatOption = shopCostEntity.getDictVatOption();
                transferOption = shopCostEntity.getDictTransferOption();
                platformRate = shopCostEntity.getPlatformRate();
                vatRate = shopCostEntity.getVatRate();
                transferRate = shopCostEntity.getTransferRate();

            }
        }

        DictBasicEntity dictPlatformOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_PLATFORM_COST.getType(), platformOption);

        DictBasicEntity dictVatOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_VAT_COST.getType(), vatOption);

        DictBasicEntity dictTransferOption = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SHOP_TRANSFER_COST.getType(), transferOption);

        //平台费
        BigDecimal dividePlatformRate = MathUtil.divide(platformRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictPlatformOption) && ShopPlatformCostEnum.MULTIPLY_PLATFORM_RATE.getCode().equals(dictPlatformOption.getValue())) {
            platformCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), dividePlatformRate);
            financialInfoDTO.setPlatformCostType(dictPlatformOption.getValue());
            financialInfoDTO.setPlatformRate(platformRate);
        }
        //转账费
        BigDecimal divideTransferRate = MathUtil.divide(transferRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictTransferOption) && ShopTransferCostEnum.MULTIPLY_TRANSFER_RATE.getCode().equals(dictTransferOption.getValue())) {
            paypalCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideTransferRate);
            financialInfoDTO.setTransferCostType(dictTransferOption.getValue());
            financialInfoDTO.setTransferRate(transferRate);
        }
        //vat费
        BigDecimal divideVatRate = MathUtil.divide(vatRate, MathUtil.BigDecimal_100);
        if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.MULTIPLY_ADD_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.multiply(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        } else if (ObjectUtils.isNotEmpty(dictVatOption) && ShopVATCostEnum.DIVISION_ADD_MULTIPLY_VAT_RATE.getCode().equals(dictVatOption.getValue())) {
            vatCost = MathUtil.divide(MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()), MathUtil.add(BigDecimal.ONE, divideVatRate)).multiply(divideVatRate);
        }
        if (ObjectUtils.isNotEmpty(dictVatOption)) {
            financialInfoDTO.setVatCostType(dictVatOption.getValue());
        }
        financialInfoDTO.setVatRate(vatRate);

        //平台费,店铺计算
        financialInfoDTO.setPlatformCost(platformCost);

        //转账费,店铺计算
        financialInfoDTO.setPaypalCost(paypalCost);
        //包装辅料费,包装辅料SKU*数量的成本价汇总
        BigDecimal accessoriesCost = BigDecimal.ZERO;
        String accessoriesSkuId = soB2cLogisticsEntity.getAccessoriesSkuId();
        if (StringUtils.isNotBlank(soB2cLogisticsEntity.getAccessoriesSkuId())) {
            List<SkuVO> list = plmTaskFeign.getSkuInfoByIds(Arrays.asList(accessoriesSkuId));
            if (CollectionUtils.isNotEmpty(list)) {
                accessoriesCost = MathUtil.multiply(list.get(0).getTargetTaxCost(), soB2cLogisticsEntity.getAccessoriesQty());
            }
        }
        //运费收入
        financialInfoDTO.setShippingCost(ObjectUtil.isEmpty(financialInfoDTO.getShippingCost()) ? BigDecimal.ZERO : financialInfoDTO.getShippingCost());
        //物流成本
        financialInfoDTO.setLogisticsCost(ObjectUtil.isEmpty(financialInfoDTO.getLogisticsCost()) ? BigDecimal.ZERO : financialInfoDTO.getLogisticsCost());

        financialInfoDTO.setAccessoriesCost(accessoriesCost);
        //VAT税费,店铺计算
        financialInfoDTO.setVatCost(vatCost);
        //总利润,订单总金额+运费收入-商品成本-物流成本-平台费-转账费-包装辅料费-VAT税费
        BigDecimal profit = ObjectUtil.defaultIfNull(financialInfoDTO.getAmount(), BigDecimal.ZERO)
                .add(ObjectUtil.defaultIfNull(financialInfoDTO.getShippingCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getItemCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getLogisticsCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(platformCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(paypalCost, BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(financialInfoDTO.getAccessoriesCost(), BigDecimal.ZERO))
                .subtract(ObjectUtil.defaultIfNull(vatCost, BigDecimal.ZERO));
        financialInfoDTO.setProfit(profit);
        //利润率,总利润/(订单总金额+运费收入)*100%
        BigDecimal itemCostProfitRate = MathUtil.divide(itemCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setItemCostProfitRate(MathUtil.compareTo(itemCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : itemCostProfitRate + "%");

        BigDecimal logisticsCostProfitRate = MathUtil.divide(financialInfoDTO.getLogisticsCost(), MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setLogisticsCostProfitRate(MathUtil.compareTo(logisticsCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : logisticsCostProfitRate + "%");

        BigDecimal paypalCostProfitRate = MathUtil.divide(paypalCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setPaypalCostProfitRate(MathUtil.compareTo(paypalCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : paypalCostProfitRate + "%");

        BigDecimal platformCostProfitRate = MathUtil.divide(platformCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setPlatformCostProfitRate(MathUtil.compareTo(platformCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : platformCostProfitRate + "%");

        BigDecimal accessoriesCostProfitRate = MathUtil.divide(accessoriesCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setAccessoriesCostProfitRate(MathUtil.compareTo(accessoriesCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : accessoriesCostProfitRate + "%");

        BigDecimal vatCostProfitRate = MathUtil.divide(vatCost, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);
        financialInfoDTO.setVatCostProfitRate(MathUtil.compareTo(vatCostProfitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : vatCostProfitRate + "%");

        BigDecimal profitRate = MathUtil.divide(profit, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost())).multiply(MathUtil.BigDecimal_100);

        BigDecimal profitRateFlag = MathUtil.divide(profit, MathUtil.add(financialInfoDTO.getAmount(), financialInfoDTO.getShippingCost()));
        financialInfoDTO.setProfitRateFlag(profitRateFlag);
        financialInfoDTO.setProfitRate(MathUtil.compareTo(profitRate, MathUtil.ZERO) == MathUtil.ZERO ? "0%" : profitRate + "%");
        return financialInfoDTO;
    }


    @Override
    public BatchResultDTO isNotNeedMerge(String id) {
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        lambdaUpdate()
                .eq(SoB2cEntity::getId, id)
                .set(SoB2cEntity::getIsNotMerge, Boolean.TRUE)
                .update();
        //操作日志
        String msg = "销售订单【{}】标记不合并";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "销售订单不合并");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "标记不合并");
    }

    /**
     * @param soB2cEntity
     * @description: 新增财务信息
     * @author Will
     * @date: 2023/9/8 12:26
     */
    private void addSoB2cFinance(SoB2cEntity soB2cEntity) {
        //物流信息
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsService.getByMainId(soB2cEntity.getId());
        if (ObjectUtil.isEmpty(logisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //明细
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(soB2cDetailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        SoB2cDTO.FinancialParamDTO dto = new SoB2cDTO.FinancialParamDTO();
        dto.setId(soB2cEntity.getId());
        dto.setIsCny(Boolean.FALSE);
        dto.setSoB2cEntity(soB2cEntity);
        dto.setSoB2cLogisticsEntity(logisticsEntity);
        dto.setSoB2cDetailList(soB2cDetailList);
        SoB2cDTO.FinancialInfoDTO financialInfoDTO = getFinancialInfo(dto, Boolean.TRUE);
        SoB2cFinanceDTO.AddDTO addDTO = BeanMapperUtils.map(SoB2cFinanceDTO.AddDTO.class, financialInfoDTO);
        addDTO.setMainId(soB2cEntity.getId());
        soB2cFinanceService.add(addDTO);
    }

    /**
     * 填充销售订单数据
     *
     * @param list
     * @param diffDays
     */
    private void fillProductSalesList(List<ReportDTO.ProductSalesPagingViewDTO> list, long diffDays) {
        List<String> shopIdList = list.stream().map(ReportDTO.ProductSalesPagingViewDTO::getShopId).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = CollectionUtils.isNotEmpty(shopIdList) ? shopInfoService.listByIds(shopIdList) : Collections.emptyList();
        //平台skuno
        List<String> platformSkuNoList = list.stream().map(ReportDTO.ProductSalesPagingViewDTO::getPlatformSkuNo).collect(Collectors.toList());

        List<SkuMappingDTO.SkuDTO> skuInfoList = skuMappingService.listByPlatformSkuNoList(platformSkuNoList);
        for (ReportDTO.ProductSalesPagingViewDTO item : list) {
            String shopId = item.getShopId();
            //平台sku
            String platformSkuNo = item.getPlatformSkuNo();
            SkuMappingDTO.SkuDTO sku = skuInfoList.stream().filter(s -> s.getPlatformSkuNo().equals(platformSkuNo)).
                    findFirst().orElse(null);
            String productSkuNo = "";
            String sellerSkuNo = "";
            if (Objects.nonNull(sku)) {
                productSkuNo = sku.getProductSkuNo();
                sellerSkuNo = sku.getFlagSkuNo();
            }
            item.setProductSkuNo(productSkuNo);
            item.setSellerSkuNo(sellerSkuNo);
            String shopName = shopInfoList.stream().filter(s -> s.getId().equals(shopId)).
                    findFirst().map(ShopInfoEntity::getName).orElse("");
            item.setShopName(shopName);
            Integer qty = item.getQty();
            Integer avgQty = Math.toIntExact(qty / diffDays);
            item.setAvgQty(avgQty);
            BigDecimal amount = item.getAmount();
            BigDecimal avgAmount = amount.divide(new BigDecimal(diffDays), 4, RoundingMode.HALF_UP);
            item.setAvgAmount(avgAmount);

        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cEntity saveOrUpdateEntity(PlatformOrderDTO dto) {
        log.debug("===== start saveOrUpdateEntity:{}", dto);
        SoB2cEntity oldEntity = null;
        try {
            oldEntity = this.getByPlatformInfo(dto.getPlatformCode(), dto.getDictPlatform());
        } catch (Exception e) {
            log.error("查询订单异常：{}", e.getMessage());
        }
        if (null == oldEntity) {
            // 组合信息
            SoB2cEntity entity = new SoB2cEntity();
            BeanUtils.copyProperties(dto, entity);
            handleData(entity, false, false);
            if (StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = StrUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                entity.setApproveStatus(approveStatusEnum);
            }
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSDD);
            entity.setCode(code);
            boolean save = false;
            try {
                save = this.save(entity);
            } catch (Exception e) {
                log.error("报错实体：{}", entity);
                log.error("保存订单信息异常：PlatformCode：{},{}", dto.getPlatformCode(), e.getMessage());
            }
            if (!save) {
                throw new ServiceException("soB2c订单保存失败");
            }
            // 新增日志
            String msg = StrUtil.format("从【{}】平台下载订单成功", dto.getDictPlatform());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "新增操作");
            return entity;
        } else {
            // 历史异常记录修复
            if (StringUtils.isBlank(oldEntity.getCode()) && !BusinessCommonConstants.hasProfile("prod")) {
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XSDD);
                oldEntity.setCode(code);
            }
            if (StringUtils.isBlank(oldEntity.getShopId()) && !BusinessCommonConstants.hasProfile("prod")) {
                oldEntity.setShopId(dto.getShopId());
            }
            if (0 == oldEntity.getExchangeRate().compareTo(BigDecimal.ZERO)) {
                handleData(oldEntity, false, false);
            }
            if (StringUtils.isNotBlank(dto.getApproveStatusStr())) {
                ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(dto.getApproveStatusStr());
                if (null == approveStatusEnum) {
                    String msg = StrUtil.format("[{}]审核状态类型存在:{}", dto.getUniqueId(), dto.getApproveStatusStr());
                    throw new ServiceException(msg);
                }
                oldEntity.setApproveStatus(approveStatusEnum);
            }
//            if (0 == oldEntity.getExchangeRate().compareTo(BigDecimal.ZERO)){
//                oldEntity.setApproveStatus(ApproveStatusEnum.REJECT);
//                oldEntity.setAbnormalType(SoB2cAbnormalTypeEnum.ENUM_RATE_NOT_EXIST_REJECT.getCode());
//                oldEntity.setRemark("汇率配置不存在");
//            }
            // 只替换更新信息
            SoB2cEntity entity = B2cOrderConsumerConverter.INSTANCE.convertUpdateMainOrder(oldEntity, dto);
            if (!oldEntity.toString().equals(entity.toString())) {
                if (!this.updateById(entity)) {
                    throw new ServiceException("soB2c订单更新失败");
                }
            }
            return oldEntity;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoB2cEntity getByPlatformInfo(String platformCode, String dictPlatform) {
        return lambdaQuery()
                .eq(SoB2cEntity::getPlatformCode, platformCode)
                .eq(SoB2cEntity::getDictPlatform, dictPlatform)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public Map<String, Object> getJson(String id) {
        List<SoB2cDetailEntity> soB2cDetailList = soB2cDetailService.listByMainId(id);
        Map<String, Object> obj = handleMatchJson(id, soB2cDetailList, new HashMap<>());
        return obj;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean matchSku(SoB2cDTO.MatchSkuDTO dto) {
        //详情id
        String detailId = dto.getId();
        SoB2cDetailEntity detailEntity = soB2cDetailService.getById(detailId);
        if (Objects.isNull(detailEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        SoB2cEntity soB2cEntity = this.getById(detailEntity.getMainId());
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        String skuId = dto.getSkuId();
        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(Arrays.asList(skuId));
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        ProductDetailEntity skuEntity = skuList.get(0);
        //平台sku
        String platformSkuNo = detailEntity.getPlatformSkuNo();
        //平台产品id
        String platformSpuNo = detailEntity.getPlatformSpuNo();

        SkuMappingDTO.AddSkuMappingDTO addSkuMappingDTO = new SkuMappingDTO.AddSkuMappingDTO();
        String skuNo = skuEntity.getSkuNo();
        addSkuMappingDTO.setProductName(skuEntity.getName());
        addSkuMappingDTO.setProductSkuId(skuEntity.getId());
        addSkuMappingDTO.setProductSkuNo(skuNo);
        addSkuMappingDTO.setShopId(soB2cEntity.getShopId());
        String typeCode = RuleTypeEnum.PLATFORM.getCode();
        addSkuMappingDTO.setType(RuleTypeEnum.PLATFORM);
        String salesPlatform = soB2cEntity.getDictPlatform();
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(salesPlatform);
        String salesPlatformName = Objects.nonNull(platformDictEnum) ? platformDictEnum.getName() : "";
        addSkuMappingDTO.setDictPlatform(salesPlatform);
        addSkuMappingDTO.setPlatformName(salesPlatformName);
        ListingInfoEntity listingInfo = listingInfoService.getByPlatformSkuNoAndSpu(platformSkuNo, platformSpuNo, typeCode);
        if (Objects.isNull(listingInfo)) {
            throw new ServiceException(ApiError.ERROR_LISTING_NOT_EXIST);
        }
        addSkuMappingDTO.setListingId(listingInfo.getId());

        detailEntity.setSkuId(skuId);
        detailEntity.setSkuNo(skuNo);
        soB2cDetailService.updateById(detailEntity);
        //添加对应关系
        return skuMappingService.add(addSkuMappingDTO);

    }

    /**
     * @param id
     * @return
     * @description 更改订单为发货 并且生成销售出库单
     * @author Lambda
     * @create 2023-12-13 17:49
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SoOutstockDTO.GenerateB2cDTO orderShipped(String id) {
        SoB2cEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单");
        }
        SoB2cLogisticsEntity soB2cLogistics = soB2cLogisticsService.getByMainId(id);
        entity.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        //更改状态
        this.updateById(entity);
        String msg = "销售订单已发货";
        operateLogService.addModuleOperateLog(StrUtil.format(msg, entity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), entity.getId(), "已发货");
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getShopId());
        if (Objects.isNull(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        SoOutstockDTO.GenerateB2cDTO dto = new SoOutstockDTO.GenerateB2cDTO();
        dto.setOrderType(OrderTypeEnum.B2C.getCode());
        dto.setSoId(entity.getId());
        dto.setSoCode(entity.getCode());
        dto.setSourceId(id);
        dto.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        dto.setSourceCode(entity.getCode());
        String chargeId = shopInfoEntity.getChargeId();
        dto.setCustomerId(shopInfoEntity.getCustomerId());
        dto.setSellerId(chargeId);
        dto.setSellerName(shopInfoEntity.getChargeName());
        SysDepartmentUserNumberDTO deptUser = null;
        if (!StringUtil.isEmpty(chargeId)) {
            deptUser = sysUserFeign.getDeptByUserId(chargeId);
        }
        if (Objects.nonNull(deptUser)) {
            dto.setSalesDeptId(deptUser.getDepartmentId());
        }
        if (Objects.isNull(soB2cLogistics)) {
            dto.setTrackNo(soB2cLogistics.getCode());
        }
        //根据主表id 查询出库的信息
        List<SoB2cDetailDTO.OutstockDTO> detailList = soB2cDetailService.listOutstockByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        dto.setWarehouseId(detailList.get(0).getWarehouseId());
        dto.setWarehouseName(detailList.get(0).getWarehouseName());
        dto.setWarehouseOrgId(detailList.get(0).getWarehouseOrgId());

        List<String> soDetailIdList = detailList.stream().map(SoB2cDetailDTO.OutstockDTO::getSoDetailId).collect(Collectors.toList());
        /**
         * 发货详情
         */
        List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailList = soB2cDeliveryFeign.listBySoDetailIds(soDetailIdList);
        for (SoB2cDetailDTO.OutstockDTO item : detailList) {
            String soDetailId = item.getSoDetailId();
            String sourceDetailId = soB2cDeliveryDetailList.stream().filter(s -> s.getSourceDetailId().equals(soDetailId)).
                    map(SoB2cDeliveryDetailEntity::getId).findFirst().orElse(soDetailId);
            item.setSourceDetailId(sourceDetailId);
        }

        List<SoOutstockDetailDTO.AddDTO> wantDetailList = B2cOrderConverter.INSTANCE.convertOutstockDetail(detailList);
        dto.setDetailList(wantDetailList);
        return dto;

    }

    /**
     * 运费测算 更改渠道
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean selectLogisticsChannel(SoB2cLogisticsDTO.SelectChannelDTO dto) {
        String id = dto.getId();
        String logisticsChannelId = dto.getLogisticsChannelId();
        //B2C销售订单主表信息
        SoB2cEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(id);
        if (ObjectUtils.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //存在的物流渠道
        String existChannelId = soB2cLogisticsEntity.getLogisticsChannelId();
        //表示不一样 就要改过
        Boolean isUpdate = !logisticsChannelId.equals(existChannelId);
        //存在的物流单 code
        String code = soB2cLogisticsEntity.getCode();
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(logisticsChannelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_METHOD_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(code)) {
            //取消物流单
            LogisticsBillDTO.CancelBillDTO cancelBillDTO = LogisticsBillDTO.CancelBillDTO.builder().
                    channelId(existChannelId).trackNo(code).referenceNumber(entity.getId()).build();
            ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(cancelBillDTO);
            //取消失败
            if (!cancelResult.isSuccess()) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_CANCEL_FAI, code);
            }
        }
        if (isUpdate) {
            soB2cLogisticsEntity.setLogisticsChannelId(logisticsChannelId);
            soB2cLogisticsEntity.setLogisticsChannelName(logisticsChannel.getName());
            soB2cLogisticsEntity.setCode("");
            //物流信息更新
            return soB2cLogisticsService.updateById(soB2cLogisticsEntity);
        }
        return true;

    }

    /**
     * 平台仓订单处理
     * 走仓库规则 通过就是审核通过 并待发货
     * 没有通过就是审核通过有待配货
     *
     * @param
     * @param map
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-18 14:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public Boolean platformWarehouseOrderHandle(String id, Map<String, Object> map) {
        SoB2cEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单表"));
        //明细信息
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }

        //仓库匹配规则结果
        RuleDeliveryWarehouseDTO.RuleMatchResultDTO ruleMatchResult = ruleDeliveryWarehouseService.getRuleOrderMatchResult(map);
        ApproveStatusEnum approveStatus = ApproveStatusEnum.APPROVE;
        entity.setApproveStatus(approveStatus);
        //配货规则是否通过
        Boolean distributionSuccess = Objects.nonNull(ruleMatchResult);

        if (!distributionSuccess) {
            //仓库规则不匹配,标识异常
            entity.setAbnormalType(SoB2cAbnormalTypeEnum.ENUM_DISTRIBUTION_REJECT.getCode());
            //明细设置仓库规则不匹配
            List<String> detailIdList = detailList.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseId())).map(SoB2cDetailEntity::getId).collect(Collectors.toList());
            soB2cDetailService.updateIsMatchWarehouseRule(detailIdList);
            entity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            this.updateById(entity);
            return Boolean.FALSE;
        }
        entity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        this.updateById(entity);

        //更新明细仓库信息
        String warehouseId = ruleMatchResult.getWarehouseId();
        //返回了仓库则更新仓库为空的数据
        if (StrUtil.isNotBlank(warehouseId)) {
            for (SoB2cDetailEntity detailEntity : detailList) {
                if (StrUtil.isNotBlank(detailEntity.getWarehouseId())) {
                    continue;
                }
                detailEntity.setWarehouseId(warehouseId);
            }
            soB2cDetailService.updateWarehouse(detailList);
        }
        return Boolean.TRUE;

    }

    /**
     * @return
     * @description 正常订单规则
     * @author Lambda
     * @create 2023-12-18 14:54
     */
    @Override
    @Async
    public Boolean pullOrderHandle(String id, List<SoB2cDetailEntity> detailList, Map<String, Object> map) {

        Boolean isSuccess = this.approveRule(id, detailList, map);
        return isSuccess;
    }


}
