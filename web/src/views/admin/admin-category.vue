<template>
  <a-layout-content style="padding: 0 50px">
    <a-layout style="padding: 24px 0; background: #fff;-ms-layout-flow: vertical-ideographic">
      <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
        <p>
          <a-form layout="inline" :model="param">
            <a-form-item>
              <a-button type="primary" @click="handleQuery()" >
                查询
              </a-button>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="add()" >
                新增
              </a-button>
            </a-form-item>
          </a-form>
        </p>
        <a-table
            :columns="columns"
            :row-key="record => record.id"
            :data-source="level1"
            :loading="loading"
            :pagination="false"
        >
          <template #cover="{ text: cover }" >
            <img v-if="cover" :src="cover" alt="avatar" style="width:40px;height:40px"/>
          </template>
          <template v-slot:action="{ text, record }">
            <a-space size="small">
              <a-button type="primary" @click="edit(record)">
                编辑
              </a-button>
              <a-popconfirm
                  title="删除后不可恢复，确认删除?"
                  ok-text="是"
                  cancel-text="否"
                  @confirm="handleDelete(record.id)"
              >
                <a-button type="primary" danger>
                  删除
                </a-button>
              </a-popconfirm>

            </a-space>
          </template>
        </a-table>
      </a-layout-content>
    </a-layout>
  </a-layout-content>
  <a-modal
      title="分类表单"
      v-model:open="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
  >
    <a-form :model="category" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="名称">
        <a-input v-model:value="category.name" />
      </a-form-item>
      <a-form-item label="父分类">
        <a-select
            ref="select"
            v-model:value="category.parent"
        >
          <a-select-option value="0">
            无
          </a-select-option>
          <a-select-option v-for="c in level1" :key="c.id" :value="c.id"
                           :disabled="category.id==c.id">
            {{c.name}}
          </a-select-option>
        </a-select>

      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="category.sort" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import {defineComponent, onMounted, reactive, ref, UnwrapRef} from 'vue';
import axios from 'axios';
import {message} from 'ant-design-vue';
import {Tool} from '@/util/tool';

export default defineComponent({
  name: 'AdminCategory',
  setup() {
    const param = ref();
    param.value = {};
    const categories = ref();
    const loading = ref(false);

    const columns = [
      {
        title: '名称',
        dataIndex: 'name'
      },
      {
        title: '父分类',
        dataIndex: 'parent'
      },
      {
        title: '顺序',
        dataIndex: 'sort'
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ];
    /**
     * 一级分类树，children属性就是二级分类
     * [{
     *    id:"",
     *    name:"",
     *    children:[{
     *      id:"",
     *      name:"",
     *    }]
     * }]
     */
    const level1 = ref(); //一级分类树，children属性就是二级分类

    /**
     * 数据查询
     **/
    const handleQuery = () => {
      loading.value = true;
      axios.get("/category/all").then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          categories.value = data.content;
          console.log("原始数组：",categories.value)

          level1.value = [];
          level1.value = Tool.array2Tree(categories.value,0);
          console.log("树形结构",level1.value);

        } else {
          message.error(data.message);
        }
      });
    };

    // --------------------表单------------------------
    const category = ref({});
    const modalVisible = ref<boolean>(false);
    const modalLoading = ref<boolean>(false);

    const handleModalOk = () => {
      modalLoading.value = true;
      axios.post("/category/save",category.value).then((response) => {
        modalLoading.value = false;
        const data = response.data; // data = commonResp
        if(data.success){
          modalVisible.value = false;

          // 重新加载列表
          handleQuery();
        }

      });

    };
    /**
     * 编辑
     */
    const edit = (record:any)=>{
      modalVisible.value = true;
      category.value = Tool.copy(record);
    };
    /**
     * 新增
     */
    const add =()=>{
      modalVisible.value = true;
      category.value = {};
    };

    const handleDelete = (id:number) => {
      axios.delete("/category/delete/" + id).then((response) => {
        const data = response.data; // data = commonResp
        if(data.success){
          // 重新加载列表
          handleQuery();
        }

      });
    };

    onMounted(() => {
      handleQuery();
    });

    return {
      param,
      // categories,
      level1,
      columns,
      loading,

      edit,
      add,
      handleDelete,
      handleQuery,

      category,
      modalVisible,
      modalLoading,
      handleModalOk
    }
  }
})
</script>
