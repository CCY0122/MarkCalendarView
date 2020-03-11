package ccy.markcalendar;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by ccy(17022) on 2020-03-11 11:26
 */
public class DemoActivity extends AppCompatActivity {

    TextView textView;
    RecyclerView recyclerView;

    List<MarkCalendarView.Bean> datas;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        recyclerView = findViewById(R.id.calendar_list);
        textView = findViewById(R.id.tv);


        datas = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            MarkCalendarView.Bean bean = new MarkCalendarView.Bean();
            bean.count = (int) (new Random().nextFloat() * 5);
//            bean.isMark = new Random().nextBoolean() && new Random().nextBoolean() && new Random().nextBoolean() && new Random().nextBoolean();
            bean.isMark = (i + 1 )  % 20 == 0;
            datas.add(bean);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2020, 1, 12); //起始日期设置为2月12号
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        MarkCalendarRecyclerViewAdapter adapter = new MarkCalendarRecyclerViewAdapter(calendar, datas);
        adapter.setOnSelectListener(new MarkCalendarRecyclerViewAdapter.OnSelectListener() {
            @Override
            public void onSelect(boolean isSelect, MarkCalendarView.Bean itemBean, int position, int positionInRecycler) {
                String selectText = (isSelect ? "选中了" : "取消选中了") + "第" + position + "个位置，打卡次数为" + itemBean.count;

                Calendar dateCopy = Calendar.getInstance();
                dateCopy.setTime(calendar.getTime());
                dateCopy.add(Calendar.DAY_OF_MONTH, -position);
                String dateText = "日期为 " + dateCopy.get(Calendar.YEAR) + "年"
                        + (dateCopy.get(Calendar.MONTH) + 1) + "月"
                        + dateCopy.get(Calendar.DAY_OF_MONTH) + "日"
                        + "星期(1表示周日)" + (dateCopy.get(Calendar.DAY_OF_WEEK));

                textView.setText(selectText + "\n" + dateText);

            }
        });
        recyclerView.setAdapter(adapter);
    }
}
