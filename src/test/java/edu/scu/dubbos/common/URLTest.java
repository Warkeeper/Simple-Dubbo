package edu.scu.dubbos.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 12:13
 */
class URLTest {

    @Test
    void testToString() {
        URL url=new URL("127.0.0.1",1230);
        url=url.addParameter("date","2019-4-16");
        url=url.addParameter("weather","cloudy");
        System.out.println(url);
    }
}