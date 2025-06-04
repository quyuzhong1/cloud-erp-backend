package com.erp.server.wms.schedule;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.kingdee.SyncSoReturnInstockService;
import com.erp.server.wms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SdyDataSyncJob {
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private SoOutstockDetailService soOutstockDetailService;
    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoReturnInstockService soReturnInstockService;
    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;
    @Resource
    private SyncSoReturnInstockService syncSoReturnInstockService;
    @Resource
    private SoReturnFeign soReturnFeign;
    @Resource
    private SoReturnReceiveService soReturnReceiveService;
    @Resource
    private DictBasicService dictBasicService;

    @XxlJob("syncSdySoOutstock")
    public void syncSdySoOutstock() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        String queryParamsStr = "";
        boolean isNewQuerySync = true;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
            queryParamsStr = jsonParam.getStr("queryParams");
            isNewQuerySync = jsonParam.getBool("isNewQuerySync", true);
        }

        //总条数
        int currentPage = 0;

        List<SoOutstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoOutstockEntity> queryWrapper = (QueryWrapper<SoOutstockEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoOutstockEntity> page = soOutstockService.page(new Page<>(currentPage + 1, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soOutstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                XxlJobHelper.log("===========当前页数：" + currentPage + "， 结果为空结束时间：" + LocalDateTime.now());
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            if (CollUtil.isEmpty(ids)) {
                XxlJobHelper.log("===========当前页数：" + currentPage + "， 结果为空结束时间：" + LocalDateTime.now());
                return;
            }
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(ids);
            syncKingdeeSoOutstockService.syncBatchDataToSdy(list, soOutstockDetailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode() , true, isNewQuerySync);
            
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "处理数量："+ list.size() +" 结束时间：" + LocalDateTime.now());
        }
    }

    @XxlJob("SyncSoReturnInstockJob")
    public void SyncSoReturnInstockJob() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        String queryParamsStr = "";
        boolean isNewQuerySync = true;
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
            queryParamsStr = jsonParam.getStr("queryParams");
            isNewQuerySync = jsonParam.getBool("isNewQuerySync", true);
        }
        //总条数
        int currentPage = 0;

        List<SoReturnInstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoReturnInstockEntity> queryWrapper = (QueryWrapper<SoReturnInstockEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoReturnInstockEntity> page = soReturnInstockService.page(new Page<>(currentPage + 1, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soReturnInstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                XxlJobHelper.log("===========当前页数：" + currentPage + "， 结果为空结束时间：" + LocalDateTime.now());
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);

            syncSoReturnInstockService.syncBatchDataToSdy(list, detailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode() , true, isNewQuerySync);
            
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "处理数量："+ list.size() +" 结束时间：" + LocalDateTime.now());
        }

    }
}
