package com.example.ostest;

public class PCB {
    public static final int STATUS_READY=0;//就绪
    public static final int STATUS_BLOCK=1;//阻塞
    public static final int STATUS_EXECUTE=2;//执行
    public int id;  //进程标识符
    public int time; // 阻塞时间
    public int variables;//中间变量

    public String IR; //IR寄存器 存储指令
    public int PSW; //程序状态寄存器
    public int PC;  //PC寄存器 存储将要读取的指令地址
}
