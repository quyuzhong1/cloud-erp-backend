package com.erp.model.dmp.enums;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import cn.hutool.core.collection.CollUtil;

/**
 * <p>
 * 拉取任务 状态 枚举
 * </p>
 *
 * @author shukai
 * @since 2024-06-11 09:37:12
 */
public enum DmpInputTaskStatusEnum implements EnumMessage {
	INIT("init", "待拉取"),
	FDS("fds", "上传fds"),
	MONGO("mongo", "保存mongo"),
	DMP("dmp", "保存dmp"),
	FINISH("finish", "完成"),
	ERROR("error", "异常"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;
    
    public static Map<DmpInputTaskStatusEnum, List<DmpInputTaskStatusEnum>> preStatusMap;
    public static Map<DmpInputTaskStatusEnum, List<DmpInputTaskStatusEnum>> nextStatusMap;
    static {
    	preStatusMap = new HashMap<>();
    	nextStatusMap = new HashMap<>();
    	DmpInputTaskStatusEnum[] values = DmpInputTaskStatusEnum.values();
    	for(DmpInputTaskStatusEnum value : values) {
    		List<DmpInputTaskStatusEnum> preStatusList = new ArrayList<>();
    		List<DmpInputTaskStatusEnum> nextStatusList = new ArrayList<>();
    		if(value == DmpInputTaskStatusEnum.INIT) {
    			
    			nextStatusList.add(DmpInputTaskStatusEnum.FDS);
    			nextStatusList.add(DmpInputTaskStatusEnum.MONGO);
    			nextStatusList.add(DmpInputTaskStatusEnum.DMP);
    			nextStatusList.add(DmpInputTaskStatusEnum.FINISH);
    		}else if(value == DmpInputTaskStatusEnum.FDS) {
    			preStatusList.add(DmpInputTaskStatusEnum.INIT);
    			
    			nextStatusList.add(DmpInputTaskStatusEnum.MONGO);
    			nextStatusList.add(DmpInputTaskStatusEnum.DMP);
    			nextStatusList.add(DmpInputTaskStatusEnum.FINISH);
    		}else if(value == DmpInputTaskStatusEnum.MONGO) {
    			preStatusList.add(DmpInputTaskStatusEnum.INIT);
    			preStatusList.add(DmpInputTaskStatusEnum.FDS);
    			
    			nextStatusList.add(DmpInputTaskStatusEnum.DMP);
    			nextStatusList.add(DmpInputTaskStatusEnum.FINISH);
    		}else if(value == DmpInputTaskStatusEnum.DMP) {
    			preStatusList.add(DmpInputTaskStatusEnum.INIT);
    			preStatusList.add(DmpInputTaskStatusEnum.FDS);
    			preStatusList.add(DmpInputTaskStatusEnum.MONGO);
    			
    			nextStatusList.add(DmpInputTaskStatusEnum.FINISH);
    		}else if(value == DmpInputTaskStatusEnum.FINISH) {
    			preStatusList.add(DmpInputTaskStatusEnum.INIT);
    			preStatusList.add(DmpInputTaskStatusEnum.FDS);
    			preStatusList.add(DmpInputTaskStatusEnum.MONGO);
    			preStatusList.add(DmpInputTaskStatusEnum.DMP);
    			
    		}
    		preStatusMap.put(value, preStatusList);
    		nextStatusMap.put(value, nextStatusList);
    	}
    }

    DmpInputTaskStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DmpInputTaskStatusEnum statusEnum : DmpInputTaskStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    public static List<DmpInputTaskStatusEnum> getNextStatus(String status){
    	return nextStatusMap.get(EnumMessage.getByCode(DmpInputTaskStatusEnum.class, status));
    }
    
    public static boolean isNextStatus(String status ,DmpInputTaskStatusEnum updateTaskStatus) {
    	DmpInputTaskStatusEnum currStatus = EnumMessage.getByCode(DmpInputTaskStatusEnum.class, status);
    	if(currStatus == updateTaskStatus) {
    		return true;
    	}
    	List<DmpInputTaskStatusEnum> list = nextStatusMap.get(updateTaskStatus);
    	if(CollUtil.isNotEmpty(list)) {
			return list.contains(currStatus);
    	}
    	return false;
    }

    /**
     * 转换异常状态名称
     * @param taskStatus 原始任务状态
     * @param errorCount 错误次数
     * @param globalErrorValue 全局异常阈值
     * @return 异常状态名称
     */
    public static String convertStateName(String taskStatus, Integer errorCount, String globalErrorValue) {
        if (DmpInputTaskStatusEnum.INIT.getCode().equals(taskStatus) && errorCount > 0) {
            return "系统重试中";
        }
        if (StringUtils.isBlank(globalErrorValue) && DmpInputTaskStatusEnum.ERROR.getCode().equals(taskStatus)) {
            return "待人工处理";
        }
        // 配置优先
        if (StringUtils.isNotBlank(globalErrorValue)){
            if (errorCount >= Integer.parseInt(globalErrorValue) &&
                    (DmpInputTaskStatusEnum.ERROR.getCode().equals(taskStatus) || DmpInputTaskStatusEnum.INIT.getCode().equals(taskStatus) )) {
               return "待人工处理";
            }
        }
        return DmpInputTaskStatusEnum.getName(taskStatus);
    }
}
