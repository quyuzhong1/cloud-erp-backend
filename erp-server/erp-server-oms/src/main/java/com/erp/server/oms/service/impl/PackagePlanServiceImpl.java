package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.model.oms.dto.PackagePlanDetailDTO;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.PackagePlanMapper;
import com.erp.server.oms.service.*;
import com.sdk.oms.wildberries.dto.*;
import com.sdk.oms.wildberries.service.WildberriesSDKService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_PACKAGE_PLAN;

/**
 * <p>
 * 组包计划主表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
 */
@Slf4j
@Service
public class PackagePlanServiceImpl extends SuperServiceImpl<PackagePlanMapper, PackagePlanEntity> implements PackagePlanService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WildberriesSDKService wildberriesSDKService;
    @Lazy
    @Resource
    private SoB2cService soB2cService;
    @Lazy
    @Resource
    private ShopInfoService shopInfoService;
    @Lazy
    @Resource
    private ShopAuthService shopAuthService;
    @Lazy
    @Resource
    private PackagePlanDetailService packagePlanDetailService;
    @Lazy
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;
    @Resource
    private SoB2cLabelService soB2cLabelService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private WorkflowTaskRecordService workflowTaskRecordService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private FileFeign fileFeign;
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackagePlanDTO.AddDTO addDTO) {
        PackagePlanEntity packagePlanEntity = new PackagePlanEntity();
        BeanMapperUtils.copy(addDTO, packagePlanEntity);

        // 数据处理
        handleData(packagePlanEntity);

        log.info("开始新增组包计划主单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZBJH);
        packagePlanEntity.setCode(code);
        boolean save = super.save(packagePlanEntity);
        if (!save) {
            throw new ServiceException("组包计划主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "组包计划主单", packagePlanEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKAGE_PLAN.getCode(), packagePlanEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        addDTO.getDetailList().forEach(detailDTO -> {
            detailDTO.setMainId(packagePlanEntity.getId());
            packagePlanDetailService.add(detailDTO);
        });
        return new BaseResultDTO.AddDTO(packagePlanEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackagePlanDTO.UpdateDTO addOrUpdateDTO) {
        PackagePlanEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "组包计划主单"));
        PackagePlanEntity packagePlanEntity = BeanMapperUtils.map(PackagePlanEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(packagePlanEntity);
        log.info("编辑 开始修改组包计划主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(packagePlanEntity);
        if (!save) {
            throw new ServiceException("组包计划主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录组包计划主单日志数据，单号：【{}】", packagePlanEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packagePlanEntity.getCode(), "组包计划主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packagePlanEntity, null, packagePlanEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO createSupply(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        String soId;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String platformCode = soB2cEntity.getPlatformCode();
        if (CharSequenceUtil.isBlank(platformCode)) {
            mqResponseDTO.setErrorMsg("销售订单平台编码不能为空");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getBySoId(soId);
        if (Objects.nonNull(packagePlanEntity)) {
            if (CharSequenceUtil.isNotBlank(packagePlanEntity.getPackageNo())) {
                data.put("packagePlanId", packagePlanEntity.getId());
                data.put("packageNo", packagePlanEntity.getPackageNo());
                mqResponseDTO.setData(data);
                return mqResponseDTO;
            }
        } else {
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
            if (Objects.isNull(soB2cLogisticsEntity)) {
                mqResponseDTO.setErrorMsg("销售订单物流信息不存在");
                return mqResponseDTO;
            }
            SoB2cDetailEntity detailEntity = soB2cDetailEntityList.get(0);
            //新增组包计划主单
            PackagePlanDTO.AddDTO addDTO = new PackagePlanDTO.AddDTO();
            addDTO.setBoxNum(1);
            addDTO.setDeliveryWarehouseId(detailEntity.getWarehouseId());
            addDTO.setDictPlatform(soB2cEntity.getDictPlatform());
            addDTO.setShopId(shopId);
            addDTO.setIsHandoverDownload(Boolean.FALSE);
            addDTO.setDeliveryWarehouseName(detailEntity.getWarehouseName());
            addDTO.setPackageStatus(PackageStatusEnum.WAIT.getCode());
            addDTO.setPrintHandoverStatus(PackagePrintStatusEnum.NOT.getCode());
            addDTO.setPrintOrderStatus(PackagePrintStatusEnum.NOT.getCode());
            addDTO.setDetailList(buildPlanDetail(soB2cEntity, soB2cLogisticsEntity));
            BaseResultDTO.AddDTO add = this.add(addDTO);
            packagePlanEntity = this.getById(add.getId());
        }
        CreateSupplyRequest request = CreateSupplyRequest.builder()
                .name(soB2cEntity.getCode())
                .build();
        try {
            CreateSupplyResponse supply = wildberriesSDKService.createSupply(authEntity.getToken(), request);
            if (CharSequenceUtil.isBlank(supply.getId())){
                mqResponseDTO.setErrorMsg(supply.getDetail());
                return mqResponseDTO;
            }
            String packageNo = supply.getId();
            packagePlanEntity.setPackageNo(packageNo);
            data.put("packagePlanId", packagePlanEntity.getId());
            data.put("packageNo", packageNo);
            this.updateById(packagePlanEntity);
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("创建通运单失败");
            log.warn("创建通运单失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO addBoxToSupply(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packageNo")) {
            mqResponseDTO.setErrorMsg("交接单号/组包号为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packagePlanId")) {
            mqResponseDTO.setErrorMsg("组包计划ID为空");
            return mqResponseDTO;
        }
        String soId;
        String packageNo;
        String packagePlanId;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        try {
            packageNo = String.valueOf(data.get("packageNo"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包号类型转换失败");
            log.warn("组包号类型转换失败: {}", data.get("packageNo"));
            return mqResponseDTO;
        }
        try {
            packagePlanId = String.valueOf(data.get("packagePlanId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包计划ID类型转换失败");
            log.warn("组包计划ID类型转换失败: {}", data.get("packagePlanId"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getById(packagePlanId);
        if (Objects.isNull(packagePlanEntity)) {
            mqResponseDTO.setErrorMsg("组包计划记录不存在");
            return mqResponseDTO;
        }
        //是否已添加
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getBySoId(soId);
        if (CharSequenceUtil.isNotBlank(detailEntityList.get(0).getBarcode())) {
            data.put("barcode", detailEntityList.get(0).getBarcode());
            mqResponseDTO.setData(data);
            return mqResponseDTO;
        }
        AddBoxToSupplyRequest request = AddBoxToSupplyRequest.builder()
                .amount(1)
                .build();
        try {
            AddBoxToSupplyResponse addBoxToSupplyResponse = wildberriesSDKService.addBoxToSupply(authEntity.getToken(), packageNo, request);
            if (CollUtil.isEmpty(addBoxToSupplyResponse.getTrbxIds())){
                mqResponseDTO.setErrorMsg(addBoxToSupplyResponse.getDetail());
                return mqResponseDTO;
            }
            //小包条码
            List<String> trbxIds = addBoxToSupplyResponse.getTrbxIds();
            data.put("barcode", String.join(",", trbxIds));
            packagePlanDetailService.updateBarcodeBySoId(packagePlanId, soId, String.join(",", trbxIds));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("添加小包失败");
            log.warn("添加小包失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO addOrderToSupply(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packageNo")) {
            mqResponseDTO.setErrorMsg("交接单号/组包号为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packagePlanId")) {
            mqResponseDTO.setErrorMsg("组包计划ID为空");
            return mqResponseDTO;
        }
        String soId;
        String packageNo;
        String packagePlanId;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        try {
            packageNo = String.valueOf(data.get("packageNo"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包号类型转换失败");
            log.warn("组包号类型转换失败: {}", data.get("packageNo"));
            return mqResponseDTO;
        }
        try {
            packagePlanId = String.valueOf(data.get("packagePlanId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包计划ID类型转换失败");
            log.warn("组包计划ID类型转换失败: {}", data.get("packagePlanId"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
        String platformCode = soB2cEntity.getPlatformCode();
        if (CharSequenceUtil.isBlank(platformCode)) {
            mqResponseDTO.setErrorMsg("销售订单平台编码不能为空");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getById(packagePlanId);
        if (Objects.isNull(packagePlanEntity)) {
            mqResponseDTO.setErrorMsg("组包计划记录不存在");
            return mqResponseDTO;
        }
        //是否已添加
        if (PackageStatusEnum.ALREADY.getCode().equals(packagePlanEntity.getPackageStatus())) {
            data.put("platformCode", platformCode);
            mqResponseDTO.setData(data);
            return mqResponseDTO;
        }
        AddOrderToSupplyRequest request = AddOrderToSupplyRequest.builder()
                .supplyId(packageNo)
                .orderId(Long.valueOf(platformCode))
                .build();
        try {
            AddOrderToSupplyResponse response = wildberriesSDKService.addOrderToSupply(authEntity.getToken(), request);
            if (Objects.nonNull(response) && !"204".equals(response.getCode())) {
                mqResponseDTO.setErrorMsg(response.getCode());
                return mqResponseDTO;
            }
            //204表示添加成功，只有完成了以上三步，组包预报单的大包交接状态变更为已提交
            data.put("platformCode", platformCode);
            packagePlanEntity.setPackageStatus(PackageStatusEnum.ALREADY.getCode());
            this.updateById(packagePlanEntity);
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("添加订单到大包失败");
            log.warn("添加订单到大包失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO getOrderSticker(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packageNo")) {
            mqResponseDTO.setErrorMsg("交接单号/组包号为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packagePlanId")) {
            mqResponseDTO.setErrorMsg("组包计划ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("platformCode")) {
            mqResponseDTO.setErrorMsg("销售订单平台编码为空");
            return mqResponseDTO;
        }
        String soId;
        String packageNo;
        String packagePlanId;
        String platformCode;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        try {
            packageNo = String.valueOf(data.get("packageNo"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包号类型转换失败");
            log.warn("组包号类型转换失败: {}", data.get("packageNo"));
            return mqResponseDTO;
        }
        try {
            packagePlanId = String.valueOf(data.get("packagePlanId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包计划ID类型转换失败");
            log.warn("组包计划ID类型转换失败: {}", data.get("packagePlanId"));
            return mqResponseDTO;
        }
        try {
            platformCode = String.valueOf(data.get("platformCode"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单平台编码类型转换失败");
            log.warn("销售订单平台编码类型转换失败: {}", data.get("platformCode"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
//        String platformCode = soB2cEntity.getPlatformCode();
        if (CharSequenceUtil.isBlank(platformCode)) {
            mqResponseDTO.setErrorMsg("销售订单平台编码不能为空");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getById(packagePlanId);
        if (Objects.isNull(packagePlanEntity)) {
            mqResponseDTO.setErrorMsg("组包计划记录不存在");
            return mqResponseDTO;
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
        if (Objects.isNull(soB2cLogisticsEntity)) {
            mqResponseDTO.setErrorMsg("销售订单物流记录不存在");
            return mqResponseDTO;
        }
        List<SoB2cLabelEntity> soB2cLabelEntities = soB2cLabelService.listSoB2cLabelByMainIds(Collections.singletonList(soId));
        //订单已生成标签
        if (CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getTrackNo()) && CollUtil.isNotEmpty(soB2cLabelEntities) && CharSequenceUtil.isNotBlank(soB2cLabelEntities.get(0).getLogisticsLabelBase64())) {
            data.put("trackNo", soB2cLogisticsEntity.getTrackNo());
            mqResponseDTO.setData(data);
            return mqResponseDTO;
        }
        OrderLabelRequest request = OrderLabelRequest.builder()
                .orders(Collections.singletonList(Long.valueOf(platformCode)))
                .width(58)
                .height(40)
                .type("png")
                .build();
        try {
            OrderLabelResponse response = wildberriesSDKService.getOrderLabel(authEntity.getToken(), request);
            if (CharSequenceUtil.isNotBlank(response.getDetail())) {
                mqResponseDTO.setErrorMsg("获取订单标签失败:" + JSONUtil.toJsonStr(response));
                return mqResponseDTO;
            }
            List<OrderLabelResponse.Sticker> stickers = response.getStickers();
            if (CollUtil.isEmpty(stickers)) {
                mqResponseDTO.setErrorMsg("获取订单标签失败: 标签为空");
                return mqResponseDTO;
            }
            OrderLabelResponse.Sticker sticker = stickers.get(0);
            String trackNo = sticker.getBarcode();
            Long orderId = sticker.getOrderId();
            String file = sticker.getFile();
            //更新跟踪号
            soB2cLogisticsService.updateLogisticsCode(soId, trackNo, trackNo, "", "");
            //更新订单标签
            List<SoB2cLabelDTO.UpdateDTO> dtoList = new ArrayList<>();
            SoB2cLabelDTO.UpdateDTO updateDTO = new SoB2cLabelDTO.UpdateDTO();
            updateDTO.setMainId(soId);
            updateDTO.setLogisticsLabelBase64("data:application/pdf;base64," + PdfUtil.ImageToPdfBase64(file));
            dtoList.add(updateDTO);
            soB2cLabelService.saveSoB2cLabel(dtoList);
            data.put("trackNo", trackNo);
            packagePlanEntity.setPackageStatus(PackageStatusEnum.ALREADY.getCode());
            this.updateById(packagePlanEntity);
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("获取订单标签失败");
            log.warn("获取订单标签失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO moveSupplyToDelivery(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packageNo")) {
            mqResponseDTO.setErrorMsg("交接单号/组包号为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packagePlanId")) {
            mqResponseDTO.setErrorMsg("组包计划ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("platformCode")) {
            mqResponseDTO.setErrorMsg("销售订单平台编码为空");
            return mqResponseDTO;
        }
        String soId;
        String packageNo;
        String packagePlanId;
        String platformCode;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        try {
            packageNo = String.valueOf(data.get("packageNo"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包号类型转换失败");
            log.warn("组包号类型转换失败: {}", data.get("packageNo"));
            return mqResponseDTO;
        }
        try {
            packagePlanId = String.valueOf(data.get("packagePlanId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包计划ID类型转换失败");
            log.warn("组包计划ID类型转换失败: {}", data.get("packagePlanId"));
            return mqResponseDTO;
        }
        try {
            platformCode = String.valueOf(data.get("platformCode"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单平台编码类型转换失败");
            log.warn("销售订单平台编码类型转换失败: {}", data.get("platformCode"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
//        String platformCode = soB2cEntity.getPlatformCode();
        if (CharSequenceUtil.isBlank(platformCode)) {
            mqResponseDTO.setErrorMsg("销售订单平台编码不能为空");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getById(packagePlanId);
        if (Objects.isNull(packagePlanEntity)) {
            mqResponseDTO.setErrorMsg("组包计划记录不存在");
            return mqResponseDTO;
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
        if (Objects.isNull(soB2cLogisticsEntity)) {
            mqResponseDTO.setErrorMsg("销售订单物流记录不存在");
            return mqResponseDTO;
        }
        try {
            BaseResponse response = wildberriesSDKService.signDelivery(authEntity.getToken(), packageNo);
            if (CharSequenceUtil.isNotBlank(response.getCode()) && !"SupplyClosed".equals(response.getStatus())) {
                mqResponseDTO.setErrorMsg("将供货单转入已完成失败:" + JSONUtil.toJsonStr(response));
                return mqResponseDTO;
            }
            data.put("moveDelivery", "success");
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("将供货单转入已完成失败");
            log.warn("将供货单转入已完成失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO getCrossSticker(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if (!data.containsKey("id")) {
            mqResponseDTO.setErrorMsg("销售订单ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packageNo")) {
            mqResponseDTO.setErrorMsg("交接单号/组包号为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("packagePlanId")) {
            mqResponseDTO.setErrorMsg("组包计划ID为空");
            return mqResponseDTO;
        }
        if (!data.containsKey("platformCode")) {
            mqResponseDTO.setErrorMsg("销售订单平台编码为空");
            return mqResponseDTO;
        }
        String soId;
        String packageNo;
        String packagePlanId;
        String platformCode;
        try {
            soId = String.valueOf(data.get("id"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单ID类型转换失败");
            log.warn("soId 类型转换失败: {}", data.get("id"));
            return mqResponseDTO;
        }
        try {
            packageNo = String.valueOf(data.get("packageNo"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包号类型转换失败");
            log.warn("组包号类型转换失败: {}", data.get("packageNo"));
            return mqResponseDTO;
        }
        try {
            packagePlanId = String.valueOf(data.get("packagePlanId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("组包计划ID类型转换失败");
            log.warn("组包计划ID类型转换失败: {}", data.get("packagePlanId"));
            return mqResponseDTO;
        }
        try {
            platformCode = String.valueOf(data.get("platformCode"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("销售订单平台编码类型转换失败");
            log.warn("销售订单平台编码类型转换失败: {}", data.get("platformCode"));
            return mqResponseDTO;
        }
        SoB2cEntity soB2cEntity = soB2cService.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            mqResponseDTO.setErrorMsg("销售订单记录不存在");
            return mqResponseDTO;
        }
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soId);
        if (Objects.isNull(receiverEntity)) {
            mqResponseDTO.setErrorMsg("销售订单接收人记录不存在");
            return mqResponseDTO;
        }
        String receiverCountry = receiverEntity.getCountry();
        if (CharSequenceUtil.isBlank(receiverCountry)) {
            ShopInfoEntity shopInfo = shopInfoService.getById(soB2cEntity.getShopId());
            receiverCountry = shopInfo.getDictCountryCode();
            return mqResponseDTO;
        }
        if (CharSequenceUtil.isBlank(platformCode)) {
            mqResponseDTO.setErrorMsg("销售订单平台编码不能为空");
            return mqResponseDTO;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainId(soId);
        if (CollUtil.isEmpty(soB2cDetailEntityList)) {
            mqResponseDTO.setErrorMsg("销售订单明细记录不存在");
            return mqResponseDTO;
        }
        String shopId = soB2cEntity.getShopId();
        if (CharSequenceUtil.isBlank(shopId)) {
            mqResponseDTO.setErrorMsg("店铺ID不能为空");
            return mqResponseDTO;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)) {
            mqResponseDTO.setErrorMsg("店铺记录不存在");
            return mqResponseDTO;
        }
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            mqResponseDTO.setErrorMsg("店铺未授权");
            return mqResponseDTO;
        }
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)) {
            mqResponseDTO.setErrorMsg("店铺授权信息不存在");
            return mqResponseDTO;
        }
        //是否已创建
        PackagePlanEntity packagePlanEntity = this.getById(packagePlanId);
        if (Objects.isNull(packagePlanEntity)) {
            mqResponseDTO.setErrorMsg("组包计划记录不存在");
            return mqResponseDTO;
        }
        String deliveryWarehouseId = packagePlanEntity.getDeliveryWarehouseId();
        if (CharSequenceUtil.isBlank(deliveryWarehouseId)) {
            mqResponseDTO.setErrorMsg("销售订单发货仓库ID不能为空");
            return mqResponseDTO;
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(deliveryWarehouseId));
        if (CollUtil.isEmpty(warehouseList)) {
            mqResponseDTO.setErrorMsg("发货仓库记录不存在");
            return mqResponseDTO;
        }
        WarehouseDTO.UpdateDTO warehouse = warehouseList.get(0);
        if (warehouse.getCountry().equals(receiverCountry)) {
            data.put("isCrossOrder", Boolean.FALSE);
            return mqResponseDTO;
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsService.getByMainId(soId);
        if (Objects.isNull(soB2cLogisticsEntity)) {
            mqResponseDTO.setErrorMsg("销售订单物流记录不存在");
            return mqResponseDTO;
        }
        List<SoB2cLabelEntity> soB2cLabelEntities = soB2cLabelService.listSoB2cLabelByMainIds(Collections.singletonList(soId));
        //订单已生成标签
        if (CharSequenceUtil.isNotBlank(soB2cLogisticsEntity.getTrackNo()) && CollUtil.isNotEmpty(soB2cLabelEntities) && CharSequenceUtil.isNotBlank(soB2cLabelEntities.get(0).getLogisticsLabelBase64())) {
            data.put("trackNo", soB2cLogisticsEntity.getTrackNo());
            mqResponseDTO.setData(data);
            return mqResponseDTO;
        }
        OrderLabelRequest request = OrderLabelRequest.builder()
                .orders(Collections.singletonList(Long.valueOf(platformCode)))
                .width(58)
                .height(40)
                .type("png")
                .build();
        try {
            CrossOrderLabelResponse response = wildberriesSDKService.getCrossOrderLabel(authEntity.getToken(), request);
            List<CrossOrderLabelResponse.Sticker> stickers = response.getStickers();
            if (CollUtil.isEmpty(stickers)) {
                mqResponseDTO.setErrorMsg("获取订单标签失败: 标签为空");
                return mqResponseDTO;
            }
            CrossOrderLabelResponse.Sticker sticker = stickers.get(0);
            String url = sticker.getUrl();
            data.put("crossLabelUrl", url);
            soB2cLabelService.updateCrossLabelUrl(soId, url);
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("获取订单标签失败");
            log.warn("获取订单标签失败: {}", e.getMessage());
            return mqResponseDTO;
        }
        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    @Override
    public PagingVO<PackagePlanDTO.PagingViewDTO> paging(PagingDTO<PackagePlanDTO.PagingParamDTO> dto) {
        PackagePlanDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<PackagePlanDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<PackagePlanDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillData(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<PackagePlanDTO.ExportDTO> exportPaging(PagingDTO<PackagePlanDTO.PagingParamDTO> dto) {
        PackagePlanDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<PackagePlanDTO.ExportDTO> pageData = baseMapper.exportPaging(query, params);
        List<PackagePlanDTO.ExportDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillExportData(list);
        return new PagingVO<>(pageData);
    }

    private void fillExportData(List<PackagePlanDTO.ExportDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(PackagePlanDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getByMainIds(ids);
        List<PackagePlanDetailDTO.ViewDTO> addDTOList = BeanUtil.copyToList(detailEntityList, PackagePlanDetailDTO.ViewDTO.class);
        Map<String, List<PackagePlanDetailDTO.ViewDTO>> map = addDTOList.stream().collect(Collectors.groupingBy(PackagePlanDetailDTO.ViewDTO::getMainId));
        List<String> shopIds = list.stream().map(PackagePlanDTO.PagingViewDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIds);
        Map<String, String> shopInfoMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        list.forEach(item -> {
            item.setShopName(shopInfoMap.getOrDefault(item.getShopId(), ""));
            item.setDictPlatformName(PlatformDictEnum.getNameByCode(item.getDictPlatform()));
            item.setPackageStatusName(PackageStatusEnum.getName(item.getPackageStatus()));
            item.setPrintHandoverStatusName(PackagePrintStatusEnum.getName(item.getPrintHandoverStatus()));
            item.setPrintOrderStatusName(PackagePrintStatusEnum.getName(item.getPrintOrderStatus()));
            item.setHandoverDownload(item.getIsHandoverDownload() ? "已下载" : "未下载");
            List<PackagePlanDetailDTO.ViewDTO> viewDTOS = map.get(item.getId());
            viewDTOS.stream().filter(e -> e.getId().equals(item.getDetailId())).findFirst().ifPresent(f -> {
                item.setTrackNo(f.getTrackNo());
                item.setTransportNo(f.getTransportNo());
            });
        });
    }

    @Override
    public void listExport(PackagePlanDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("组包计划导出", EXPORT_OMS_PACKAGE_PLAN.getCode(),dto);
    }

    @Override
    public void batchOrderPrint(List<String> ids, HttpServletResponse response) {
        List<PackagePlanEntity> entityList = this.listByIds(ids);
        List<String> errorCodeList = new ArrayList<>();
        List<String> base64List = new ArrayList<>();
        for (PackagePlanEntity entity : entityList) {
            String base64 = this.print(entity.getId());
            if(StringUtils.isEmpty(base64)){
                errorCodeList.add(entity.getCode());
            }else{
                base64List.add(base64);
            }
        }
        if(CollectionUtils.isNotEmpty(errorCodeList)){
            throw new ServiceException("组包预报批量打印失败,单号:{},无标签",errorCodeList);
        }
        if(CollectionUtils.isNotEmpty(base64List)){
            try {
                String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);

                // 设置响应头，告诉浏览器返回的是一个 PDF 文件
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
                BASE64Decoder decoder = new BASE64Decoder();
                try (OutputStream out = response.getOutputStream()) {
                    // 将 Base64 编码的字符串解码为字节数组
                    byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                    // 将字节数组写入到响应输出流中
                    out.write(pdfBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                log.error("组包预报批量打印打印失败>>>>>>>", e);
                throw new ServiceException(e.getMessage());
            }
        }
    }

    @Override
    public BatchResultDTO addPlan(PackagePlanDTO.SoB2cDTO dto) {
        List<WorkflowTaskRecordEntity> workflowTaskRecordEntities = workflowTaskRecordService.listBySourceId(dto.getSoId(), WorkflowTaskRecordTypeEnum.PACKAGE_PLAN_GENERATE.getCode());
        if(CollectionUtils.isNotEmpty(workflowTaskRecordEntities)){
            return BatchResultDTO.fail(dto.getSoId(), dto.getSoCode(), "该订单已生成组包计划任务");
        }
        //自动生成并完成节点功能
        WorkflowTaskRecordDTO.AddTaskDTO addTaskDTO = new WorkflowTaskRecordDTO.AddTaskDTO();
        addTaskDTO.setSourceId(dto.getSoId());
        addTaskDTO.setSourceCode(dto.getSoCode());
        addTaskDTO.setDictBasicTypeEnum(DictBasicTypeEnum.WORKFLOW_TASK_NODE); //type
        addTaskDTO.setSourceTypeEnum(WorkflowTaskRecordTypeEnum.PACKAGE_PLAN_GENERATE);//subType
        addTaskDTO.setTraceId(MDC.get("traceId"));

        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getSoId());
        map.put("errorType", SoB2cErrorTypeEnum.PACKAGE_PLAN_GENERATE.getCode());
        addTaskDTO.setFirstNodeInputData(map);
        workflowTaskRecordEntities = workflowTaskRecordService.addTask(addTaskDTO);
        if(CollUtil.isEmpty(workflowTaskRecordEntities)){
            throw new ServiceException(ApiError.NOT_EXIST,DictBasicTypeEnum.WORKFLOW_TASK_NODE.getDesc());
        }
        SendResult result = mqProducerService.syncClassMsgWithDelayLevel(RocketMqTopic.OMS_WORKFLOW_TASK_RECORD_TOPIC, RocketMqTagEnum.OMS_WORKFLOW_TASK_RECORD_TAG.getName(), addTaskDTO, dto.getSoId(),2);
        if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
            throw new RuntimeException(StrUtil.format("生成组包计划通过发送任务编排MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return BatchResultDTO.success(dto.getSoId(), dto.getSoCode(), "生成组包计划任务编排已生成");
    }

    @Override
    public void removeBySoId(String id) {
        if (CharSequenceUtil.isBlank(id)){
            return;
        }
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getBySoId(id);
        if(CollectionUtils.isNotEmpty(detailEntityList)){
            packagePlanDetailService.removeByIds(detailEntityList);
        }
        List<String> mainIds = detailEntityList.stream().map(PackagePlanDetailEntity::getMainId).collect(Collectors.toList());
        this.lambdaUpdate().in(PackagePlanEntity::getId,mainIds).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        if (CharSequenceUtil.isBlank(id)){
            return BatchResultDTO.fail(id, id, "组包计划不存在");
        }
        this.removeById(id);
        packagePlanDetailService.removeByMainId(id);
        return BatchResultDTO.success(id, id, "删除组包计划成功");
    }

    @Override
    public List<PackagePlanDTO.LabelDTO> getNoHandoverLabel(List<String> codeList) {
        return baseMapper.getNoHandoverLabel(codeList);
    }

    @Override
    public void downloadHandoverLabel(PackagePlanDTO.LabelDTO dto) {
        PackagePlanEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包计划单");
        }
        String shopId = entity.getShopId();
        ShopAuthEntity authEntity = shopAuthService.getByShopId(shopId);
        if (Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "店铺授权信息");
        }
        String packageNo = entity.getPackageNo();
        if (CharSequenceUtil.isBlank(packageNo)){
            throw new ServiceException("组包计划单供货单ID不能为空");
        }
        String handoverLabelUrl = entity.getHandoverLabelUrl();
        if (CharSequenceUtil.isNotBlank(handoverLabelUrl)){
            throw new ServiceException("组包计划单交接标签已存在");
        }
        SupplyLabelResponse response = wildberriesSDKService.getSupplyLabel(authEntity.getToken(), packageNo);
        if (CharSequenceUtil.isNotBlank(response.getMessage())){
            throw new ServiceException("获取交接标签失败:" + response.getMessage());
        }
        if (CharSequenceUtil.isNotBlank(response.getDetail())){
            throw new ServiceException("获取交接标签失败:" + response.getDetail());
        }
        String pdfBase64 = null;
        try {
            pdfBase64 = PdfUtil.ImageToPdfBase64(response.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        byte[] bytes = Base64.getDecoder().decode(pdfBase64);
//        String labelUrl = FastDFSClientUtil.uploadFile(bytes, UUID.randomUUID().toString(), null);
        String labelUrl = fileFeign.uploadFileByBase64(pdfBase64);
        log.info("获取交接标签成功，labelUrl地址：{}", labelUrl);
        entity.setHandoverLabelUrl(labelUrl);
        entity.setIsHandoverDownload(true);
        entity.setTransportNo(response.getBarcode());
        this.updateById(entity);
    }

    @Override
    public void batchHandoverPrint(List<String> ids, HttpServletResponse response) {
        List<PackagePlanEntity> entityList = this.listByIds(ids);
        List<String> errorCodeList = new ArrayList<>();
        List<String> base64List = new ArrayList<>();
        for (PackagePlanEntity entity : entityList) {
            if(CharSequenceUtil.isBlank(entity.getHandoverLabelUrl())){
                errorCodeList.add(entity.getCode());
                continue;
            }
            byte[] bytes = fileFeign.downloadFile(entity.getHandoverLabelUrl());
            base64List.add("data:application/pdf;base64," + Base64.getEncoder().encodeToString(bytes));
        }
        if(CollectionUtils.isNotEmpty(errorCodeList)){
            throw new ServiceException("组包计划单批量打印交接标签失败,单号:{}",errorCodeList);
        }
        if(CollectionUtils.isNotEmpty(base64List)){
            try {
                String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);
                // 设置响应头，告诉浏览器返回的是一个 PDF 文件
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
                BASE64Decoder decoder = new BASE64Decoder();
                try (OutputStream out = response.getOutputStream()) {
                    // 将 Base64 编码的字符串解码为字节数组
                    byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                    // 将字节数组写入到响应输出流中
                    out.write(pdfBytes);
                } catch (IOException e) {
                    log.error("组包计划单批量打印交接标签失败:{}",e.getMessage());
                }
            } catch (Exception e) {
                log.error("组包计划单批量打印交接标签失败>>>>>>>", e);
                throw new ServiceException(e.getMessage());
            }
        }
    }

    private String print(String id) {
        String base64 = "";
        PackagePlanEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            return base64;
        }
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getByMainIds(Collections.singletonList(id));
        if(CollectionUtils.isEmpty(detailEntityList)){
            return base64;
//            throw new ServiceException("组包计划【{}】详情不存在",entity.getCode());
        }
        PackagePlanDetailEntity packagePlanDetailEntity = detailEntityList.get(0);
        String soId = packagePlanDetailEntity.getSoId();
        List<SoB2cLabelEntity> soB2cLabelEntities = soB2cLabelService.listSoB2cLabelByMainIds(Collections.singletonList(soId));
        if(CollectionUtils.isEmpty(soB2cLabelEntities)){
            return base64;
//            throw new ServiceException("【{}】订单标签不存在",entity.getCode());
        }
        base64 = soB2cLabelEntities.get(0).getLogisticsLabelBase64();
        if (CharSequenceUtil.isNotBlank(base64)) {
            entity.setPrintOrderStatus(PackagePrintStatusEnum.ALREADY.getCode());
            this.updateById(entity);
        }
//        else {
//            throw new ServiceException("打印失败");
//        }
        return base64;
    }

    private void fillData(List<PackagePlanDTO.PagingViewDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(PackagePlanDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getByMainIds(ids);
        List<PackagePlanDetailDTO.ViewDTO> addDTOList = BeanUtil.copyToList(detailEntityList, PackagePlanDetailDTO.ViewDTO.class);
        Map<String, List<PackagePlanDetailDTO.ViewDTO>> map = addDTOList.stream().collect(Collectors.groupingBy(PackagePlanDetailDTO.ViewDTO::getMainId));
        List<String> shopIds = list.stream().map(PackagePlanDTO.PagingViewDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(shopIds);
        Map<String, String> shopInfoMap = shopInfoEntityList.stream().collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        list.forEach(item -> {
            item.setDetailList(map.getOrDefault(item.getId(), new ArrayList<>()));
            item.setShopName(shopInfoMap.getOrDefault(item.getShopId(), ""));
            item.setDictPlatformName(PlatformDictEnum.getNameByCode(item.getDictPlatform()));
            item.setPackageStatusName(PackageStatusEnum.getName(item.getPackageStatus()));
            item.setPrintHandoverStatusName(PackagePrintStatusEnum.getName(item.getPrintHandoverStatus()));
            item.setPrintOrderStatusName(PackagePrintStatusEnum.getName(item.getPrintOrderStatus()));
            item.setHandoverDownload(item.getIsHandoverDownload() ? "已下载" : "未下载");
        });
    }

    private List<PackagePlanDetailDTO.AddDTO> buildPlanDetail(SoB2cEntity soB2cEntity, SoB2cLogisticsEntity soB2cLogisticsEntity) {
        List<PackagePlanDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        PackagePlanDetailDTO.AddDTO addDTO = new PackagePlanDetailDTO.AddDTO();
        addDTO.setSoId(soB2cEntity.getId());
        addDTO.setSoCode(soB2cEntity.getCode());
        addDTO.setPlatformCode(soB2cEntity.getPlatformCode());
        addDTO.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        addDTO.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        addDTO.setTrackNo(soB2cLogisticsEntity.getTrackNo());
        addDTO.setTransportNo(soB2cLogisticsEntity.getCode());
        addDTOList.add(addDTO);
        return addDTOList;
    }

    @Override
    public PackagePlanEntity getBySoId(String soId) {
        if (CharSequenceUtil.isBlank(soId)) {
            return null;
        }
        List<PackagePlanDetailEntity> detailEntityList = packagePlanDetailService.getBySoId(soId);
        if (CollUtil.isEmpty(detailEntityList)) {
            return null;
        }
        List<String> mainIds = detailEntityList.stream().map(PackagePlanDetailEntity::getMainId).distinct().collect(Collectors.toList());
        return this.lambdaQuery().in(PackagePlanEntity::getId, mainIds).orderByDesc(PackagePlanEntity::getCreateTime).last("limit 1").one();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackagePlanEntity packagePlanEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
