package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.enums.FbaDeliveryStatusEnum;
import com.erp.model.wms.enums.FbaPlatformShipmentStatusEnum;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.erp.server.wms.service.FbaShipmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA货件表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaShipmentServiceImpl extends SuperServiceImpl<FbaShipmentMapper, FbaShipmentEntity> implements FbaShipmentService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private FbaShipmentDetailService fbaShipmentDetailService;

    @Override
    public PagingVO<FbaShipmentDTO.ListDTO> paging(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<FbaShipmentDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<FbaShipmentDTO.ListDTO> records) {

        for (FbaShipmentDTO.ListDTO record : records) {
            //设置发货状态中文
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));
            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总 TODO
            record.setDeliveryQty(0);
            //签收数量 QuantityReceived TODO
            record.setReceiveQty(0);
            //在途数量 QuantityReceived-发货数量，不为0时显示红色 TODO
            record.setTransportQty(0);
        }
    }

    @Override
    public Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.DeliverRecordView> listDeliverRecord(String id) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.ShipmentStatusRecordView> listShipmentStatusRecord(String code) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(PagingDTO<FbaShipmentDTO.ReceiveRecordParam> dto) {
        return null;
    }

    @Override
    public FbaShipmentDTO.ViewDTO view(String id) {

        FbaShipmentEntity entity = this.getById(id);

        FbaShipmentDTO.ViewDTO viewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentToViewDTO(entity);

        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(id));


        return null;
    }

    @Override
    public Boolean finishShipment(BaseIdsDTO.IdsDTO ids) {
        return null;
    }

    @Override
    public List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(BaseIdsDTO.IdsDTO ids) {
        return null;
    }

    @Override
    public Boolean generateDeliverSave(List<FbaShipmentDTO.GenerateDeliverView> list) {
        return null;
    }
}
