package com.erp.server.bi.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.model.bi.entity.BiDataSourceCostEntity;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.SalesPlatformEnum;
import com.erp.server.bi.enums.BiDataSourceCostEnum;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/27 17:22
 */
public class BiDataSourceCostExcelListener extends AnalysisEventListener<Map<Integer,String>> {

    private BiDataSourceCostService biDataSourceCostService;

    private BiDataSourceCostDetailService biDataSourceCostDetailService;

    private List<DmpShopInfoEntity> shopList;

    private List<FindUserDTO> userList;

    private List<BiDictEntity> dictList;

    private  List<Map<Integer,String>> list ;

    private Map<Integer,String> headMap;

    private List<String> headList;

    public BiDataSourceCostExcelListener(BiDataSourceCostService biDataSourceCostService,BiDataSourceCostDetailService biDataSourceCostDetailService,
                                         List<DmpShopInfoEntity> shopList,List<FindUserDTO> userList,List<BiDictEntity> dictList) {
        this.biDataSourceCostService = biDataSourceCostService;
        this.biDataSourceCostDetailService = biDataSourceCostDetailService;
        this.shopList = shopList;
        this.userList = userList;
        this.dictList = dictList;
        this.list = new ArrayList<>();
    }

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext analysisContext) {

        List<String> errorMsgList = new ArrayList<>();
        //遍历map下的数据
        Iterator<Map.Entry<Integer, String>> iterator = map.size() == 0 ? null : map.entrySet().iterator();
        BiDataSourceCostEntity entity = new BiDataSourceCostEntity();
        List<BiDataSourceCostDetailEntity> detailList = new ArrayList<>();
        //旧数据时记录
        if (ObjectUtils.isNotEmpty(iterator)) {
            while (iterator .hasNext()){
                Map.Entry entry  =  (java.util.Map.Entry)iterator.next();
                if (ObjectUtils.isEmpty(entry.getKey())) {
                    continue;
                }
                Integer mapKey = Integer.valueOf(entry.getKey().toString()) ;
                String value = ObjectUtils.isEmpty(entry.getValue()) ? "" : entry.getValue().toString() ;
                String key = headMap.get(mapKey);
                BiDataSourceCostDetailEntity detailEntity = new BiDataSourceCostDetailEntity();
                if (StringUtils.isNotBlank(key))  {
                    if (BiDataSourceCostEnum.MONTH.getDesc().equals(key)) {
                        DateFormat format= new SimpleDateFormat("yyyy年M月");
                        try {
                            Date parse = format.parse(value);
                            entity.setMonth(LocalDateUtil.date2LocalDateTime(parse));
                        } catch (ParseException e) {
                            errorMsgList.add("月份格式错误");
                        }
                        continue;
                    }
                    if (BiDataSourceCostEnum.DEPTNAME.getDesc().equals(key)) {
                        entity.setDeptName(value);
                        continue;
                    }
                    if (BiDataSourceCostEnum.PLATFORMNAME.getDesc().equals(key)) {
                        entity.setPlatformName(value);
                        continue;
                    }
                    if (BiDataSourceCostEnum.SITE.getDesc().equals(key)) {
                        entity.setSite(value);
                        continue;
                    }
                    if (BiDataSourceCostEnum.SHOPNAME.getDesc().equals(key)) {
                        entity.setShopName(value);
                        continue;
                    }
                    if (BiDataSourceCostEnum.CHARGENAME.getDesc().equals(key)) {
                        if (CollectionUtils.isNotEmpty(userList)) {
                            FindUserDTO findUserDTO = userList.stream().filter(obj -> obj.getUserName().equals(value)).findFirst().orElse(null);
                            if (ObjectUtils.isEmpty(findUserDTO)) {
                                errorMsgList.add("系统中不存在该负责人");
                                continue;
                            }
                            entity.setChargeId(findUserDTO.getUserId());
                        }
                        entity.setChargeName(value);
                        continue;
                    }
                    if (BiDataSourceCostEnum.COMBINATION.getDesc().equals(key)) {
                        entity.setCombination(value);
                        continue;
                    }
                    if (CollectionUtils.isNotEmpty(dictList)) {
                        String costType = dictList.stream().filter(obj -> obj.getName().equals(key)).map(BiDictEntity::getValue).findFirst().orElse(null);
                        if (StringUtils.isNotBlank(costType)) {
                            detailEntity.setCostType(costType);
                            //既不是数值也不是百分比
                            if (!StrUtils.isDigit(value) && !StrUtils.isPercentage(value)) {
                                errorMsgList.add("成本必须是数值或百分比数据");
                            }
                            if (StrUtils.isDigit(value)) {
                                detailEntity.setCostValue(MathUtil.valueOf(value));
                                detailEntity.setValueType(MathUtil.ZERO);
                                detailList.add(detailEntity);
                            } else {
                                detailEntity.setValueType(MathUtil.ONE);
                                String costValue = value.replace("%", "");
                                detailEntity.setCostValue(MathUtil.divide(MathUtil.valueOf(costValue),new BigDecimal(100),4));
                                detailList.add(detailEntity);
                            }
                        }
                    }
                }
            }
            if (StringUtils.isBlank(entity.getDeptName())) {
                errorMsgList.add("销售事业部不能为空");
            }
            if (StringUtils.isBlank(entity.getPlatformName())) {
                errorMsgList.add("平台名称不能为空");
            } else {
                SalesPlatformEnum platformEnum = SalesPlatformEnum.getByName(entity.getPlatformName());
                if (ObjectUtils.isEmpty(platformEnum)) {
                    errorMsgList.add("系统中不存在此平台名称");
                }
            }
            if (StringUtils.isBlank(entity.getSite())) {
                errorMsgList.add("站点不能为空");
            }
            if (StringUtils.isBlank(entity.getShopName())) {
                errorMsgList.add("店铺名称不能为空");
            }
            if (StringUtils.isBlank(entity.getChargeName())) {
                errorMsgList.add("负责人不能为空");
            }
            DmpShopInfoEntity dmpShopInfoEntity = shopList.stream().filter(obj -> obj.getName().equals(entity.getShopName()) && obj.getSite().equals(entity.getSite()) && obj.getPlatformName().equals(entity.getPlatformName())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(dmpShopInfoEntity)) {
                errorMsgList.add("在平台站点中未找到该店铺");
            }
            String errStr = "";
            if (errorMsgList.size() > 0) {
                for (int i = 0; i < errorMsgList.size(); i++) {
                    Integer indexTemp = i + 1;
                    errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
                }
                map.put(map.size() ,errStr);
                list.add(map);
                return;
            }
            entity.setShopId(dmpShopInfoEntity.getId());
            //根据月份、店铺数据查询
            BiDataSourceCostEntity cost = biDataSourceCostService.getByCostParam(entity);
            if (ObjectUtils.isEmpty(cost)) {
                //新增成本主表数据
                biDataSourceCostService.save(entity);
                if (CollectionUtils.isNotEmpty(detailList)) {
                    detailList.forEach(obj -> obj.setCostId(entity.getId()));
                    biDataSourceCostDetailService.saveBatch(detailList);
                }
            } else {
                entity.setId(cost.getId());
                //更新成本主表数据
                biDataSourceCostService.updateById(entity);
                //删除成本明细重新新增
                biDataSourceCostDetailService.removeByCostId(cost.getId());
                if (CollectionUtils.isNotEmpty(detailList)) {
                    detailList.forEach(obj -> obj.setCostId(entity.getId()));
                    biDataSourceCostDetailService.saveBatch(detailList);
                }
            }
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer,String> map, AnalysisContext analysisContext) {
        List<String> headList = map.values().stream().collect(Collectors.toList());
        headList.add("错误信息");
        this.headMap = map;
        this.headList = headList;
    }

    public List<String> getHead(){
        return headList;
    }

    public List<Map<Integer,String>> getDateList(){
        return list;
    }

    /**
     * @description: 全部解析完回调此方法
     * @author Will
     * @date: 2022/12/16 10:26
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
