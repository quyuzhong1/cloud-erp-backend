package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.TransferInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据修复任务临时使用
 *
 * @Author Cloud
 * @Date 2023/7/13 15:06
 **/
@Component
@Slf4j
public class DataRecoveryJob {

    private static final String TYPE_SO_OUTSTOCK_AMOUNT = "soOutstockAmount";

    private static final String TYPE_SO_RETURN_INSTOCK_PRICE = "soReturnInstockPrice";

    private static final String TYPE_ALL = "all";

    private static final int DEFAULT_PAGE_SIZE = 500;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoReturnInstockService soReturnInstockService;
    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @XxlJob("soOutStockDataRecovery")
    public void SoOutStockDataRecovery() {
        String jobParam = XxlJobHelper.getJobParam();
        if (CharSequenceUtil.isBlank(jobParam)) {
            XxlJobHelper.log("参数错误= {}",jobParam);
            return;
        }
        JSONObject param = JSONUtil.parseObj(jobParam);
        List<String> ids = param.getBeanList("ids", String.class);
        String type = param.get("type", String.class);
        Boolean isManual = param.get("isManual", Boolean.class);
        Boolean isPushKingdee = param.getBool("isPushKingdee", Boolean.FALSE);
        if(CollUtil.isEmpty(ids)){
            if("soReturnInstockService".equals(type)){
                ids = soOutstockService.getIdsByTemp("so_return_instock");
            }else if("transferInfoService".equals(type)){
                ids = soOutstockService.getIdsByTemp("transfer_info");
            }else if("soOutstockService".equals(type)){
                ids = soOutstockService.getIdsByTemp("so_outstock");
            }

        }
        if (ObjectUtils.isEmpty(ids)) {
            XxlJobHelper.log("参数错误ids={}", ids);
            return;
        }

        ids.parallelStream().forEach(item -> {
            SoOutstockEntity soOutstock = soOutstockService.getById(item);
            try {
                if(CharSequenceUtil.isNotBlank(type) && "soReturnInstockService".equals(type)){
                    SoReturnInstockEntity entity = soReturnInstockService.getById(item);
                    if (Objects.nonNull(entity)){
                        soReturnInstockService.disApprove(entity, isPushKingdee);
                    }
                }else if(CharSequenceUtil.isNotBlank(type) && "transferInfoService".equals(type)){
                    TransferInfoEntity entity = transferInfoService.getById(item);
                    if (Objects.nonNull(entity)){
                        transferInfoService.disApprove(entity, isPushKingdee, isManual);
                    }
                }else if(CharSequenceUtil.isNotBlank(type) && "soOutstockService".equals(type)){
                    soOutstockService.disApprove(soOutstock, isPushKingdee);
                }
            } catch (Exception e) {
                XxlJobHelper.log("数据修复失败，id={} e ={}", item, e);
                log.error("数据修复失败，id={} e ={}", item, e);
            }
        });


    }

    @XxlJob("soAmountFieldsRefresh")
    public void soAmountFieldsRefresh() {
        JSONObject param = parseJobParam();
        String type = param.getStr("type", TYPE_ALL);
        List<String> ids = param.getBeanList("ids", String.class);
        Boolean isPushKingdee = param.getBool("isPushKingdee", Boolean.FALSE);
        Boolean approveOnly = param.getBool("approveOnly", Boolean.TRUE);
        Integer pageSize = param.getInt("pageSize", DEFAULT_PAGE_SIZE);
        LocalDate startDate = parseDate(param.getStr("startDate"));
        LocalDate endDate = parseDate(param.getStr("endDate"));
        if (CollUtil.isNotEmpty(ids)) {
            refreshByIds(type, ids, isPushKingdee);
            return;
        }
        if (TYPE_ALL.equals(type) || TYPE_SO_OUTSTOCK_AMOUNT.equals(type)) {
            refreshSoOutstockAmount(startDate, endDate, pageSize, approveOnly, isPushKingdee);
        }
        if (TYPE_ALL.equals(type) || TYPE_SO_RETURN_INSTOCK_PRICE.equals(type)) {
            refreshSoReturnInstockPrice(startDate, endDate, pageSize, approveOnly);
        }
    }

    private JSONObject parseJobParam() {
        String jobParam = XxlJobHelper.getJobParam();
        if (CharSequenceUtil.isBlank(jobParam)) {
            return new JSONObject();
        }
        return JSONUtil.parseObj(jobParam);
    }

    private void refreshByIds(String type, List<String> ids, Boolean isPushKingdee) {
        if (TYPE_ALL.equals(type) || TYPE_SO_OUTSTOCK_AMOUNT.equals(type)) {
            soOutstockService.refreshAmountFields(ids, isPushKingdee);
            XxlJobHelper.log("销售出库金额字段重算完成，数量={}", ids.size());
        }
        if (TYPE_ALL.equals(type) || TYPE_SO_RETURN_INSTOCK_PRICE.equals(type)) {
            soReturnInstockService.refreshPriceFields(ids);
            XxlJobHelper.log("销售退货入库价格字段重算完成，数量={}", ids.size());
        }
    }

    private void refreshSoOutstockAmount(LocalDate startDate, LocalDate endDate, Integer pageSize,
                                         Boolean approveOnly, Boolean isPushKingdee) {
        int currentPage = 1;
        while (true) {
            LambdaQueryWrapper<SoOutstockEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(SoOutstockEntity::getId);
            if (Boolean.TRUE.equals(approveOnly)) {
                queryWrapper.eq(SoOutstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus());
            }
            if (Objects.nonNull(startDate)) {
                queryWrapper.ge(SoOutstockEntity::getBillDate, startDate);
            }
            if (Objects.nonNull(endDate)) {
                queryWrapper.le(SoOutstockEntity::getBillDate, endDate);
            }
            queryWrapper.orderByAsc(SoOutstockEntity::getId);
            IPage<SoOutstockEntity> page = soOutstockService.page(new Page<>(currentPage, getPageSize(pageSize)), queryWrapper);
            List<String> ids = page.getRecords().stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
            if (CollUtil.isEmpty(ids)) {
                break;
            }
            soOutstockService.refreshAmountFields(ids, isPushKingdee);
            XxlJobHelper.log("销售出库金额字段重算完成，当前页={}，数量={}", currentPage, ids.size());
            if (currentPage >= page.getPages()) {
                break;
            }
            currentPage++;
        }
    }

    private void refreshSoReturnInstockPrice(LocalDate startDate, LocalDate endDate, Integer pageSize,
                                             Boolean approveOnly) {
        int currentPage = 1;
        while (true) {
            LambdaQueryWrapper<SoReturnInstockEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(SoReturnInstockEntity::getId);
            if (Boolean.TRUE.equals(approveOnly)) {
                queryWrapper.eq(SoReturnInstockEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus());
            }
            if (Objects.nonNull(startDate)) {
                queryWrapper.ge(SoReturnInstockEntity::getBillDate, startDate);
            }
            if (Objects.nonNull(endDate)) {
                queryWrapper.le(SoReturnInstockEntity::getBillDate, endDate);
            }
            queryWrapper.orderByAsc(SoReturnInstockEntity::getId);
            IPage<SoReturnInstockEntity> page = soReturnInstockService.page(new Page<>(currentPage, getPageSize(pageSize)), queryWrapper);
            List<String> ids = page.getRecords().stream().map(SoReturnInstockEntity::getId).collect(Collectors.toList());
            if (CollUtil.isEmpty(ids)) {
                break;
            }
            soReturnInstockService.refreshPriceFields(ids);
            XxlJobHelper.log("销售退货入库价格字段重算完成，当前页={}，数量={}", currentPage, ids.size());
            if (currentPage >= page.getPages()) {
                break;
            }
            currentPage++;
        }
    }

    private Integer getPageSize(Integer pageSize) {
        if (Objects.isNull(pageSize) || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    private LocalDate parseDate(String date) {
        if (CharSequenceUtil.isBlank(date)) {
            return null;
        }
        return LocalDate.parse(date);
    }

    private List<String> getInnerSoOutStockIds() {
        List<String> result= new ArrayList<>();

        Map<String,Object> condition=new HashMap<>();
        condition.put("return_msg","同步成功");
        condition.put("lastSql","cast(mq_data AS json)->>'fIsGenForIos'='true'");
        List<String> kingdeeCodeList=new ArrayList<>();
        kingdeeCodeList = dmpTaskFeign.getKingdeeSourceCode(condition);

        if(CollUtil.isNotEmpty(kingdeeCodeList)){
            kingdeeCodeList.stream().forEach(kingdeeCode->{
                LambdaQueryWrapper<SoOutstockEntity> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.select(SoOutstockEntity::getId);
                queryWrapper.eq(SoOutstockEntity::getApproveStatus,"approve");
//                queryWrapper.eq(SoOutstockEntity::getIsDeleted,"f");
                queryWrapper.eq(SoOutstockEntity::getCode,kingdeeCode);

                // 按条件查询
                List<SoOutstockEntity> queryResult=soOutstockService.list(queryWrapper);
                if(CollUtil.isNotEmpty(queryResult)){
                    // 添加 id
                    queryResult.stream().forEach(item->result.add(item.getId()));
                }
            });
        }

        return result;
    }
}
