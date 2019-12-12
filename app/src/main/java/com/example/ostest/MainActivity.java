package com.example.ostest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tv_running_process;
    private ProgressBar process;
    private ImageView iv_idle;

    private ListView lv_ready;
    private ListView lv_block;

    private EditText et_input;
    private Button bt_add;
    private TextView tv_running_code;
    private TextView tv_running_results;
    private ListView lv_results;

    private List<PCB> readyQueue;
    private List<PCB> blockQueue;
    private List<PCB> resultsQueue;

    private boolean isRunning=false;
    private PCB runningProcess;

    private int id=1;

    private String[] code;

    private ReadyAdapter readyAdapter;
    private BlockAdapter blockAdapter;
    private ResultsAdapter resultsAdapter;

    private int  timefile=5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除顶部标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        initView();
        initClick();
        initReadyQueue();
        initBlockQueue();
        initResults();
        //对cpu状态进行检测
        detection();
        CPU();
    }

    private void CPU() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (true){
                        Thread.sleep(1000);
                        runningCode();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private void runningCode() {
                if (runningProcess!=null&&runningProcess.PC<code.length){
                    execCode();
                }
            }
            //解析代码
            private void execCode(String s,int a) {
                timefile--;
                Log.d("dada",String.valueOf(timefile));
                if (timefile%5==0){
                    //因时间片轮转加入到就绪队列末尾
                    BeReady();
                    timefile=5;
                    Log.d("dada1",String.valueOf(timefile));
                }else {
                    if (s.equals("end")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //将结果添加到结果队列去
                                resultsQueue.add(runningProcess);
                                readyAdapter.notifyDataSetChanged();
                                tv_running_process.setText("");
                                tv_running_code.setText("");
                                tv_running_results.setText("");
                                distroy();
                                timefile=5;
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private void detection() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (true){
                        Thread.sleep(1000);
                        checkIdle();

                        checkReady();

                        checkBlock();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //检查当前是否是空闲状态

            private void checkIdle() {
                if (readyQueue.isEmpty()&& blockQueue.isEmpty()&&!isRunning){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_idle.setVisibility(View.VISIBLE);
                            process.setVisibility(View.GONE);
                        }
                    });
                }
            }
            //检查就绪队列
            private void checkReady() {
                if (!readyQueue.isEmpty()&&!isRunning){
                    runningProcess=readyQueue.get(0);
                    readyQueue.remove(0);
                    //更改执行状态
                    isRunning=true;

                    code=runningProcess.IR.split(";");
                    if (!code[code.length - 1].equals("end")) {
                        runningProcess.IR=runningProcess.IR+";end";
                        code=runningProcess.IR.split(";");
                    }
                    //更新界面
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (runningProcess!=null){
                                tv_running_process.setText("id:"+runningProcess.id);
                            }
                            readyAdapter.notifyDataSetChanged();
                            iv_idle.setVisibility(View.GONE);
                            process.setVisibility(View.VISIBLE);
                        }
                    });
                    if (code.length<=0){
                        errCode();
                    }
                    }
                }


            private void checkBlock() {
                //若阻塞队列不为空，就一直更新阻塞队列里的阻塞时间
                if (!blockQueue.isEmpty()){
                    for (int i=0;i<blockQueue.size();i++){
                        blockQueue.get(i).time--;
                        //更新界面
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                blockAdapter.notifyDataSetChanged();
                            }
                        });
                        //当有一个进程阻塞时间小于0后，将改进程唤醒
                        if (blockQueue.get(i).time<0){
                            wakeup(i);
                        }
                    }
                }
            }
        }.start();
    }
    public void wakeup(int i){
        //从阻塞队列中移除一个进程
        PCB pcb=blockQueue.get(i);
        pcb.PSW=PCB.STATUS_READY;
        blockQueue.remove(i);
        //添加到就绪队列中
        readyQueue.add(pcb);
        //更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blockAdapter.notifyDataSetChanged();
                readyAdapter.notifyDataSetChanged();
            }
        });
    }
    public void errCode(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runningProcess.variables=-1024;
                resultsQueue.add(runningProcess);
                resultsAdapter.notifyDataSetChanged();

                tv_running_process.setText(" ");
                tv_running_code.setText(" ");
                tv_running_results.setText(" ");

                distroy();

                Toast.makeText(getApplicationContext(),"输入进程内容有误",Toast.LENGTH_SHORT).show();
            }


        });
    }
    //销毁PCB
    public void distroy() {
        //更新运行状态
        isRunning=false;
        //释放资源
        runningProcess=null;
        //进程数减一
        id--;
    }
    public void BeReady(){

    }
    //初始化就绪队列
    private void initReadyQueue() {
        readyQueue=new ArrayList<>();
        readyAdapter=new ReadyAdapter(this,readyQueue);
        lv_ready.setAdapter(readyAdapter);
    }
    //初始化阻塞队列
    private void initBlockQueue() {
        blockQueue=new ArrayList<>();
        blockAdapter=new BlockAdapter(this,blockQueue);
        lv_block.setAdapter(blockAdapter);
    }
    //初始化结果队列
    private void initResults() {
        resultsQueue=new ArrayList<>();
        resultsAdapter=new ResultsAdapter(this,resultsQueue);
        lv_results.setAdapter(resultsAdapter);
    }



    private void create(String IR,int i){
        //创建一个PCB，并初始化
        PCB pcb=new PCB();
        pcb.id=id;
        pcb.IR=IR;
        pcb.variables=0;
        pcb.PSW= PCB.STATUS_READY;
        pcb.time=0;
        pcb.PC=0;

        //插入就绪队列
        readyQueue.add(pcb);
        readyAdapter.notifyDataSetChanged();
        //进程数目加一
        id++;
    }
    private void initClick() {
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String IR=et_input.getText().toString().trim();
                if (!TextUtils.isEmpty(IR)){
                    if (id<=10){
                        create(IR,5);
                    }else {
                        Toast.makeText(MainActivity.this,"内存已满",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"请将信息填写完整",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initView() {
        tv_running_process=findViewById(R.id.tv_running_process);
        process=findViewById(R.id.process);
        iv_idle=findViewById(R.id.iv_idle);
        lv_ready=findViewById(R.id.lv_ready);
        lv_block=findViewById(R.id.lv_block);

        et_input=findViewById(R.id.et_input);
        bt_add=findViewById(R.id.bt_add);
        tv_running_code=findViewById(R.id.tv_running_code);
        tv_running_results=findViewById(R.id.tv_running_results);
        lv_results=findViewById(R.id.lv_results);
    }
}
