package edu.scu.dubbos.config;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 10:40
 */
public class ApplicationConfig {
    private String name;

    public ApplicationConfig() {
    }

    public ApplicationConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void checkAndUpdate(){
        if(name==null){
            name="DubbosDefaultApplication";
        }
    }
}
