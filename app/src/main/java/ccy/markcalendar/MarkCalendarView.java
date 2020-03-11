package ccy.markcalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ccy(17022) on 2020-01-10 16:17
 * 仿github、豆瓣观影记录的打卡日历view。每一列代表一周，周日为一周的开始。
 * 为了避免一次性要绘制的列数过多（比如传了365天的数据，那么一次要画53列！），
 * 建议使用RecyclerView配合{@link MarkCalendarRecyclerViewAdapter}，以保证屏幕外的item不会提前被绘制且能回收复用
 */
public class MarkCalendarView extends View {

    public static String[] monthStr = new String[]{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
    public static String[] weekStr = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};


    private final ViewConfiguration viewConfiguration;

    /**
     * 打卡数据
     */
    private List<Bean> datas;
    /**
     * 打卡数据的起始日期。即日历的第一格为该日期。
     */
    private Calendar startDate;
    /**
     * 打卡单元边长
     */
    private int unitWidth;

    /**
     * 上间距，用于显示月份年份文字
     */
    private float topPadding;

    /**
     * 列数
     */
    private int columnCount;

    /**
     * 文字画笔
     */
    private Paint textPaint;

    private UnitPainter unitPainter = new DefaultUnitPainter();

    /**
     * 是否绘制月份文字
     */
    private boolean isDrawMonthText = true;
    /**
     * 是否绘制年份文字
     */
    private boolean isDrawYearText = true;

    private OnSelectListener onSelectListener;


    public MarkCalendarView(Context context) {
        this(context, null);
    }

    public MarkCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        viewConfiguration = ViewConfiguration.get(context);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MarkCalendarView);
        unitWidth = (int) ta.getDimension(R.styleable.MarkCalendarView_unitWidth,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics()));
        ta.recycle();


        init();

    }

    private void init() {
        initPaint();

        topPadding = getMinimunVerticalPadding();
    }

    /**
     * 设置数据
     *
     * @param startDate 起始时间
     * @param datas     打卡数据
     */
    public void setDatas(Calendar startDate, List<Bean> datas) {
        this.startDate = startDate;
        this.datas = datas;

        //举例：假设data的数量有15条，起始时间是周三，那么第一列是从周日到周三，减去第一列后，还有（15-4=11）天，
        //按照7天一列的情况，后续还需ceil(11.0/7.0) = 2列，所以一共要3列。
        int startedDayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);
        columnCount = 1 + (int) Math.ceil((datas.size() - startedDayOfWeek) / 7.0f);
        requestLayout();
    }


    private void initPaint() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF3E3E3E);
    }

    /**
     * 日历的上方要显示月份和年份，计算所需留出的空间
     */
    public float getMinimunVerticalPadding() {
        float topPadding = dp2px(9);

        textPaint.setTextSize(sp2px(14));
        Paint.FontMetrics yearMetrics = textPaint.getFontMetrics();
        float textHeight = yearMetrics.descent - yearMetrics.ascent;
        topPadding += textHeight;

        textPaint.setTextSize(sp2px(10));
        Paint.FontMetrics monthMetrics = textPaint.getFontMetrics();
        float textHeight2 = monthMetrics.descent - monthMetrics.ascent;
        topPadding += textHeight2;

        return topPadding;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wMode != MeasureSpec.EXACTLY) {
            wSize = unitWidth * columnCount;
        }

        if (hMode != MeasureSpec.EXACTLY) {
            hSize = (int) (7 * unitWidth + topPadding);
        }

        setMeasuredDimension(wSize, hSize);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (datas == null || datas.isEmpty()) {
            return;
        }

        drawText(canvas);
        drawUnit(canvas);
    }

    protected void drawText(Canvas canvas) {
        canvas.save();

        if (isDrawYearText) {
            textPaint.setTextSize(sp2px(14));
            Paint.FontMetrics yearMetrics = textPaint.getFontMetrics();
            float textHeight = yearMetrics.descent - yearMetrics.ascent;
            //文字距顶部3dp
            float drawTextCenterY = dp2px(3) + textHeight / 2;
            canvas.drawText(
                    "" + startDate.get(Calendar.YEAR),
                    0,
                    drawTextCenterY - (yearMetrics.ascent + yearMetrics.descent) / 2,
                    textPaint
            );
        }
        if (isDrawMonthText) {
            textPaint.setTextSize(sp2px(10));
            Paint.FontMetrics monthMetrics = textPaint.getFontMetrics();
            float textHeight = monthMetrics.descent - monthMetrics.ascent;
            //文字距底部3dp
            float drawTextCenterY = topPadding - dp2px(3) - textHeight / 2;
            canvas.drawText(
                    "" + monthStr[startDate.get(Calendar.MONTH)],
                    0,
                    drawTextCenterY - (monthMetrics.ascent + monthMetrics.descent) / 2,
                    textPaint
            );
        }

        canvas.restore();
    }

    protected void drawUnit(Canvas canvas) {
        canvas.save();
        int row = startDate.get(Calendar.DAY_OF_WEEK);
        int column = 1;
        int left = 0;
        int top = (int) topPadding;
        for (int i = 0; i < datas.size(); i++) {

            unitPainter.onDraw(
                    canvas,
                    left + unitWidth * (column - 1),
                    top + unitWidth * (row - 1),
                    unitWidth,
                    unitWidth,
                    datas.get(i));

            if (--row == 0) {
                column++;
                row = 7;
            }
        }
        canvas.restore();
    }

    //onTouch相关变量
    private float lastX = 0;
    private boolean isClickEvent = true; //判定是否属于点击事件

    /**
     * 不使用scroller来写滑动逻辑了，利用RecyclerView就好,这样保证了回收复用，可以传入大量数据
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x, y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                isClickEvent = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                int deltaX = (int) (lastX - x);
                if (Math.abs(deltaX) < viewConfiguration.getScaledTouchSlop()) {
                    isClickEvent = true;
                } else {
                    isClickEvent = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                if (isClickEvent) {  //判定为点击事件
                    int index = calculateIndexByXY(x, y);
                    if (index != -1) {
                        toggleSelectIndex(index);
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private int calculateIndexByXY(float x, float y) {
        int row = 1 + (int) ((y - topPadding) / unitWidth);
        int column = 1 + (int) (x / unitWidth);

        //举例：若起始日期在周二，那么第一列只有前3个有值（周日、周一、周二），要考虑仅无值的后4个
        int startIndexOffset = startDate.get(Calendar.DAY_OF_WEEK);
        int index = 7 * (column - 1) + startIndexOffset - row;

//        Log.d("ccy", "row = " + row + "; column = " + column + ";index = " + index);
        if (index < 0 || index >= datas.size()) {
            index = -1;
        }
        return index;
    }

    public float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
                .getDisplayMetrics());
    }

    public float sp2px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources()
                .getDisplayMetrics());
    }


    public class DefaultUnitPainter implements UnitPainter {

        private int[] colors = new int[]{
                Color.rgb(236, 237, 240),
                Color.rgb(204, 227, 149),
                Color.rgb(142, 199, 121),
                Color.rgb(78, 151, 72),
                Color.rgb(49, 95, 46),
        };

        private Paint paint;

        public DefaultUnitPainter() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(dp2px(1));
        }

        public int getColor(int count) {
            if (count <= 0) {
                return colors[0];
            } else if (count >= 4) {
                return colors[4];
            } else {
                return colors[count];
            }
        }

        @Override
        public void onDraw(Canvas canvas, int left, int top, int suggestWidth, int suggestHeight, Bean bean) {
            if (bean == null || bean.count < 0) {
                return;
            }

            canvas.save();

            paint.setColor(getColor(bean.count));
            canvas.drawRoundRect(
                    left + dp2px(1),
                    top + dp2px(1),
                    left + suggestWidth - dp2px(1),
                    top + suggestHeight - dp2px(1),
                    dp2px(2),
                    dp2px(2),
                    paint
            );

            if (bean.isSelect) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0xFFFF0000);
                canvas.drawRoundRect(
                        left + dp2px(1),
                        top + dp2px(1),
                        left + suggestWidth - dp2px(1),
                        top + suggestHeight - dp2px(1),
                        dp2px(2),
                        dp2px(2),
                        paint
                );
                paint.setStyle(Paint.Style.FILL);

            }

            if (bean.isMark) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0xFF7CB4EF);
                canvas.drawCircle(
                        left + suggestWidth / 2.0f,
                        top + suggestHeight / 2.0f,
                        suggestWidth / 4.0f,
                        paint
                );
                paint.setStyle(Paint.Style.FILL);
            }

            canvas.restore();
        }

        public float dp2px(float dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
                    .getDisplayMetrics());
        }
    }


    public UnitPainter getUnitPainter() {
        return unitPainter;
    }

    public void setUnitPainter(UnitPainter unitPainter) {
        this.unitPainter = unitPainter;
        invalidate();
    }

    public boolean isDrawMonthText() {
        return isDrawMonthText;
    }

    public void setDrawMonthText(boolean drawMonthText) {
        isDrawMonthText = drawMonthText;
//        invalidate();
    }

    public boolean isDrawYearText() {
        return isDrawYearText;
    }

    public void setDrawYearText(boolean drawYearText) {
        isDrawYearText = drawYearText;
//        invalidate();
    }


    public void toggleSelectIndex(int selectIndex) {

        //不重置其他位置被选中的状态，允许多选

        datas.get(selectIndex).isSelect = !datas.get(selectIndex).isSelect;
        if (onSelectListener != null) {
            onSelectListener.onSelect(datas.get(selectIndex).isSelect, selectIndex);
        }

        invalidate();
    }


    public OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public List<Bean> getDatas() {
        return datas;
    }

    public Calendar getStartDate() {
        return startDate;
    }


    /**
     * 打卡单元绘画者
     */
    public interface UnitPainter {

        /**
         * 绘制单元。提供LTWH绘制范围
         *
         * @param canvas
         * @param left
         * @param top
         * @param suggestWidth
         * @param suggestHeight
         * @param bean
         */
        void onDraw(Canvas canvas, int left, int top, int suggestWidth, int suggestHeight, Bean bean);
    }

    public interface OnSelectListener {
        /**
         * @param isSelect 是被选中了还是被取消选中了
         * @param index
         */
        void onSelect(boolean isSelect, int index);
    }

    public static class Bean {
        /**
         * 次数
         */
        public int count;
        /**
         * 是否选中状态
         */
        public boolean isSelect;
        /**
         * 自定义标记位
         */
        public boolean isMark;

        @Override
        public String toString() {
            return "Bean{" +
                    "count=" + count +
                    ", isSelect=" + isSelect +
                    ", isMark=" + isMark +
                    '}';
        }
    }


}
