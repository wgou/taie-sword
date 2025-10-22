<template>
  <div class="mod-js-code">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.name" placeholder="名称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.identification" placeholder="标识" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table 
      v-loading="dataListLoading" 
      :data="dataList" 
      border 
      @selection-change="dataListSelectionChangeHandle" 
      @sort-change="dataListSortChangeHandle" 
      style="width: 100%"
    >
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="name" label="名称" header-align="center" align="center" show-overflow-tooltip width="150"></el-table-column>
      <el-table-column prop="identification" label="标识" header-align="center" align="center" show-overflow-tooltip width="150"></el-table-column>
      <el-table-column prop="updateDate" label="修改时间" header-align="center" align="center" show-overflow-tooltip width="150"></el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center" show-overflow-tooltip width="400"></el-table-column>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button type="primary" link @click="deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
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
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update 
      v-model="addOrUpdateVisible" 
      v-if="addOrUpdateVisible" 
      ref="addOrUpdate" 
      @refreshDataList="getDataList"
    ></add-or-update>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import { defineComponent, reactive, toRefs } from "vue";
import AddOrUpdate from "./add-or-update.vue";

export default defineComponent({
  components: {
    AddOrUpdate
  },
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/jsCode/page",
      getDataListIsPage: true,
      deleteURL: "/jsCode/delete",
      deleteIsBatch: false,
      dataForm: {
        name: "",
        identification: ""
      }
    });
    return { ...useView(state), ...toRefs(state) };
  }
});
</script>

<style scoped>
.mod-js-code {
  padding: 20px;
}
</style>

