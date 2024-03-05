<template>
  <a-layout>
    <the-header></the-header>
    <router-view/>
    <a-layout-footer style="text-align: center">
      Ant Design ©2018 Created by Ant UED
    </a-layout-footer>
  </a-layout>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TheHeader from '@/components/the-header.vue'; // @ is an alias to /src

export default defineComponent({
  name: 'app',
  components:{
    TheHeader
  }
});
</script>