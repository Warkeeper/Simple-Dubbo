package edu.scu.dubbos.registry;

import java.util.List;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 15:56
 */
public interface ChildListener {
    void childChanged(String path, List<String> children);
}
