package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.DeclareLabelTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cDeclareProductMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import com.erp.server.oms.service.SoB2cReceiverService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_DECLARE;

/**
 * <p>
 * B2C销售订单申报产品信息表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
 */
@Slf4j
@Service
public class SoB2cDeclareProductServiceImpl extends SuperServiceImpl<SoB2cDeclareProductMapper, SoB2cDeclareProductEntity> implements SoB2cDeclareProductService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    @Lazy
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    @Lazy
    private SoB2cService soB2cService;
    @Resource
    @Lazy
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeclareProductDTO.AddDTO addDTO) {
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = new SoB2cDeclareProductEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeclareProductEntity);

        // 数据处理
        handleData(soB2cDeclareProductEntity);

        log.info("开始新增B2C销售订单申报产品信息单");
        boolean save = super.save(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C销售订单申报产品信息单" , soB2cDeclareProductEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeclareProductEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeclareProductEntity.getId(), soB2cDeclareProductEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO update(SoB2cDeclareProductDTO.UpdateDTO updateDTO) {
        SoB2cDeclareProductEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单申报产品信息单");
        }
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = B2cOrderConverter.INSTANCE.convertDeclareProductByDto(updateDTO);
        //销售订单
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getSoId());
        if (!CharSequenceUtil.equals(soB2cEntity.getApproveStatus().getCode(), ApproveStatusEnum.APPROVE.getCode())
            || !CharSequenceUtil.equals(soB2cEntity.getBillStatus(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode())) {
            throw new ServiceException("仅支持已审核-配货中的订单可操作");
        }

        // 数据处理
        handleData(soB2cDeclareProductEntity);
        log.info("编辑 开始修改B2C销售订单申报产品信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }
        // 记录主单操作日志
            log.info("编辑 开始记录B2C销售订单申报产品信息单日志数据，id：【{}】", soB2cDeclareProductEntity.getId());
            String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cDeclareProductEntity.getId(), "B2C销售订单申报产品信息单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeclareProductEntity, ModuleTypeEnum.SO_B2C_DECLARE.getCode(), soB2cDeclareProductEntity.getSoId(), null, msg, "批量修改报关");
        return BatchResultDTO.success(updateDTO.getId(), soB2cEntity.getCode(), "报关修改操作");
    }
    /**
     * 根据销售订单id获取申报信息
     * @param id
     * @return
     */
    @Override
    public List<SoB2cDeclareProductEntity> listBySoId(String id) {
        if (StringUtils.isEmpty(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SoB2cDeclareProductEntity::getSoId,id).list();
    }

    /**
     * 根据订单id删除申报信息
     * @param id
     */
    @Override
    public void removeBySoId(String id) {
        if (StringUtils.isNotEmpty(id)){
            lambdaUpdate().eq(SoB2cDeclareProductEntity::getSoId, id).remove();
        }
    }

    @Override
    public List<SoB2cDeclareProductDTO.ViewDTO> listViewBySoIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<SoB2cDeclareProductDTO.ViewDTO> viewDTOS = baseMapper.listViewBySoIds(ids);
        buildDeclareProductInfo(viewDTOS);
        return viewDTOS;
    }

    private void buildDeclareProductInfo(List<SoB2cDeclareProductDTO.ViewDTO> viewDTOS) {
        if (CollectionUtils.isEmpty(viewDTOS)){
            return;
        }
        viewDTOS.forEach(viewDTO ->{
            viewDTO.setDeclareLabelName(DeclareLabelTypeEnum.getName(viewDTO.getDeclareLabel()));
        });
    }

    @Override
    public Boolean exportExcel(SoB2cDeclareProductDTO.ListDTO dto) {
        downloadTaskFeign.saveExportTask("申报信息", EXPORT_OMS_SO_B2C_DECLARE.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2cDeclareProductDTO.ViewDTO> exportSoB2CDeclare(PagingDTO<SoB2cDeclareProductDTO.ListDTO> dto) {
        if (CollectionUtils.isEmpty(dto.getParams().getIds())){
            return new PagingVO<>();
        }
        Page<SoB2cDeclareProductDTO.ViewDTO> page = baseMapper.listViewBySoIds(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams().getIds());
        buildDeclareProductInfo(page.getRecords());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.ERROR_DECLARE_NOT_EXIST);
        }
        return new PagingVO<>(page);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeclareProductEntity soB2cDeclareProductEntity) {
        if (StringUtils.isEmpty(soB2cDeclareProductEntity.getSoId()) || StringUtils.isEmpty(soB2cDeclareProductEntity.getSkuId())){
            return;
        }
        // 重置申报标识
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soB2cDeclareProductEntity.getSoId());
        String country = Objects.nonNull(receiverEntity)?Objects.nonNull(receiverEntity.getCountry())?receiverEntity.getCountry():"":"";

        List<ProductCustomsEntity> productCustomsList = plmTaskFeign.listProductCustomsBySkuIds(ProductCustomsSkuDTO.builder()
                .skuIds(Collections.singletonList(soB2cDeclareProductEntity.getSkuId())).country(country).build());

        ProductCustomsEntity customs = soB2cService.getCustomsByCountry(country,soB2cDeclareProductEntity.getSkuId(),productCustomsList);

        //申报标签
        BigDecimal toDeclarePrice = customs.getToDeclarePrice();
        int compare = MathUtil.compareTo(soB2cDeclareProductEntity.getToDeclarePrice(), toDeclarePrice );
        if (compare > 0){
            //高申报
            soB2cDeclareProductEntity.setDeclareLabel(DeclareLabelTypeEnum.HIGH.getCode());
        }else if (compare < 0){
            //低申报
            soB2cDeclareProductEntity.setDeclareLabel(DeclareLabelTypeEnum.LOW.getCode());
        }else {
            //正常申报
            soB2cDeclareProductEntity.setDeclareLabel(DeclareLabelTypeEnum.NORMAL.getCode());
        }
    }
}
