package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cRefundDTO;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.erp.model.oms.entity.SoB2cRefundEntity;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.oms.mapper.SoB2cRefundMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cRefundDetailService;
import com.erp.server.oms.service.SoB2cRefundService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_BI_RETURN_INFO;

/**
 * <p>
 * 退款订单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-25
 */
@Service
public class SoB2cRefundServiceImpl extends SuperServiceImpl<SoB2cRefundMapper, SoB2cRefundEntity> implements SoB2cRefundService {

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
    private SoB2cRefundDetailService soB2cRefundDetailService;

    /**
     * 售后订单分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-25 14:09
     */
    @Override
    public PagingVO<SoB2cRefundDTO.PagingViewDTO> paging(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        SoB2cRefundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<SoB2cRefundDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2cRefundDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoB2cRefundDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportExcel(SoB2cRefundDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveExportTask("退款订单导出", EXPORT_BI_RETURN_INFO.getCode(), dto);
    }

    @Override
    public PagingVO<SoB2cRefundDTO.PagingViewDTO> exportRefund(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        SoB2cRefundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<SoB2cRefundDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2cRefundDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoB2cRefundDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TKD);
        soB2cRefundEntity.setCode(code);
        this.save(soB2cRefundEntity);

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增退款单【%s】", code), ModuleTypeEnum.REFUND_ORDER.getCode(), soB2cRefundEntity.getId(), "新增操作");
        if(CollectionUtils.isEmpty(soB2cRefundDetailEntityList)){
            return;
        }
        soB2cRefundDetailEntityList.forEach(v->v.setMainId(soB2cRefundEntity.getId()));
        soB2cRefundDetailService.saveBatch(soB2cRefundDetailEntityList);
    }

    @Override
    public SoB2cRefundEntity getByPlatformRefundCode(String platformRefundNo) {
        if(StringUtils.isBlank(platformRefundNo)){
            return null;
        }
        return lambdaQuery().eq(SoB2cRefundEntity::getPlatformRefundNo,platformRefundNo).last( SqlConstants.LIMIT_1).one();
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<SoB2cRefundDTO.PagingViewDTO> list) {
        List<String> soIds = list.stream().map(v->v.getSoId()).distinct().collect(Collectors.toList());
        List<String> skuIds = list.stream().map(v->v.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoOutstockDetailEntity> allOutList = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoB2cRefundDTO.PagingViewDTO item : list) {
            String dictPlatform = item.getDictPlatform();
            item.setPlatformName(PlatformDictEnum.getNameByCode(dictPlatform));
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
