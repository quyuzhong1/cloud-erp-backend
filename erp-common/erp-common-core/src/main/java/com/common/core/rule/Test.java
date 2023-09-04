package com.common.core.rule;/**
 * @author Lambda
 * @Classname User
 * @Description TODO
 * @Date 2023-09-04 14:19
 * @Created by yl
 */

import lombok.Data;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-09-04 14:19
 */
@Data
public class Test {

    private String skuNo;

    private String platform;


    public Test(String skuNo,String platform) {
        this.skuNo = skuNo;
        this.platform = platform;
    }
}
