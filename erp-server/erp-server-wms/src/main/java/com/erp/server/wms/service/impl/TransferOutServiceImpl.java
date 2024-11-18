package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.TransferDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.model.wms.entity.TransferInEntity;
import com.erp.model.wms.entity.TransferOutDetailEntity;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.TransferDirectionEnum;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.model.wms.enums.TransitOwnerEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.TransferOutMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_TRANSFER_OUT;

/**
 * <p>
 * 分布式调出单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferOutServiceImpl extends SuperServiceImpl<TransferOutMapper, TransferOutEntity> implements TransferOutService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private TransferOutDetailService transferOutDetailService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private TransferInService transferInService;

    @Resource
    private TransferInDetailService transferInDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public List<TransferOutEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery()
                .in(TransferOutEntity::getSourceId,ids)
                .eq(TransferOutEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(TransferOutDTO.AddDTO addDTO) {
        // 验证数据
        ValidatorUtil.validateEntity(addDTO);
        // 调入仓库和调出仓库不能一样
        ValidatorUtil.isTrue(!Objects.equals(addDTO.getInWarehouseId(), addDTO.getOutWarehouseId()),()->new ServiceException("分布式调出单调入仓库和调出仓库不能一样"));

        TransferOutEntity transferOutEntity = new TransferOutEntity();
        BeanMapperUtils.copy(addDTO, transferOutEntity);
        handleData(transferOutEntity);
        log.info("开始新增分步式调出单主单");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDC, BusinessNoTypeEnum.CODE_FBDC.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FBDC);
        transferOutEntity.setCode(code);
        boolean save = super.save(transferOutEntity);
        ValidatorUtil.isTrue(save,()->new ServiceException("分步式调出单保存失败"));
        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增了一个分步式调出单【%s】", code), ModuleTypeEnum.TRANSFER_OUT.getCode(), transferOutEntity.getId(), "新增操作");
        //新增明细
        transferOutDetailService.add(addDTO.getDetailList(), transferOutEntity.getId());
        return transferOutEntity.getId();
    }

    @Override
    public PagingVO<TransferOutDTO.PagingViewDTO> paging(PagingDTO<TransferOutDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TransferOutDTO.PagingViewDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        List<TransferOutDTO.PagingViewDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        // 数据填充处理
        filling(records);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(TransferOutDTO.ExportDTO param) {
        downloadTaskFeign.saveDownloadTask("分布式调出订单", EXPORT_WMS_TRANSFER_OUT.getCode(), param);
    }

    @Override
    public List<TransferOutDTO.TabListDTO> listCount(PermissionsDTO param) {
        TransferOutDTO.PagingParamDTO searchParam = new TransferOutDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ApproveStatusQtyDTO> statusList = this.baseMapper.listCount(searchParam);
        // 根据状态转换成map
        Map<String,ApproveStatusQtyDTO> statusMap = statusList.stream().collect(Collectors.toMap(ApproveStatusQtyDTO::getApproveStatus, Function.identity()));
        // 只返回待审核、已审核、审核不通过的数据
        List<TransferOutDTO.TabListDTO> resultList = Lists.newArrayListWithExpectedSize(3);
        Map<PageListTypeEnum, List<ApproveStatusEnum>> pageApproveStatusMap = Maps.newHashMap();
        pageApproveStatusMap.put(PageListTypeEnum.WAIT_SUBMIT,  Lists.newArrayList(ApproveStatusEnum.WAIT_SUBMIT));
        pageApproveStatusMap.put(PageListTypeEnum.TO_BE_APPROVE,  Lists.newArrayList(ApproveStatusEnum.APPROVE_ING));
        pageApproveStatusMap.put(PageListTypeEnum.APPROVE,  Lists.newArrayList(ApproveStatusEnum.APPROVE));
        pageApproveStatusMap.put(PageListTypeEnum.REJECT,  Lists.newArrayList(ApproveStatusEnum.REJECT));
        Arrays.stream(PageListTypeEnum.values()).forEach(pageListTypeEnum -> {
            // 获取对应的业务单据状态
            List<ApproveStatusEnum> approveStatusEnumList = pageApproveStatusMap.get(pageListTypeEnum);
            if(Objects.nonNull(approveStatusEnumList)) {
                Integer statusQty = approveStatusEnumList.stream().mapToInt(approveStatus-> {
                    return statusMap.getOrDefault(approveStatus.getStatus(), new ApproveStatusQtyDTO()).getCount();
                }).sum();
                TransferOutDTO.TabListDTO tab = new TransferOutDTO.TabListDTO(pageListTypeEnum.getCode(),pageListTypeEnum.getName(), statusQty);
                resultList.add(tab);
            }
        });
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(TransferOutDTO.UpdateDTO updateDTO) {
        TransferOutEntity originTransferOutEntity = super.getById(updateDTO.getId());
        if (Objects.isNull(originTransferOutEntity)){
            throw new ServiceException("未找到分步式调出单");
        }
        // 调入仓库和调出仓库不能一样
        ValidatorUtil.isTrue(!Objects.equals(updateDTO.getInWarehouseId(), updateDTO.getOutWarehouseId()),()->new ServiceException("分布式调出单调入仓库和调出仓库不能一样"));
        ValidatorUtil.isTrue((Objects.equals(originTransferOutEntity.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || Objects.equals(originTransferOutEntity.getApproveStatus(), ApproveStatusEnum.REJECT.getStatus()) )
                        && Objects.equals(originTransferOutEntity.getInvalidStatus(),Boolean.FALSE),
                ()->new ServiceException("只有待提交或审核不通过并且未作废数据支持修改"));

        TransferOutEntity nowTransferOutEntity =  BeanMapperUtils.map(TransferOutEntity.class, updateDTO);

        handleData(nowTransferOutEntity);
        log.info("编辑 开始修改分步式调出单数据，单号：【{}】", originTransferOutEntity.getCode());
        boolean save = super.updateById(nowTransferOutEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("分步式调出单保存失败"));

        // 修改明细数据（包含增删改）
        log.info("编辑 开始修改分步式调出单明细数据，单号：【{}】", originTransferOutEntity.getCode());
        transferOutDetailService.update(updateDTO.getDetailList(), nowTransferOutEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录分步式调出单日志数据，单号：【{}】", originTransferOutEntity.getCode());
        operateLogService.addModuleOperateLogByObj(originTransferOutEntity, nowTransferOutEntity, ModuleTypeEnum.TRANSFER_OUT.getCode(), nowTransferOutEntity.getId(), "", "");
    }

    @Override
    public TransferOutDTO.ViewDTO view(String id) {
        TransferOutEntity transferOutEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到分步式调出单数据"));
        TransferOutDTO.ViewDTO data = BeanMapperUtils.map(TransferOutDTO.ViewDTO.class, transferOutEntity);
        List<TransferOutDetailEntity> members = transferOutDetailService.listByMainId(id);
        List<TransferOutDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferOutDetailDTO.ViewDTO.class, members);
        this.fillingView(data, viewDetailList);
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
        // 判断id是否正确
        List<TransferOutEntity> list = super.listByIds(ids);
        Map<String, TransferOutEntity> transferOutEntityMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        // 能查询到的数据id集合
        List<String> findIds = list.stream().map(TransferOutEntity::getId).distinct().collect(Collectors.toList());
        for(String id : ids) {
            ValidatorUtil.isTrue(findIds.contains(id),()->new ServiceException("分步式调出单数据不存在"));
            TransferOutEntity transferOutEntity = transferOutEntityMap.get(id);
            //待提交或审核不通过并且未作废允许提交
            if((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(transferOutEntity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(transferOutEntity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(transferOutEntity.getInvalidStatus())) {
                throw new ServiceException("只有待提交或审核不通过并且未作废数据支持提交");
            }
        }
        // 更新单据审核状态
        log.info("提交 开始修改分步式调出状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        this.updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        // TODO 启动流程

        // 记录操作日志
        log.info("提交 开始记录分步式调出日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个分步式调出单【%s】", ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "提交操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(TransferOutDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(Collections.singletonList(dto.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(BaseApproveParamDTO baseApproveParamDTO,TransferOutEntity entity) {
        List<String> ids = Collections.singletonList(entity.getId());// 提交审核的单据id
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(baseApproveParamDTO.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(baseApproveParamDTO.getComment())) {
            throw new ServiceException("审核不通过请填写审核意见");
        }
        List<TransferOutEntity> list = Collections.singletonList(entity);
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(list),()->new ServiceException("未找到分步式调出单数据"));
        Map<String, TransferOutEntity> transferOutEntityMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        // 只有审核中的数据允许审核
        ids.stream().forEach(id->{
            ValidatorUtil.isTrue(transferOutEntityMap.containsKey(id),()->new ServiceException("分步式调出单数据不存在"));
            ValidatorUtil.isTrue(Objects.equals(transferOutEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()),()->new ServiceException("只有审核中数据支持审核"));
        });
        String hisStatusName = ApproveStatusEnum.APPROVE_ING.getName(); // 原单据审核状态
        ApproveStatusEnum approveStatus = Objects.equals(ApproveTypeEnum.PASS, approveType) ? ApproveStatusEnum.APPROVE : ApproveStatusEnum.REJECT; // 新审核状态
        String content = "";
        if(Objects.equals(ApproveTypeEnum.PASS, approveType)) { // 审核通过
            content = CharSequenceUtil.format("状态由【{}】变更为【{}】, 意见：{}", hisStatusName, approveStatus.getName(), baseApproveParamDTO.getComment());
            // TODO 审核通过流程
            this.updateInventoryTransCore(list);
        } else if (Objects.equals(ApproveTypeEnum.REJECT, approveType)) { // 审核不通过
            content = CharSequenceUtil.format("状态由【{}】变更为【{}】, 不通过原因：{}", hisStatusName, approveStatus.getName(), baseApproveParamDTO.getComment());
            // TODO 中止当前审批流程
        }
        log.info("审核 开始修改分步式调出单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        updateForApprove(ids, approveStatus.getStatus()); // 修改单据状态

        //操作日志
        log.info("审核 开始修改分步式调出单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().
                map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(content, ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "状态变更");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
        List<TransferOutEntity> list = super.listByIds(ids);
        Map<String, TransferOutEntity> transferOutEntityMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        //只有待提交的数据允许删除
        ids.stream().forEach(id->{
            ValidatorUtil.isTrue(transferOutEntityMap.containsKey(id),()->new ServiceException("分步式调出单数据不存在"));
            TransferOutEntity transferOutEntity = transferOutEntityMap.get(id);
            ValidatorUtil.isTrue(Objects.equals(transferOutEntity.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) && Objects.equals(transferOutEntity.getInvalidStatus(), Boolean.FALSE),()->new ServiceException("只有待提交并且未作废数据支持删除"));
        });
        // 删除日志数据
        log.info("删除 开始删除分步式调出单日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的分步式调出单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(TransferOutEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "删除操作");
        // 删除明细数据
        log.info("删除 开始删除分步式调出单明细数据，id集合：【{}】", JSONObject.toJSONString(ids));
        transferOutDetailService.removeByMainIds(ids);

        // 删除主单数据
        log.info("删除 开始删除分步式调出单主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
        super.removeByIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalid(List<String> ids, String remark) {
        List<TransferOutEntity> list = super.listByIds(ids);
        Map<String, TransferOutEntity> transferOutEntityMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        //只有待提交的数据允许作废
        ids.stream().forEach(id->{
            ValidatorUtil.isTrue(transferOutEntityMap.containsKey(id),()->new ServiceException("分步式调出单数据不存在"));
            TransferOutEntity transferOutEntity = transferOutEntityMap.get(id);
            ValidatorUtil.isTrue(Objects.equals(transferOutEntity.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || Objects.equals(transferOutEntity.getApproveStatus(), ApproveStatusEnum.REJECT.getStatus()),()->new ServiceException("只有待提交和审核不通过数据支持作废"));
            //已作废数据不支持作废
            ValidatorUtil.isTrue(Objects.equals(transferOutEntity.getInvalidStatus(), Boolean.FALSE),()->new ServiceException("已作废数据不支持作废"));
        });
        log.info("作废 开始修改分步式调出单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        lambdaUpdate().in(TransferOutEntity::getId, ids)
                .set(TransferOutEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(TransferOutEntity::getInvalidRemark, remark)
                .update();

        log.info("作废 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个分步式调出单【%s】，作废原因：".concat(remark), ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "作废操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancel(List<String> ids) {
        List<TransferOutEntity> list = super.listByIds(ids);
        Map<String, TransferOutEntity> transferOutEntityMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        // 只有审核中的数据允许撤销
        ids.stream().forEach(id->{
            TransferOutEntity transferOutEntity = transferOutEntityMap.get(id);
            ValidatorUtil.isTrue(Objects.nonNull(transferOutEntity),()->new ServiceException("分步式调出单数据不存在"));
            ValidatorUtil.isTrue(Objects.equals(transferOutEntity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()),()->new ServiceException("只有审核中数据支持撤销流程"));
        });
        log.info("撤销  开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));
        workflowFeign.cancelProcess(ids);

        log.info("撤销 开始修改分布式调出单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("分布式调出单【%s】取消流程", ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "取消流程操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(TransferOutEntity transferOutEntity) {
        List<String> ids = Collections.singletonList(transferOutEntity.getId());
        List<TransferOutEntity> list = Collections.singletonList(transferOutEntity);
        list.forEach(v-> ValidatorUtil.isTrue(Objects.equals(v.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()),()->new ServiceException("只有已审核数据支持反审核")));
        // 检查是否已经有下推单据
        List<TransferInEntity> transferInEntityList = transferInService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(transferInEntityList)) {
            throw new ServiceException("分步式调出单已下推分步式调入单，不支持反审核");
        }

        log.info("反审核 开始修改分步式调出单状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        ApproveStatusEnum approveStatus = ApproveStatusEnum.WAIT_SUBMIT;
        updateForDisApprove(ids, approveStatus.getStatus()); // 修改单据状态为待提交

        log.info("反审核 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个分步式调出单【%s】", ModuleTypeEnum.TRANSFER_OUT.getCode(), pairList, "反审核操作");
        // 库存交易反审核
        inventoryTransCoreService.batchUnApprove(new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_OUT, ids));
        // TODO 流程
        return BatchResultDTO.success(transferOutEntity.getId(),transferOutEntity.getCode(),"操作成功");
    }

    @Override
    public List<TransferOutDTO.ViewGenerateTransferInDTO> viewGenerateTransferIn(List<String> ids) {
        List<TransferOutDTO.ViewGenerateTransferInDTO> dataList = this.baseMapper.viewGenerateTransfer(ids);
        if(CollUtil.isEmpty(dataList)) {
            return null;
        }
        // 获取所有的sku信息
        List<String> skuIds = dataList.stream().map(TransferOutDTO.ViewGenerateTransferInDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(skuIds);
        Map<String,ProductDetailEntity> skuMap = skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity()));

        // 调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());

        // 分步式调出单明细id集合
        List<String> sourceDetailIds = dataList.stream().map(TransferOutDTO.ViewGenerateTransferInDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        // 根据分步式调出单明细id集合查询已下推的分布式调入单明细
        List<TransferInDetailEntity> transferInDetailList = transferInDetailService.listBySourceDetailIds(sourceDetailIds);
        Map<String,List<TransferInDetailEntity>> transferInDetailMap = transferInDetailList.stream().collect(Collectors.groupingBy(TransferInDetailEntity::getSourceDetailId));

        dataList.stream().forEach(data->{
            // 产品名称
            String productName = skuMap.getOrDefault(data.getSkuId(),new ProductDetailEntity()).getName();
            data.setProductName(productName);

            // 调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> Objects.equals(e.getValue(), data.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            data.setTransferDirectionName(transferDirectionName);
            // 单据来源
            data.setSourceType(SourceTypeEnum.TRANSFER_OUT.getCode());
            // 累计已下推分步式调入数量
            Integer pushedQty = 0;
            if(transferInDetailMap.containsKey(data.getSourceDetailId())) {
                pushedQty = transferInDetailMap.get(data.getSourceDetailId()).stream().map(TransferInDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            if(data.getQty() <= pushedQty) {
                data.setPlanQty(0);
            } else {
                data.setPlanQty(data.getQty() - pushedQty);
            }
        });
        return dataList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateTransferIn(ValidList<TransferOutDTO.GenerateTransferInDTO> dataList) {
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(dataList),()->new ServiceException("下推数据不能为空"));
        // 分步式调出单主单id集合
        List<String> sourceIds = dataList.stream().map(TransferOutDTO.GenerateTransferInDTO::getSourceId).distinct().collect(Collectors.toList());
        List<TransferOutEntity> transferOutList =  this.listByIds(sourceIds);
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(transferOutList),()->new ServiceException("未找到分步式调出单信息"));
        boolean noApprove = transferOutList.stream().anyMatch(r->!Objects.equals(r.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()));
        ValidatorUtil.isTrue(!noApprove,()->new ServiceException("只有已审核分步式调出单支持下推单据"));
        Map<String,TransferOutEntity> transferOutEntityMap =  transferOutList.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));

        // 分步式调出单明细id集合
        List<String> sourceDetailIds = dataList.stream().map(TransferOutDTO.GenerateTransferInDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listByIds(sourceDetailIds);
        Map<String,TransferOutDetailEntity> transferDetailMap = transferOutDetailList.stream().collect(Collectors.toMap(TransferOutDetailEntity::getId, Function.identity()));
        // 计划调入数量不能大于调出数量
        dataList.stream().forEach(data->{
            TransferOutDetailEntity transferOutDetailEntity = transferDetailMap.get(data.getSourceDetailId());
            if(data.getPlanQty() > transferOutDetailEntity.getQty()) {
                throw new ServiceException("计划调入数量不能大于调出数量");
            }
        });

        // 根据分步式调出单明细id集合查询已下推的分布式调入单明细
        List<TransferInDetailEntity> transferInDetailList = transferInDetailService.listBySourceDetailIds(sourceDetailIds);
        Map<String,List<TransferInDetailEntity>> transferInDetailMap = transferInDetailList.stream().collect(Collectors.groupingBy(TransferInDetailEntity::getSourceDetailId));

        // 一次可以下推多个分步式调出单，按调出单id分组
        Map<String,List<TransferOutDTO.GenerateTransferInDTO>> sourceMap = dataList.stream().collect(Collectors.groupingBy(TransferOutDTO.GenerateTransferInDTO::getSourceId));
        ValidList<TransferInDTO.ViewGenerateTransferInDTO> transferInList = new ValidList();
        sourceMap.forEach((sourceId,pushList)->{
            TransferOutEntity transferOutEntity = transferOutEntityMap.get(sourceId);
            // 调拨方向
            String transferDirection = transferOutEntity.getTransferDirection();
            // 调拨类型
            String transferType = transferOutEntity.getType();
            pushList.stream().forEach(pushData->{
                TransferOutDetailEntity transferOutDetailEntity = transferDetailMap.get(pushData.getSourceDetailId());
                // 累计下推的分步式调入数量不能大于调出数量
                Integer pushedQty = 0;
                if(transferInDetailMap.containsKey(pushData.getSourceDetailId())) {
                    pushedQty = transferInDetailMap.get(pushData.getSourceDetailId()).stream().map(TransferInDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                if(pushData.getPlanQty() + pushedQty > transferOutDetailEntity.getQty()) {
                    throw new ServiceException(CharSequenceUtil.format("【{}】已下推数量合计不能大于调出数量", transferOutDetailEntity.getSkuNo()));
                }
                TransferInDTO.ViewGenerateTransferInDTO transferInDTO = wrapTransferIn(transferType, transferDirection, pushData, transferOutDetailEntity);
                transferInList.add(transferInDTO);
            });
        });
        transferInService.generateTransferIn(transferInList);// 下推生成分步式调入单
    }

    @Override
    public List<TransferOutDTO.ChooseListDTO> listTransferOut(TransferOutDTO.SearchParamDTO param) {
        TransferOutEntity transferOutEntity = super.getById(param.getSourceId());
        ValidatorUtil.isTrue(Objects.nonNull(transferOutEntity),()->new ServiceException("未找到分布式调出单信息"));
        return transferOutDetailService.listChoose(param, transferOutEntity);
    }

    @Override
    public List<TransferOutEntity> findByCodes(List<String> codes) {
        return lambdaQuery().in(TransferOutEntity::getCode, codes).list();
    }

    @Override
    public PagingVO<TransferOutDTO.PagingViewDTO> exportTransferOut(PagingDTO<TransferOutDTO.ExportDTO> dto) {

        Page<TransferOutDTO.PagingViewDTO> page = this.baseMapper.exportList(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if(!CollUtil.isEmpty(page.getRecords())) {
            filling(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(TransferOutEntity::getId, ids)
                .set(TransferOutEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核更新审核状态、审核人、审核时间
     * @param ids
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForApprove(List<String> ids, String approveStatus) {
        // 当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().in(TransferOutEntity::getId, ids)
                .set(TransferOutEntity::getApproveUserId, userInfo.getUid())
                .set(TransferOutEntity::getApproveUserName, userInfo.getUserName())
                .set(TransferOutEntity::getApproveStatus, approveStatus)
                .set(TransferOutEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核更新审核状态、审核人、审核时间
     * @param ids
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(TransferOutEntity::getId, ids)
                .set(TransferOutEntity::getApproveUserId, "")
                .set(TransferOutEntity::getApproveUserName, "")
                .set(TransferOutEntity::getApproveStatus, approveStatus)
                .set(TransferOutEntity::getApproveTime, null)
                .update();
    }

    /**
     * 新增修改数据处理
     * @param transferOutEntity
     */
    private void handleData(TransferOutEntity transferOutEntity) {
        // 验证仓库信息
        WarehouseDTO.UpdateDTO warehouseIn = warehouseService.detailWithCache(transferOutEntity.getInWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseIn) && StrUtils.isNotEmpty(warehouseIn.getId()),()->new ServiceException("调入仓库未找到"));
        transferOutEntity.setInWarehouseName(warehouseIn.getName());

        WarehouseDTO.UpdateDTO warehouseOut = warehouseService.detailWithCache(transferOutEntity.getOutWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseOut) && StrUtils.isNotEmpty(warehouseOut.getId()),()->new ServiceException("调出仓库未找到"));
        transferOutEntity.setOutWarehouseName(warehouseOut.getName());

        //组织信息
        List<String> orgIds = Lists.newArrayList(warehouseIn.getOrgId(), warehouseOut.getOrgId()).stream().distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        Map<String,BaseIdDTO.CodeDTO> orgMap = accountingCompanyList.stream().collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, Function.identity()));
        transferOutEntity.setInOrgId(warehouseIn.getOrgId());
        transferOutEntity.setOutOrgId(warehouseOut.getOrgId());
        transferOutEntity.setInOrgName(orgMap.get(warehouseIn.getOrgId()).getName());
        transferOutEntity.setOutOrgName(orgMap.get(warehouseOut.getOrgId()).getName());

        //调拨类型
        if (Objects.equals(transferOutEntity.getInOrgId(), transferOutEntity.getOutOrgId()))  {
            transferOutEntity.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            transferOutEntity.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        //仓管员
        if (CharSequenceUtil.isNotBlank(transferOutEntity.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(transferOutEntity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                transferOutEntity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        // 在途归属（默认调入方）
        transferOutEntity.setTransitOwner(TransitOwnerEnum.TRANSFER_IN.getCode());
    }

    /**
     * 分页查询、导出数据处理
     * @param list
     */
    private void filling(List<TransferOutDTO.PagingViewDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(TransferOutDTO.PagingViewDTO::getSkuId).distinct().collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        Map<String,ProductDetailEntity> skuMap = productDetailList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity()));
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }
        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        for (TransferOutDTO.PagingViewDTO data : list) {
            //产品名称
            String productName = skuMap.getOrDefault(data.getSkuId(),new ProductDetailEntity()).getName();
            data.setProductName(productName);

            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> Objects.equals(e.getValue(), data.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            data.setTransferDirectionName(transferDirectionName);

            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        }
    }

    /**
     * 详情填充
     * @param data
     */
    private void fillingView(TransferOutDTO.ViewDTO data, List<TransferOutDetailDTO.ViewDTO> viewDetailList) {
        // 调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        // 调拨方向名称
        String transferDirectionName = transferDirectionList.stream().filter(e -> Objects.equals(e.getValue(), data.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
        data.setTransferDirectionName(transferDirectionName);
        // 审核状态
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        // 调拨类型
        data.setTypeName(TransferTypeEnum.getNameByCode(data.getType()));
        // 仓管员名称
        if (CharSequenceUtil.isNotBlank(data.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(data.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                data.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        // 在途归属
        data.setTransitOwnerName(TransitOwnerEnum.getNameByCode(data.getTransitOwner()));
        // 明细信息填充
        List<String> skuIds = viewDetailList.stream().map(TransferOutDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, List<SkuVO>> skuMap = skuList.stream().collect(Collectors.groupingBy(SkuVO::getSkuId));
        viewDetailList.stream().forEach(member->{
            //产品名称
            if(skuMap.containsKey(member.getSkuId()) && CollUtil.isNotEmpty(skuMap.get(member.getSkuId()))) {
                SkuVO skuVO = skuMap.get(member.getSkuId()).get(0);
                member.setProductName(skuVO.getSkuName());
                member.setVariantProperty(skuVO.getVariantProperty());
            }
            //根据组织、仓库、仓位、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(data.getOutWarehouseId(), member.getSkuId(), member.getOutWarehouseLocation());
            member.setCurInventoryQty(curInventoryQty);
        });
        data.setDetailList(viewDetailList);
    }

    /**
     * 更新库存数据
     * @param list
     */
    public void updateInventoryTransCore(List<TransferOutEntity> list) {
        List<String> mainIds = list.stream().map(TransferOutEntity::getId).distinct().collect(Collectors.toList());
        Map<String,TransferOutEntity> mainMap = list.stream().collect(Collectors.toMap(TransferOutEntity::getId, Function.identity()));
        List<TransferOutDetailEntity> detailList = transferOutDetailService.listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException("未找到分步式调拨明细数据");
        }

        InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
        inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.TRANSFER_OUT.getCode());
        List<TransferDTO> members = Lists.newArrayListWithExpectedSize(detailList.size());
        detailList.stream().forEach(detailEntity->{
            TransferDTO transferDTO = new TransferDTO();
            transferDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_OUT);

            TransferOutEntity transferOutEntity = mainMap.get(detailEntity.getMainId());
            transferDTO.setSourceId(transferOutEntity.getId());
            transferDTO.setSourceCode(transferOutEntity.getCode());
            transferDTO.setSourceDetailId(detailEntity.getId());
            transferDTO.setBillDate(transferOutEntity.getBillDate());
            transferDTO.setCurWarehouseId(transferOutEntity.getOutWarehouseId());
            transferDTO.setCurWarehouseLocation(detailEntity.getOutWarehouseLocation());
            transferDTO.setTargetWarehouseId(transferOutEntity.getInWarehouseId());
            transferDTO.setTargetWarehouseLocation("");// 调入仓位为空
            transferDTO.setSkuId(detailEntity.getSkuId());
            transferDTO.setSkuNo(detailEntity.getSkuNo());
            transferDTO.setQty(detailEntity.getQty());
            members.add(transferDTO);
        });
        inventoryTransferDTO.setParamList(members);
        inventoryTransCoreService.approveByType(inventoryTransferDTO);
    }

    /**
     * 填充下推分步式调入数据
     * @param transferType
     * @param transferDirection
     * @param pushData
     * @param transferOutDetailEntity
     * @return
     */
    private TransferInDTO.ViewGenerateTransferInDTO wrapTransferIn(String transferType, String transferDirection,
                                                                   TransferOutDTO.GenerateTransferInDTO pushData,TransferOutDetailEntity transferOutDetailEntity) {
        TransferInDTO.ViewGenerateTransferInDTO transferInDTO = new TransferInDTO.ViewGenerateTransferInDTO();
        transferInDTO.setTransferType(TransferTypeEnum.getByCode(transferType));
        transferInDTO.setSourceCode(pushData.getSourceCode());
        transferInDTO.setSourceId(pushData.getSourceId());
        transferInDTO.setSourceDetailId(pushData.getSourceDetailId());
        transferInDTO.setBillDate(LocalDate.now());
        transferInDTO.setOutDate(pushData.getBillDate());
        transferInDTO.setTransferDirection(TransferDirectionEnum.getByCode(transferDirection));
        transferInDTO.setOutWarehouseId(pushData.getOutWarehouseId());
        transferInDTO.setOutWarehouseLocation(pushData.getOutWarehouseLocation());
        transferInDTO.setInWarehouseId(pushData.getInWarehouseId());
        transferInDTO.setSkuId(pushData.getSkuId());
        transferInDTO.setSkuNo(pushData.getSkuNo());
        transferInDTO.setOutQty(transferOutDetailEntity.getQty());
        transferInDTO.setPlanQty(pushData.getPlanQty());
        transferInDTO.setRemark(pushData.getRemark());
        return transferInDTO;
    }



}
