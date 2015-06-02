# SimpleSwitch
不需要图片资源的Switch，参考https://github.com/Leaking/SlideSwitch 和android-support-v7的SwitchCompat，还需完善，多多指教。
###   对SlideSwitch改进有3：
###   1.继承CompoundButton，方便使用，不用自定义接口了。
###   2.在OnDraw过程中不创建对象
###   3.优化onTouchEvent和动画的逻辑，例如手指触摸滑块并且移动才视为要移动滑块，快速连续点击SimpleSwitch不影响滑块滑动，嵌套在ScrollView里面时，SimpleSwitch工作正常，不会卡住。
###   下图中依次是多个SlideSwitch，最后一个是SimpleSwitch
![image](https://github.com/lr0775/SimpleSwitch/blob/master/img1.png)
![image](https://github.com/lr0775/SimpleSwitch/blob/master/img2.png)
