package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.model.oms.entity.RefundOrderDetailEntity;
import com.erp.model.oms.entity.RefundOrderEntity;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.mapper.RefundOrderMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RefundOrderDetailService;
import com.erp.server.oms.service.RefundOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_BI_RETURN_INFO;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REQUISITION_APPLICATION;

/**
 * <p>
 * 退款订单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-25
 */
@Service
public class RefundOrderServiceImpl extends SuperServiceImpl<RefundOrderMapper, RefundOrderEntity> implements RefundOrderService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private RefundOrderDetailService refundOrderDetailService;

    /**
     * 售后订单分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-25 14:09
     */
    @Override
    public PagingVO<RefundOrderDTO.PagingViewDTO> paging(PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        RefundOrderDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<RefundOrderDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportExcel(RefundOrderDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("退款订单导出", EXPORT_BI_RETURN_INFO.getCode(), dto);
    }

    @Override
    public PagingVO<RefundOrderDTO.PagingViewDTO> exportRefund(PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        RefundOrderDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<RefundOrderDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RefundOrderEntity refundOrderEntity, List<RefundOrderDetailEntity> refundOrderDetailEntityList) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TKD);
        this.save(refundOrderEntity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增退款单【%s】", code), ModuleTypeEnum.REFUND_ORDER.getCode(), refundOrderEntity.getId(), "新增操作");

        refundOrderDetailEntityList.forEach(v->v.setMainId(refundOrderEntity.getId()));
        refundOrderDetailService.saveBatch(refundOrderDetailEntityList);
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<RefundOrderDTO.PagingViewDTO> list) {
        String key = DictBasicTypeEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
        List<String> soIds = list.stream().map(v->v.getSoId()).distinct().collect(Collectors.toList());
        List<String> skuIds = list.stream().map(v->v.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoOutstockDetailEntity> allOutList = soOutstockFeign.listDetailBySoIds(soIds);
        for (RefundOrderDTO.PagingViewDTO item : list) {
            String dictPlatform = item.getDictPlatform();
            String platformName = dictBasicList.stream().filter(d -> d.getValue().equals(dictPlatform)).
                    map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            item.setPlatformName(platformName);
            String status = item.getStatus();
            String name = RefundOrderStatusEnum.getName(status);
            item.setStatusName(name);
            SkuVO skuVO = skuVoList.stream().filter(s->s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            item.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> outList = allOutList.stream().filter(s->s.getSoId().equals(item.getSoId()) && s.getSkuId().equals(item.getSkuId())).collect(Collectors.toList());
            item.setOutQty(outList.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));
        }
    }
}
