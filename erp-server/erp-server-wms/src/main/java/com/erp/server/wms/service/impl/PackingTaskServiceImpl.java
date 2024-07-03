package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.PackingWeightStatusEnum;
import com.erp.model.wms.enums.PickingSourceTypeEnum;
import com.erp.server.wms.convert.PackingConverter;
import com.erp.server.wms.mapper.PackingTaskMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackingTaskDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 装箱任务表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@Service
public class PackingTaskServiceImpl extends SuperServiceImpl<PackingTaskMapper, PackingTaskEntity> implements PackingTaskService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackingTaskDTO.AddDTO addDTO) {
        PackingTaskEntity packingTaskEntity = new PackingTaskEntity();
        BeanMapperUtils.copy(addDTO, packingTaskEntity);

        // 数据处理
        handleData(packingTaskEntity);

        log.info("开始新增装箱任务单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        packingTaskEntity.setCode(code);
        boolean save = super.save(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, packingTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(packingTaskEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackingTaskDTO.UpdateDTO updateDTO) {
        PackingTaskEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "装箱任务单"));
        PackingTaskEntity packingTaskEntity =  BeanMapperUtils.map(PackingTaskEntity.class, updateDTO);

        // 数据处理
        handleData(packingTaskEntity);
        log.info("编辑 开始修改装箱任务单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(packingTaskEntity);
        if(!save) {
            throw new ServiceException("装箱任务单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录装箱任务单日志数据，单号：【{}】", packingTaskEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packingTaskEntity.getCode(), "装箱任务单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packingTaskEntity, null, packingTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void addPackingByB2BDelivery(SoDeliveryNoticeEntity soDeliveryNoticeEntity) {
        //关联单号是否已存在装箱任务
        Integer count = countBySourceIdAndSourceType(soDeliveryNoticeEntity.getId(), PickingSourceTypeEnum.B2B.getCode());
        if (count > 0){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.b2bDeliveryToPackingTask(soDeliveryNoticeEntity);
        //查询明细
        List<SoDeliveryNoticeDetailEntity> detailEntityList = soDeliveryNoticeDetailService.listDetailByMainId(soDeliveryNoticeEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.b2bDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }

    /**
     * 根据来源订单及类型统计是否已经创建任务
     * @param sourceId
     * @param sourceType
     * @return
     */
    private Integer countBySourceIdAndSourceType(String sourceId, String sourceType) {
        if (StringUtils.isBlank(sourceId) && StringUtils.isBlank(sourceType)){
            return MathUtil.ZERO;
        }
        return lambdaQuery().eq(PackingTaskEntity::getSourceId, sourceId).eq(PackingTaskEntity::getSourceType,sourceType).count();
    }

    @Override
    public void addPackingByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity) {
        String demandType = firstMileDeliveryEntity.getDemandType();
        String sourceType = FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode().equals(demandType)? PickingSourceTypeEnum.THIRD.getCode(): PickingSourceTypeEnum.FBA.getCode();
        //关联单号是否已存在装箱任务
        Integer count = countBySourceIdAndSourceType(firstMileDeliveryEntity.getId(), sourceType);
        if (count > 0){
            return;
        }
        PackingTaskEntity packingTaskEntity = PackingConverter.INSTANCE.firstMileDeliveryToPackingTask(firstMileDeliveryEntity,sourceType);
        //查询明细
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listDetailByMainId(firstMileDeliveryEntity.getId());
        packingTaskEntity.setDeliveryQty(detailEntityList.stream().map(FirstMileDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO,Integer::sum));
        packingTaskEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.ZXRW));
        this.save(packingTaskEntity);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱任务单" , packingTaskEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKING_TASK.getCode(), packingTaskEntity.getId(), "新增操作");
        List<PackingTaskDetailEntity> taskDetailList = PackingConverter.INSTANCE.firstMileDeliveryDetailToPackingTaskDetail(detailEntityList);
        taskDetailList.forEach(packingTaskDetailEntity -> packingTaskDetailEntity.setMainId(packingTaskEntity.getId()));
        //新增任务明细
        packingTaskDetailService.saveBatch(taskDetailList);
    }
    /**
     * 装箱任务-分页查询
     * @param dto
     * @return
     */
    @Override
    public PagingVO<PackingTaskDTO.PagingViewDTO> paging(PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        Page<PackingTaskDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        PackingTaskDTO.PagingParamDTO params = dto.getParams();
        IPage<PackingTaskDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        //补充数据
        buildPackingTask(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 补充数据
     * @param records
     */
    private void buildPackingTask(List<PackingTaskDTO.PagingViewDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> taskIds = records.stream().map(PackingTaskDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        //装箱状态 称重状态 异常原因 装箱数量 装箱重量（设备更新） 拣货数量
        List<PackingTaskDTO.StatusDTO> statusDTOList = baseMapper.selectPackingStatusByIds(taskIds);
        Map<String, PackingTaskDTO.StatusDTO> statusDTOMap = statusDTOList.stream().collect(Collectors.toMap(PackingTaskDTO.StatusDTO::getId, Function.identity()));
        records.forEach(pagingViewDTO -> {
            PackingTaskDTO.StatusDTO statusDTO = statusDTOMap.get(pagingViewDTO.getId());
            if (Objects.nonNull(statusDTO)){
                pagingViewDTO.setPackingStatus(StringUtils.isBlank(statusDTO.getPackingStatus())? PackingTaskStatusEnum.UNPACKED.getCode() : statusDTO.getPackingStatus());
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.getName(pagingViewDTO.getPackingStatus()));
                pagingViewDTO.setWeightingStatus(StringUtils.isBlank(statusDTO.getWeightingStatus())? PackingWeightStatusEnum.UNWEIGHTED.getCode() : statusDTO.getWeightingStatus());
                pagingViewDTO.setWeightingStatusName(PackingTaskStatusEnum.getName(pagingViewDTO.getWeightingStatus()));
                pagingViewDTO.setErrorMsg(statusDTO.getErrorMsg());
                pagingViewDTO.setPackedQty(Objects.isNull(statusDTO.getPackedQty())? MathUtil.ZERO: statusDTO.getPackedQty());
                pagingViewDTO.setPickedQty(statusDTO.getPickedQty());
                pagingViewDTO.setPackageWeight(Objects.isNull(statusDTO.getPackingWeight())? BigDecimal.ZERO: MathUtil.divide(statusDTO.getPackingWeight(), MathUtil.BigDecimal_1000));
            }else {
                pagingViewDTO.setPackingStatus(PackingTaskStatusEnum.UNPACKED.getCode());
                pagingViewDTO.setPackingStatusName(PackingTaskStatusEnum.UNPACKED.getName());
                pagingViewDTO.setWeightingStatus(PackingWeightStatusEnum.UNWEIGHTED.getCode());
                pagingViewDTO.setWeightingStatusName(PackingWeightStatusEnum.UNWEIGHTED.getName());
                pagingViewDTO.setPackedQty(MathUtil.ZERO);
            }
        });
    }

    /**
     * 按照分类进行统计
     * @param dto
     * @return
     */
    @Override
    public List<PackingTaskDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<PackingTaskDTO.TypeCountDTO> countList = baseMapper.listTabCount(dto.getPermissionSql());
        //重构数据
        List<PackingTaskDTO.TabListDTO> tabList = new ArrayList<>(3);
        Integer unpackedCount = countList.stream().filter(e -> PackingTaskStatusEnum.UNPACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.UNPACKED.getCode(),PackingTaskStatusEnum.UNPACKED.getName(), unpackedCount));
        Integer packingCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKING.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKING.getCode(),PackingTaskStatusEnum.PACKING.getName(), packingCount));
        Integer packedCount = countList.stream().filter(e -> PackingTaskStatusEnum.PACKED.getCode().equals(e.getType())).map(PackingTaskDTO.TypeCountDTO::getCount).findFirst().orElse(MathUtil.ZERO);
        tabList.add(new PackingTaskDTO.TabListDTO(PackingTaskStatusEnum.PACKED.getCode(),PackingTaskStatusEnum.PACKED.getName(), packedCount));
        return tabList;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PackingTaskEntity packingTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
