<template>
  <a-layout-header class="header">
    <div class="logo"/>
    <a-menu
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
    >
      <a-menu-item key="/">
        <router-link to="/">首页</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/user" v-if="Boolean(user.id)">
        <router-link to="/admin/user">用户管理</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/ebook" v-if="Boolean(user.id)">
        <router-link to="/admin/ebook">电子书管理</router-link>
      </a-menu-item>
      <a-menu-item key="/admin/category" v-if="Boolean(user.id)">
        <router-link to="/admin/category">分类管理</router-link>
      </a-menu-item>
      <a-menu-item key="/about">
        <router-link to="/about">关于我们</router-link>
      </a-menu-item>
      <span v-if="Boolean(user.id)">
        <a class="login-menu" style="right: 120px">
          <span>您好：{{user.name}}</span>
        </a>
        <a-popconfirm
            title="确认退出登录?"
            ok-text="是"
            cancel-text="否"
            @confirm="logout">
          <a class="login-menu">
            <span>退出登录</span>
          </a>
        </a-popconfirm>
      </span>
      <a @click="showLoginModal" class="login-menu" v-show="!Boolean(user.id)">
        <span>登录</span>
      </a>
    </a-menu>

    <a-modal
        title="登录"
        v-model:open="loginModalVisible"
        :confirm-loading="loginModalLoading"
        @ok="login"
    >
      <a-form :model="loginUser" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="登录名">
          <a-input v-model:value="loginUser.loginName"/>
        </a-form-item>
        <a-form-item label="密码">
          <a-input v-model:value="loginUser.password" type="password"/>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout-header>
</template>

<script lang="ts">
import {defineComponent, ref, computed} from 'vue';
import axios from "axios";
import {message} from "ant-design-vue";
import store from "@/store";

declare let hexMd5: any
declare let KEY:any

export default defineComponent({
  name: 'the-header',
  setup() {
    // 用来登录
    const user = computed(()=>store.state.user)

    // 用来登录
    const loginUser = ref({
      loginName: "test123",
      password: "test1213"
    });

    const loginModalVisible = ref(false);
    const loginModalLoading = ref(false);
    const showLoginModal = () => {
      loginModalVisible.value = true;
    };

    // 登录
    const login = () => {
      console.log("开始登录");
      loginModalLoading.value = true;
      if(loginUser.value.password.length<6||loginUser.value.password.length>32){
        message.warn("密码长度需要在6-32之间")
        loginModalLoading.value = false;
        return;
      }
      loginUser.value.password = hexMd5(loginUser.value.password + KEY);
      axios.post('/user/login',loginUser.value).then((response)=>{
        loginModalLoading.value = false;
        const data = response.data;
        if(data.success){
          loginModalVisible.value = false;
          message.success("登录成功！");
          store.commit("setUser", data.content);
        }else{
          message.error(data.message);
        }
      });
    };
    // 退出登录
    const logout = () => {
      console.log("退出登录开始");
      axios.get('/user/logout/'+user.value.token).then((response)=>{
        const data = response.data;
        if(data.success){
          message.success("退出登录成功！");
          store.commit("setUser", {});
        }else{
          message.error(data.message);
        }
      });
    };

    return {
      loginModalVisible,
      loginModalLoading,
      showLoginModal,
      loginUser,
      login,
      logout,
      user,
    }

  }
});
</script>

<style scoped>
.logo {
  float: left;
  width: 120px;
  height: 31px;
  margin: 16px 24px 16px 0;
  background: rgba(255, 255, 255, 0.3);
}

.ant-row-rtl .logo {
  float: right;
  margin: 16px 0 16px 24px;
}

.site-layout-background {
  background: #fff;
}

.login-menu {
  color: white;
  position: absolute;
  right: 50px;
  top: 0px;
}

.login-menu:hover {
  text-decoration: underline; /* 鼠标悬停时添加下划线 */
  color: dodgerblue; /* 鼠标悬停时文字颜色变为蓝色 */
}
</style>
