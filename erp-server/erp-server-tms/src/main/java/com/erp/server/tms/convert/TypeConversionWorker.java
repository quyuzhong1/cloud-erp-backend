package com.erp.server.tms.convert;

import com.erp.tms.aliexpress.model.channel.response.ChannelResponse;
import io.seata.common.util.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName TypeConversionWorker
 * @description: TODO
 * @date 2023年11月08日
 * @version: 1.0
 */
@Component
@Named("TypeConversionWorker")
public class TypeConversionWorker {
    /**
     * 递四方费用模式转换
     *
     * @param obj
     * @return
     */
    @Named("taxModelToDSF")
    public String toJsonString(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (Objects.equals("DDU", obj)){
            return "U";
        }else if (Objects.equals("DDP", obj)){
            return "P";
        }else {
            return null;
        }
    }

    /**
     * 递四方费用模式转换
     *
     * @param obj
     * @return
     */
    @Named("yOrNToBoolean")
    public Boolean yOrNToBoolean(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (Objects.equals("Y", obj)){
            return true;
        }else if (Objects.equals("N", obj)){
            return false;
        }else {
            return false;
        }
    }
    /**
     * 递四方费用模式转换
     *
     * @param obj
     * @return
     */
    @Named("gTokg")
    public Double gTokg(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj instanceof Integer){
            int res = (Integer) obj/1000;
            return (double) res;
        }else if (obj instanceof Double){
            Double d = (Double) obj;
            return d/1000;
        }else {
            return Double.valueOf(0);
        }
    }
    @Named("gTokgStr")
    public String gTokgStr(Object obj) {
        if (Objects.isNull(obj)) {
            return "0";
        }
        if (obj instanceof Integer){
            int res = (Integer) obj/1000;
            return String.valueOf(res);
        }else if (obj instanceof Double){
            Double d = (Double) obj;
            double v = d / 1000;
            return String.valueOf(v);
        }else {
            return "0";
        }
    }
    /**
     * 虾皮 渠道状态0正常1.暂停2.已关闭（默认0）
     * @param obj
     * @return
     */
    @Named("booleanToStatus")
    public Integer booleanToStatus(Object obj) {
        if (Objects.isNull(obj)) {
            return 0;
        }
        if (obj instanceof Boolean){
            if ((Boolean)obj){
                return 0;
            }else {
                return 2;
            }
        }else {
            return 0;
        }
    }

    @Named("convertAging")
    public String convertAging(ChannelResponse chanelInfo){
        if (Objects.nonNull(chanelInfo.getMaxProcessDay())&& Objects.nonNull(chanelInfo.getMinProcessDay())){
            return chanelInfo.getMinProcessDay() +"-"+ chanelInfo.getMaxProcessDay();
        }else{
            return StringUtils.EMPTY;
        }
    }
}
