package com.lmj.platformserver.utils;

import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;

public class CaptchaUtil {

    private static final Integer PIC_HEIGHT = 38;
    private static final Integer PIC_WIDTH = 100;
    private static final CodeGenerator CODE_GENERATOR = new MathGenerator();

    public static String createCaptcha() {
        ShearCaptcha captcha = cn.hutool.captcha.CaptchaUtil.createShearCaptcha(PIC_WIDTH, PIC_HEIGHT);
        captcha.setGenerator(CODE_GENERATOR);
        captcha.createCode();
        return captcha.getImageBase64();
    }

    public static boolean verifyCaptcha(String code, String userInputCode) {
        return CODE_GENERATOR.verify(code, userInputCode);
    }
}
