package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.PlatformFulfillOrderDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.PlatformSoOutStockDetailDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.*;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpSyncFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryInterceptFeign;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.rpc.wms.feign.WmsFbaInventoryFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA.LWAException;
import com.erp.sdk.oms.amz.spapi.api.FbaOutboundApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentoutbound.CancelFulfillmentOrderResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.convert.SoMultiChannelConverter;
import com.erp.server.oms.kingdee.SyncAmazonSoMultiChannelService;
import com.erp.server.oms.mapper.SoMultiChannelMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_MULTI_CHANNEL;

/**
 * <p>
 * 多渠道订单主表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SoMultiChannelServiceImpl extends SuperServiceImpl<SoMultiChannelMapper, SoMultiChannelEntity> implements SoMultiChannelService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WmsFbaInventoryFeign wmsFbaInventoryFeign;
    @Lazy
    @Resource
    private SoB2cService soB2cService;
    @Lazy
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private SoMultiChannelDetailService soMultiChannelDetailService;
    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SyncAmazonSoMultiChannelService syncAmazonSoMultiChannelService;
    @Resource
    private DmpSyncFeign dmpSyncFeign;
    @Resource
    private CfgQueryOptionFeign cfgQueryOptionFeign;
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SoB2cDeliveryInterceptFeign soB2cDeliveryInterceptFeign;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private LogisticsFeign logisticsFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoMultiChannelDTO.AddDTO addDTO) {
        SoMultiChannelEntity soMultiChannelEntity = new SoMultiChannelEntity();
        BeanMapperUtils.copy(addDTO, soMultiChannelEntity);
        // 数据处理
        handleData(soMultiChannelEntity);
        // 生成发货单号
        String deliveryCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WFHD);
        soMultiChannelEntity.setDeliveryCode(deliveryCode);
        if (CharSequenceUtil.isBlank(soMultiChannelEntity.getRemark())) {
            soMultiChannelEntity.setRemark(deliveryCode);
        }
        log.info("开始新增多渠道订单主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_DQDD);
        soMultiChannelEntity.setCode(code);
        boolean save = super.save(soMultiChannelEntity);
        if (!save) {
            throw new ServiceException("多渠道订单主单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】卖家订单编号【{}】", UserContext.getDefaultLoginUser().getUserName(), "多渠道订单", soMultiChannelEntity.getCode(), soMultiChannelEntity.getDeliveryCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelEntity.getId(), "新增操作");
        //新增明细（如果有明细的话）
        List<SoMultiChannelDetailEntity> soMultiChannelDetailEntities = soMultiChannelDetailService.addDetail(soMultiChannelEntity, addDTO.getDetailList());
        //新增三方仓发货单
        addThirdWarehouseDelivery(soMultiChannelEntity, soMultiChannelDetailEntities);
        //回填销售订单状态
        soB2cService.updateSoB2cDistribution(soMultiChannelEntity);
        return new BaseResultDTO.AddDTO(soMultiChannelEntity.getId(), code);
    }

    /**
     * 新增三方仓发货单
     *
     * @param soMultiChannelEntity
     * @param soMultiChannelDetailEntities
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void addThirdWarehouseDelivery(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailEntity> soMultiChannelDetailEntities) {
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus());
        thirdWarehouseDeliveryEntity.setSoCode(soMultiChannelEntity.getSoCode());
        thirdWarehouseDeliveryEntity.setSoId(soMultiChannelEntity.getSoId());
        thirdWarehouseDeliveryEntity.setCode(soMultiChannelEntity.getDeliveryCode());
        thirdWarehouseDeliveryEntity.setDictPlatform(soMultiChannelEntity.getDictPlatform());
        thirdWarehouseDeliveryEntity.setPlatformCode(soMultiChannelEntity.getPlatformCode());
        thirdWarehouseDeliveryEntity.setThirdWarehousePlatform(soMultiChannelEntity.getDeliveryPlatform());
        thirdWarehouseDeliveryEntity.setShippingMethod(soMultiChannelEntity.getShippingMethod());
        List<ThirdWarehouseDeliveryDetailEntity> detailEntityList = new ArrayList<>();
        //根据SKU+库存SKU（FNSKU）行合并数量累加
        for (SoMultiChannelDetailEntity detailEntity : soMultiChannelDetailEntities) {
            ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
            thirdWarehouseDeliveryDetailEntity.setSkuId(detailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSkuNo(detailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setDeliveryQty(detailEntity.getQty());
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(soMultiChannelEntity.getDeliveryWarehouseId());
            thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo(detailEntity.getFnSku());
            thirdWarehouseDeliveryDetailEntity.setPlatformWarehouseCode("");
            thirdWarehouseDeliveryDetailEntity.setSourceSkuId(detailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(detailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setSoDetailId(detailEntity.getSoDetailId());
            detailEntityList.add(thirdWarehouseDeliveryDetailEntity);
        }
        thirdWarehouseDeliveryEntity.setDetailEntityList(detailEntityList);
        thirdWarehouseDeliveryFeign.add(thirdWarehouseDeliveryEntity);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoMultiChannelDTO.UpdateDTO addOrUpdateDTO) {
        SoMultiChannelEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "多渠道订单主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SoMultiChannelEntity soMultiChannelEntity = BeanMapperUtils.map(SoMultiChannelEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soMultiChannelEntity);
        log.info("编辑 开始修改多渠道订单主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soMultiChannelEntity);
        if (!save) {
            throw new ServiceException("多渠道订单主单保存失败");
        }
        // 修改明细数据（包含增删改）（如果有明细的话）
        soMultiChannelDetailService.updateDetail(soMultiChannelEntity, addOrUpdateDTO.getDetailList());
        // 记录主单操作日志
        log.info("编辑 开始记录多渠道订单主单日志数据，单号：【{}】", soMultiChannelEntity.getDeliveryCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soMultiChannelEntity.getDeliveryCode(), "多渠道订单主单");
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soMultiChannelEntity, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SoMultiChannelDTO.ListDTO> paging(PagingDTO<SoMultiChannelDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoMultiChannelDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SoMultiChannelDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoMultiChannelDTO.PagingParamDTO searchParam = new SoMultiChannelDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoMultiChannelDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<SoMultiChannelDTO.TabListDTO> list1 = new ArrayList<>();
        //待提交
        int waitSubmitCount = list.stream().filter(e -> ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(e.getApproveStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(ApproveStatusEnum.WAIT_SUBMIT.getCode(), ApproveStatusEnum.WAIT_SUBMIT.getName(),"",null,"", waitSubmitCount));
        //审核中
        int approveIngCount = list.stream().filter(e -> ApproveStatusEnum.APPROVE_ING.getCode().equals(e.getApproveStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(ApproveStatusEnum.APPROVE_ING.getCode(), ApproveStatusEnum.APPROVE_ING.getName(),"",null,"", approveIngCount));
        //创建中
        int approveCount = list.stream().filter(e -> ApproveStatusEnum.APPROVE.getCode().equals(e.getApproveStatus()) && CreateStatusEnum.CREATING.getCode().equals(e.getCreateStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(CreateStatusEnum.CREATING.getCode(), CreateStatusEnum.CREATING.getName(),"",null,"", approveCount));
        //创建成功
        int successCount = list.stream().filter(e -> ApproveStatusEnum.APPROVE.getCode().equals(e.getApproveStatus()) && CreateStatusEnum.SUCCESS.getCode().equals(e.getCreateStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(CreateStatusEnum.SUCCESS.getCode(), CreateStatusEnum.SUCCESS.getName(),"",null,"", successCount));
        //审核不通过
        int rejectCount = list.stream().filter(e -> ApproveStatusEnum.REJECT.getCode().equals(e.getApproveStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(ApproveStatusEnum.REJECT.getCode(), ApproveStatusEnum.REJECT.getName(),"",null,"", rejectCount));
        //已作废
        int voidedCount = list.stream().filter(e -> InvalidStatusEnum.VOIDED.getStatus().equals(e.getInvalidStatus())).mapToInt(SoMultiChannelDTO.TabListDTO::getCount).sum();
        list1.add(new SoMultiChannelDTO.TabListDTO(InvalidStatusEnum.VOIDED.getStatus().toString(), InvalidStatusEnum.VOIDED.getName(),"",null,"", voidedCount));
        return list1;
    }

    @Override
    public void exportList(SoMultiChannelDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("多渠道订单导出", EXPORT_SO_MULTI_CHANNEL.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SoMultiChannelEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到多渠道订单主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改多渠道订单主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        //启动流程（如果需要的话）
        log.info("提交 开始启动多渠道订单主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录多渠道订单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getDeliveryCode(), "多渠道订单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SoMultiChannelDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SoMultiChannelDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SoMultiChannelEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getDeliveryCode(), "多渠道订单主单", approveType.getName(), dto.getComment());
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(SoMultiChannelEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        //此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SoMultiChannelEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到多渠道订单主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        if (!CreateStatusEnum.CREATING.getCode().equals(entity.getCreateStatus())){
            dmpSyncFeign.batchNoNeedSyncBySourceCode(new BaseIdsDTO.SourceCodeDTO(Collections.singletonList(entity.getCode()), "反审核取消同步"));
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getDeliveryCode(), "多渠道订单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SoMultiChannelEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        //只允许创建失败允许反审核
        if (!(Objects.equals(entity.getCreateStatus(), CreateStatusEnum.FAILED.getCode()) || Objects.equals(entity.getCreateStatus(), CreateStatusEnum.CANCEL.getCode()))) {
            throw new ServiceException("只允许创建失败和取消订单支持反审核");
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SoMultiChannelEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到多渠道订单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_DELETE);
        }
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())){
            throw new ServiceException("已作废订单不允许删除");
        }
        //进行发货拦截
//        deliveryIntercept(entity, true, false, "多渠道订单删除");
        // 删除主单数据
        log.info("删除 开始删除多渠道订单主单主单数据，id：【{}】", id);
        super.removeById(id);
        //删除明细数据（如果有明细数据的话）
        soMultiChannelDetailService.removeByMainId(id);
        // 删除日志数据
        log.info("删除 开始删除多渠道订单主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getDeliveryCode(), "多渠道订单主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getDeliveryCode(), "删除多渠道订单主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        SoMultiChannelEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到多渠道订单主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改多渠道订单主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getDeliveryCode(), "多渠道订单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SoMultiChannelEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus(), dto.getComment());
        if (dto.getType().equals(ApproveType.PASS)) {
            // 创建亚马逊订单
            sendPusTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_ADD.getCode());
            //更新订单创建状态
            this.lambdaUpdate().set(SoMultiChannelEntity::getCreateStatus, CreateStatusEnum.CREATING.getCode())
                    .eq(SoMultiChannelEntity::getId, entity.getId()).update();
            //更新销售订单状态
            soB2cService.lambdaUpdate().set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode())
                    .eq(SoB2cEntity::getId, entity.getSoId()).update();
        } else if (ApproveType.REJECT.equals(dto.getType())) {
            //不通过发起拦截
            deliveryIntercept(entity, true, false, "多渠道订单审核不通过");
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendPusTask(List<SoMultiChannelEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncAmazonSoMultiChannelService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送亚马逊
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    @Override
    public List<SoMultiChannelDTO.SoViewDTO> listSoMultiChannel(List<String> ids, String deliveryWarehouseId, String shopId) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<SoMultiChannelDTO.SoViewDTO> soViewDTOS = baseMapper.listSoMultiChannelBySoId(ids);
        //已审核 且 待配货或配货中可以下推多渠道订单
        List<String> soCodeList = soViewDTOS.stream().filter(e -> !ApproveStatusEnum.APPROVE.getCode().equals(e.getApproveStatus()) || !(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode().equals(e.getBillStatus()) || SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode().equals(e.getBillStatus()))).map(SoMultiChannelDTO.SoViewDTO::getSoCode).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(soCodeList)) {
            throw new ServiceException("销售订单【" + soCodeList + "】非已审核且待配货或配货中，不能下推多渠道订单");
        }
        List<String> soCodeList2 = soViewDTOS.stream().filter(SoMultiChannelDTO.SoViewDTO::getInvalidStatus).map(SoMultiChannelDTO.SoViewDTO::getSoCode).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(soCodeList2)) {
            throw new ServiceException("销售订单【" + soCodeList2 + "】已作废，不能下推多渠道订单");
        }
        List<String> soCodeList3 = soViewDTOS.stream().filter(e -> CharSequenceUtil.isBlank(e.getSkuId())).map(SoMultiChannelDTO.SoViewDTO::getSoCode).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(soCodeList3)) {
            throw new ServiceException("销售订单【" + soCodeList3 + "】未绑定SKU，不能下推多渠道订单");
        }
        // 数据填充处理
        fillData(soViewDTOS, deliveryWarehouseId, shopId);
        return soViewDTOS;
    }

    @Override
    public SoMultiChannelDTO.AddDTO buildAddDTO(SoMultiChannelDTO.SaveDTO dto, String id, ShopInfoEntity shopInfoEntity, SoB2cEntity soB2cEntity, LogisticsChannelEntity channelEntity, SoB2cReceiverEntity receiverEntity) {
        SoMultiChannelDTO.AddDTO addDTO = SoMultiChannelConverter.INSTANCE.soB2cToAddDTO(dto, shopInfoEntity, soB2cEntity, channelEntity, receiverEntity);
        List<SoMultiChannelDTO.SoViewDTO> detailList1 = dto.getDetailList().stream().filter(e -> e.getSoId().equals(id)).collect(Collectors.toList());
        List<SoMultiChannelDetailDTO.AddDTO> detailList = SoMultiChannelConverter.INSTANCE.soB2cDetailToAddDTO(detailList1);
        addDTO.setDetailList(detailList);
        return addDTO;
    }

    @Override
    public void updateSoMultiChannel(SoMultiChannelDTO.CreateResultDTO createResultDTO) {
        if (ObjectUtil.isEmpty(createResultDTO)) {
            return;
        }
        SoMultiChannelEntity soMultiChannelEntity = this.getById(createResultDTO.getId());
        if (Objects.isNull(soMultiChannelEntity)) {
            return;
        }
        //创建成功标记销售订单发货状态
        soB2cService.lambdaUpdate()
                .set(CreateStatusEnum.SUCCESS.getCode().equals(createResultDTO.getCreateStatus()), SoB2cEntity::getMultiChannelType, SoB2cMultiChannelTypeEnum.AMAZON_DELIVERY.getCode())
                .set(CreateStatusEnum.FAILED.getCode().equals(createResultDTO.getCreateStatus()), SoB2cEntity::getMultiChannelType, SoB2cMultiChannelTypeEnum.AMAZON_FAILED.getCode())
                .eq(SoB2cEntity::getId, soMultiChannelEntity.getSoId()).update();
        //更新多渠道订单创建状态
        this.lambdaUpdate()
                .set(SoMultiChannelEntity::getCreateStatus, createResultDTO.getCreateStatus())
                .set(SoMultiChannelEntity::getSignOrderError, CharSequenceUtil.isNotBlank(createResultDTO.getMsg()) ? createResultDTO.getMsg() : "")
                .eq(SoMultiChannelEntity::getId, createResultDTO.getId()).update();
        if (CreateStatusEnum.FAILED.getCode().equals(createResultDTO.getCreateStatus())) {
            dmpSyncFeign.batchNoNeedSyncBySourceCode(new BaseIdsDTO.SourceCodeDTO(Collections.singletonList(soMultiChannelEntity.getCode()), "亚马逊订单创建失败，取消同步"));
        }

        operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新多渠道订单创建状态:【{}】", CreateStatusEnum.getName(createResultDTO.getCreateStatus())), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), createResultDTO.getId(), "多渠道订单状态");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO reCreate(SoMultiChannelEntity entity) {
        if (!(ApproveStatusEnum.APPROVE.equals(entity.getApproveStatus()) && (CreateStatusEnum.FAILED.getCode().equals(entity.getCreateStatus()) || CreateStatusEnum.WAIT.getCode().equals(entity.getCreateStatus())))) {
            return BatchResultDTO.fail(entity.getId(), entity.getDeliveryCode(), "只允许审核通过，创建失败或待创建的单据重新创建");
        }
        // 创建亚马逊订单
        sendPusTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_ADD.getCode());
        //更新订单创建状态
        this.lambdaUpdate().set(SoMultiChannelEntity::getCreateStatus, CreateStatusEnum.CREATING.getCode())
                .eq(SoMultiChannelEntity::getId, entity.getId()).update();
        //更新销售订单状态
        soB2cService.lambdaUpdate()
                .set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode())
                .set(SoB2cEntity::getMultiChannelType, SoB2cMultiChannelTypeEnum.AMAZON_APPROVING.getCode())
                .eq(SoB2cEntity::getId, entity.getSoId()).update();
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), "重新创建成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public BatchResultDTO deliveryIntercept(SoMultiChannelEntity entity, Boolean isCancel, Boolean isValidate, String remark) {
        if (Objects.isNull(entity) || CharSequenceUtil.isBlank(entity.getSoId())) {
            return BatchResultDTO.fail("", "", "销售订单不存在不进行拦截");
        }
        String logisticsChannelId = entity.getLogisticsChannelId();
        String logisticsChannelName = entity.getLogisticsChannelName();
        String deliveryCode = entity.getDeliveryCode();
        String trackNo = entity.getTrackNo();
        SoB2cEntity soB2cEntity = soB2cService.getById(entity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            return BatchResultDTO.fail("", "",  "销售订单不存在不进行拦截");
        }
        //重新查询订单信息
        entity = this.getById(entity.getId());
        if (Objects.isNull(entity)){
            addDeliveryIntercept(logisticsChannelId, logisticsChannelName, trackNo, remark, soB2cEntity, deliveryCode);
            return BatchResultDTO.success("", "", "发货拦截作废成功");
        }
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getDeliveryCode(), "订单状态已作废，不能发货拦截");
        }
        if (CreateStatusEnum.SUCCESS.getCode().equals(entity.getCreateStatus()) && isCancel) {
            AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(entity.getDeliveryShopId());
            FbaOutboundApi api = AmazonSpApiInitUtils.create(FbaOutboundApi.class, shopInfoDTO, false);
            try {
                ApiResponse<CancelFulfillmentOrderResponse> cancelFulfillmentOrderResponseApiResponse = api.cancelFulfillmentOrderWithHttpInfo(entity.getDeliveryCode());
                this.lambdaUpdate()
                        .set(SoMultiChannelEntity::getCreateStatus, CreateStatusEnum.CANCEL.getCode())
                        .set(SoMultiChannelEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT)
                        .set(SoMultiChannelEntity::getInvalidStatus, Boolean.TRUE)
                        .set(SoMultiChannelEntity::getInvalidRemark, "发货拦截作废")
                        .eq(SoMultiChannelEntity::getId, entity.getId()).update();
                entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
                entity.setInvalidStatus(Boolean.TRUE);
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("亚马逊发货拦截成功，订单号：{},接口返回：{}", entity.getDeliveryCode(), JSONObject.toJSONString(cancelFulfillmentOrderResponseApiResponse.getData())), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), entity.getSoId(), "多渠道订单发货拦截");
            } catch (ApiException | LWAException e) {
                log.error("亚马逊发货拦截异常：", e);
                return BatchResultDTO.fail(entity.getId(), entity.getDeliveryCode(), "亚马逊取消订单失败" + e.getMessage());
            }
        }
        if (!CreateStatusEnum.CREATING.getCode().equals(entity.getCreateStatus())) {
            dmpSyncFeign.batchNoNeedSyncBySourceCode(new BaseIdsDTO.SourceCodeDTO(Collections.singletonList(entity.getCode()), "反审核取消同步"));
        }
        if (ApproveStatusEnum.APPROVE_ING.equals(entity.getApproveStatus())) {
            this.cancelProcess(new ApproveDTO.CancelProcessDTO(entity.getId()));
        }
        //作废数据
        if (!entity.getInvalidStatus() && isValidate) {
            this.lambdaUpdate()
                    .set(SoMultiChannelEntity::getInvalidStatus, Boolean.TRUE)
                    .set(SoMultiChannelEntity::getInvalidRemark, "发货拦截作废")
                    .eq(SoMultiChannelEntity::getId, entity.getId()).update();
        }
        addDeliveryIntercept(logisticsChannelId, logisticsChannelName, trackNo, remark, soB2cEntity, entity.getDeliveryCode());
        return BatchResultDTO.success(entity.getId(), entity.getDeliveryCode(), "发货拦截作废成功");
    }

    private void addDeliveryIntercept(String logisticsChannelId, String logisticsChannelName, String trackNo, String remark, SoB2cEntity soB2cEntity, String deliveryCode) {
        ThirdWarehouseDeliveryEntity thirdWarehouseDelivery = null;
        if (CharSequenceUtil.isNotBlank(deliveryCode)){
            thirdWarehouseDelivery = thirdWarehouseDeliveryFeign.getByCodeAndSoId(deliveryCode, soB2cEntity.getId());
        }else {
            thirdWarehouseDelivery = thirdWarehouseDeliveryFeign.getLatestBySoId(soB2cEntity.getId());
        }
        //发货单标记取消发货
        if (Objects.nonNull(thirdWarehouseDelivery) && !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(thirdWarehouseDelivery.getStatus())) {
            thirdWarehouseDelivery.setStatus(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode());
            thirdWarehouseDeliveryFeign.update(thirdWarehouseDelivery);
        }
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("发货拦截作废订单"), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soB2cEntity.getId(), "多渠道订单发货拦截");
        if (CharSequenceUtil.isNotBlank(soB2cEntity.getMultiChannelType())) {
            //销售订单取消多渠道标识
            soB2cService.lambdaUpdate()
                    .set(SoB2cEntity::getMultiChannelType, "")
                    .set(SoB2cEntity::getAbnormalType, SoB2cAbnormalTypeEnum.INTERCEPT_SUCCESS_REJECT.getCode())
                    .set(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode())
                    .set(SoB2cEntity::getIsIntercept, Boolean.FALSE)
                    .set(SoB2cEntity::getApproveStatus, ApproveStatusEnum.REJECT)
                    .eq(SoB2cEntity::getId, soB2cEntity.getId()).update();
        }
        //检查拦截单是否存在，不存在就新增
        List<SoB2cDeliveryInterceptEntity> soB2cDeliveryInterceptEntities = soB2cDeliveryInterceptFeign.listBySourceIds(Collections.singletonList(soB2cEntity.getId()));
        if (Objects.nonNull(soB2cEntity) && CollUtil.isEmpty(soB2cDeliveryInterceptEntities)) {
            SoB2cDeliveryInterceptDTO.AddDTO addDTO = B2cOrderConverter.INSTANCE.convertIntercept(soB2cEntity);
            addDTO.setBillType(OrderTypeEnum.B2C.getCode());
            addDTO.setRemark(remark);
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
            List<SoB2cDeliveryInterceptDetailDTO.AddDTO> detailList = B2cOrderConverter.INSTANCE.convertInterceptDetail(soB2cDetailEntityList);
            addDTO.setDetailList(detailList);
            addDTO.setLogisticsChannelId(logisticsChannelId);
            addDTO.setLogisticsChannelName(logisticsChannelName);
            addDTO.setTransportNo(trackNo);
            addDTO.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getCode());
            addDTO.setHandleResult(HandleResultEnum.SUCCESS.getCode());
            addDTO.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            addDTO.setHandleUserName(UserContext.getDefaultLoginUser().getUserName());
            addDTO.setHandleTime(LocalDateTime.now());
            soB2cDeliveryInterceptFeign.add(addDTO);
        }
    }

    @Override
    public SoMultiChannelEntity getBySoId(String soId, Boolean isContainDelete) {
        if (CharSequenceUtil.isBlank(soId)) {
            return null;
        }
        if (Boolean.TRUE.equals(isContainDelete)) {
            return baseMapper.getBySoId(soId);
        } else {
            return this.lambdaQuery().eq(SoMultiChannelEntity::getSoId, soId).eq(SoMultiChannelEntity::getInvalidStatus, Boolean.FALSE).one();
        }
    }

    @Override
    public SoMultiChannelEntity getByDeliveryCode(String deliveryCode) {
        return this.lambdaQuery().eq(SoMultiChannelEntity::getDeliveryCode, deliveryCode).eq(SoMultiChannelEntity::getInvalidStatus, Boolean.FALSE).one();
    }

    @Override
    public List<SoMultiChannelEntity> queryMultiChannelDeliveryStatus() {
        return baseMapper.queryMultiChannelDeliveryStatus();
    }

    @Override
    public SoOutstockDTO.GenerateB2cDTO getSoOutstockGenerateB2cDTO(String deliveryCode) {
        List<SoMultiChannelEntity> list = this.lambdaQuery().eq(SoMultiChannelEntity::getDeliveryCode, deliveryCode).list();
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("多渠道订单不存在");
        }
        SoMultiChannelEntity soMultiChannelEntity = list.stream().filter(e -> e.getInvalidStatus().equals(Boolean.FALSE)).findFirst().orElse(null);
        if (Objects.isNull(soMultiChannelEntity)) {
            throw new ServiceException("多渠道订单已作废");
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soMultiChannelEntity.getSoId());
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException("销售订单不存在");
        }
        String shopId = soB2cEntity.getShopId();
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfoEntity)) {
            throw new ServiceException("店铺不存在");
        }
        SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverService.getByMainId(soB2cEntity.getId());
        if (Objects.isNull(soB2cReceiverEntity)) {
            throw new ServiceException("销售订单接收人不存在");
        }
        String customerId = shopInfoEntity.getCustomerId();
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(customerId);
        if (Objects.isNull(customerInfoEntity)) {
            throw new ServiceException("客户不存在");
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            throw new ServiceException("销售订单详情不存在");
        }
        //获取三方仓发货单
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = thirdWarehouseDeliveryFeign.getByCodeAndSoId(soMultiChannelEntity.getDeliveryCode(), soB2cEntity.getId());
        if (Objects.isNull(thirdWarehouseDeliveryEntity)) {
            throw new ServiceException("三方仓发货单不存在");
        }
        List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDeliveryDetailEntityList = thirdWarehouseDeliveryFeign.listByMainIds(Collections.singletonList(thirdWarehouseDeliveryEntity.getId()));
        if (CollUtil.isEmpty(thirdWarehouseDeliveryDetailEntityList)) {
            throw new ServiceException("三方仓发货单详情不存在");
        }
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO = SoMultiChannelConverter.INSTANCE.convertSoOutstockGenerateB2cDTO(soMultiChannelEntity, soB2cEntity, thirdWarehouseDeliveryEntity, soB2cDetailEntityList.get(0), customerInfoEntity, soB2cReceiverEntity);
        LinkedList<SoOutstockDetailDTO.AddDTO> detailList = new LinkedList<>();
        thirdWarehouseDeliveryDetailEntityList.forEach(thirdWarehouseDeliveryDetailEntity -> {
            SoB2cDetailEntity detailEntity = soB2cDetailEntityList.stream().filter(e -> e.getId().equals(thirdWarehouseDeliveryDetailEntity.getSoDetailId())).findFirst().orElse(null);
            SoOutstockDetailDTO.AddDTO addDTO = SoMultiChannelConverter.INSTANCE.convertSoOutstockGenerateB2cDetailDTO(thirdWarehouseDeliveryDetailEntity, detailEntity);
            detailList.add(addDTO);
        });
        generateB2cDTO.setDetailList(detailList);
        return generateB2cDTO;
    }

    @Override
    public void updateSoOutstock(PlatformSoOutStockDTO dto) {
        String deliveryCode = dto.getMerchantOrderId();
        List<PlatformSoOutStockDetailDTO> detailList = dto.getDetailList();

        SoMultiChannelEntity soMultiChannelEntity = this.getByDeliveryCode(deliveryCode);
        if (Objects.isNull(soMultiChannelEntity)) {
            return;
        }
        List<SoMultiChannelDetailEntity> soMultiChannelDetailEntityList = soMultiChannelDetailService.listByMainIds(Collections.singletonList(soMultiChannelEntity.getId()));
        if (CollUtil.isEmpty(soMultiChannelDetailEntityList)) {
            return;
        }
        soMultiChannelDetailEntityList.forEach(soMultiChannelDetailEntity -> {
            PlatformSoOutStockDetailDTO platformSoOutStockDetailDTO = detailList.stream().filter(e -> soMultiChannelDetailEntity.getSoDetailId().equals(e.getMerchantOrderItemId())).findFirst().orElse(null);
            if (Objects.nonNull(platformSoOutStockDetailDTO)) {
                Integer qtyShipped = platformSoOutStockDetailDTO.getQtyShipped();
                soMultiChannelDetailEntity.setHasOutstockQty(qtyShipped + soMultiChannelDetailEntity.getHasOutstockQty());
                if (soMultiChannelDetailEntity.getHasOutstockQty() >= soMultiChannelDetailEntity.getDeliveryQty()) {
                    soMultiChannelDetailEntity.setOutstockStatus(OutstockStatusEnum.ALL.getCode());
                } else if (soMultiChannelDetailEntity.getHasOutstockQty() > 0) {
                    soMultiChannelDetailEntity.setOutstockStatus(OutstockStatusEnum.PART.getCode());
                } else {
                    soMultiChannelDetailEntity.setOutstockStatus(OutstockStatusEnum.NONE.getCode());
                }
                soMultiChannelDetailService.updateById(soMultiChannelDetailEntity);
            }
        });
    }

    @Override
    public void updateSoMultiChannelStatus(PlatformFulfillOrderDTO bean, SoMultiChannelEntity soMultiChannelEntity) {
        SoMultiChannelEntity old = this.getById(soMultiChannelEntity.getId());
        soMultiChannelEntity.setDeliveryTime(bean.getDeliveryTime());
        soMultiChannelEntity.setTrackNo(bean.getTrackNo());
        LogisticsChannelEntity logisticsChannelEntity = null;
        if (CharSequenceUtil.isNotBlank(bean.getChannelCode())) {
            soMultiChannelEntity.setPlatformChannelCode(bean.getChannelCode());
            List<LogisticsChannelEntity> channelEntityList = logisticsFeign.getChannelByCode(bean.getChannelCode());
            if (CollUtil.isNotEmpty(channelEntityList)){
                logisticsChannelEntity = channelEntityList.get(0);
                soMultiChannelEntity.setLogisticsChannelId(logisticsChannelEntity.getId());
                soMultiChannelEntity.setLogisticsChannelName(logisticsChannelEntity.getName());
                //更新销售订单
            }
        }
        soMultiChannelEntity.setBillStatus(CharSequenceUtil.isNotBlank(bean.getOrderStatus()) ? bean.getOrderStatus() : "");
        soMultiChannelEntity.setDeliveryStatus(CharSequenceUtil.isNotBlank(bean.getDeliveryStatus()) ? bean.getDeliveryStatus() : "");
        soMultiChannelEntity.setShipmentCode(bean.getShipmentId());
        if ("CANCELLED".equalsIgnoreCase(bean.getOrderStatus()) || "CANCELLED_BY_FULFILLER".equalsIgnoreCase(bean.getDeliveryStatus()) || "CANCELLED_BY_SELLER".equalsIgnoreCase(bean.getDeliveryStatus())) {
            soMultiChannelEntity.setCreateStatus(CreateStatusEnum.CANCEL.getCode());
        }
        this.updateById(soMultiChannelEntity);
        operateLogService.addModuleOperateLogByObj(old, soMultiChannelEntity, ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelEntity.getId(), "状态同步");
        if (CharSequenceUtil.isNotBlank(bean.getTrackNo()) && CharSequenceUtil.isNotBlank(soMultiChannelEntity.getSoId())) {
            //更新销售订单物流跟踪号 和三方仓发货单跟踪号
            soB2cLogisticsService.lambdaUpdate()
                    .set(SoB2cLogisticsEntity::getTrackNo, bean.getTrackNo())
                    .set(SoB2cLogisticsEntity::getCode, bean.getTrackNo())
                    .set(Objects.nonNull(bean.getDeliveryTime()), SoB2cLogisticsEntity::getDeliveryTime, bean.getDeliveryTime())
                    .set(Objects.nonNull(logisticsChannelEntity), SoB2cLogisticsEntity::getLogisticsChannelId, logisticsChannelEntity.getId())
                    .set(Objects.nonNull(logisticsChannelEntity), SoB2cLogisticsEntity::getLogisticsChannelName, logisticsChannelEntity.getName())
                    .eq(SoB2cLogisticsEntity::getMainId, soMultiChannelEntity.getSoId())
                    .update();
            String msg = "";
            if (Objects.nonNull(logisticsChannelEntity)){
                msg = CharSequenceUtil.format("多渠道订单物流更新从【{}】为【{}】", old.getLogisticsChannelName(), logisticsChannelEntity.getName());
            }
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新物流单信息跟踪号【{}】运单号【{}】发货时间【{}】", bean.getTrackNo(), bean.getTrackNo(), bean.getDeliveryTime()) + msg, ModuleTypeEnum.SO_B2C.getCode(), soMultiChannelEntity.getSoId(), "多渠道订单信息同步");
        }
    }

    @Override
    public void updateSoMultiOutstockQty(List<SoMultiChannelDetailDTO.OutstockQtyDTO> outstockQtyDTOList) {
        if (CollUtil.isEmpty(outstockQtyDTOList)) {
            return;
        }
        List<String> deliveryCodeList = outstockQtyDTOList.stream().map(SoMultiChannelDetailDTO.OutstockQtyDTO::getDeliveryCode).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<SoMultiChannelEntity> list = this.lambdaQuery().in(SoMultiChannelEntity::getDeliveryCode, deliveryCodeList).list();
        List<String> ids = list.stream().map(SoMultiChannelEntity::getId).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<SoMultiChannelDetailEntity> soMultiChannelDetailEntities = soMultiChannelDetailService.listByMainIds(ids);
        if (CollUtil.isEmpty(soMultiChannelDetailEntities)) {
            return;
        }
        for (SoMultiChannelDetailEntity soMultiChannelDetailEntity : soMultiChannelDetailEntities) {
            int sum = outstockQtyDTOList.stream().filter(e -> e.getSoDetailId().equals(soMultiChannelDetailEntity.getSoDetailId())).mapToInt(SoMultiChannelDetailDTO.OutstockQtyDTO::getOutstockQty).sum();
            if (sum > 0) {
                int hasOutstockQty = Math.max(soMultiChannelDetailEntity.getHasOutstockQty() - sum, 0);
                String outstockStatus;
                if (hasOutstockQty >= soMultiChannelDetailEntity.getDeliveryQty()) {
                    outstockStatus = OutstockStatusEnum.ALL.getCode();
                } else if (hasOutstockQty > 0) {
                    outstockStatus = OutstockStatusEnum.PART.getCode();
                } else {
                    outstockStatus = OutstockStatusEnum.NONE.getCode();
                }
                soMultiChannelDetailService.lambdaUpdate()
                        .set(SoMultiChannelDetailEntity::getHasOutstockQty, hasOutstockQty)
                        .set(SoMultiChannelDetailEntity::getOutstockStatus, outstockStatus)
                        .eq(SoMultiChannelDetailEntity::getId, soMultiChannelDetailEntity.getId())
                        .update();
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("更新多渠道订单明细信息出库数量【{}】出库状态【{}】", hasOutstockQty, OutstockStatusEnum.getName(outstockStatus)), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), soMultiChannelDetailEntity.getMainId(), "销售出库单删除");
            }
        }
    }

    @Override
    public SoB2cDTO.SaveSoB2cDistributionDTO buildDistributionDTO(SoMultiChannelDTO.SaveDTO dto) {
        SoB2cDTO.SaveSoB2cDistributionDTO saveSoB2cDistributionDTO = new SoB2cDTO.SaveSoB2cDistributionDTO();
        List<String> soIds = dto.getDetailList().stream().map(SoMultiChannelDTO.SoViewDTO::getSoId).distinct().collect(Collectors.toList());
        saveSoB2cDistributionDTO.setIds(soIds);
        saveSoB2cDistributionDTO.setIsCover(Boolean.TRUE);
        List<SoB2cDTO.SaveSoB2cDistributionDetailDTO> detailList = new ArrayList<>();
        for (SoMultiChannelDTO.SoViewDTO soViewDTO : dto.getDetailList()) {
            SoB2cDTO.SaveSoB2cDistributionDetailDTO saveSoB2cDistributionDetailDTO = new SoB2cDTO.SaveSoB2cDistributionDetailDTO();
            saveSoB2cDistributionDetailDTO.setId(soViewDTO.getSoId());
            saveSoB2cDistributionDetailDTO.setDetailId(soViewDTO.getSoDetailId());
            saveSoB2cDistributionDetailDTO.setLogisticsChannelId(dto.getLogisticsChannelId());
            saveSoB2cDistributionDetailDTO.setWarehouseId(dto.getDeliveryWarehouseId());
            detailList.add(saveSoB2cDistributionDetailDTO);
        }
        saveSoB2cDistributionDTO.setDetailList(detailList);
        return saveSoB2cDistributionDTO;
    }

    @Override
    public List<SoMultiChannelEntity> getLastBySoId(List<String> soIds, String createStatus) {
        if (CollUtil.isEmpty(soIds) || CharSequenceUtil.isBlank(createStatus)) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<SoMultiChannelEntity>()
                        .select(SoMultiChannelEntity::getId, SoMultiChannelEntity::getSoId, SoMultiChannelEntity::getCreateStatus,SoMultiChannelEntity::getSignOrderError, SoMultiChannelEntity::getCreateTime)
                .in(SoMultiChannelEntity::getSoId, soIds)
                .eq(SoMultiChannelEntity::getCreateStatus, createStatus)
                .orderByDesc(SoMultiChannelEntity::getCreateTime));
    }

    private void fillData(List<SoMultiChannelDTO.SoViewDTO> soViewDTOS, String deliveryWarehouseId, String shopId) {
        if (CollUtil.isEmpty(soViewDTOS)) {
            return;
        }
        //平台信息
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());
        List<String> skuIds = soViewDTOS.stream().map(SoMultiChannelDTO.SoViewDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> skuNos = soViewDTOS.stream().map(SoMultiChannelDTO.SoViewDTO::getSkuNo).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        // 商品信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        //获取FBA可售库存
        List<FbaInventoryDTO.InventoryDTO> inventoryDTOList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(deliveryWarehouseId) && CharSequenceUtil.isNotBlank(shopId)) {
            FbaInventoryDTO.QueryDTO queryDTO = new FbaInventoryDTO.QueryDTO();
            queryDTO.setWarehouseIds(Collections.singletonList(deliveryWarehouseId));
            queryDTO.setShopIds(Collections.singletonList(shopId));
            queryDTO.setSkuNos(skuNos);
            inventoryDTOList = wmsFbaInventoryFeign.listFbaInventory(queryDTO);
        }
        List<FbaInventoryDTO.InventoryDTO> finalInventoryDTOList = inventoryDTOList;
        soViewDTOS.forEach(soViewDTO -> {
            //产品名称
            skuVOS.stream().filter(obj -> obj.getSkuId().equals(soViewDTO.getSkuId())).findFirst().ifPresent(f -> {
                soViewDTO.setProductName(f.getSkuName());
            });
            //平台名称
            dictList.stream().filter(obj -> obj.getValue().equals(soViewDTO.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).ifPresent(soViewDTO::setDictPlatformName);
            //FBA库存 获取最大库存记录
            finalInventoryDTOList.stream().filter(obj -> obj.getSkuNo().equals(soViewDTO.getSkuNo()) && CharSequenceUtil.isNotBlank(deliveryWarehouseId) && obj.getWarehouseId().equals(deliveryWarehouseId))
                    .max(Comparator.comparing(FbaInventoryDTO.InventoryDTO::getFulfillableQty)).ifPresent(f -> {
                        soViewDTO.setFulfillableQty(f.getFulfillableQty());
                        soViewDTO.setFnSku(f.getFnSku());
                        soViewDTO.setPlatformSpuNo(f.getAsin());
                        soViewDTO.setPlatformProductName(f.getPlatformProductName());
                        soViewDTO.setPlatformSkuNo(f.getMsku());
                        soViewDTO.setFbaInventoryId(f.getId());
                    });
        });

    }

    @Override
    public SoMultiChannelDTO.ViewDTO view(String id) {
        SoMultiChannelEntity soMultiChannelEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到多渠道订单主单数据"));
        SoMultiChannelDTO.ViewDTO data = BeanMapperUtils.map(SoMultiChannelDTO.ViewDTO.class, soMultiChannelEntity);
        data.setApproveStatus(soMultiChannelEntity.getApproveStatus().getStatus());
        data.setApproveStatusName(soMultiChannelEntity.getApproveStatus().getName());
        // 数据填充处理
        fillOne(data);
        // 查询明细数据（如果有的话）
        List<SoMultiChannelDetailEntity> detailList = soMultiChannelDetailService.listByMainIds(Collections.singletonList(id));
        List<SoMultiChannelDetailDTO.ViewDTO> detailList1 = BeanMapperUtils.copyList(SoMultiChannelDetailDTO.ViewDTO.class, detailList);
        data.setDetailList(detailList1);
        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(SoMultiChannelEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_MULTI_CHANNEL.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private Map<String, Object> getVariablesMap(SoMultiChannelEntity entity) {
        Map<String, Object> map = BeanUtil.beanToMap(entity);
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(entity.getDeliveryShopId());
        //发货店铺负责人
        map.put("shopChargeId", shopInfoEntity.getChargeId());
        map.put("shopChargeName", shopInfoEntity.getChargeName());
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SO_MULTI_CHANNEL.getCode());
        dto.setVariablesMap(map);
        return cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
    }

    private void fillOne(SoMultiChannelDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        // 属性赋值
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        data.setCreateStatusName(CreateStatusEnum.getName(data.getCreateStatus()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        data.setBillStatusName(data.getBillStatus());
        data.setDeliveryStatusName(data.getDeliveryStatus());
        //平台类型名称
        String dictPlatformName = dictList.stream().filter(obj -> obj.getValue().equals(data.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        data.setDictPlatformName(dictPlatformName);
        String deliveryPlatformName = dictList.stream().filter(obj -> obj.getValue().equals(data.getDeliveryPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        data.setDeliveryPlatformName(deliveryPlatformName);
        if (CharSequenceUtil.isNotBlank(data.getCountry())){
            List<DictCountryEntity> dictCountryEntities = sysDictFeign.listCountryByIds(Collections.singletonList(data.getCountry()));
            if (CollectionUtil.isNotEmpty(dictCountryEntities)){
                data.setCountryName(dictCountryEntities.get(0).getNameCn());
            }
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     * @param comment
     */
    public void updateForApprove(String id, String approveStatus, String comment) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SoMultiChannelEntity::getId, id)
                .set(SoMultiChannelEntity::getApproveUserId, userInfo.getUid())
                .set(SoMultiChannelEntity::getApproveUserName, userInfo.getUserName())
                .set(SoMultiChannelEntity::getApproveStatus, approveStatus)
                .set(SoMultiChannelEntity::getApproveTime, LocalDateTime.now())
                .update(new SoMultiChannelEntity());
        //记录审核状态变更
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("订单审核操作【{}】备注【{}】", ApproveStatusEnum.getName(approveStatus), CharSequenceUtil.isNotBlank(comment) ? comment : ""), ModuleTypeEnum.SO_MULTI_CHANNEL.getCode(), id, "审核操作");
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SoMultiChannelEntity::getId, id)
                .set(SoMultiChannelEntity::getApproveUserId, "")
                .set(SoMultiChannelEntity::getApproveUserName, "")
                .set(SoMultiChannelEntity::getApproveStatus, approveStatus)
                .set(SoMultiChannelEntity::getApproveTime, null)
                .update(new SoMultiChannelEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SoMultiChannelEntity::getId, id)
                .set(SoMultiChannelEntity::getApproveStatus, approveStatus)
                .update(new SoMultiChannelEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SoMultiChannelDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        List<String> ids = list.stream().map(SoMultiChannelDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<ProcessTaskManagementEntity> processTaskManagementEntities = workflowFeign.listProcessByBusinessId(ids);
        // 属性赋值
        for (SoMultiChannelDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setCreateStatusName(CreateStatusEnum.getName(data.getCreateStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setBillStatusName(data.getBillStatus());
            data.setDeliveryStatusName(data.getDeliveryStatus());
            data.setOutstockStatusName(OutstockStatusEnum.getName(data.getOutstockStatus()));
            //平台类型名称
            String dictPlatformName = dictList.stream().filter(obj -> obj.getValue().equals(data.getDictPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setDictPlatformName(dictPlatformName);
            String deliveryPlatformName = dictList.stream().filter(obj -> obj.getValue().equals(data.getDeliveryPlatform())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            data.setDeliveryPlatformName(deliveryPlatformName);
            List<String> curApproveName = processTaskManagementEntities.stream().filter(req -> req.getBusinessId().equals(data.getId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveName).distinct().collect(Collectors.toList());
            String userName = StringUtils.join(curApproveName, ",");
            data.setApproveUserName(userName);
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(SoMultiChannelEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException("只有待提交并且未作废数据支持提交");
        }
        return;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(SoMultiChannelEntity soMultiChannelEntity) {
        if (CharSequenceUtil.isBlank(soMultiChannelEntity.getShopName())) {
            ShopInfoEntity shopInfoEntity = shopInfoService.getById(soMultiChannelEntity.getShopId());
            if (ObjectUtil.isEmpty(shopInfoEntity)) {
                throw new ServiceException("未找到店铺信息");
            }
            soMultiChannelEntity.setShopName(shopInfoEntity.getName());
            soMultiChannelEntity.setDeliveryPlatform(shopInfoEntity.getDictPlatform());
            soMultiChannelEntity.setDeliveryWarehouseId(shopInfoEntity.getWarehouseId());
            soMultiChannelEntity.setDeliveryWarehouseName(shopInfoEntity.getWarehouseName());
        }
        LogisticsChannelEntity logisticsChannelEntity = FeignQuery.getById(LogisticsChannelEntity.class, soMultiChannelEntity.getLogisticsChannelId());
        if (ObjectUtil.isEmpty(logisticsChannelEntity)) {
            throw new ServiceException("未找到物流渠道信息");
        }
        //校验物流渠道编码 防止配错
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey("multiChannelLogiticsCode");
        List<String> codeList = dtoList.stream().map(DictBasicDTO.ViewDTO::getValue).collect(Collectors.toList());
        if (!codeList.contains(logisticsChannelEntity.getCode())) {
            throw new ServiceException("物流渠道【{}】不支持创建多渠道订单", logisticsChannelEntity.getCode());
        }
        if (CharSequenceUtil.isBlank(soMultiChannelEntity.getLogisticsChannelName())) {
            soMultiChannelEntity.setLogisticsChannelName(logisticsChannelEntity.getName());
        }
        if (CharSequenceUtil.isBlank(soMultiChannelEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soMultiChannelEntity.getPlatformCode())) {
            SoB2cEntity entity = soB2cService.getById(soMultiChannelEntity.getSoId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException("未找到订单信息");
            }
            soMultiChannelEntity.setDictPlatform(entity.getDictPlatform());
            soMultiChannelEntity.setSoCode(entity.getCode());
            soMultiChannelEntity.setPlatformCode(entity.getPlatformCode());
        }
        if (CharSequenceUtil.isBlank(soMultiChannelEntity.getCreateStatus())) {
            soMultiChannelEntity.setCreateStatus(CreateStatusEnum.WAIT.getCode());
        }
        if (Objects.isNull(soMultiChannelEntity.getApproveStatus())) {
            soMultiChannelEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        }
        if (Objects.isNull(soMultiChannelEntity.getInvalidStatus())) {
            soMultiChannelEntity.setInvalidStatus(InvalidStatusEnum.NOT_VOIDED.getStatus());
        }
    }
}
