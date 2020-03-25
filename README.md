# SuspendedBall
悬浮球
## 悬浮球介绍

### 安装后需要的权限
*  非root手机
1. 激活设备管理器
2. 打开辅助功能
3. 允许出现在最顶部权限

*  root手机
1. 打开辅助功能
2. 允许出现在最顶部权限
3. 申请root权限

### 功能
*  上拉：返回桌面
*  下拉：通知栏
*  右滑：多任务
*  左滑：锁屏
*  注，非root手机锁屏后再解锁会出现指纹不管用
*  下拉不动：移除悬浮球
*  上拉不动：
   *  非root手机：打开电源菜单
	 *  root手机：截屏并分享
*  长按：移动
*  主界面可调颜色和大小
*  关闭后再打开保持原来的状态
##  代码分析
###  文件结构
|-- AccessibilityUtils.java<br>
|-- AdminReceiver.java<br>
|-- ExecShell.java<br>
|-- FileIO.java<br>
|-- MainActivity.java<br>
|-- PolicyManagerUtils.java<br>
|-- Root.java<br>
|-- RootMethod.java<br>
|-- SuspendedBallManager.java<br>
|-- SuspendedBallService.java<br>
|-- SuspendedBallView.java<br>

###  [AccessibilityUtils.java](https://github.com/crazyStrome/SuspendedBall/blob/master/app/src/main/java/com/example/hsp/suspendedball/AccessibilityUtils.java)
检车辅助功能是否打开，检查电源选项，以及相关的悬浮球逻辑处理。

###  [AdminReceiver.java](https://github.com/crazyStrome/SuspendedBall/blob/master/app/src/main/java/com/example/hsp/suspendedball/AdminReceiver.java)
设备管理的接受类，若申请激活设备管理器，必须实现此继承类

###  [ExecShell.java](https://github.com/crazyStrome/SuspendedBall/blob/master/app/src/main/java/com/example/hsp/suspendedball/ExecShell.java)
检查设备是否已经获取root权限，如果获取root权限，则可以使用android的shell执行命令，比如截图
```java
public ArrayList<String> executeCommand(SHELL_CMD shellCmd)
```
