package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.InvoiceSettingConverter;
import com.erp.server.oms.mapper.CfgInvoiceSettingMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.entity.AddCompanyDTO;
import com.sdk.third.tf.entity.UpdateCompanyDTO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2025-04-07
 */
@Slf4j
@Service
public class CfgInvoiceSettingServiceImpl extends SuperServiceImpl<CfgInvoiceSettingMapper, CfgInvoiceSettingEntity> implements CfgInvoiceSettingService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;

    @Resource
    DictBasicService dictBasicService;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private TfFiscalService tfFiscalService;
    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    @Override
    public PagingVO<CfgInvoiceSettingDTO.PagingViewDTO> paging(PagingDTO<CfgInvoiceSettingDTO.PagingParamDTO> dto) {
        CfgInvoiceSettingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<CfgInvoiceSettingDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        // 直接通过一个SQL查询获取所有数据
        IPage<CfgInvoiceSettingDTO.PagingViewDTO> pageData = baseMapper.pagingWithShops(query, params);
        return new PagingVO(pageData);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgInvoiceSettingDTO.AddDTO dto) {
        //校验CNPJ、邮编格式
        checkCode(dto.getLeiCode(), dto.getPostCode());
        //校验CNPJ是否唯一
        if (StrUtil.isNotBlank(dto.getLeiCode())) {
            LambdaQueryWrapper<CfgInvoiceSettingEntity> queryWrapper = new LambdaQueryWrapper<CfgInvoiceSettingEntity>().eq(CfgInvoiceSettingEntity::getLeiCode, dto.getLeiCode());
            if (super.count(queryWrapper) > 0) {
                throw new ServiceException("CNPJ 已存在，不能重复");
            }
        }
        //保存
        CfgInvoiceSettingEntity entity = new CfgInvoiceSettingEntity();
        BeanMapperUtils.copy(dto, entity);
        entity.setCertificateUrl(dto.getAttachmentUrlList().get(0));
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException("发票设置保存失败");
        }
        //保存附件
        Class<CfgInvoiceSettingEntity> settingEntityClass = CfgInvoiceSettingEntity.class;
        TableName tableName = settingEntityClass.getDeclaredAnnotation(TableName.class);
        String type = tableName.value();
        omsAttachmentService.batchSaveOrUpdate(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, entity.getId());
        //CfgInvoiceSettingEntity -> AddCompanyDTO
        AddCompanyDTO addCompanyDTO = InvoiceSettingConverter.INSTANCE.invoiceSettinToAddCompanyDTOTo(entity);
        //username
        addCompanyDTO.setUsername(addCompanyDTO.getRazaoSocial().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ""));
        //调用TF
        String token = tfFiscalService.createCompany(addCompanyDTO);
        //更新token
        this.update(
                new LambdaUpdateWrapper<CfgInvoiceSettingEntity>()
                        .eq(CfgInvoiceSettingEntity::getId, entity.getId())
                        .set(CfgInvoiceSettingEntity::getToken, token)
        );
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "VAT发票设置", entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgInvoiceSettingDTO.UpdateDTO dto) {
        //查询旧数据
        CfgInvoiceSettingEntity old = super.getById(dto.getId());
        //判断旧数据是否存在
        if (ObjectUtil.isEmpty(old)) {
            throw new ServiceException("此发票设置不存在");
        }
        //校验CNPJ、是否被修改
        if (!StrUtil.equals(old.getLeiCode(), dto.getLeiCode())) {
            throw new ServiceException("CNPJ 不允许修改");
        }
        //校验邮编格式
        checkCode(dto.getLeiCode(), dto.getPostCode());
        //转换格式
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = BeanMapperUtils.map(CfgInvoiceSettingEntity.class, dto);
        //禁止更新以下字段
        cfgInvoiceSettingEntity.setStartCode(old.getStartCode());
        cfgInvoiceSettingEntity.setLeiCode(old.getLeiCode());
        cfgInvoiceSettingEntity.setStateTaxNo(old.getStateTaxNo());
        cfgInvoiceSettingEntity.setNo(old.getNo());
        cfgInvoiceSettingEntity.setCertificateUrl(dto.getAttachmentUrlList().get(0));
        cfgInvoiceSettingEntity.setToken(old.getToken());
        log.info("编辑 开始修改发票设置数据，id：【{}】", old.getId());
        //修改
        boolean update = super.updateById(cfgInvoiceSettingEntity);
        if (!update) {
            throw new ServiceException("发票设置保存失败");
        }
        //保存附件
        OmsAttachmentEntity omsAttachmentEntity = omsAttachmentService.getOne(new LambdaQueryWrapper<OmsAttachmentEntity>()
                .eq(OmsAttachmentEntity::getAttachUrl, dto.getAttachmentUrlList().get(0)).eq(OmsAttachmentEntity::getIsDeleted, false));
        if (ObjectUtil.isEmpty(omsAttachmentEntity)){
            Class<CfgInvoiceSettingEntity> settingEntityClass = CfgInvoiceSettingEntity.class;
            TableName tableName = settingEntityClass.getDeclaredAnnotation(TableName.class);
            String type = tableName.value();
            omsAttachmentService.batchSaveOrUpdate(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, cfgInvoiceSettingEntity.getId());
        }
        //调用TF
        UpdateCompanyDTO updateCompanyDTO = InvoiceSettingConverter.INSTANCE.invoiceSettinToUpdateCompanyDTOTo(cfgInvoiceSettingEntity);
        updateCompanyDTO.setUsername(updateCompanyDTO.getRazaoSocial().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ""));
        tfFiscalService.updateCompany(updateCompanyDTO);
        //保存日志
        log.info("编辑 开始记录发票设置日志数据，id：【{}】", cfgInvoiceSettingEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgInvoiceSettingEntity.getId(), "发票设置");
        operateLogService.addModuleOperateLogByObj(old, cfgInvoiceSettingEntity, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), cfgInvoiceSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean delete(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        List<CfgInvoiceSettingEntity> entities = this.list(new LambdaQueryWrapper<CfgInvoiceSettingEntity>().eq(CfgInvoiceSettingEntity::getId, ids.get(0)));
        boolean remove = this.lambdaUpdate().in(CfgInvoiceSettingEntity::getId, ids).remove();
        if (remove) {
            //删除附件
            omsAttachmentService.deleteByBusinessIds(ids);
            //删除明细
            cfgInvoiceSettingDetailService.delateByMainIds(ids, Boolean.TRUE);
            //调用TF
            tfFiscalService.deleteCompany(entities.get(0).getLeiCode());
        }
        return remove;
    }

    /**
     * @description: 详情
     * @author: hcg
     * @date: 2025/4/17 13:20
     **/
    @Override
    public CfgInvoiceSettingDTO.ViewDTO view(String id) {
        CfgInvoiceSettingEntity old = super.getById(id);
        CfgInvoiceSettingDTO.ViewDTO dto = new CfgInvoiceSettingDTO.ViewDTO();
        BeanUtil.copyProperties(old, dto);
        if (ObjectUtil.isEmpty(old)) {
            throw new ServiceException("此发票设置不存在");
        }
        List<OmsAttachmentDTO.UpdateDTO> attchmentList = omsAttachmentService.getByBusinessIds(Arrays.asList(id));
        List<String> attachmentUrlList = attchmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attchmentList.stream().map(OmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        dto.setAttachmentUrlList(attachmentUrlList);
        dto.setAttachmentNameList(attachmentNameList);
        return dto;
    }

    /**
     * @description: 禁用
     * @author: hcg
     * @date: 2025/4/17 13:21
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(CfgInvoiceSettingDTO.UpdateStatusDTO dto) {
        CfgInvoiceSettingEntity invoiceSettingEntity = baseMapper.selectById(dto.getId());
        if (ObjectUtil.isEmpty(invoiceSettingEntity)) {
            throw new ServiceException("当前发票设置不存在");
        }
        if (invoiceSettingEntity.getDisabled().equals(dto.getDisabled())) {
            throw new ServiceException("当前状态与修改状态一致，无需修改");
        }
        CfgInvoiceSettingEntity entity = BeanUtil.copyProperties(dto, CfgInvoiceSettingEntity.class);
        return baseMapper.updateById(entity) > 0;
    }

    /**
     * @description: 获取公司名称下拉框
     * @param: []
     * @author: hcg
     * @date: 2025/4/17 13:21
     **/
    @Override
    public List<CfgInvoiceInvalidDTO.DropDownDTO> getCompanyName() {
        List<CfgInvoiceSettingEntity> invoiceSettingEntityList = this.list(new LambdaQueryWrapper<CfgInvoiceSettingEntity>()
                .select(CfgInvoiceSettingEntity::getCompanyName, CfgInvoiceSettingEntity::getId, CfgInvoiceSettingEntity::getDisabled));
        List<CfgInvoiceInvalidDTO.DropDownDTO> dropDownList = invoiceSettingEntityList.stream()
                .map(entity -> {
                    CfgInvoiceInvalidDTO.DropDownDTO dto = new CfgInvoiceInvalidDTO.DropDownDTO();
                    dto.setCode(entity.getCompanyName());
                    dto.setValue(entity.getId());
                    dto.setDisabled(entity.getDisabled());
                    return dto;
                })
                .collect(Collectors.toList());
        return dropDownList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSerialNo(CfgInvoiceSettingDTO.UpdateSerialDTO dto) {
        //查询旧数据
        CfgInvoiceSettingEntity old = super.getById(dto.getId());
        //判断旧数据是否存在
        if (ObjectUtil.isEmpty(old)) {
            throw new ServiceException("此发票设置不存在");
        }
        Integer oldNo = old.getNo();
        //- 新序列号：不能与当前序列号一致，只能填入数字
        if (Objects.equals(oldNo, dto.getNo())) {
            throw new ServiceException("新序列号与当前序列号一致，无需修改");
        }
        String oldStartCode = old.getStartCode();
        //- 起始编号：只能填入数字
        if (!StrUtil.isNumeric(dto.getStartCode())) {
            throw new ServiceException("起始编号只能填入数字");
        }
        old.setNo(dto.getNo());
        old.setStartCode(dto.getStartCode());
        this.lambdaUpdate().eq(CfgInvoiceSettingEntity::getId, dto.getId())
                .set(CfgInvoiceSettingEntity::getNo, dto.getNo())
                .set(CfgInvoiceSettingEntity::getStartCode, dto.getStartCode()).update();
        //调用TF
        UpdateCompanyDTO updateCompanyDTO = InvoiceSettingConverter.INSTANCE.invoiceSettinToUpdateCompanyDTOTo(old);
        updateCompanyDTO.setUsername(updateCompanyDTO.getRazaoSocial().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ""));
        tfFiscalService.updateCompany(updateCompanyDTO);
        //保存日志
        log.info("序列号修改 开始记录发票设置日志数据，id：【{}】", old.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的发票设置单据,新序列号由【{}】改为【{}】，起始编号由【{}】改为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), old.getId(), oldNo,dto.getNo(),oldStartCode,dto.getStartCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_VAT_INVOICE.getCode(), old.getId(), "序列号修改");
    }

    @Override
    public void updateSerialNoById(String id, Integer no, Integer startCode) {
        if (CharSequenceUtil.isBlank(id)){
            return;
        }
        if (Objects.isNull(no) && Objects.isNull(startCode)){
            return;
        }
        this.lambdaUpdate().eq(CfgInvoiceSettingEntity::getId, id)
               .set(Objects.nonNull(no),CfgInvoiceSettingEntity::getNo,no)
               .set(Objects.nonNull(startCode),CfgInvoiceSettingEntity::getStartCode,String.valueOf(startCode + 1)).update();
    }

    /**
     * @description: 校验邮编、法人国家经济号
     * @author: hcg
     * @date: 2025/4/17 13:05
     **/
    private void checkCode(String leiCode, String postCode) {
        if (StrUtil.isNotBlank(leiCode)) {
            // 示例格式：XX XXX XXX/XXX，例如：12 345 678/901
            String regex = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$";
            if (!leiCode.matches(regex)) {
                throw new ServiceException("CNPJ 格式不正确，格式应为：XX XXX XXX/XXXX-XX");
            }
        }
        if (StrUtil.isNotBlank(postCode)) {
            // 示例格式：XXXXX-XXX
            String regex = "^\\d{5}-\\d{3}$";
            if (!postCode.matches(regex)) {
                throw new ServiceException("邮编格式不正确，格式应为：XXXXX-XXX");
            }
        }
    }
}
