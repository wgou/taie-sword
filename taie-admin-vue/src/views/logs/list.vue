<template>
  <div class="mod-log__list">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="包名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.content" placeholder="日志内容" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <!-- <el-table-column prop="pkg" label="包名" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column> -->
      <el-table-column prop="content" label="日志内容" header-align="center" align="center" min-width="300px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="time" label="时间" header-align="center" align="center" width="180px" sortable="custom"></el-table-column>
      <el-table-column prop="source" label="日志产生源" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="level" label="日志类型" header-align="center" align="center" width="100px">
        <template v-slot="scope">
          <el-tag :type="getLevelType(scope.row.level)" size="small">{{ getLevelText(scope.row.level) }}</el-tag>
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
import { defineComponent, reactive, toRefs } from "vue";

export default defineComponent({
  props: {
    deviceId: {
      type: String,
      default: ""
    }
  },
  setup(props) {
    const state = reactive({
      post: true,
      getDataListURL: "/logInfo/page",
      getDataListIsPage: true,
      dataForm: {
        deviceId: props.deviceId || "",
        pkg: "",
        content: ""
      }
    });

    return {
      ...useView(state),
      ...toRefs(state)
    };
  },
  mounted() {
    if (this.deviceId) {
      this.dataForm.deviceId = this.deviceId;
      this.getDataList();
    }
  },
  methods: {
    getLevelText(level: number) {
      const levelMap: Record<number, string> = {
        0: "调试",
        1: "信息",
        2: "警告",
        3: "错误",
        4: "严重"
      };
      return levelMap[level] || "未知";
    },
    getLevelType(level: number): "success" | "info" | "warning" | "danger" | "" {
      const typeMap: Record<number, "success" | "info" | "warning" | "danger" | ""> = {
        0: "info",
        1: "success",
        2: "warning",
        3: "danger",
        4: "danger"
      };
      return typeMap[level] || "";
    }
  }
});
</script>

<style scoped>
.mod-log__list {
  padding: 20px;
}
</style>
