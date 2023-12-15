package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.PickingTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.server.wms.mapper.SoB2cDeliveryMapper;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryServiceImpl extends SuperServiceImpl<SoB2cDeliveryMapper, SoB2cDeliveryEntity> implements SoB2cDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;
    @Autowired
    private SoB2cFeign soB2cFeign;
    @Autowired
    private LogisticsBillFeign logisticsBillFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeliveryDTO.AddDTO addDTO) {
        SoB2cDeliveryEntity soB2cDeliveryEntity = new SoB2cDeliveryEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeliveryEntity);

        List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), SoB2cDeliveryDetailEntity.class);

        // 数据处理
        handleData(soB2cDeliveryEntity, soB2cDeliveryDetailEntities);

        log.info("开始新增b2c发货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHDC);
        soB2cDeliveryEntity.setCode(code);
        boolean save = super.save(soB2cDeliveryEntity);
        if(!save) {
            throw new ServiceException("b2c发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单" , soB2cDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "新增操作");
        // 新增明细
        soB2cDeliveryDetailService.add(soB2cDeliveryDetailEntities, soB2cDeliveryEntity.getId());
        return new BaseResultDTO.AddDTO(soB2cDeliveryEntity.getId(), code);
    }

    @Override
    public List<SoB2cDeliveryDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return null;
    }

    @Override
    public PagingVO<SoB2cDeliveryDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public SoB2cDeliveryDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO manualDelivery(String id) {
        return null;
    }

    @Override
    public BatchResultDTO falseDelivery(String id) {
        return null;
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids) {
        return null;
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybill(List<String> ids) {
        return null;
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintDistributionDTO> printDistribution(List<String> ids) {
        return null;
    }

    @Override
    public SoB2cDeliveryEntity getByBusinessCode(String businessCode) {
        return this.getOne(new LambdaQueryWrapper<>(SoB2cDeliveryEntity.class)
                .eq(SoB2cDeliveryEntity::getSoCode, businessCode)
                .or()
                .eq(SoB2cDeliveryEntity::getTransportNo, businessCode)
        );
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> detailEntityList) {
        int deliveryQtySum = detailEntityList.stream().mapToInt(req -> req.getDeliveryQty()).sum();
        // 单品单数：SKU1个，数量1个
        if (detailEntityList.size() == 1 && deliveryQtySum == 1) {
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.SINGLE_ITEM_SINGLE.getCode());
        } else if (detailEntityList.size() == 1 && deliveryQtySum > 1) {
            // 单品多数：SKU1个，数量大于1
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.SINGLE_ITEM_MULTI.getCode());
        } else {
            // 多品多数：SKU大于1个
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.MULTI_ITEM_MULTI.getCode());
        }

        //查询B2C销售订单
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //查询B2C销售订单物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //订单信息
        SoB2cEntity soB2cEntity = soB2cEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setDictPlatform(soB2cEntity.getDictPlatform());
        soB2cDeliveryEntity.setShopId(soB2cEntity.getShopId());
        soB2cDeliveryEntity.setShopName(soB2cEntity.getShopName());

        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        soB2cDeliveryEntity.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        //根据物流跟踪单号查询物流单详情
//        LogisticsBillDTO.BaseDTO logisticsBillByTrackNo = logisticsBillFeign.getLogisticsBillByTrackNo(soB2cLogisticsEntity.getCode());
        soB2cDeliveryEntity.setTransportNo("test123456");
    }
}
