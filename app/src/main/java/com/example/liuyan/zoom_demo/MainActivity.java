package com.example.liuyan.zoom_demo;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class MainActivity extends AppCompatActivity {
    private String function_selected;
    private long function_selected_id;
    private String set_dis;
    private String number;
    private String name;
    private String dirname;
    private static String[] title = {"function", "set_distance", "point_1x", "point_1y", "point_2x", "point_2y", "distance", "mid_pointx", "mid_pointy", "cost_time", "result"};
    private boolean is_changed = true;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_1 =  this.findViewById(R.id.button);
        final EditText tV =  this.findViewById(R.id.editText);
        final Spinner spinner = this.findViewById(R.id.spinner);
        final Spinner spinner1 = this.findViewById(R.id.spinner2);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                function_selected = (String)spinner.getSelectedItem();
                function_selected_id = spinner.getSelectedItemId();
                set_dis = (String) spinner1.getSelectedItem();
                number = tV.getText().toString();
                //vibrator.vibrate(30);

                //监控number是否变化
                tV.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        is_changed = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //is_changed = true;
                    }
                });
                //number变化则创建新的文件，否则不创建
                if(is_changed) {
                    create_Excel();
                    is_changed = false;
                }

                //传递参数
                Intent intent = new Intent(MainActivity.this, sub_activity.class);
                intent.putExtra("function_selected",function_selected);
                intent.putExtra("function_selected_id", function_selected_id);
                intent.putExtra("set_dis",set_dis);
                intent.putExtra("number", number);
                intent.putExtra("excel_name", name);
                intent.putExtra("dirname", dirname);
                startActivity(intent);

            }
        });
    }



    //创建路径和文件
    public void create_Excel(){
        boolean flag = false;
        SimpleDateFormat timesdf = new SimpleDateFormat("yyyy-MM-dd");
        String FileTime =timesdf.format(new Date());
        dirname = "zoom_test";
        name = FileTime + "_test_" + number + ".xls";
        File dir = new File("/storage/sdcard0/" + dirname);
        if (!dir.exists()){
            flag = true;
        }
        if(is_changed) {
            dir.mkdir();
        }

            File filename = new File(dir, name);
            //FileOutputStream fos;
            try {

                    //fos = new FileOutputStream(filename, true);
                    WritableWorkbook writebook = Workbook.createWorkbook(filename);
                    WritableSheet w_sheet = writebook.createSheet("test", 0);
                    for (int i = 0; i < title.length; i++) {
                        Label lable = new Label(i, 0, title[i]);
                        w_sheet.addCell(lable);
                    }
                    //w_sheet.addCell(new Label(3, 3 , "111"));
                    writebook.write();
                    writebook.close();

            } catch (Exception e) {
                e.printStackTrace();
            }


    }
}
