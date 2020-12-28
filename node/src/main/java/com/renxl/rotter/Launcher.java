package com.renxl.rotter;

import com.renxl.rotter.config.CompomentBuilder;
import com.renxl.rotter.config.CompomentManager;

/**
 *
 *
 * 程序正常退出
 * 使用System.exit()
 * 终端使用Ctrl+C触发的中断
 * 系统关闭
 * OutOfMemory宕机
 * 使用Kill pid命令干掉进程（注：在使用kill -9 pid时，是不会被调用的）
 *
 *
 * @description:
 * @author: renxl
 * @create: 2020-12-28 19:57
 */
public class Launcher {
    public static void main(String[] args) {
        //
        CompomentManager manager = CompomentBuilder.bulid();
        manager.start();

        // 关闭处理
        Runtime.getRuntime().addShutdownHook(new Thread(() -> manager.stop()));





    }
}
