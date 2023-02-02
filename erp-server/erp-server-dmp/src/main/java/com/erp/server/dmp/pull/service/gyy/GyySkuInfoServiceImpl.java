package com.erp.server.dmp.pull.service.gyy;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyySkuInfoEntity;
import com.erp.model.dmp.gyy.bean.CombineItemsBean;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpSkuInfoService;
import com.erp.server.dmp.utils.GyyApiUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.core.Local;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管易云商品信息
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_ITEMS_GET)
public class GyySkuInfoServiceImpl implements IReportSaveService<GyySkuInfoEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpErrorLogService dmpErrorLogService;

    @Resource
    private DmpSkuInfoService dmpSkuInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    @Qualifier("gyySkuInfoServiceImpl")
    private IReportSaveService reportSaveService;

    public static void main(String[] args) {
        GyySkuInfoServiceImpl gyySkuInfoService = new GyySkuInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_ITEMS_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(8);
        jobTaskDTO.setApiName("管易云商品查询");
        jobTaskDTO.setId(33L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyySkuInfoEntity> orderEntities = null;
        try {
            orderEntities = gyySkuInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }

    /**
     * 拉取商品数据
     * @param dto 任务信息
     * @return
     */
    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyySkuInfoEntity> gyySkuInfoEntityList = pullDate(dto);
        if (gyySkuInfoEntityList != null && gyySkuInfoEntityList.size() > 0) {
            for (GyySkuInfoEntity gyySkuInfoEntity : gyySkuInfoEntityList) {
                OrderMongoDTO orderMongoDTO = new OrderMongoDTO();
                orderMongoDTO.setId(gyySkuInfoEntity.getId());
                List<GyySkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_SKU, GyySkuInfoEntity.class);
                if (mongoData != null && mongoData.size() > 0) {
                    for (GyySkuInfoEntity mongoDatum : mongoData) {
                        // 比较数据是否相同
                        if (!mongoDatum.toString().equals(gyySkuInfoEntity.toString())) {
                            // 修改数据
                            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(gyySkuInfoEntity), MapUtil.class);
                            try {
                                mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SKU, GyySkuInfoEntity.class);
                            } catch (Exception e) {
                                DmpErrorLogEntity dmpErrorLogEntity = new DmpErrorLogEntity();
                                dmpErrorLogEntity.setTaskId(dto.getJobTaskDTO().getId());
                                dmpErrorLogEntity.setParams("");
                                dmpErrorLogEntity.setErrorMsg("==== 管易云修改mongodb商品数据失败，[ 订单号 = " + gyySkuInfoEntity.getCode() + "], 错误信息 = " + e.getMessage());
                                dmpErrorLogEntity.setReturnMsg("");
                                dmpErrorLogEntity.setCreateTime(LocalDateTime.now());
                                dmpErrorLogService.add(dmpErrorLogEntity);
                                throw new RuntimeException("==== 管易云修改mongodb商品数据失败，[ 订单号 = " + gyySkuInfoEntity.getCode() + "], 错误信息 = " , e);
                            }
                        }
                    }
                } else {
                    mongoService.saveMongoData(gyySkuInfoEntity, MongoTableNameContant.ORIGINAL_GYY_SKU);
                }
                //存储数据到中台  sku信息只保留金蝶数据
//                analysisSku(gyySkuInfoEntity);
            }
        }
    }

    /**
     * 请求管易云商品接口
     * @param dto
     * @return
     */
    private List<GyySkuInfoEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return GyyApiUtils.querySkuList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void analysisOrder(GyySkuInfoEntity gyySkuInfoEntity) throws Exception {
        List<DmpSkuInfoEntity> dmpSkuInfoEntitylist = new ArrayList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());

        List<CombineItemsBean> combineItems = gyySkuInfoEntity.getCombineItems();
        if (combineItems.size() > 0) {
            for (CombineItemsBean combineItem : combineItems) {
                DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
                //商品编码
                dmpSkuInfoEntity.setItemCode(combineItem.getItemCode());

                //sku编号
                dmpSkuInfoEntity.setSkuNo(combineItem.getItemCode());

                //中文名
                dmpSkuInfoEntity.setNameCn(combineItem.getItemName());

                //英文名
                dmpSkuInfoEntity.setNameEn("");

                //统一成本价
                dmpSkuInfoEntity.setDefaultCost(gyySkuInfoEntity.getCostPrice());

                //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
                dmpSkuInfoEntity.setStatus(null);

                //商品创建时间
                if (StringUtils.isNotBlank(gyySkuInfoEntity.getCreateDate())) {
                    dmpSkuInfoEntity.setSkuCreateTime(LocalDateTime.parse(gyySkuInfoEntity.getCreateDate(),sdf));
                }

                //商品修改时间
                if (StringUtils.isNotBlank(gyySkuInfoEntity.getModifyDate())) {
                    dmpSkuInfoEntity.setSkuUpdateTime(LocalDateTime.parse(gyySkuInfoEntity.getModifyDate(), sdf));
                }

                //品牌
                dmpSkuInfoEntity.setBrandName(gyySkuInfoEntity.getItemBrandName());

                //商品目录(一级)
                dmpSkuInfoEntity.setParentCategoryName(null);

                //商品目录(二级)
                dmpSkuInfoEntity.setCategoryName(null);

                //售价
                dmpSkuInfoEntity.setSalePrice(gyySkuInfoEntity.getSalesPrice());

                //申报价格
                dmpSkuInfoEntity.setDeclarePrice(new BigDecimal(BigInteger.ZERO));

                //开发员id
                dmpSkuInfoEntity.setDeveloperId(null);

                //开发员名称
                dmpSkuInfoEntity.setDeveloperName(null);

                //平台标识
                dmpSkuInfoEntity.setPlatformSign("管易云");

                dmpSkuInfoEntity.setCreateTime(LocalDateTime.now());
                dmpSkuInfoEntitylist.add(dmpSkuInfoEntity);
            }

        } else {
            DmpSkuInfoEntity dmpSkuInfoEntity = new DmpSkuInfoEntity();
            //sku编号
            dmpSkuInfoEntity.setSkuNo(gyySkuInfoEntity.getCode());

            //中文名
            dmpSkuInfoEntity.setNameCn(gyySkuInfoEntity.getName());

            //英文名
            dmpSkuInfoEntity.setNameEn("");

            //统一成本价
            dmpSkuInfoEntity.setDefaultCost(gyySkuInfoEntity.getCostPrice());

            //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
            dmpSkuInfoEntity.setStatus(null);

            //商品创建时间
            if (StringUtils.isNotBlank(gyySkuInfoEntity.getCreateDate())) {
                dmpSkuInfoEntity.setSkuCreateTime(LocalDateTime.parse(gyySkuInfoEntity.getCreateDate(), sdf));
            }

            //商品修改时间
            if (StringUtils.isNotBlank(gyySkuInfoEntity.getModifyDate())) {
                dmpSkuInfoEntity.setSkuUpdateTime(LocalDateTime.parse(gyySkuInfoEntity.getModifyDate(), sdf));
            }

            //品牌
            dmpSkuInfoEntity.setBrandName(gyySkuInfoEntity.getItemBrandName());

            //商品目录(一级)
            dmpSkuInfoEntity.setParentCategoryName(null);

            //商品目录(二级)
            dmpSkuInfoEntity.setCategoryName(null);

            //售价
            dmpSkuInfoEntity.setSalePrice(gyySkuInfoEntity.getSalesPrice());

            //申报价格
            dmpSkuInfoEntity.setDeclarePrice(new BigDecimal(BigInteger.ZERO));

            //开发员id
            dmpSkuInfoEntity.setDeveloperId(null);

            //开发员名称
            dmpSkuInfoEntity.setDeveloperName(null);

            //平台标识
            dmpSkuInfoEntity.setPlatformSign("管易云");

            dmpSkuInfoEntity.setCreateTime(LocalDateTime.now());
            dmpSkuInfoEntitylist.add(dmpSkuInfoEntity);
        }

        for (DmpSkuInfoEntity dmpSkuInfoEntity : dmpSkuInfoEntitylist) {
            dmpSkuInfoService.checkOrder(dmpSkuInfoEntity);
        }
    }

}
