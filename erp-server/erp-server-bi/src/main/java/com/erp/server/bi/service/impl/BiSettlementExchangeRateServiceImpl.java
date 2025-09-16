package com.erp.server.bi.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.model.dmp.entity.AfterSaleEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.bi.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.bi.service.BiSettlementExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 10:00
 */
@Service
@Slf4j
public class BiSettlementExchangeRateServiceImpl extends ServiceImpl<BiSettlementExchangeRateMapper, BiSettlementExchangeRateEntity>
        implements BiSettlementExchangeRateService {

    @Resource
    private MQProducerService<JSONObject> mQProducerService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public PagingVO<BiSettlementExchangeRateDTO.ListDTO> paging(PagingDTO<BiSettlementExchangeRateDTO.SearchParamDTO> pagingDTO) {
        Page<Object> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<BiSettlementExchangeRateDTO.ListDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        List<BiSettlementExchangeRateDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            //币别
            List<String> currencyList = records.stream().flatMap(obj -> Stream.of(obj.getSourceCurrencyCode(), obj.getTargetCurrencyCode())).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listByCurrency(currencyList);

            for (BiSettlementExchangeRateDTO.ListDTO listDTO : records) {
                if (CollectionUtils.isNotEmpty(viewList)) {
                    //原币名称
                    String sourceCurrencyName = viewList.stream().filter(obj -> obj.getId().equals(listDTO.getSourceCurrencyCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    listDTO.setSourceCurrencyName(sourceCurrencyName);
                    //目标币名称
                    String targetCurrencyName = viewList.stream().filter(obj -> obj.getId().equals(listDTO.getTargetCurrencyCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    listDTO.setTargetCurrencyName(targetCurrencyName);
                }
                listDTO.setApproveStatusName(ApproveStatusEnum.getName(listDTO.getApproveStatus()));
            }
        }
        return new PagingVO<>(pageData);
    }


    /**
     * @description:
     * @author Will
     * @date: 2023/4/23 20:17
     */
    private void updateSettlementExchangeRate (List<BiSettlementExchangeRateEntity> entityList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("list",entityList);
        //异步推送mq
        mQProducerService.asyncClassMsg(RocketMqTopic.DMP_ERP_ORDER_UPDATE_TOPIC, RocketMqTagEnum.CHANGE_CURRENCY_TAG.getName(), jsonObject, "currency");
    }

    @Override
    public List<Map<String, Object>> listSettlementExchangeRate() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        LambdaQueryWrapper<BiSettlementExchangeRateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(BiSettlementExchangeRateEntity::getSettlementDateBegin);
        List<BiSettlementExchangeRateEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<BiSettlementExchangeRateEntity>> newMap = list.stream().collect(Collectors.groupingBy(obj -> obj.getSettlementDateBegin().toString().concat(",").concat(obj.getSettlementDateEnd().toString())));
            List<Map.Entry<String, List<BiSettlementExchangeRateEntity>>> collect = newMap.entrySet().stream().sorted(Comparator.comparing(obj -> obj.getKey().split(",")[0])).collect(Collectors.toList());
            for (Map.Entry<String, List<BiSettlementExchangeRateEntity>> entry:collect) {
               String key = entry.getKey();
               String[] date = key.split(",");
               List<BiSettlementExchangeRateEntity> value = entry.getValue();
               LinkedHashMap<String,Object> map = new LinkedHashMap<>();
               List<String> settlementDateList = new ArrayList<>();
               settlementDateList.add(date[0]);
               settlementDateList.add(date[1]);
               map.put("settlementDateList",settlementDateList);
               for (BiSettlementExchangeRateEntity entity : value) {
                   String rate = MathUtil.compareTo(entity.getExchangeRate(), BigDecimal.ZERO) == 0 ? BigDecimal.ZERO.toString() : entity.getExchangeRate().toString();
                   map.put(entity.getSourceCurrencyCode(), rate);
               }
               mapList.add(map);
           }

        }
        return mapList;
    }


    @Override
    public BiSettlementExchangeRateEntity getByKingdeeId(String kingdeeId) {
        return lambdaQuery().eq(BiSettlementExchangeRateEntity::getKingdeeId,kingdeeId).one();
    }

    @Override
    public String add(BiSettlementExchangeRateDTO.AddDTO addDTO) {
        BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
        BeanMapperUtils.copy(addDTO,entity);
        boolean save = this.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return entity.getId();
    }

    @Override
    public Boolean update(BiSettlementExchangeRateDTO.UpdateDTO updateDTO) {
        BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
        BeanMapperUtils.copy(updateDTO,entity);
        boolean update = this.updateById(entity);
        if (!update) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return update;
    }

    /**
     * 添加redis缓存
     */
    private void setRedisExchangeRate (BiSettlementExchangeRateEntity entity) {
        String existKey = CharSequenceUtil.format(RedisKeyConstant.SETTLEMENT_EXCHANGE_RATE, entity.getTargetCurrencyCode(),entity.getSourceCurrencyCode());

        List<BiSettlementExchangeRateEntity> rateList = baseMapper.listByCurrencyCode(entity.getTargetCurrencyCode(), entity.getSourceCurrencyCode());
        if (CollectionUtils.isEmpty(rateList)) {
            throw new ServiceException(CharSequenceUtil.format("目标币别【{}】、原币别【{}】未查询到汇率",entity.getTargetCurrencyCode(),entity.getSourceCurrencyCode()));
        }
        boolean isHas = redisUtil.hasKey(existKey);
        if (isHas) {
            //删除缓存
            redisUtil.keys(existKey).forEach(key -> redisUtil.del(key));
        }
        //添加缓存
        redisUtil.set(existKey,rateList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<BiSettlementExchangeRateEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus()))).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("汇率提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<BiSettlementExchangeRateEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("汇率【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("汇率【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
            //更新单据汇率
            updateSettlementExchangeRate(list);
            //添加redis
            list.stream().forEach(this::setRedisExchangeRate);
            //同步订货通
            createSyncDhtMsg(list);
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("汇率【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }

        return Boolean.TRUE;
    }

    private void createSyncDhtMsg(List<BiSettlementExchangeRateEntity> list) {
        //过滤掉目标币别不是cny的数据
        list = list.stream().filter(v->!v.getTargetCurrencyCode().equals(CurrencyEnum.CNY.getCurrencyCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<DmpPushMsgEntity> msgList = new ArrayList<>();
        for (BiSettlementExchangeRateEntity entity : list) {
            DmpPushMsgEntity dmpPushMsgEntity = buildDmpPushMsgEntity(entity);
            msgList.add(dmpPushMsgEntity);
        }
        if (CollectionUtils.isNotEmpty(msgList)) {
            dmpTaskFeign.batchCreateDmpPushMsg(msgList);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<BiSettlementExchangeRateEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("汇率反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新单据为待提交
        updateApproveStatusForApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //添加redis
        list.stream().forEach(this::setRedisExchangeRate);
        return Boolean.TRUE;
    }

    @Override
    public BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode) {
        String targetCurrencyCode = CurrencyEnum.CNY.getCurrencyCode();
        if (CharSequenceUtil.isBlank(date)) {
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }else{
            // 尝试解析日期和时间部分
            try{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date parse = sdf.parse(date);
                date = sdf.format(parse);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return listRedisByCurrencyCode(date, targetCurrencyCode, sourceCurrencyCode);
    }

    @Override
    public BigDecimal listRedisByCurrencyCode (String date, String targetCurrencyCode, String sourceCurrencyCode) {
        //数据验证
        checkNotBlank(date,targetCurrencyCode,sourceCurrencyCode);

        //如果目标币别和来源币别一致则直接返回1
        if (CharSequenceUtil.equals(targetCurrencyCode,sourceCurrencyCode)) {
            return BigDecimal.ONE;
        }
        //查询redis中存储的成本信息
        String existKey = CharSequenceUtil.format(RedisKeyConstant.SETTLEMENT_EXCHANGE_RATE,CurrencyEnum.CNY.getCurrencyCode(),sourceCurrencyCode);
        List<BiSettlementExchangeRateEntity> rateList = (List<BiSettlementExchangeRateEntity>) redisUtil.get(existKey);
        if (CollectionUtils.isEmpty(rateList)) {
            //查询库中数据添加缓存
            rateList = baseMapper.listByCurrencyCode(targetCurrencyCode, sourceCurrencyCode);
            if (CollectionUtils.isNotEmpty(rateList)) {
                //添加缓存
                redisUtil.set(existKey,rateList);
            }
        }
        if (CollectionUtils.isEmpty(rateList)) {
            return null;
        }
        //格式化日期
        LocalDate localDate = LocalDateUtil.parseStrToLocalDate(date);
        //汇率
        return rateList.stream().filter(obj -> obj.getSettlementDateBegin().isEqual(localDate)
                        || obj.getSettlementDateEnd().isEqual(localDate)
                        || (obj.getSettlementDateBegin().isBefore(localDate) && obj.getSettlementDateEnd().isAfter(localDate)))
                .sorted(Comparator.comparing(BiSettlementExchangeRateEntity::getUpdateTime, Comparator.reverseOrder()))
                .map(BiSettlementExchangeRateEntity::getExchangeRate)
                .findFirst().orElse(null);
    }

    // 构建DmpPushMsgEntity
    private DmpPushMsgEntity buildDmpPushMsgEntity(BiSettlementExchangeRateEntity biSettlementExchangeRateEntity) {
        DmpPushMsgEntity dmpPushMsgEntity = new DmpPushMsgEntity();
        dmpPushMsgEntity.setTargetPlatform(PlatformDictEnum.DHT.getCode());
        dmpPushMsgEntity.setSourcePlatform(ServiceCodeNameEnum.DMP.getCode());
        dmpPushMsgEntity.setSourceType(SourceTypeEnum.BD_RATE.getCode());
        dmpPushMsgEntity.setSourceId(biSettlementExchangeRateEntity.getId());
        dmpPushMsgEntity.setSourceCode(biSettlementExchangeRateEntity.getSourceCurrencyCode());
        dmpPushMsgEntity.setPushData(JSONUtil.toJsonStr(biSettlementExchangeRateEntity));
        dmpPushMsgEntity.setMessageCreateTime(LocalDateTime.now());
        return dmpPushMsgEntity;
    }


    /**
     * 根据ids查询数据
     */
    private List<BiSettlementExchangeRateEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<BiSettlementExchangeRateEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(BiSettlementExchangeRateEntity::getId, ids)
                .set(BiSettlementExchangeRateEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        this.lambdaUpdate().in(BiSettlementExchangeRateEntity::getId, ids)
                .set(BiSettlementExchangeRateEntity::getApproveStatus, approveStatus)
                .set(BiSettlementExchangeRateEntity::getApproveTime,ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus) ? LocalDateTime.now() : null)
                .update();
    }

    /**
     * @return true重叠。false不重叠
     */
    public static boolean isOverlap(String settlementDateBegin1, String settlementDateEnd1, String settlementDateBegin2, String settlementDateEnd2) {
        LocalDate date1 = LocalDate.parse(settlementDateBegin1);
        LocalDate date2 = LocalDate.parse(settlementDateEnd1);
        LocalDate date3 = LocalDate.parse(settlementDateBegin2);
        LocalDate date4 = LocalDate.parse(settlementDateEnd2);
        if (date1.isAfter(date2) || date3.isAfter(date4)) {
            throw new ServiceException(ApiError.ERROR_97011);
        }
        return (date1.compareTo(date3) >= 0 && date4.compareTo(date1) >= 0) || (date3.compareTo(date1) >= 0 && date2.compareTo(date3) >= 0);
    }


    /**
     * 数据验证
     * @author will
     * @date 2024/8/7 17:21
     * @param date
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     */
    private void checkNotBlank (String date, String targetCurrencyCode, String sourceCurrencyCode) {
        if (CharSequenceUtil.isBlank(date)) {
            throw new ServiceException("汇率查询时间不能为空");
        }
        if (CharSequenceUtil.isBlank(targetCurrencyCode)) {
            throw new ServiceException("汇率查询目标币别不能为空");
        }
        if (CharSequenceUtil.isBlank(sourceCurrencyCode)) {
            throw new ServiceException("汇率查询来源币别不能为空");
        }
    }
}
