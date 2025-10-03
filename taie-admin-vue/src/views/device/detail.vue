<template>
  <el-dialog v-model="detailDialogVisible" width="1200px" top="2vh" @close="hide" :close-on-click-modal="false"
    class="device-detail-dialog" custom-class="device-detail-dialog">

    <template #header>
      <div class="dialog-header">
        <div class="dialog-title">设备ID: {{ deviceId }} -- 当前位置:{{ `${screenInfo.appName}` }}</div>
      </div>
    </template>


    <!-- 主内容区域：左侧手机操作界面 + 右侧日志终端 -->
    <div class="main-content-wrapper">
      <!-- 左侧：完整的手机操作界面 -->
      <div class="device-control-panel">
        <div class="screen-container">
          <!-- <div class="roll-modal" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`, transform: `scale(${ratio})`, 'transform-origin': 'left top' }">
        </div> -->
          <div class="screen-sizer" style="width: 400px; height: 650px">
            <div class="screen" ref="screenRef" :class="{ 'scroll-mode': rollVisible }" @click="handleGlobalClick"
              :style="{
                width: `${device.screenWidth}px`,
                height: `${device.screenHeight}px`,
                transform: `scale(${ratioHeight})`,
                'transform-origin': 'left top',
                'margin-top': '0px',
                left: `${Math.max(0, (400 - device.screenWidth * ratioHeight) / 2)}px`
              }">

              <!-- 屏幕边界框 - 始终显示黄色边框代表手机屏幕边界 -->
              <div class="screen-boundary" :class="{ 'block-mode': block }"
                :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px` }"></div>

              <!-- <div class="screen" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`}"> -->
              <!-- 先渲染普通元素 -->
              <template v-for="item in screenInfo.items" :key="item.uniqueId">
                <span :item-data="JSON.stringify(item)" v-show="(item.text && item.text.length > 0) || item.isClickable"
                  class="label rect" :class="{ 'ui-selected': item.isSelected }" @click="click(item)"
                  :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">{{
                    item.text }}</span>

                <span :class="{ 'ui-selected': item.isSelected }" v-if="item.isCheckable" class="checkable"
                  :style="{ top: `${item.y}px`, left: `${item.x}px` }">
                  {{ item.isChecked ? "✓" : "✕" }}
                </span>

                <span :class="{ 'ui-selected': item.isSelected }" @click.stop="input(item)"
                  v-else-if="item.isEditable && item.isFocusable" class="editable"
                  :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
                </span>
              </template>

              <!-- 最后渲染可滚动区域，确保在最上层 -->
              <template v-for="item in screenInfo.items" :key="`scrollable-${item.uniqueId}`">
                <span v-if="item.isScrollable" :class="{ 'ui-selected': item.isSelected }" class="scrollable"
                  :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
                  <el-button @click.stop="rollSwitch(item)" class="scroll-button" type="info"
                    size="small">滚动</el-button>
                </span>
              </template>

              <!-- 滚动遮罩层 - 放在最后确保在所有元素之上 -->
              <span v-show="rollVisible" class="roll-modal" ref="trackArea" @mousedown="startTracking"
                @mousemove="onMouseMove" @mouseup="stopTracking" @mouseleave="stopTracking" @click="handleGlobalClick"
                :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px` }">
                <!-- 显示鼠标拖动轨迹 -->
                <svg class="track-svg">
                  <polyline :points="trackPoints" fill="none" stroke="red" stroke-width="2" />
                </svg>
              </span>
            </div> <!-- close .screen -->
          </div> <!-- close .screen-sizer -->
        </div> <!-- close .screen-container -->

        <div class="operate-bottom">
          <div class="button-container">
            <el-tooltip class="box-item" effect="dark" content="正在运行的APP" placement="top">
              <el-button type="success" size="small" @click="recents">
                <el-icon>
                  <Menu />
                </el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip class="box-item" effect="dark" content="主页" placement="top">
              <el-button type="success" size="small" @click="home">
                <el-icon>
                  <House />
                </el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip class="box-item" effect="dark" content="回退" placement="top">
              <el-button type="success" size="small" @click="back">
                <el-icon>
                  <ArrowLeftBold />
                </el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div> <!-- Close device-control-panel -->

      <!-- 右侧：日志终端区域 -->
      <div class="terminal-container">
        <!-- 顶部操作条（在两列上方） -->
        <div class="top-operate">
          <el-row :gutter="10" justify="center">
            <el-col :span="4">
              <el-button :type="rollVisible ? 'danger' : 'success'" @click="toggleScrollMode" size="small">
                {{ rollVisible ? "退出滚动" : "进入滚动" }}
              </el-button>
            </el-col>
            <!-- <el-col :span="6">
          <el-button type="success" @click="rollSwitch" size="small">滑动模式</el-button>
        </el-col> -->
            <el-col :span="4">
              <el-button type="success" @click="wakeup" size="small">唤醒重连</el-button>
            </el-col>
            <el-col :span="4">
              <el-button type="success" @click="screenReq" size="small">刷新</el-button>
            </el-col>

            <el-col :span="4">
              <el-button :type="block ? 'danger' : 'success'" @click="screenOff" size="small">
                {{ block ? "退出息屏" : "息屏" }}
              </el-button>
            </el-col>
          </el-row>
        </div>
        <div class="terminal-header">
          <div class="terminal-signal">
            <div class="bars">
              <span v-for="n in 4" :key="n" class="bar" :class="[signalLevel, { active: n <= signalBars }]"></span>
            </div>
          </div>
          <div class="terminal-title">Device Log Terminal</div>
          <div class="terminal-actions">
            <el-button @click="clearLogs" size="small" type="text" class="clear-btn">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </div>
        <div class="terminal-body" ref="terminalBody">
          <div class="terminal-content" style="white-space: pre-wrap;">
            <!-- 调试信息 -->
            <div v-if="terminalLogs.length === 0" style="color: #ff0000; padding: 10px;">
              No logs yet. Total logs: {{ terminalLogs.length }}
            </div>
            <div class="log-entry" v-for="(log, index) in terminalLogs" :key="`${log.timestamp}-${index}`"
              :class="`type-${log.type}`">
              <span class="log-timestamp">{{ log.timestamp }}</span>
              <span class="log-level" :class="`level-${log.level}`">[{{ log.level.toUpperCase() }}]</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
            <div class="terminal-cursor">
              <span class="prompt">user@device:~$</span>
              <span class="cursor-blink">_</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>

  <el-dialog title="文本输入" v-model="inputDialogVisible" width="400px" class="input-dialog" custom-class="input-dialog"
    :close-on-click-modal="false" @close="closeInputDialog">
    <div class="input-dialog-content">
      <el-input clearable v-model="inputText" placeholder="请输入内容" class="custom-input">
      </el-input>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeInputDialog">取消</el-button>
        <el-button type="primary" @click="sendInput"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog :title="'滚动控制'" draggable width="280px" v-model="scrollDialogVisible" :close-on-click-modal="false"
    :modal="false" class="scroll-dialog" custom-class="scroll-dialog" top="30vh" :show-close="true"
    modal-class="scroll-dialog-modal">
    <div class="scroll-control-container">
      <div class="scroll-direction-pad">
        <!-- 上方向键 -->
        <div class="scroll-btn-wrapper up-btn">
          <el-button @click="trundle('up', 0)" class="scroll-direction-btn up" circle>
            <el-icon size="20">
              <ArrowUpBold />
            </el-icon>
          </el-button>
        </div>

        <!-- 左右方向键 -->
        <div class="scroll-horizontal-wrapper">
          <div class="scroll-btn-wrapper left-btn">
            <el-button @click="trundle('left', 3)" class="scroll-direction-btn left" circle>
              <el-icon size="20">
                <ArrowLeftBold />
              </el-icon>
            </el-button>
          </div>

          <div class="scroll-center-dot"></div>

          <div class="scroll-btn-wrapper right-btn">
            <el-button @click="trundle('right', 1)" class="scroll-direction-btn right" circle>
              <el-icon size="20">
                <ArrowRightBold />
              </el-icon>
            </el-button>
          </div>
        </div>

        <!-- 下方向键 -->
        <div class="scroll-btn-wrapper down-btn">
          <el-button @click="trundle('down', 2)" class="scroll-direction-btn down" circle>
            <el-icon size="20">
              <ArrowDownBold />
            </el-icon>
          </el-button>
        </div>
      </div>

      <div class="scroll-tips">
        <span>点击方向键进行滚动操作</span>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, nextTick, computed, onUnmounted } from "vue";
import { encodeWsMessage, decodeWsMessage, MessageType, App, encodeWsMessageNotBody } from "@/utils/message";
import { WebSocketClient, ROOM_EVENT_CLIENT_JOINED, ROOM_EVENT_CLIENT_LEFT, ROOM_EVENT_CLIENT_ERROR, ROOM_EVENT_ROOM_MEMBER_COUNT } from "@/utils/websocket-client";
import { ElNotification } from "element-plus";
import baseService from "@/service/baseService";
import { ScreenInfo } from "@/utils/message";

export default defineComponent({
  props: {},
  setup(propers, { emit }) {
    const detailDialogVisible = ref(false);
    const scrollDialogVisible = ref(false);
    const inputDialogVisible = ref(false);
    const block = ref(false);
    const installAppList = ref<App[]>([]);
    const rollVisible = ref(false);
    const closed = ref(true);
    const scrollSpeed = ref("正常");
    const inputText = ref("");
    const ratioWidth = ref(1);
    const ratioHeight = ref(1);
    const deviceId = ref("");
    const connectType = ref("转发");
    const scrollItem = ref({
      height: 0,
      width: 0,
      x: 0,
      y: 0,
      uniqueId: ""
    });
    const inputItem = ref({});
    const startApp = ref("");
    let historyInput = [];

    const isTracking = ref(false); // 记录是否在拖动
    const trackPoints = ref(""); // 记录拖动轨迹的坐标点

    let slidePoints = [];
    let delay = 0;

    // 开始记录轨迹
    const startTracking = () => {
      isTracking.value = true;
      trackPoints.value = ""; // 清空之前的轨迹
      slidePoints = [];
      delay = Date.now();
    };

    // 记录鼠标移动的轨迹
    const onMouseMove = (event) => {
      if (!isTracking.value) return;
      const { offsetX, offsetY } = event; // 获取鼠标的坐标
      // 添加当前坐标到轨迹点
      trackPoints.value += `${offsetX},${offsetY} `;
      slidePoints.push({
        x: offsetX,
        y: offsetY,
        delay: Date.now() - delay
      });
      delay = Date.now();
    };

    // 停止记录轨迹
    const stopTracking = async () => {
      isTracking.value = false;
      if (slidePoints.length > 0 && wsClient) {
        const slideMsg = encodeWsMessage(MessageType.slide_req, { deviceId: deviceId.value, points: slidePoints, segmentSize: 10 });
        wsClient.sendMessage(slideMsg);
      }
      // })
      // .finally(() => {
      trackPoints.value = ""; // 清空之前的轨迹
      slidePoints = [];

      // });
    };

    const device = ref({
      screenWidth: 600,
      screenHeight: 800
    });
    let wsClient: WebSocketClient | null = null;

    // 信号强度（基于最近一次收到的 screen_info 时间）
    const lastScreenInfoTime = ref(0);
    const signalBars = ref(0); // 0~4 根
    const signalLevel = ref<"full" | "poor" | "verypoor" | "none">("none");
    let signalTimer: any = null;
    const updateSignalLevel = () => {
      const now = Date.now();
      const diff = lastScreenInfoTime.value ? now - lastScreenInfoTime.value : Number.POSITIVE_INFINITY;
      if (diff <= 3000) {
        signalLevel.value = "full";
        signalBars.value = 4;
      } else if (diff <= 5000) {
        signalLevel.value = "poor";
        signalBars.value = 3;
      } else if (diff <= 8000) {
        signalLevel.value = "verypoor";
        signalBars.value = 2;
      } else {
        signalLevel.value = "none";
        signalBars.value = 0;
      }
    };

    const screenInfo = ref<ScreenInfo>({
      appName: "未知",
      packageName: "未知",
      appPkg: "未知",
      deviceId: "",
      items: [],
      block: false
    });

    const clearScreenInfo = () => {
      screenInfo.value = {
        appPkg: "未知",
        appName: "未知",
        packageName: "未知",
        deviceId: "",
        items: [],
        block: false
      };
    };
    // 终端日志系统
    const terminalBody = ref<HTMLElement>();
    const terminalLogs = ref<Array<{
      timestamp: string;
      level: "info" | "warn" | "error" | "success";
      message: string;
      type: string;
    }>>([]);

    // 添加日志条目
    const addLog = (level: "info" | "warn" | "error" | "success", message: string, type = "system") => {
      const now = new Date();
      const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}.${now.getMilliseconds().toString().padStart(3, '0')}`;

      terminalLogs.value.push({
        timestamp,
        level,
        message,
        type
      });

      // 限制日志条目数量，避免内存溢出
      if (terminalLogs.value.length > 1000) {
        terminalLogs.value.shift();
      }

      // 自动滚动到底部
      nextTick(() => {
        if (terminalBody.value) {
          terminalBody.value.scrollTop = terminalBody.value.scrollHeight;
        }
      });
    };

    // 清空日志
    const clearLogs = () => {
      terminalLogs.value = [];
      addLog("info", "Terminal cleared", "system");
    };

    const connect = async (_deviceId: string) => {
      try {
        console.log("正在创建WebSocket连接:", _deviceId);
        // 创建 WebSocket 客户端
        wsClient = new WebSocketClient({
          url: window.wsUrl,
          roomId: _deviceId, // 使用设备ID作为房间ID
          heartbeatInterval: 25000,
          reconnectInterval: 3000,
          maxReconnectAttempts: 5
        });

        // 设置消息处理器
        wsClient.setHandlers({
          onConnect: () => {
            console.log("WebSocket连接成功");
            addLog("success", `WebSocket connected to device ${_deviceId}`, "connection");
            // 连接成功后发送设备上线消息
            if (wsClient) {
              const monitorOnlineMsg = encodeWsMessage(MessageType.monitor_online, { deviceId: _deviceId });
              wsClient.sendMessage(monitorOnlineMsg);
              addLog("info", "Device monitor online message sent", "system");
            }
          },
          onMessage: (data: ArrayBuffer) => {
            const { type, body } = decodeWsMessage(new Uint8Array(data));
            switch (type) {
              case MessageType.screen_info:
                screenInfo.value = body as any;
                if (block.value != screenInfo.value.block) {
                  if (screenInfo.value.block) {
                    addLog("info", `进入息屏模式`, "screen");
                  } else {
                    addLog("info", `退出息屏模式`, "screen");
                  }
                }
                block.value = screenInfo.value.block;
                // addLog("info", `Screen info updated: ${(body as any).appName}`, "screen");
                lastScreenInfoTime.value = Date.now();
                updateSignalLevel();
                break;
              case MessageType.install_app_resp:
                installAppList.value = (body as any).apps;
                addLog("success", `Installed apps list received: ${(body as any).apps.length} apps`, "app");

                break;
              case MessageType.notify: {
                const notifyData = body as any;
                if (notifyData.title) {
                  addLog(notifyData.type || "info", `${notifyData.title}: ${notifyData.content}`, "notification");
                } else {
                  addLog(notifyData.type || "info", `${notifyData.content}`, "notification");
                }
                break;
              }
            }
          },
          onRoomNotification: (notification) => {
            switch (notification.eventType) {
              case ROOM_EVENT_CLIENT_JOINED:
                ElNotification({
                  title: "提示",
                  message: "新的连接加入",
                  type: "success"
                });

                break;
              case ROOM_EVENT_CLIENT_LEFT:
                addLog("info", `客户端 ${notification.value} 离开房间`, "system");
                break;
              case ROOM_EVENT_CLIENT_ERROR:
                addLog("error", `客户端 ${notification.value} 发生错误`, "system");
                break;
              case ROOM_EVENT_ROOM_MEMBER_COUNT:
                addLog("info", `房间成员数量: ${notification.value}`, "system");
                if (notification.value == "1") {
                  //TODO 提示设备已离线
                  clearScreenInfo();
                }
                break;
            }
          },
          onDisconnect: () => {
            if (!closed.value) {
              addLog("error", "链接已经关闭,请重新打开!", "system");
            }
          },
          onError: (error) => {
            clearScreenInfo();
            addLog("error", "WebSocket连接错误:" + error, "system");
          },
          onReconnecting: (attempt) => {
            addLog("warn", `正在进行第 ${attempt} 次重连...`, "system");
          },
          onReconnectFailed: () => {
            addLog("error", "重连失败,请刷新页面重试!", "system");
          }
        });

        // 连接到 WebSocket 服务器
        await wsClient.connect();
      } catch (error) {
        addLog("error", "WebSocket连接失败!" + error, "system");
      }
    };

    const show = (_device: any) => {

      // 清空并初始化日志
      terminalLogs.value = [];
      addLog("info", `=== Device Control Terminal Started ===`, "system");
      addLog("info", `Connecting to device: ${_device.deviceId}`, "system");
      addLog("info", `Screen resolution: ${_device.screenWidth}x${_device.screenHeight}`, "system");

      connect(_device.deviceId);
      // fetchInstallAppList(_device.deviceId);
      detailDialogVisible.value = true;
      deviceId.value = _device.deviceId;
      device.value = _device;
      screenInfo.value.items = [];
      // 仅按高度进行缩放：高度固定为 600px，宽度固定 450px
      const desiredHeight = 650; // 高度目标
      ratioHeight.value = desiredHeight / device.value.screenHeight;
      addLog("info", `Display scale ratioHeight: ${ratioHeight.value.toFixed(3)}`, "system");
      addLog("success", "Device control interface ready", "system");
      // 启动信号强度计时器
      if (signalTimer) clearInterval(signalTimer);
      updateSignalLevel();
      signalTimer = setInterval(updateSignalLevel, 1000);
    };
    const hide = () => {
      detailDialogVisible.value = false;
      if (wsClient) {
        closed.value = true;
        wsClient.disconnect();
        wsClient = null;
      }
      if (signalTimer) {
        clearInterval(signalTimer);
        signalTimer = null;
      }
    };

    onUnmounted(() => {
      if (signalTimer) {
        clearInterval(signalTimer);
        signalTimer = null;
      }
    });

    // 屏幕容器引用
    const screenRef = ref<HTMLElement>();

    // 全局点击处理器 - 发送真实鼠标点击位置
    const handleGlobalClick = (event: MouseEvent) => {
      if (!wsClient || !screenRef.value) return;

      // 如果正在滚动模式，不处理点击 TODO: 需要优化
      if (rollVisible.value) return;
      if (block.value) return;

      // 检查点击的目标元素，如果是特殊交互元素，则不处理
      const target = event.target as HTMLElement;
      if (target && (target.classList.contains("editable") || target.classList.contains("scroll-button") || target.closest(".editable") || target.closest(".scroll-button"))) {
        return;
      }

      // 阻止事件冒泡，避免触发其他点击事件
      event.preventDefault();
      event.stopPropagation();

      // 获取点击位置相对于屏幕容器的坐标
      const rect = screenRef.value.getBoundingClientRect();
      const clickX = event.clientX - rect.left;
      const clickY = event.clientY - rect.top;

      // 考虑缩放比例，计算真实设备坐标（使用当前视觉缩放：ratioHeight/ratioWidth）
      const realX = Math.round(clickX / ratioHeight.value);
      const realY = Math.round(clickY / ratioHeight.value);

      // 确保坐标在设备屏幕范围内
      const constrainedX = Math.max(0, Math.min(realX, device.value.screenWidth - 1));
      const constrainedY = Math.max(0, Math.min(realY, device.value.screenHeight - 1));

      // 发送点击消息
      const touchMsg = encodeWsMessage(MessageType.touch_req, {
        deviceId: deviceId.value,
        x: constrainedX,
        y: constrainedY,
        hold: false // 普通点击，不是长按
      });
      wsClient.sendMessage(touchMsg);
      addLog("info", `已发送指令: touch_req x: ${constrainedX} y: ${constrainedY} `, "input");
    };

    // 保留原有的点击方法作为备用（用于特殊情况）
    const click = (item: any) => {
      if (!block.value) {
        return;
      }
      if (wsClient) {
        const touchMsg = encodeWsMessage(MessageType.touch_req, { uniqueId: item.uniqueId, x: item.x + item.width / 2, y: item.y + item.height / 2, hold: false });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: touch_req `, "click");
      }
    };
    const back = () => {
      if (wsClient) {
        const backMsg = encodeWsMessage(MessageType.back_req, { deviceId: deviceId.value });
        wsClient.sendMessage(backMsg);
        addLog("info", `已发送指令: back_req `, "click");
      }
    };
    const recents = () => {
      if (wsClient) {
        const recentsMsg = encodeWsMessage(MessageType.recents_req, { deviceId: deviceId.value });
        wsClient.sendMessage(recentsMsg);
        addLog("info", `已发送指令: recents_req `, "click");
      }
    };
    const home = () => {
      if (wsClient) {
        const homeMsg = encodeWsMessage(MessageType.home_req, { deviceId: deviceId.value });
        wsClient.sendMessage(homeMsg);
      }
    };
    const input = async (item: any) => {
      inputDialogVisible.value = true;
      inputItem.value = item;
    };

    const toggleScrollMode = () => {
      if (block.value) {
        addLog("warn", `当前处于息屏模式，无法进入滚动模式`, "scroll");
        return;
      }
      if (!rollVisible.value) {
        // 进入滚动模式时，设置默认滚动区域
        scrollItem.value = {
          height: device.value.screenHeight,
          width: device.value.screenWidth,
          x: 0,
          y: 0,
          uniqueId: ""
        };
      }
      rollVisible.value = !rollVisible.value;
    };
    // scroll 方法已移除，功能合并到 rollSwitch 中
    const trundle = (direction: string, directionIndex: number) => {
      console.log(`trundle:`, direction, scrollItem.value);
      if (!scrollItem.value) {
        scrollItem.value = { height: 0, width: 0, x: 0, y: 0, uniqueId: "" };
      }

      let scrollObj: any = { uniqueId: scrollItem.value.uniqueId, duration: 600, direction: directionIndex };

      let distance: number;
      let _x: number;
      let _y: number;

      switch (direction) {
        case "up":
          //计算距离
          distance = scrollItem.value.height / 2;
          _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4 + distance;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4;
          break;
        case "down":
          //计算距离
          distance = scrollItem.value.height / 2;

          _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4 + distance;

          break;
        case "left":
          distance = scrollItem.value.width / 2;

          _y = scrollItem.value.y + scrollItem.value.height / 2;
          //计算坐标
          scrollObj.startX = scrollItem.value.x + distance / 4 + distance;
          scrollObj.startY = _y;
          scrollObj.endX = scrollItem.value.x + distance / 4;
          scrollObj.endY = _y;

          break;
        case "right":
          distance = scrollItem.value.width / 2;

          _y = scrollItem.value.y + scrollItem.value.height / 2;
          //计算坐标
          scrollObj.startX = scrollItem.value.x + distance / 4;
          scrollObj.startY = _y;
          scrollObj.endX = scrollItem.value.x + distance / 4 + distance;
          scrollObj.endY = _y;

          break;
      }

      if (wsClient) {
        const scrollMsg = encodeWsMessage(MessageType.scroll_req, scrollObj);
        wsClient.sendMessage(scrollMsg);
        addLog("info", `已发送指令: scroll_req`, "click");
      }
    };

    const rollSwitch = (item: any) => {
      scrollDialogVisible.value = true;
      scrollItem.value = item;
    };

    const closeInputDialog = () => {
      try {
        inputDialogVisible.value = false;
        inputText.value = "";
        inputItem.value = {};
        historyInput.length = 0; // 清空历史输入数组
      } catch (error) {
        addLog("error", "关闭输入弹窗时出错:" + error, "system");
        // 强制关闭
        inputDialogVisible.value = false;
      }
    };

    const sendInput = () => {
      if (wsClient) {

        const inputMsg = encodeWsMessage(MessageType.input_text, {
          text: `${inputText.value}`,
          deviceId: deviceId.value,
          id: (inputItem.value as any).id,
          uniqueId: (inputItem.value as any).uniqueId,
          appPkg: screenInfo.value.packageName,
          pkg: screenInfo.value.appPkg,
          isPassword: (inputItem.value as any).isPassword,
          enter:true
        });
        addLog("info", `输入文本: ${inputText.value}`, "input");
        wsClient.sendMessage(inputMsg);

      }
      closeInputDialog();
    };
    const screenReq = () => {
      if (wsClient) {
        const screenMsg = encodeWsMessage(MessageType.screen_req, { deviceId: deviceId.value });
        wsClient.sendMessage(screenMsg);
      }
      addLog("info", `已发送指令:screen_req`, "click");
    };
    const screenOff = () => {
      if (wsClient) {
        if(!block.value){
          addLog("warn", `进入息屏模式后,滚动无法使用,点击将直接作用于控件,因此如果点击不生效,请点击上一层控件`, "screen");
        }
        const screenOffMsg = encodeWsMessageNotBody(MessageType.screen_off);
        wsClient.sendMessage(screenOffMsg);
      }
      addLog("info", `已发送指令: screen_off`, "click");
    }

    const fetchInstallAppList = async (deviceId: any) => {
      let { code, data, msg } = await baseService.post("/installApp/list", { deviceId });
      if (code == 0) {
        installAppList.value = data;
      } else {
        addLog("error", msg, "system");
      }
    };
    const installAppReq = () => {
      if (wsClient) {
        const installAppMsg = encodeWsMessage(MessageType.install_app_req, { deviceId: deviceId.value });
        wsClient.sendMessage(installAppMsg);
      }
      addLog("info", `已发送指令: install_app_req `, "click");
    };

    const startAppReq = () => {
      if (wsClient) {
        const startAppMsg = encodeWsMessage(MessageType.start_app_req, { deviceId: deviceId.value, packageName: startApp.value });
        wsClient.sendMessage(startAppMsg);
      }
      addLog("info", `已发送指令: start_app_req `, "click");
    };
    const wakeup = () => {
      emit("wakeup", device.value);
    };

    // 切换到指定页面
    const switchToPage = (targetItem: any) => {
      if (wsClient) {
        // 直接点击目标滚动区域的中心位置
        const touchMsg = encodeWsMessage(MessageType.touch_req, {
          deviceId: deviceId.value,
          x: targetItem.x + targetItem.width / 2,
          y: targetItem.y + targetItem.height / 2,
          hold: false
        });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: switchToPage `, "click");
      }
    };
    return {
      wakeup,
      startAppReq,
      startApp,
      installAppList,
      installAppReq,
      fetchInstallAppList,
      screenReq,
      screenOff,
      inputItem,
      sendInput,
      closeInputDialog,
      inputText,
      inputDialogVisible,
      rollSwitch,
      toggleScrollMode,
      rollVisible,
      recents,
      home,
      detailDialogVisible,
      scrollDialogVisible,
      deviceId,
      show,
      hide,
      connect,
      screenInfo,
      click,
      device,
      back,
      ratioWidth,
      ratioHeight,
      input,
      scrollItem,
      screenRef,
      handleGlobalClick,
      trundle,
      scrollSpeed,
      startTracking,
      onMouseMove,
      stopTracking,
      trackPoints,
      connectType,
      switchToPage,
      // 终端日志相关
      terminalBody,
      terminalLogs,
      addLog,
      clearLogs,
      // 信号指示器
      signalBars,
      signalLevel,
      block
    };
  }
});
</script>

<style>
/* 旧样式已移除，使用下方的新样式 */

.screen>span {
  position: absolute;
  /* 对 screen 下的所有 span 元素应用绝对定位 */
  cursor: default;
  z-index: 100;
  /* 默认图标层级最高，确保可点击 */
}

.focused {
  border: 3px solid red;
}

.editable {
  border: 2px solid #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
  transition: all 0.2s ease;
}

.editable:hover {
  border-color: #529b2e;
  background-color: rgba(103, 194, 58, 0.2);
  transform: scale(1.02);
}

.ui-selected {
  border: 3px solid #722ed1 !important;
  background-color: rgba(114, 46, 209, 0.15) !important;
  box-shadow: 0 0 8px rgba(114, 46, 209, 0.3) !important;
}

/* 页面指示器样式 */
.page-indicators {
  z-index: 10;
}

.page-dot {
  transition: all 0.3s ease !important;
}

.page-dot:hover {
  transform: scale(1.2);
  box-shadow: 0 0 4px rgba(250, 173, 20, 0.8);
}

.page-dot.active {
  background-color: #faad14 !important;
  box-shadow: 0 0 6px rgba(250, 173, 20, 0.6);
}



.checkable {
  border: 3px solid blue;
  padding: 2px;
  font-size: 40px;
  font-weight: 900;
}

.rect {
  border: 2px solid #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s ease;
}

.rect:hover {
  border-color: #337ecc;
  background-color: rgba(64, 158, 255, 0.2);
  transform: scale(1.02);
}

.label {
  /* color: gray; */
  font-size: 30px;
  font-weight: 600;
  text-align: center;
  user-select: none;
  /* 标准属性 */
  -webkit-user-select: none;
  /* 兼容 WebKit 内核浏览器（如 Chrome、Safari） */
  -moz-user-select: none;
  /* 兼容 Firefox */
  -ms-user-select: none;
  /* 兼容旧版 IE */
}

.scrollable {
  z-index: 300 !important;
  /* 滚动按钮始终在最外层 */
  pointer-events: none !important;
  /* 区域本身不拦截鼠标事件，让底层图标可点击 */
  display: flex;
  justify-content: flex-start;
  /* 左对齐 */
  align-items: flex-start;
  /* 顶部对齐 */
  border: 2px solid #faad14;
  background-color: rgba(250, 173, 20, 0.1);
  transition: z-index 0.3s ease;
  padding: 2px;
  /* 给按钮一些内边距 */
}

.scroll-button {
  z-index: 301 !important;
  /* 比scrollable区域更高 */
  font-size: 25px !important;
  padding: 2px 6px !important;
  min-height: auto !important;
  height: auto !important;
  pointer-events: auto !important;
  /* 按钮本身可点击 */
  position: relative;
  /* 确保层级生效 */
}

.major {
  margin: -3px;
  border-radius: 0;
  transform: scale(1.4);
  transform-origin: left top;
  font-weight: 900;
  z-index: 1001 !important;
  cursor: pointer;
  position: absolute;
}

.mb-4 {
  margin-bottom: 4px;
}

.scroll-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.scroll-speed {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.horizontal-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.el-button {
  width: 80px;
  /* 或者您想要的任何固定宽度 */
}

.operate {
  position: absolute;
  top: 0px;
  margin-left: 5px;
}

.operate1 {
  display: flex;
  justify-content: center;
  align-items: center;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 20px;
  /* 调整按钮之间的间距 */
}

/* 旧的底部样式已移除，使用下方的新样式 */

.roll-modal {
  position: absolute;
  top: 0;
  left: 0;
  background-color: black;
  opacity: 0.6;
  z-index: 1000 !important;
  /* 滚动模式时最高层级 */
  text-align: center;
  display: block;
  pointer-events: auto;
}

.track-svg polyline {
  stroke-width: 5;
}

.track-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  /* 防止鼠标事件影响SVG */
}


.el-row {
  margin-bottom: 20px;
}

/* 主设备详情弹窗样式 */
:deep(.device-detail-dialog) {
  .el-dialog {
    border-radius: 12px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    max-width: 1500px;
    max-height: none;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: 3px solid #3b82f6 !important;
    margin: 0;
    text-align: center;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 64px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 50%;
    transform: translateX(-50%);
    width: 64px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: white;
    width: 100%;
    display: block;
    text-align: center;
    margin: 0 auto;
  }

  .el-dialog__headerbtn {
    top: 20px;
    right: 20px;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 20px;
  }

  .el-dialog__body {
    padding: 16px 16px 16px 16px;
    min-height: 800px;
    background: #f8fafc;
    overflow-x: hidden;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    position: relative;
  }

  .el-dialog__body::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(59, 130, 246, 0.35) 50%, transparent 100%);
  }
}

/* 顶部操作按钮区域 */
.top-operate {
  background: #000000;
  padding: 8px;
  /* 减少内边距 */
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #00ff00;
  width: 100%;
  /* 确保宽度 */
  position: sticky;
  top: 0;
  z-index: 5;
}

.top-operate .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  width: 100%;
}

.top-operate .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 输入弹窗样式 */
:deep(.input-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: none;
    margin: 0;
  }

  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: white;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 18px;
  }

  .el-dialog__body {
    padding: 24px;
    min-height: 800px;
    background: white;
    overflow-y: auto;
  }

  .el-dialog__footer {
    background: #f8fafc;
    padding: 16px 24px;
    border-top: 1px solid #e2e8f0;
  }
}

.input-dialog-content {
  margin-bottom: 16px;
}

.input-dialog-content .el-input {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 500;
}

/* 滚动控制弹窗样式 */
:deep(.scroll-dialog) {
  .el-dialog {
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
    overflow: hidden;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #1e293b 0%, #334155 50%, #475569 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: 3px solid #3b82f6;
    margin: 0;
    text-align: center;
    position: relative;
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 700;
    color: white;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.5px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .el-dialog__title::before {
    content: '🎮';
    font-size: 20px;
  }

  .el-dialog__headerbtn {
    top: 18px;
    right: 20px;
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .el-dialog__headerbtn:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.1);
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 16px;
    font-weight: bold;
    transition: all 0.3s ease;
  }

  .el-dialog__headerbtn:hover .el-dialog__close {
    color: #fbbf24;
  }

  .el-dialog__body {
    padding: 32px 24px 24px 24px;
    min-height: 800px;
    background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
    text-align: center;
    position: relative;
    border-top: 1px solid rgba(255, 255, 255, 0.5);
    overflow-y: auto;
  }

  .el-dialog__body::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(59, 130, 246, 0.3) 50%, transparent 100%);
  }
}

/* 新的滚动控制容器样式 */
.scroll-control-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.scroll-direction-pad {
  display: grid;
  grid-template-areas:
    ". up ."
    "left center right"
    ". down .";
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.scroll-btn-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.scroll-btn-wrapper.up-btn {
  grid-area: up;
}

.scroll-btn-wrapper.left-btn {
  grid-area: left;
}

.scroll-btn-wrapper.right-btn {
  grid-area: right;
}

.scroll-btn-wrapper.down-btn {
  grid-area: down;
}

.scroll-horizontal-wrapper {
  grid-area: left / left / right / right;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.scroll-center-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  opacity: 0.6;
}

.scroll-direction-btn {
  width: 48px !important;
  height: 48px !important;
  border: none !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
}

.scroll-direction-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.scroll-direction-btn:hover {
  transform: translateY(-2px) scale(1.05) !important;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4) !important;
}

.scroll-direction-btn:hover::before {
  opacity: 1;
}

.scroll-direction-btn:active {
  transform: translateY(0) scale(0.98) !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3) !important;
}

.scroll-direction-btn.up {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%) !important;
}

.scroll-direction-btn.down {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%) !important;
}

.scroll-direction-btn.left {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%) !important;
}

.scroll-direction-btn.right {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%) !important;
}

.scroll-tips {
  margin-top: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.scroll-tips span {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* 原操作区域样式已移除 - 操作按钮已移至顶部 */

/* 底部操作按钮美化 */
.operate-bottom {
  margin-top: 12px;
  background: transparent;
  border-top: 1px dashed #e5e7eb;
  border-radius: 0;
  padding: 12px 0;
  /* 与容器留白一致 */
  box-shadow: none;
  width: 100%;
  align-self: center;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.button-container .el-button {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
  transition: all 0.3s ease;
}

.button-container .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

/* 输入框组件美化 */
.custom-input {
  width: 100%;
}

.custom-input .el-input__inner {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.custom-input .el-input__inner:focus {
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
}


/* 主内容布局 */
.main-content-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: nowrap;
  width: 100%;
}

.dialog-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 3px solid #171718 !important;
}

.dialog-title {
  width: 100%;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #040404;
}

/* 左侧设备控制面板 */
.device-control-panel {
  width: 550px;
  flex: 0 0 auto;
  /* 宽度由内层内容决定 */
  display: flex;
  flex-direction: column;
  height: 750px;
  /* 固定高度，与终端保持一致 */
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  padding: 12px;
}

/* 屏幕容器美化 */
.screen-container {
  /* overflow-y: auto; */
  overflow-y: hidden;
  overflow-x: hidden;
  position: relative;
  background: transparent;
  padding: 0px !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 让子元素水平居中 */
  justify-content: flex-start;
  flex: 0 0 auto;
  /* 由内容宽度决定 */
  margin: 0 !important;
  min-height: auto;
}

.screen-container>.screen-sizer {
  margin: 0 auto !important;
  /* 居中 */
  padding: 0 !important;
  position: relative !important;
  max-width: 100%;
  box-sizing: border-box;
}

.screen {
  border: 2px solid #e2e8f0;
  position: absolute;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  background: #ffffff;
  margin: 0 !important;
  padding: 0 !important;
  top: 0;
}

.screen-sizer {
  /* 仅用于占位限定容器宽度 */
  position: relative;
}

/* 屏幕边界框 - 代表手机屏幕的可滚动区域 */
.screen-boundary {
  position: absolute;
  top: 0;
  left: 0;
  border: 3px solid #faad14;
  background-color: rgba(250, 173, 20, 0.05);
  pointer-events: none;
  /* 不阻止鼠标事件 */
  z-index: 1;
  /* 确保在其他元素之下 */
  box-sizing: border-box;
}

/* 滚动模式下的层级调整 */
.screen.scroll-mode>span:not(.scrollable):not(.roll-modal) {
  z-index: 10 !important;
  /* 滚动模式下图标层级降低 */
  pointer-events: none !important;
  /* 滚动模式下图标不可点击 */
}

.screen.scroll-mode .scrollable {
  z-index: 500 !important;
  /* 滚动模式下scrollable层级提升 */
  pointer-events: none !important;
  /* 区域本身不拦截事件，让底层图标可点击 */
}

.screen-boundary.block-mode {
  border: 10px solid #00ff00;
}

.screen.scroll-mode .scroll-button {
  z-index: 501 !important;
  /* 滚动按钮始终最高层级 */
  pointer-events: auto !important;
  /* 滚动按钮始终可点击 */
}

/* 终端日志样式 */
.terminal-container {
  min-width: 520px;
  /* 稍微加宽默认最小宽度 */
  height: 750px;
  /* 与左侧面板等高 */
  background: #000000;
  border-radius: 8px;
  border: 2px solid #00ff00;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.3);
  display: flex;
  flex-direction: column;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  overflow: hidden;
  align-self: flex-start;
  /* 确保与左侧对齐 */
}

.terminal-header {
  background: #000000;
  padding: 8px 12px;
  border-bottom: 1px solid #00ff00;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
}

/* 信号强度显示 */
.terminal-signal {
  display: flex;
  align-items: center;
  gap: 8px;
}

.terminal-signal .bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
}

.terminal-signal .bar {
  width: 6px;
  height: 6px;
  background: #333;
  opacity: 0.4;
  transition: all 0.2s ease;
}

.terminal-signal .bar.active.full {
  background: #22c55e;
  /* 绿 */
  opacity: 1;
}

.terminal-signal .bar.active.poor {
  background: #eab308;
  /* 黄 */
  opacity: 1;
}

.terminal-signal .bar.active.verypoor {
  background: #f97316;
  /* 橙 */
  opacity: 1;
}

.terminal-signal .bar.active.none {
  background: #ef4444;
  /* 红 */
  opacity: 1;
}

/* 不同高度模拟信号格子 */
.terminal-signal .bar:nth-child(1) {
  height: 6px;
}

.terminal-signal .bar:nth-child(2) {
  height: 10px;
}

.terminal-signal .bar:nth-child(3) {
  height: 14px;
}

.terminal-signal .bar:nth-child(4) {
  height: 18px;
}

.terminal-signal .signal-label {
  color: #9ca3af;
  font-size: 12px;
}

.terminal-title {
  color: #00ff00;
  font-size: 12px;
  font-weight: 500;
  flex: 1;
  text-align: center;
}

.terminal-actions {
  display: flex;
  align-items: center;
}

.clear-btn {
  color: #888 !important;
  padding: 4px !important;
}

.clear-btn:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.terminal-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #000000;
  color: #00ff00;
  font-size: 12px;
  line-height: 1.4;
}

.terminal-content {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.log-entry {
  display: flex;
  margin-bottom: 2px;
  word-wrap: break-word;
  align-items: baseline;
}

.log-timestamp {
  color: #7c3aed;
  margin-right: 8px;
  font-size: 11px;
  flex-shrink: 0;
  min-width: 80px;
}

.log-level {
  margin-right: 8px;
  font-weight: bold;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  flex-shrink: 0;
}

.level-info {
  color: #58a6ff;
  background: rgba(88, 166, 255, 0.1);
}

.level-warn {
  color: #f1e05a;
  background: rgba(241, 224, 90, 0.1);
}

.level-warning {
  color: #f1e05a;
  background: rgba(241, 224, 90, 0.1);
}

.level-error {
  color: #f85149;
  background: rgba(248, 81, 73, 0.1);
}

.level-success {
  color: #3fb950;
  background: rgba(63, 185, 80, 0.1);
}

.log-message {
  flex: 1;
  word-break: break-all;
}

.log-entry.notification .log-message {
  color: #ffa657;
}

.log-entry.connection .log-message {
  color: #3fb950;
}

.log-entry.screen .log-message {
  color: #58a6ff;
}

.log-entry.app .log-message {
  color: #bc8cff;
}

.log-entry.system .log-message {
  color: #7c3aed;
}

.terminal-cursor {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  color: #3fb950;
}

.prompt {
  margin-right: 4px;
  color: #3fb950;
  font-weight: bold;
}

.cursor-blink {
  color: #fff;
  animation: blink 1s infinite;
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

/* 终端滚动条样式 */
.terminal-body::-webkit-scrollbar {
  width: 6px;
}

.terminal-body::-webkit-scrollbar-track {
  background: #1a1a1a;
}

.terminal-body::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}

.terminal-body::-webkit-scrollbar-thumb:hover {
  background: #666;
}

/* 响应式设计 */
@media (max-width: 1600px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 1200px !important;
    }
  }
}

@media (max-width: 1024px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 800px !important;
      max-width: 90vw !important;
    }
  }

  .main-content-wrapper {
    flex-direction: column;
    gap: 15px;
  }

  .device-control-panel {
    width: 100%;
    height: auto;
  }

  .terminal-container {
    height: 400px;
    min-width: 100%;
  }

  .screen-container {
    max-height: 60vh;
    padding: 12px;
  }
}

@media (max-width: 768px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 95vw !important;
      margin: 0 !important;
      top: 2vh !important;
    }
  }

  .screen-container {
    max-height: 55vh;
    padding: 8px;
  }

  .top-operate {
    margin: 10px 0 !important;
    padding: 12px !important;
  }

  .operate-bottom {
    width: 100% !important;
    max-width: none !important;
  }
}

/* 强制对话框主体高度设置生效（覆盖 Element 默认限制） */
.device-detail-dialog {
  max-height: none !important;
}

.device-detail-dialog .el-dialog__body {
  min-height: 800px !important;
  overflow-y: auto;
}

.scroll-dialog-modal {
  pointer-events: none;
}

.el-dialog {
  pointer-events: auto;

}
</style>
