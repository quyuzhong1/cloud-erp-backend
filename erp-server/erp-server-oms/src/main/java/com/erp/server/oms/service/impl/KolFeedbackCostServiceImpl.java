package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.oms.dto.KolFeedbackDTO;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.erp.server.oms.mapper.KolFeedbackCostMapper;
import com.erp.server.oms.service.KolFeedbackCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolFeedbackCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.erp.model.scm.enums.ModuleTypeEnum;
import cn.hutool.crypto.digest.DigestUtil;
import com.erp.server.oms.service.CfgKolOptionService;
import com.erp.model.oms.entity.CfgKolOptionEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.model.sys.entity.DictCurrencyEntity;
import org.apache.commons.lang3.StringUtils;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.common.business.dto.base.BatchResultDTO;
/**
 * <p>
 * KOL回片费用表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolFeedbackCostServiceImpl extends SuperServiceImpl<KolFeedbackCostMapper, KolFeedbackCostEntity> implements KolFeedbackCostService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgKolOptionService cfgKolOptionService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackCostDTO.AddDTO addDTO) {
        KolFeedbackCostEntity kolFeedbackCostEntity = new KolFeedbackCostEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackCostEntity);

        // 数据处理
        handleData(kolFeedbackCostEntity);

        // 校验唯一性：费用名称ID + urlHash
        checkUnique(kolFeedbackCostEntity, null);

        log.info("开始新增KOL回片费用单");
        boolean save = super.save(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片费用单" , kolFeedbackCostEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_FEEDBACK_COST.getCode(), kolFeedbackCostEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(kolFeedbackCostEntity.getId(), kolFeedbackCostEntity.getId());
    }

    /**
    * 批量新增
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchAdd(KolFeedbackCostDTO.BatchAddDTO dto) {
        List<KolFeedbackCostDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolFeedbackCostDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("KOL回片费用批量新增失败", e);
                String remark = addDTO.getRemark() != null ? addDTO.getRemark() : "";
                addResult = BatchResultDTO.fail("", remark, e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolFeedbackCostDTO.UpdateDTO addOrUpdateDTO) {
        KolFeedbackCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "KOL回片费用单"));
        KolFeedbackCostEntity kolFeedbackCostEntity =  BeanMapperUtils.map(KolFeedbackCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackCostEntity);
        
        // 校验唯一性：费用名称ID + urlHash
        checkUnique(kolFeedbackCostEntity, old.getId());
        
        log.info("编辑 开始修改KOL回片费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录KOL回片费用单日志数据，id：【{}】", kolFeedbackCostEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackCostEntity.getId(), "KOL回片费用单");
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackCostEntity, ModuleTypeEnum.KOL_FEEDBACK_COST.getCode(), kolFeedbackCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolFeedbackCostDTO.ListDTO> paging(PagingDTO<KolFeedbackCostDTO.ParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<KolFeedbackCostDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<KolFeedbackCostDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackCostEntity kolFeedbackCostEntity) {
        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolFeedbackCostEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolFeedbackCostEntity.getUrl());
            kolFeedbackCostEntity.setUrlHash(urlHash);
        }

        // costType 通过 costTypeId 查询 CfgKolOptionService
        if (StrUtil.isNotBlank(kolFeedbackCostEntity.getCostTypeId())) {
            try {
                CfgKolOptionEntity cfgKolOptionEntity = cfgKolOptionService.getById(kolFeedbackCostEntity.getCostTypeId());
                if (cfgKolOptionEntity != null && StrUtil.isNotBlank(cfgKolOptionEntity.getName())) {
                    kolFeedbackCostEntity.setCostType(cfgKolOptionEntity.getName());
                }
            } catch (Exception e) {
                log.warn("获取费用类型信息失败，costTypeId: {}", kolFeedbackCostEntity.getCostTypeId(), e);
            }
        }

        // 计算本位币金额：baseAmount = originalAmount * exchangeRate
        if (kolFeedbackCostEntity.getOriginalAmount() != null && kolFeedbackCostEntity.getExchangeRate() != null) {
            BigDecimal baseAmount = MathUtil.multiplyWithTwo(kolFeedbackCostEntity.getOriginalAmount(), kolFeedbackCostEntity.getExchangeRate());
            kolFeedbackCostEntity.setBaseAmount(baseAmount);
        }
    }

    /**
     * 校验唯一性：费用名称ID + urlHash
     * @param kolFeedbackCostEntity 当前实体
     * @param excludeId 排除的ID（修改时使用，排除当前记录）
     */
    private void checkUnique(KolFeedbackCostEntity kolFeedbackCostEntity, String excludeId) {
        if (StrUtil.isBlank(kolFeedbackCostEntity.getCostTypeId()) || StrUtil.isBlank(kolFeedbackCostEntity.getUrlHash())) {
            return;
        }

        // 查询是否存在相同的费用名称ID和urlHash的记录
        KolFeedbackCostEntity existEntity = lambdaQuery()
                .eq(KolFeedbackCostEntity::getCostTypeId, kolFeedbackCostEntity.getCostTypeId())
                .eq(KolFeedbackCostEntity::getUrlHash, kolFeedbackCostEntity.getUrlHash())
                .eq(KolFeedbackCostEntity::getIsDeleted, false)
                .ne(excludeId != null, KolFeedbackCostEntity::getId, excludeId)
                .one();

        if (existEntity != null) {
            throw new ServiceException("该回片链接和费用名称的组合已存在，不能重复添加");
        }
    }
    @Override
    public BatchResultDTO delete(String id) {
        KolFeedbackCostEntity entity = super.getById(id);
        if (entity == null) {
            throw new ServiceException("KOL回片费用不存在");
        }
        
        // 执行删除
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException("删除失败");
        }
        
        // 返回成功结果，使用备注作为 code
        String code = StrUtil.isNotBlank(entity.getRemark()) ? entity.getRemark() : entity.getId();
        return BatchResultDTO.success(entity.getId(), code);
    }

    /**
     * 填充列表数据（币别转换、金额拼接）
     */
    private void fillList(List<KolFeedbackCostDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        
        // 获取所有币别信息
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        if (CollUtil.isEmpty(currencyList)) {
            return;
        }
        
        // 构建币别 ID -> 币别实体的映射
        Map<String, DictCurrencyEntity> currencyMap = currencyList.stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, c -> c, (k1, k2) -> k1));
        
        // 获取 CNY 的 symbol
        String cnySymbol = currencyList.stream()
                .filter(c -> "CNY".equals(c.getSymbol()))
                .findFirst()
                .map(DictCurrencyEntity::getSymbol)
                .orElse("¥");
        
        // 遍历列表进行数据填充
        for (KolFeedbackCostDTO.ListDTO data : list) {
            // 设置币别名称
            if (StringUtils.isNotBlank(data.getCurrency())) {
                DictCurrencyEntity currency = currencyMap.get(data.getCurrency());
                if (currency != null) {
                    data.setCurrencyName(currency.getName());
                    
                    // 拼接原币金额显示：symbol + 金额
                    if (data.getOriginalAmount() != null) {
                        String symbol = StringUtils.isNotBlank(currency.getSymbol()) ? currency.getSymbol() : "";
                        data.setOriginalAmountDisplay(symbol + " " + data.getOriginalAmount());
                    }
                }
            }
            
            // 拼接本位币金额显示：CNY symbol + 金额
            if (data.getBaseAmount() != null) {
                data.setBaseAmountDisplay(cnySymbol + " " + data.getBaseAmount());
            }
        }
    }
}
