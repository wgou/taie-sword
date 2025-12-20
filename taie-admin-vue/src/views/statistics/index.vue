<template>
  <div class="mod-statistics__list">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-select v-model="dataForm.type" placeholder="统计类型" clearable style="width: 150px">
          <el-option label="页面访问" :value="1"></el-option>
          <el-option label="下载点击" :value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="x"
          @change="onTimeRangeChange"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="pageCode" label="页面标识" header-align="center" align="center" width="120px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="type" label="统计类型" header-align="center" align="center" width="120px">
        <template v-slot="scope">
          <el-tag :type="scope.row.type === 1 ? 'success' : 'primary'">
            {{ scope.row.type === 1 ? "页面访问" : "下载点击" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP地址" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="addr" label="所在城市" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="userAgent" label="User-Agent" header-align="center" align="center" min-width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="referer" label="来源URL" header-align="center" align="center" min-width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="created" label="创建时间" header-align="center" align="center" width="180px" sortable="custom">
        <template v-slot="scope">
          {{ formatDateTime(scope.row.created) }}
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
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import { defineComponent, reactive, toRefs, ref } from "vue";

export default defineComponent({
  setup() {
    const timeRange = ref<any>(null);

    const state = reactive({
      post: true,
      getDataListURL: "/statis/page",
      getDataListIsPage: true,
      dataForm: {
        type: null as number | null,
        start: null as string | null,
        end: null as string | null
      }
    });

    return {
      ...useView(state),
      ...toRefs(state),
      timeRange
    };
  },
  methods: {
    formatDateTime(date: Date | string | null) {
      if (!date) return "";

      const d = new Date(date);
      if (isNaN(d.getTime())) return "";

      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      const hours = String(d.getHours()).padStart(2, "0");
      const minutes = String(d.getMinutes()).padStart(2, "0");
      const seconds = String(d.getSeconds()).padStart(2, "0");

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
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
    onTimeRangeChange(value: any) {
      if (value && Array.isArray(value) && value.length === 2) {
        // 将时间戳转换为字符串格式 "yyyy-MM-dd HH:mm:ss"
        const startTime = Number(value[0]);
        const endTime = Number(value[1]);

        this.dataForm.start = this.formatTime(startTime);
        this.dataForm.end = this.formatTime(endTime);
      } else {
        this.dataForm.start = null;
        this.dataForm.end = null;
      }
    }
  }
});
</script>

<style scoped>
.mod-statistics__list {
  padding: 20px;
}
</style>
