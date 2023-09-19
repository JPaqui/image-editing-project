<script setup lang="ts">
import * as img from '../imageHandler'

const imageList = img.getImageListRef()

function choseImage(image : any) {
    img.setChosenImage(image)
    img.showImage(document.getElementById("show"))
}

function loadImage(id : number | string) {
    img.setImageInGallery(id)
    return 'img' + id
}
</script>

<template>
<div id="gallery">
    <img v-for="image of imageList" :id="loadImage(image.id)" @click="choseImage(image)" class="imageInGallery"/>
</div>
</template>