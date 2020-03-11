package ccy.markcalendar;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ccy(17022) on 2020-01-13 15:01
 * 每个item（MarkCalendarView）只绘制一个月的数据。
 */
public class MarkCalendarRecyclerViewAdapter extends RecyclerView.Adapter<MarkCalendarRecyclerViewAdapter.ViewHolder> {

    /**
     * 源数据
     */
    private List<MarkCalendarView.Bean> sourceDatas;
    /**
     * recyclerView每个item用的数据：
     * pair.first指item的起始日期，pair.second指item对应的数据
     */
    private List<Pair<Calendar, List<MarkCalendarView.Bean>>> itemDatas;

    int selectIndex = -1;

    private OnSelectListener onSelectListener;

    public MarkCalendarRecyclerViewAdapter(Calendar startDate, List<MarkCalendarView.Bean> sourceDatas) {
        this.sourceDatas = sourceDatas;
        this.itemDatas = sourceDatasToItemDatasAdapter(startDate, sourceDatas);
    }


    /**
     * 以1个月为1个item，同时也要满足最后1列满7个（如果当月剩余天数无法让最后1列满7个，那么从下一个月取对应天数）
     *
     * @param startDate
     * @param sourceDatas
     * @return
     */
    public static List<Pair<Calendar, List<MarkCalendarView.Bean>>> sourceDatasToItemDatasAdapter(Calendar startDate, List<MarkCalendarView.Bean> sourceDatas) {
        List<Pair<Calendar, List<MarkCalendarView.Bean>>> result = new ArrayList<>();

        //举例：假设起始日期为"周三，15号，该月共31天",周三对应DAY_OF_WEEK = 4，即第一列只有4格，
        //查看当月1号为周三，为了让最后一列能铺满，所以要从上个月取3天（周二周一周日），一共使用（15+3=18）天来作为1个item
        int totalTraverseDays = 0;
        Calendar startDateCopy = Calendar.getInstance();
        startDateCopy.setTime(startDate.getTime());

        while (totalTraverseDays < sourceDatas.size()) {

            int currentDay = startDateCopy.get(Calendar.DAY_OF_MONTH);
            int currentWeekOfDay = startDateCopy.get(Calendar.DAY_OF_WEEK);
            int currentMonthDays = startDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH);
            Log.d("ccy", "星期（1表示周日）为" + currentWeekOfDay + ",日期为" + currentDay + "号，月天数为" + currentMonthDays);
            //查看当月1号为周几
            Calendar tmp = Calendar.getInstance();
            tmp.setTime(startDateCopy.getTime());
            tmp.set(Calendar.DAY_OF_MONTH, 1);
            int day1week = tmp.get(Calendar.DAY_OF_WEEK);
            int shouldGetNextMonthDaysCount = day1week - 1;
            Log.d("ccy", "需要从上个月取" + shouldGetNextMonthDaysCount + "天");
            Log.d("ccy", "一共要使用" + (currentDay + shouldGetNextMonthDaysCount) + "天");

            List<MarkCalendarView.Bean> item = sourceDatas.subList(totalTraverseDays, Math.min(sourceDatas.size(), totalTraverseDays + currentDay + shouldGetNextMonthDaysCount));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDateCopy.getTime());
            Pair<Calendar, List<MarkCalendarView.Bean>> pair = new Pair<>(calendar, item);
            result.add(pair);


            startDateCopy.add(Calendar.DAY_OF_MONTH, -(currentDay + shouldGetNextMonthDaysCount));
            totalTraverseDays += currentDay + shouldGetNextMonthDaysCount;
        }


        return result;
    }


    @Override
    public int getItemCount() {
        return itemDatas == null ? 0 : itemDatas.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MarkCalendarView calendarView = new MarkCalendarView(parent.getContext());
        return new ViewHolder(calendarView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //为避免显示不下的风险，第1个和最后1个item不显示年月
        if (position == 0 || position == getItemCount() - 1) {
            ((MarkCalendarView) holder.itemView).setDrawMonthText(false);
            ((MarkCalendarView) holder.itemView).setDrawYearText(false);
        } else {
            ((MarkCalendarView) holder.itemView).setDrawMonthText(true);
            //只有年份不同的节点才显示一个年份
            if (itemDatas.get(position - 1).first.get(Calendar.YEAR) == itemDatas.get(position).first.get(Calendar.YEAR)) {
                ((MarkCalendarView) holder.itemView).setDrawYearText(false);
            } else {
                ((MarkCalendarView) holder.itemView).setDrawYearText(true);
            }
        }

        ((MarkCalendarView) holder.itemView).setDatas(itemDatas.get(position).first, itemDatas.get(position).second);

    }

    public List<MarkCalendarView.Bean> getSourceDatas() {
        return sourceDatas;
    }


    public List<Pair<Calendar, List<MarkCalendarView.Bean>>> getItemDatas() {
        return itemDatas;
    }



    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        if (this.selectIndex != -1) {
            sourceDatas.get(this.selectIndex).isSelect = false;
        }

        this.selectIndex = selectIndex;

        if (this.selectIndex != -1) {
            sourceDatas.get(this.selectIndex).isSelect = true;
        }
    }


    public OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            final MarkCalendarView view = (MarkCalendarView) itemView;
            view.setOnSelectListener(new MarkCalendarView.OnSelectListener() {
                @Override
                public void onSelect(boolean isSelect, int index) {
                    //这个index是单个item内的index，要转换成源数据对应的index

                    MarkCalendarView.Bean bean = view.getDatas().get(index);
                    int sourceSelectIndex = sourceDatas.indexOf(bean);
                    if (sourceSelectIndex == -1) {
                        throw new RuntimeException("item回调的index没在源数据中？？");
                    }

                    //单选模式，要清除上一个被选中的bean
                    Log.d("ccy", "新位置 = " + sourceSelectIndex + ";它是否选中 = " + isSelect + "; 原先选中的位置 = " + selectIndex);
                    if (selectIndex >= 0 && selectIndex < sourceDatas.size() && sourceSelectIndex != selectIndex) {
                        sourceDatas.get(selectIndex).isSelect = false;
                    }
                    selectIndex = sourceSelectIndex;

                    if (onSelectListener != null) {
                        onSelectListener.onSelect(isSelect, sourceDatas.get(selectIndex), selectIndex, getAdapterPosition());
                    }

                    notifyDataSetChanged();
                }
            });
        }
    }


    public interface OnSelectListener {

        /**
         * @param isSelect           被选中还是取消选中
         * @param itemBean           数据
         * @param position           指源数据中的位置
         * @param positionInRecycler 指recyclerView对应item的position
         */
        void onSelect(boolean isSelect, MarkCalendarView.Bean itemBean, int position, int positionInRecycler);
    }

}
