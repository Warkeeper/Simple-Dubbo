package edu.scu.dubbos.demo.provider;

import edu.scu.dubbos.demo.api.DemoService;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 16:16
 */
public class DemoServiceImpl3 implements DemoService {

    @Override
    public String hello(String name) {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return "Hello," + name+" , this is DemoService3";
    }
}
