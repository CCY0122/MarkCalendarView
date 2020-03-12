# MarkCalendarView
仿github、leetcode、豆瓣观影记录的打卡日历view

## 效果图：

<br/>

![img1](Screenshot_2020-03-11-12-46-40.png)

<br/>

![img2](MyVideo_2.gif)

## 使用：


1、直接使用`MarkCalendarView` :xml中使用它，在代码中调用`markCalendarView.setDatas(Calendar startDate, List<Bean> datas)`传入时间数据(第一个参数为起始日期，第二个参数为从起始日期开始前面的打卡历史数据，里面记录着当天打卡次数等信息）。<br/>
2、打卡数据很多的话，建议使用RecyclerView，以保证屏幕外的数据不会被绘制。已提供对应的adapter:`MarkCalendarRecyclerViewAdapter`。demo见`DemoActivity`
3、可自定义打卡小正方形的绘制逻辑（如定制打卡次数所对应的颜色，默认是绿色），祥见`MarkCalendarView` 源码
