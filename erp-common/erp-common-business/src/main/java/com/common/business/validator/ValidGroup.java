package com.common.business.validator;

import javax.validation.groups.Default;

/**
 * 验证分组 用于校验不同的方法
 * 继承默认的校验分组，不填写任何分组的校验注解的属性，都会属于默认分组
 *
 * @Author Cloud
 * @Date 2023/7/19 12:05
 **/
public interface ValidGroup extends Default {

    interface Add extends ValidGroup {

    }
    interface Update extends ValidGroup {

    }
    interface Query extends ValidGroup {

    }
    interface Delete extends ValidGroup {

    }
}
