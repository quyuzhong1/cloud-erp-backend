package com.erp.model.sys.entity.password;


import com.common.core.utils.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @Classname PassHandler

 * @Date 2022-07-06 11:49
 * @Created by yl
 */
@Slf4j
public class PassHandler {

    private PassHandler() {
    }

    /**
     * checkPass:校验密码是否一致
     *
     * @param inputPass 用户传入的密码
     * @param salt      数据库保存的密码随机码
     * @param pass      数据库保存的密码MD5
     * @return
     */
    public static boolean checkPass(String inputPass, String salt, String pass) {
        String pwdMd5 = Md5Util.md5(inputPass);
        String md5 = Md5Util.md5(pwdMd5 + salt);
        log.info("checkPass input:{},db:{}",md5,pass);
        return md5.equals(pass);
    }

    /**
     * buildPassword:用于用户注册时产生一个密码
     *
     * @param inputPass 输入的密码
     * @return PassInfo 返回一个密码对象，记得保存
     */
    public static PassEntity buildPassword(String inputPass) {
        //产生一个6位数的随机码
        String salt = RandomStringUtils.randomAlphabetic(10);
        //加密后的密码
        String encryptPassword = Md5Util.md5(Md5Util.md5(inputPass) + salt);
        //返回对象
        return new PassEntity(salt, encryptPassword);
    }
}
