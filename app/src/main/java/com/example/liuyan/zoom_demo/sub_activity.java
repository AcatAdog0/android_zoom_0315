package com.example.liuyan.zoom_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.util.Timer;
import java.util.TimerTask;



public class sub_activity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView text_view;
    private View view1;
    private String selected_item;
    private long selected_item_id;
    private float matrix_value[] = new float[9];
    private float matrix_xvalue;
    private float matrix_yvalue;
    private float screen_w;
    private float screen_h;
    private float current_w;
    private float current_h;
    private float ori_w;
    private float ori_h;
    private float scale;
    private double scale_temp;

    private boolean is_zoom;
    //private boolean is_min;
    private long start_time;
    private long time_difference;
    private boolean is_first;

    private float translation_x;
    private float translation_y;
    private float point_x;
    private float point_y;
    private float point_1x;
    private float point_1y;
    private float point_2x;
    private float point_2y;
    //private float left;

    private float distance_between_points;
    private PointF point_mid;

    private Matrix matrix = new Matrix();
    private Matrix text_view_matrix = new Matrix();
    private Matrix saved_matrix = new Matrix();//存放图片当前位置的matrix
    private Matrix ori_matrix = new Matrix();//初始matrix
    private Matrix max_matrix = new Matrix();//最大matrix

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 第一个按下的手指的点
    private PointF start_point = new PointF();
    // 两个按下的手指的触摸点的中点
    private PointF mid_point = new PointF();
    private float point_mid_x;
    private float point_mid_y;
    // 初始的两个手指按下的触摸点的距离
    private float ori_dis = 1f;
    private float new_dis;
    //放大缩小的初始距离
    private float zoom_ori_dis;
    private float set_dis;
    private String test_number;
    private File filename;
    private String dirname;
    private String name;
    private File dir;
    private String[] dataset = {"","","","","","","","","","",""};
    private Vibrator vibrator;
    private int result;

    private float center_x;
    private float center_y;
    private float navigation_bar_height;

    private Date last_operation_time;
    private boolean is_save;
    private boolean is_touched;

    private Timer timer;
    private TimerTask task;






    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_activity);
        mImageView = this.findViewById(R.id.image_demo);
        mImageView.bringToFront();
        text_view = this.findViewById(R.id.textView2);
        Button btn_2 = this.findViewById(R.id.button2);
        final BitmapDrawable bit_draw = (BitmapDrawable) mImageView.getDrawable();
        final Bitmap bitmap = bit_draw.getBitmap();
        //text_view.setBackgroundDrawable(getResources().getDrawable(R.drawable.textview_border));
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        navigation_bar_height = resources.getDimensionPixelSize(resourceId);


        final float dp_scale = getResources().getDisplayMetrics().density;


        Rect outRect1 = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        screen_w = metrics.widthPixels;
        screen_h = metrics.heightPixels;
        Log.e("screen_w", String.valueOf(screen_w));
        Log.e("screen_w", String.valueOf(screen_h));

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();

        float density  = dm.density;		// 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi;		// 屏幕密度（每寸像素：120/160/240/320）
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;

        Log.e("DisplayMetrics", "xdpi=" + xdpi + "; ydpi=" + ydpi);
        Log.e("DisplayMetrics", "density=" + density + "; densityDPI=" + densityDPI);






        ori_w = bitmap.getWidth();
        ori_h = bitmap.getHeight();
        current_w = ori_w = bitmap.getWidth();
        is_first = true;

        Log.e("image_w", String.valueOf(ori_w));
        Log.e("image_h", String.valueOf(ori_h));
        Intent intent = getIntent();

        //选择的函数
        selected_item = intent.getStringExtra("function_selected");
        //选择的函数id
        selected_item_id = intent.getLongExtra("function_selected_id", 0);
        //自定义初始距离
        set_dis = Float.parseFloat(intent.getStringExtra("set_dis"));
        if (set_dis == 0) {
            set_dis = 10;
        }
        //测试编号
        test_number = intent.getStringExtra("number");

        dirname = intent.getStringExtra("dirname");
        name = intent.getStringExtra("excel_name");
        dir = new File("/storage/sdcard0/" + dirname);
        filename = new File(dir, name);


        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                is_touched = true;




                /*RelativeLayout.LayoutParams par
                        = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();*/
                //text_view.setText(mImageView.getLeft());
                ori_matrix.set(view.getImageMatrix());
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                max_matrix.set(matrix);
                mImageView.setScaleType(ImageView.ScaleType.MATRIX);
                Log.e("number", String.valueOf(test_number));
                // 判断触摸事件
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        // 第一个手指按下
                        if (is_first) {
                            start_time = System.currentTimeMillis();
                            point_1x = event.getX(0);
                            point_1y = event.getY(0);
                            Log.e("1x", String.valueOf(point_1x));
                            Log.e("1y", String.valueOf(point_1y));
                        }

                        matrix.set(view.getImageMatrix());
                        saved_matrix.set(matrix);//记录当前位置
                        start_point.set(event.getX(), event.getY());//get移动距离
                        mode = DRAG;
                        saved_matrix.set(mImageView.getImageMatrix());

                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // 第二个手指按下
                        if (is_first) {
                            point_2x = event.getX(1);
                            point_2y = event.getY(1);

                            //距离和中点
                            distance_between_points = distance(event);
                            point_mid = middle(event);
                            point_mid_x = (point_1x + point_2x) / 2;
                            point_mid_y = (point_1y + point_2y) / 2;
                            saved_matrix.set(mImageView.getImageMatrix());

//                            Log.e("2x", String.valueOf(point_2x));
//                            Log.e("2y", String.valueOf(point_2y));
//                            Log.e("distance", String.valueOf(distance_between_points));
//                            Log.e("mid", String.valueOf(point_mid));
                        }
                        ori_dis = distance(event);
                        //ori_dis_copy = distance(event);
                        if (ori_dis > 10f) //防止不规则手指触碰
                        {
                            saved_matrix.set(matrix);
                            mid_point = middle(event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        is_first = false;
                        is_touched = false;

                        //last time being touched
                        last_operation_time = new Date(System.currentTimeMillis());

                        is_save();


                        Log.e("is_save", String.valueOf(is_save));
                        //back to boundary（边界检查）
                        if (matrix_xvalue < 0) {
                            if (matrix_yvalue < 0) {
                                matrix.postTranslate(-matrix_xvalue, -matrix_yvalue);
                            } else {
                                matrix.postTranslate(-matrix_xvalue, 0);
                            }
                            break;
                        }
                        if (matrix_xvalue + current_w > screen_w) {
                            if (matrix_yvalue < 0) {
                                matrix.postTranslate(screen_w - matrix_xvalue - current_w, -matrix_yvalue);
                            } else {
                                matrix.postTranslate(-matrix_xvalue - current_w + screen_w, 0);
                            }
                            break;
                        }
                        if (matrix_yvalue < 0) {
                            matrix.postTranslate(0, -matrix_yvalue);
                            break;
                        }
                        if (matrix_yvalue + current_h > screen_h) {
                            matrix.postTranslate(0, screen_h - matrix_yvalue - current_h);
                            break;
                        }
                        saved_matrix.set(mImageView.getImageMatrix());

                        break;


                    case MotionEvent.ACTION_POINTER_UP:
                        // 手指放开
                        mode = NONE;
                        is_first = false;
                        is_zoom = false;
                        maxmin_scale();
                        saved_matrix.set(mImageView.getImageMatrix());
                        break;
                    case MotionEvent.ACTION_MOVE:

                        maxmin_scale();
                        // 手指滑动
                        if (mode == DRAG) {
                            // 一个手指拖动
                            matrix.set(saved_matrix);//在没有移动的基础上进行移动
                            point_x = event.getX();
                            point_y = event.getY();

                            matrix.getValues(matrix_value);
                            //left = matrix_value[2];

                            translation_x = point_x - start_point.x;
                            translation_y = point_y - start_point.y;


                            matrix.postTranslate(translation_x, translation_y);
                            matrix.getValues(matrix_value);     //获得matrix数组里的元素
                            matrix_xvalue = matrix_value[2];    //图片最左侧到屏幕左边界的长度
                            matrix_yvalue = matrix_value[5];    //图片最上侧到屏幕上边界的长度
                            Log.e("x", String.valueOf(current_h));
                            Log.e("x", String.valueOf(screen_h));
                            Log.e("x", String.valueOf(matrix_xvalue));
                            Log.e("x", String.valueOf(matrix_yvalue));
                            Log.e("x", String.valueOf(text_view.getLeft()));
                        }
                        if (mode == ZOOM) {
                            //zoom_w = ori_w;
                            // 两个手指滑动
                            new_dis = distance(event);

                            //判断是否进行缩放
                            is_zoom();
                            //is_min();
                            if (is_zoom) {
                                matrix.set(saved_matrix);//在没有缩放的基础上进行缩放
                                scale = new_dis / ori_dis;

                                //选择zoom_function
                                switch ((int) selected_item_id) {
                                    case 0:
                                        //scale = scale;
                                        break;
                                    case 1:
                                        scale_temp = Math.sqrt(scale);
                                        scale = (float) scale_temp;
                                        break;
                                    case 2:
                                        scale_temp = Math.pow(scale, 2);
                                        scale = (float) scale_temp;
                                        break;
                                }
                                matrix.postScale(scale, scale, mid_point.x, mid_point.y);
                                Log.e("ori", String.valueOf(scale_temp));
                                Log.e("id", String.valueOf(selected_item_id));


                                //图片的当前高宽
                                matrix.getValues(matrix_value);
                                current_w = ori_w * matrix_value[0];
                                current_h = ori_h * matrix_value[4];

                            }
                        }
                        //saved_matrix.set(mImageView.getImageMatrix());
                        break;

                }

                // 设置图片的matrix为最后的matrix
                view.setImageMatrix(matrix);
                return true;
            }


            // 计算两个触摸点之间的距离
            private float distance(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }

            // 计算两个触摸点的中点
            private PointF middle(MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                return new PointF(x / 2, y / 2);
            }

            private void is_save() {

                //get current time
                Date time_now = new Date(System.currentTimeMillis());

                long time_period = time_now.getTime() - last_operation_time.getTime();
                Log.e("time_period", String.valueOf(time_period));

                if (time_period >= 3000)
                    is_save = true;
                if (time_period < 3000)
                    is_save = false;
                last_operation_time = new Date(System.currentTimeMillis());
                Log.e("time_now", String.valueOf(last_operation_time));
            }


            //图片的最大和最小放缩倍数
            private void maxmin_scale() {

                if (current_w < ori_w) {
                    matrix.set(ori_matrix);
                    //saved_matrix = ori_matrix;
                    ori_matrix.getValues(matrix_value);
                    Log.e("new", String.valueOf(ori_matrix));
                    current_w = ori_w * matrix_value[0];
                    matrix.postScale(ori_w / current_w, ori_w / current_w, mid_point.x, mid_point.y);
                    matrix.getValues(matrix_value);
                    current_w = ori_w * matrix_value[0];
                }
                if (current_w > screen_w) {
                    //mImageView.setScaleType(ImageView.ScaleType.CENTER);
                    matrix.set(max_matrix);
                    max_matrix.getValues(matrix_value);
                    current_w = ori_w * matrix_value[0];
                    matrix.postScale(screen_w / current_w, screen_w / current_w, mid_point.x, mid_point.y);
                }

            }


            //判断是否zoom,并且初始化zoom_ori_dis
            private void is_zoom() {

                if (ori_dis > set_dis) {
                    is_zoom = true;
                }
                if (new_dis > set_dis && ori_dis < set_dis) {
                    is_zoom = true;
                    ori_dis = set_dis;
                }
//                if(new_dis - ori_dis < set_dis|| current_w < ori_w){
//                    is_zoom = false;
//                }
//                if(new_dis < ori_dis){
//                    is_zoom = true;
//                    zoom_ori_dis = ori_dis;
//                }
//                if(new_dis - ori_dis > set_dis){
//                    is_zoom = true;
//                    zoom_ori_dis = ori_dis + set_dis;
//                }
            }

        });



        //计算时间并存储到文件里
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(30);   //震动反馈
                is_first = true;    //重置is_first

                time_difference = System.currentTimeMillis() - start_time;
                check_point(matrix);    //检查最后图片位置是否符合要求

                create_dataset();       //创建excel表头
                data2Excel();           //把需要参数写入excel

                Context context = getApplicationContext();
                CharSequence text = "您已完成此次任务";   //提示实验完成
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.e("time", String.valueOf(time_difference));
            }


            //创建excel表头
            public String[] create_dataset(){

                dataset[0] = selected_item;
                if(set_dis == 10){
                    dataset[1] = String.valueOf(0);
                }
                else {
                    dataset[1] = String.valueOf(set_dis);
                }
                dataset[2] = String.valueOf(point_1x);
                dataset[3] = String.valueOf(point_1y);
                dataset[4] = String.valueOf(point_2x);
                dataset[5] = String.valueOf(point_2y);
                dataset[6] = String.valueOf(distance_between_points);
                dataset[7] = String.valueOf(point_mid_x);
                dataset[8] = String.valueOf(point_mid_y);
                dataset[9] = String.valueOf(time_difference);
                dataset[10] = String.valueOf(result);
                return dataset;
            }

            //check point
            public void check_point(Matrix matrix){

                matrix.getValues(matrix_value);
                float matrix_x = matrix_value[2];
                float matrix_y = matrix_value[5];
                center_x = screen_w / 2;
                center_y = text_view.getHeight() / 2 + text_view.getTop();

                Log.e("height", String.valueOf(center_y));
                Log.e("width", String.valueOf(center_x));


                float current_x = current_w / 2 + matrix_x;
                float current_y = current_h / 2 + matrix_y;

                float x = current_x - center_x;
                float y = current_y - center_y;
                float distance = (float)Math.sqrt(x * x + y * y);
                float delta = matrix_x - text_view.getLeft();
                Log.e("distance", String.valueOf(distance));
                Log.e("distance", String.valueOf(current_x));
                Log.e("distance", String.valueOf(text_view.getLeft()));
                if(distance < 10 && delta < 30 && delta > -20){
                    result = 1;
                }
                else {
                    result = 0;
                }



            }


            //data to Excle
            public void data2Excel(){

                try{


                    //Workbook wb = Workbook.getWorkbook(filename);
                    //WorkbookSettings ws = new WorkbookSettings();
                    Workbook workbook = Workbook.getWorkbook(filename);
                    Sheet sheet = workbook.getSheet(0);
                    int length = sheet.getRows();
                    Log.e("length", String.valueOf(length));
                    WritableWorkbook writebook = Workbook.createWorkbook(filename, workbook);
                    WritableSheet w_sheet = writebook.getSheet(0);

                    for (int i = 0; i < dataset.length; i++ ) {
                        Label label = new Label(i, length, dataset[i]);
                        w_sheet.addCell(label);
                    }
                    writebook.write();
                    writebook.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                }


        });


    }
}
