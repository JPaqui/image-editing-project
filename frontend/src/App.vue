<script setup lang="ts">
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup
import Gallery from './components/Galerie.vue'
import Submit from './components/Submit.vue';
import Show from './components/Show.vue';
import * as img from './imageHandler'


img.refreshImageList()

</script>

<template>
  <Submit/>
  <Gallery/>
  <Show/>
</template>

<style>

:root {
  --bg-primary: #23232e;
  --bg-secondary: #141418;
}

body {
  margin: 0px;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;

  display: grid;
  grid-template-columns: repeat(12, 1fr);
  position: relative;

}

.tooltip {
  grid-column: 2 / 5;
  position: absolute;
  background-color: rgb(0, 0, 0);
  color: wheat;
  opacity: 0.25;
  margin: 0.5em;
  padding: 1em;

  transition: opacity 250ms ease;
}

.tooltip:hover {
  opacity: 1;
}

.tooltip > p {
  margin: 0;
}

.metaData {
  list-style: none;
  column-count: 2;
  margin-top: 0.5em;
  padding: 0;
}

.paramsContainer {
  display: flex;
  flex-direction: column;
  align-items: center;
}

#inputApply {
  margin-bottom: 0.5em;
}

#inputApply > button {
  margin: 0.1em;
}

.fixedContainer {
  position: fixed;
  background-color: rgba(0, 0, 0, 0.75);
  color: wheat;
  height: fit-content;
  opacity: 0.25;
  transition: opacity 250ms ease;
  padding: 1em;
  right: 0%;
  margin: 0.5em
}

.fixedContainer:hover {
  opacity: 1;
}

#divShow {
  top: 0%;
}

#divShow > *:not(:last-child) {
  margin-bottom: 5px;
}

.downloadContainer {
  position: fixed;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100vw;
  height: 100vh;
}

.downloadContainer > div {
  padding: 1em;
  background: black;
  color: wheat;
}

#fileSubmit {
  bottom: 0%;
}

.imageInGallery {
  width: 100%;
  height: auto;
}

.imageInGallery:hover {
    filter:brightness(1.4)
}

#gallery {
    grid-column: 1 / 2;
    height: 100vh;
    overflow-y: scroll;
    background-color: var(--bg-primary);

    display: flex;
    flex-direction: column;
}

#gallery::-webkit-scrollbar {
  width: 0.5vw;
  height: 0.5vw;
}

#gallery::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

#gallery::-webkit-scrollbar-thumb {
  background: #6649b8;
}

#imgDivShow {
  grid-column: 2 / 13;
  align-items: center;
  display: inline-block;
  vertical-align: middle;
  height: 100vh;
  background-image: linear-gradient(to right bottom, rgb(26, 26, 26), black);
}

#show {
  display: inline-block;
  vertical-align: middle;
  object-fit: contain;
  height: 100%;
  max-width: 100%;
}

#errorPrompt {
  color: red;
}

</style>
