package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.CfgVatInvoiceTemplateTypeEnum;
import com.erp.model.oms.enums.SoB2cVatStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CfgVatInvoiceMapper;
import com.erp.server.oms.service.CfgVatInvoiceService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * VAT发票设置 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@Service
public class CfgVatInvoiceServiceImpl extends SuperServiceImpl<CfgVatInvoiceMapper, CfgVatInvoiceEntity> implements CfgVatInvoiceService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileTemplateFeign fileTemplateFeign;
    @Lazy
    @Resource
    private SoB2cService soB2cService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgVatInvoiceDTO.AddDTO addDTO) {
        CfgVatInvoiceEntity cfgVatInvoiceEntity = new CfgVatInvoiceEntity();
        BeanMapperUtils.copy(addDTO, cfgVatInvoiceEntity);
        // 数据处理
        handleData(cfgVatInvoiceEntity);
        log.info("开始新增VAT发票设置");
        boolean save = super.save(cfgVatInvoiceEntity);
        if(!save) {
            throw new ServiceException("VAT发票设置保存失败");
        }
        //启用配置
        updateSoB2CState(cfgVatInvoiceEntity.getShopId(),cfgVatInvoiceEntity.getDisabled(),cfgVatInvoiceEntity.getEnableTime());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "VAT发票设置" , cfgVatInvoiceEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgVatInvoiceEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgVatInvoiceEntity.getId(), cfgVatInvoiceEntity.getId());
    }

    /**
     * 根据店铺id更新销售订单发票配置状态
     *
     * @param shopId
     * @param disabled
     * @param enableTime
     */
    @Override
    public void updateSoB2CState(String shopId, Boolean disabled, LocalDateTime enableTime) {
        if (disabled){
            return;
        }
        soB2cService.updateFbaNotVatInvoice(shopId,enableTime, SoB2cVatStatusEnum.PENDING.getCode());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgVatInvoiceDTO.UpdateDTO addOrUpdateDTO) {
        CfgVatInvoiceEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "VAT发票设置"));
        CfgVatInvoiceEntity cfgVatInvoiceEntity =  BeanMapperUtils.map(CfgVatInvoiceEntity.class, addOrUpdateDTO);
        //编辑时不修改字段重新赋值
        cfgVatInvoiceEntity.setShopId(old.getShopId());
        cfgVatInvoiceEntity.setShopCountryId(old.getShopCountryId());
        cfgVatInvoiceEntity.setDisabled(old.getDisabled());
        // 数据处理
        handleData(cfgVatInvoiceEntity);
        log.info("编辑 开始修改VAT发票设置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgVatInvoiceEntity);
        if(!save) {
            throw new ServiceException("VAT发票设置保存失败");
        }
        updateSoB2CState(cfgVatInvoiceEntity.getShopId(),cfgVatInvoiceEntity.getDisabled(),cfgVatInvoiceEntity.getEnableTime());
        // 记录主单操作日志
        log.info("编辑 开始记录VAT发票设置日志数据，id：【{}】", cfgVatInvoiceEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgVatInvoiceEntity.getId(), "VAT发票设置");
        operateLogService.addModuleOperateLogByObj(old, cfgVatInvoiceEntity, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgVatInvoiceEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgVatInvoiceDTO.PagingViewDTO> paging(PagingDTO<CfgVatInvoiceDTO.PagingParamDTO> dto) {
        CfgVatInvoiceDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgVatInvoiceDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgVatInvoiceDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(CfgVatInvoiceEntity entity, Boolean disabled) {
        if (!entity.getDisabled().equals(disabled)){
            //更新配置
            this.lambdaUpdate().eq(CfgVatInvoiceEntity::getId, entity.getId()).set(CfgVatInvoiceEntity::getDisabled, disabled).update();
            String msg = StrUtil.format("用户【{}】修改【{}】由【{}】改为【{}】", UserContext.getDefaultLoginUser().getUserName(), "VAT发票设置" , entity.getDisabled() ? "禁用" : "启用", disabled? "禁用" : "启用");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), entity.getId(), "编辑操作");
        }
        //启用配置
        updateSoB2CState(entity.getShopId(),disabled,entity.getEnableTime());
    }

    @Override
    public CfgVatInvoiceEntity getEnableCfgByShopId(String shopId) {
        List<CfgVatInvoiceEntity> list = this.lambdaQuery().eq(CfgVatInvoiceEntity::getShopId, shopId).list();
        if (CollUtil.isEmpty(list)){
            return null;
        }
        //获取启用时间 最新的一条
        CfgVatInvoiceEntity entity = list.stream().filter(e -> !e.getDisabled() && e.getEnableTime().isBefore(LocalDateTime.now())).max(Comparator.comparing(CfgVatInvoiceEntity::getEnableTime)).orElse(null);
        if (Objects.isNull(entity)){
            //返回最新一条配置
            CfgVatInvoiceEntity entity1 = list.stream().max(Comparator.comparing(CfgVatInvoiceEntity::getCreateTime)).orElse(null);
            return entity1;
        }
        return entity;
    }

    @Override
    public List<CfgVatInvoiceEntity> listCfgByShopIds(List<String> shopIdList) {
        if (CollUtil.isEmpty(shopIdList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(CfgVatInvoiceEntity::getShopId,shopIdList).list();
    }

    @Override
    public String createVatInvoicePdf(CfgVatInvoiceDTO.InvoiceTemplateDTO invoiceTemplateDTO) {
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.CFG_VAT_INVOICE);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.CFG_VAT_INVOICE.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
        if (inputStream == null) {
            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
            throw new ServiceException("获取fastdfs文件为空");
        }
        Map<String, Object> map = BeanUtil.beanToMap(invoiceTemplateDTO);
        JRBeanCollectionDataSource detailDTOS = new JRBeanCollectionDataSource(invoiceTemplateDTO.getDetailDTOS());
        map.put("detailDTOS", detailDTOS);
        JRBeanCollectionDataSource totalDTOS = new JRBeanCollectionDataSource(Collections.singleton(invoiceTemplateDTO.getTotalDTOS()));
        map.put("totalDTOS", totalDTOS);
        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map, Collections.singletonList(invoiceTemplateDTO));
        return FastDFSClientUtil.uploadFile(bytes, invoiceTemplateDTO.getInvoiceCode() + ".pdf", null);
    }

    @Override
    public Boolean delete(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return Boolean.TRUE;
        }
        return this.lambdaUpdate().in(CfgVatInvoiceEntity::getId, ids).remove();
    }

    @Override
    public List<BatchResultDTO> delete(List<String> ids, Boolean isReturnDetail) {
        // 返回成功结果
        List<CfgVatInvoiceEntity> list = this.listByIds(ids);
        boolean remove = this.lambdaUpdate().in(CfgVatInvoiceEntity::getId, ids).remove();

        List<BatchResultDTO>resultDTOList=new ArrayList<>();
        for (CfgVatInvoiceEntity entity : list) {
            ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, entity.getShopId());
            if (!Objects.isNull(shopInfo)){
                resultDTOList.add(BatchResultDTO.success(entity.getId(), shopInfo.getName(), "删除成功"));
            }else {
                resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getShopId(), "删除成功"));
            }
        }
        return resultDTOList;
    }

    private void fillList(List<CfgVatInvoiceDTO.PagingViewDTO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        List<DictCountryDTO.ListDTO> listDTOS = sysUserFeign.countryList();
        Map<String, String> countryMap = listDTOS.stream().collect(Collectors.toMap(DictCountryDTO.ListDTO::getId, DictCountryDTO.ListDTO::getNameCn));
        records.forEach(pagingViewDTO -> {
            pagingViewDTO.setShopCountryName(countryMap.getOrDefault(pagingViewDTO.getShopCountryId(), CharSequenceUtil.EMPTY));
            pagingViewDTO.setCountryName(countryMap.getOrDefault(pagingViewDTO.getCountryId(), CharSequenceUtil.EMPTY));
            pagingViewDTO.setTemplateTypeName(CfgVatInvoiceTemplateTypeEnum.getName(pagingViewDTO.getTemplateType()));
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgVatInvoiceEntity cfgVatInvoiceEntity) {
        String shopId = cfgVatInvoiceEntity.getShopId();
        ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
        if (Objects.isNull(shopInfo)){
            throw new ServiceException("店铺记录不存在");
        }
        if (!PlatformDictEnum.AMAZON.getCode().equals(shopInfo.getDictPlatform())){
            throw new ServiceException("只允许亚马逊平台店铺新增VAT发票配置");
        }
        if (CharSequenceUtil.isBlank(cfgVatInvoiceEntity.getId())){
            Integer count = this.lambdaQuery().eq(CfgVatInvoiceEntity::getShopId, shopId).count();
            if (count > 0){
                throw new ServiceException("店铺【{}】已存在发票配置",shopInfo.getName());
            }
        }
        if (Objects.isNull(cfgVatInvoiceEntity.getDisabled())){
            cfgVatInvoiceEntity.setDisabled(Boolean.FALSE);
        }
        if (Objects.isNull(cfgVatInvoiceEntity.getIsAutoUpload())){
            cfgVatInvoiceEntity.setIsAutoUpload(Boolean.TRUE);
        }
        if (CharSequenceUtil.isBlank(cfgVatInvoiceEntity.getTemplateType())){
            cfgVatInvoiceEntity.setTemplateType(CfgVatInvoiceTemplateTypeEnum.ERP.getCode());
        }
        //详细地址+城市+州/省+邮编+国家
        cfgVatInvoiceEntity.setCompanyAddress(cfgVatInvoiceEntity.getAddress() + "," + cfgVatInvoiceEntity.getCity() + "," + cfgVatInvoiceEntity.getProvince() + "," + cfgVatInvoiceEntity.getPostCode() + "," + cfgVatInvoiceEntity.getCountryId());
    }
}
