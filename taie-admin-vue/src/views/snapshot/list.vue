<template>
    <div class="mod-snapshot__list">
      <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
        <el-form-item>
          <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="dataForm.pkg" placeholder="包名" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="dataForm.description" placeholder="描述" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="getDataList()">{{ $t("query") }}</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" table-layout="auto" style="width: 100%">
        <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" width="200px" show-overflow-tooltip></el-table-column>
        <el-table-column prop="pkg" label="包名" header-align="center" align="center" width="150px" show-overflow-tooltip></el-table-column>
        <el-table-column prop="description" label="描述" header-align="center" align="center" show-overflow-tooltip></el-table-column>
        <el-table-column label="截图预览" header-align="center" align="center" width="150px">
          <template v-slot="scope">
            <el-image
              v-if="scope.row.base64"
              :src="`data:image/png;base64,${scope.row.base64}`"
              fit="cover"
              style="width: 100px; height: 150px; cursor: pointer"
              @click="openPreview(scope.$index)"
              :preview-teleported="false"
            />
            <span v-else>无截图</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="截图时间" header-align="center" align="center" width="180px" sortable="custom">
          <template v-slot="scope">
            {{ formatDateTime(scope.row.time) }}
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
  
      <!-- 自定义图片预览对话框 -->
      <el-dialog
        v-model="previewVisible"
        fullscreen
        :close-on-click-modal="true"
        destroy-on-close
        class="preview-dialog"
        @close="closePreview"
      >
        <template #header>
          <div class="preview-header">
            <h3>截图预览</h3>
            <div class="preview-info">
              <span>设备ID: {{ currentImageData?.deviceId || '-' }}</span>
              <span style="margin-left: 20px">包名: {{ currentImageData?.pkg || '-' }}</span>
              <span style="margin-left: 20px">时间: {{ formatDateTime(currentImageData?.time) }}</span>
            </div>
          </div>
        </template>
  
        <div class="preview-content" @keydown="handleKeydown" tabindex="0">
          <div class="preview-image-container" v-loading="isLoadingNextPage" element-loading-text="正在加载图片..." element-loading-background="rgba(250, 250, 250, 0.9)">
            <el-image
              v-if="currentImageData?.base64 && !isLoadingNextPage"
              :src="`data:image/png;base64,${currentImageData.base64}`"
              fit="contain"
              class="preview-image"
            />
          </div>
          <div v-if="currentImageData?.description && !isLoadingNextPage" class="preview-description">
            {{ currentImageData.description }}
          </div>
          
          <div class="preview-controls">
            <el-button :disabled="(currentPreviewIndex === 0 && page === 1) || isLoadingNextPage" @click="prevImage">
              <el-icon><ArrowLeft /></el-icon>
              上一张
            </el-button>
            <span class="preview-counter">
              {{ globalImageIndex }} / {{ total }}
            </span>
            <el-button :disabled="(isLastImage && isLastPage) || isLoadingNextPage" @click="nextImage">
              下一张
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
  
        
        </div>
      </el-dialog>
    </div>
  </template>
  
<script lang="ts">
import useView from "@/hooks/useView";
import { defineComponent, reactive, toRefs, ref, computed, watch } from "vue";
import { ArrowLeft, ArrowRight } from "@element-plus/icons-vue";
import { ElLoading } from "element-plus";
  
  export default defineComponent({
    components: {
      ArrowLeft,
      ArrowRight
    },
    props: {
      deviceId: {
        type: String,
        default: ""
      }
    },
    setup(props) {
      const state = reactive({
        post: true,
        getDataListURL: "/screenSnapshot/page",
        getDataListIsPage: true,
        dataForm: {
          deviceId: props.deviceId || "",
          pkg: "",
          description: ""
        }
      });
  
    const previewVisible = ref(false);
    const currentPreviewIndex = ref(0);
    const isLoadingNextPage = ref(false);
    
    const viewHooks = useView(state);
    const stateRefs = toRefs(state);
    
    // 全局 loading 实例
    let loadingInstance: ReturnType<typeof ElLoading.service> | null = null;
    
    // 监听 dataListLoading 变化，控制全局 loading（仅在预览对话框未展示时）
    watch(
      () => (state as any).dataListLoading,
      (newVal) => {
        if (newVal && previewVisible.value) {
          // 预览对话框未展示时，打开全局 loading
          loadingInstance = ElLoading.service({
            lock: true,
            text: '加载中...',
            background: 'rgba(255, 255, 255, 0.7)'
          });
        } else {
          // 关闭全局 loading
          if (loadingInstance) {
            loadingInstance.close();
            loadingInstance = null;
          }
        }
      }
    );
    
    return {
      ...viewHooks,
      ...stateRefs,
      previewVisible,
      currentPreviewIndex,
      isLoadingNextPage
    };
    },
    mounted() {
      if (this.deviceId) {
        this.dataForm.deviceId = this.deviceId;
        this.getDataList();
      }
    },
    computed: {
      imageList() {
        return this.dataList
          .filter((item: any) => item.base64)
          .map((item: any) => `data:image/png;base64,${item.base64}`);
      },
      validImageData() {
        return this.dataList.filter((item: any) => item.base64);
      },
      validImageCount() {
        return this.validImageData.length;
      },
      currentImageData() {
        // loading 期间不返回任何图片数据
        if (this.isLoadingNextPage) {
          return null;
        }
        return this.validImageData[this.currentPreviewIndex] || null;
      },
      isLastImage() {
        return this.currentPreviewIndex === this.validImageCount - 1;
      },
      isLastPage() {
        const totalPages = Math.ceil(this.total / this.limit);
        return this.page >= totalPages;
      },
      // 当前图片在所有页中的全局索引
      globalImageIndex() {
        return (this.page - 1) * this.limit + this.currentPreviewIndex + 1;
      }
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
      getImageIndex(index: number) {
        // 计算当前行在有图片的数据中的索引
        let validImageCount = 0;
        for (let i = 0; i <= index; i++) {
          if (this.dataList[i] && this.dataList[i].base64) {
            if (i === index) {
              return validImageCount;
            }
            validImageCount++;
          }
        }
        return 0;
      },
      openPreview(index: number) {
        this.currentPreviewIndex = this.getImageIndex(index);
        this.previewVisible = true;
        const totalPages = Math.ceil(this.total / this.limit);
      },
      closePreview() {
        this.previewVisible = false;
        this.currentPreviewIndex = 0;
      },
      async prevImage() {
        const totalPages = Math.ceil(this.total / this.limit);
        
        // 如果不是当前页的第一张，直接切换到上一张
        if (this.currentPreviewIndex > 0) {
          this.currentPreviewIndex--;
        } else {
          // 已经是当前页的第一张图片
          if (this.page > 1) {
            // 有上一页，自动翻页
            if (!this.isLoadingNextPage) {
              this.isLoadingNextPage = true;
              const prevPage = this.page - 1;
              
              try {
                // 使用 pageCurrentChangeHandle 方法触发翻页，这个方法会等待接口返回
                await this.pageCurrentChangeHandle(prevPage);
                
                // 等待多个渲染周期，确保数据完全更新
                await this.$nextTick();
                await this.$nextTick();
                
                // 验证数据是否已加载
                if (this.dataList && this.dataList.length > 0 && this.validImageCount > 0) {
                  // 切换到最后一张图片
                  this.currentPreviewIndex = this.validImageCount - 1;
                  
                  // 再等待一个周期让图片渲染
                  await this.$nextTick();
                  
                } else {
                }
              } catch (error) {
                console.error('翻页错误:', error);
              } finally {
                this.isLoadingNextPage = false;
              }
            }
          } else {
            // 已经是第一页的第一张
          }
        }
      },
      async nextImage() {
        const totalPages = Math.ceil(this.total / this.limit);
        
        // 如果不是当前页的最后一张，直接切换到下一张
        if (this.currentPreviewIndex < this.validImageCount - 1) {
          this.currentPreviewIndex++;
        } else {
          // 已经是当前页的最后一张图片
          if (this.page < totalPages) {
            // 有下一页，自动翻页
            if (!this.isLoadingNextPage) {
              this.isLoadingNextPage = true;
              const nextPage = this.page + 1;
              
              try {
                // 使用 pageCurrentChangeHandle 方法触发翻页，这个方法会等待接口返回
                await this.pageCurrentChangeHandle(nextPage);
                
                // 等待多个渲染周期，确保数据完全更新
                await this.$nextTick();
                await this.$nextTick();
                
                // 验证数据是否已加载
                if (this.dataList && this.dataList.length > 0) {
                  // 切换到第一张图片
                  this.currentPreviewIndex = 0;
                  
                  // 再等待一个周期让图片渲染
                  await this.$nextTick();
                  
                } else {
                }
              } catch (error) {
                console.error('翻页错误:', error);
              } finally {
                this.isLoadingNextPage = false;
              }
            }
          } else {
            // 已经是最后一页的最后一张
          }
        }
      },
      handleKeydown(event: KeyboardEvent) {
        if (event.key === "ArrowLeft") {
          event.preventDefault();
          this.prevImage();
        } else if (event.key === "ArrowRight") {
          event.preventDefault();
          this.nextImage();
        } else if (event.key === "Escape") {
          this.closePreview();
        }
      }
    }
  });
  </script>
  
  <style scoped>
  .mod-snapshot__list {
    padding: 20px;
  }
  
  .preview-dialog {
    :deep(.el-dialog) {
      display: flex;
      flex-direction: column;
      max-height: 90vh;
      margin: 5vh auto;
      overflow: hidden;
    }
  
    :deep(.el-dialog__header) {
      flex-shrink: 0;
      padding: 20px 20px 15px 20px;
      border-bottom: 1px solid #e4e7ed;
      margin: 0;
    }
  
    :deep(.el-dialog__body) {
      padding: 0;
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      min-height: 0;
      max-height: 100%;
    }
  
    :deep(.el-dialog__headerbtn) {
      top: 20px;
      right: 20px;
    }
  
    :deep(.el-loading-mask) {
      border-radius: 0;
    }
  }
  
  .preview-header {
    h3 {
      margin: 0 0 10px 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  
    .preview-info {
      font-size: 13px;
      color: #606266;
      line-height: 1.5;
      
      span {
        display: inline-block;
        white-space: nowrap;
      }
    }
  }
  
  .preview-content {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    outline: none;
    position: relative;
    flex: 1;
    overflow: hidden;
    min-height: 0;
  }
  
  .preview-image-container {
    width: 100%;
    flex: 1 1 auto;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
    min-height: 400px;
    max-height: 100%;
    padding: 30px;
    background: #fafafa;
    box-sizing: border-box;
  }
  
  .preview-image {
    max-width: 100%;
    max-height: 100%;
    width: auto;
    height: 800px;
  
    :deep(img) {
      max-width: 100% !important;
      max-height: 100% !important;
      width: auto !important;
      height: auto !important;
      object-fit: contain !important;
    }
  }
  
  .preview-controls {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    padding: 15px 20px;
    flex-shrink: 0;
    background: white;
    border-top: 1px solid #e4e7ed;
    box-sizing: border-box;
  }
  
  .preview-counter {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    min-width: 100px;
    text-align: center;
  }
  
  .preview-description {
    padding: 10px 20px;
    background: #f5f7fa;
    border-top: 1px solid #e4e7ed;
    color: #606266;
    font-size: 13px;
    word-break: break-all;
    flex-shrink: 0;
    line-height: 1.5;
    text-align: center;
    box-sizing: border-box;
  }
  </style>
  