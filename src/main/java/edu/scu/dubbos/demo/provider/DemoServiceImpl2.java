package edu.scu.dubbos.demo.provider;

import edu.scu.dubbos.demo.api.DemoService;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 16:16
 */
public class DemoServiceImpl2 implements DemoService {

    @Override
    public String hello(String name) {
        return "Hello," + name+" , this is DemoService2";
    }
}
