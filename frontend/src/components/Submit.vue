<script setup lang="ts">

import { ref } from 'vue';
import * as http from '../http-api'

let file = ref()

function handleFileUpload( event : any ) {
    file.value = event.target.files[0]
}


</script>

<template>
  <div class="fixedContainer" id="fileSubmit">
      <label>File
        <input type="file" id="file" ref="file" @change="handleFileUpload( $event )"/>
      </label>
      <button v-on:click="http.submitFile(file,file.name)">Submit</button>
  </div>
</template>