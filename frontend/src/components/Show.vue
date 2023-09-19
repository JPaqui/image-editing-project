<script setup lang="ts">

import { ref, onUpdated } from 'vue'
import * as http from '../http-api'
import * as img from '../imageHandler'

interface Algorithm {
    value : string
    name : string
}

const algoList : Algorithm[] = [
    {value : "addLuminosityRGB", name : 'Increase brightness'},
    {value : "equalize", name : "Equalize"},
    {value : "hueFilter", name : "Change hue"},
    {value : "gradientImageSobel", name : "Show outline"},
    {value : "blur", name : "Blur"},
    {value : "rainbow", name : "Rainbow filter"},
    {value : "hueSelector", name : "Hue selector"},
    {value : "scale", name : "Change size"},
    {value : "reverseHue", name : "Reverse hue"},
    {value : "negative", name : "Negative"},
    {value : "flip", name : "Flip"},
    {value : "rotate", name : "Rotate"},
    {value : "wave", name : "Wave"},
    {value : "sphere", name : "Sphere"},
    {value : "sepia", name : "Sepia"},
    {value : "mozaic", name : "Mozaic"},
    {value : "twist", name : "Twist"},
    {value : "halftoning", name : "Halftoning"}]


const chosenImage = img.getChosenImage()

const imageSize = img.getImageSizeRef()

const showTooltip = ref()
showTooltip.value = false

const algoValue = ref()
algoValue.value = "addLuminosityRGB"

const showDownload = ref()
showDownload.value = false

onUpdated(() => {
    document.querySelectorAll(".sliderInput").forEach(sliderInput => {
        sliderInput.querySelector("input[type=range]")?.addEventListener("input", function() {
            updateTextInput(<HTMLElement> sliderInput)
        })
        sliderInput.querySelector("input[type=number]")?.addEventListener("input", function() {
            updateRangeInput(<HTMLElement> sliderInput)
        })
    })
})

function showImage(params : string[][]) {
    img.showAlgo(document.querySelector("#show"), params, (<HTMLInputElement> document.querySelector("#chainAlgo")).checked)
}

function showPrev() {
    img.retrievePrevImage(document.getElementById('show')) 
}

function showNext() {
    img.retrieveNextImage(document.getElementById('show'))
}

function getAlgo() {
    algoValue.value = (<HTMLInputElement> document.getElementById("algoSelector"))?.value
}

// Download the image stored in modifiedImage to the client computer

function changeDownload(value : boolean) {
    showDownload.value = value
}

function downloadImage() {
    const url = window.URL.createObjectURL(img.getModifiedImageRef().value)
    const link = document.createElement("a")
    link.href = url

    let fileName = (<HTMLInputElement> document.querySelector("#downloadNameInput")).value
    if (fileName === "") fileName = chosenImage.value.name

    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    showDownload.value = false
}

function getParams(algorithm : string) : string[][] {
    let ret = [['algorithm', algorithm]]
    switch (algorithm) {
        case 'addLuminosityRGB':
            ret.push(['gain', (<HTMLInputElement> document.getElementById('gain')).value])
            break
        case 'equalize':
            ret.push(['canal', (<HTMLInputElement> document.getElementById('canal')).value])
            break
        case 'hueFilter':
            ret.push(['hue', (<HTMLInputElement> document.getElementById('hue')).value])
            break
        case 'gradientImageSobel':
            break
        case 'blur':
            ret.push(['type', (<HTMLInputElement> document.getElementById('type')).value])
            ret.push(['size', (<HTMLInputElement> document.getElementById('size')).value])
            break
        case 'rainbow':
            ret.push(['direction', (<HTMLInputElement> document.getElementById('direction')).value])
            break
        case 'hueSelector':
            ret.push(['min', (<HTMLInputElement> document.getElementById('min')).value])
            ret.push(['max', (<HTMLInputElement> document.getElementById('max')).value])
            break
        case 'scale':
            ret.push(['width', (<HTMLInputElement> document.getElementById('width')).value])
            ret.push(['height', (<HTMLInputElement> document.getElementById('height')).value])
            break
        case 'reverseHue':
            break
        case 'negative':
            break
        case 'flip':
            ret.push(['axis', (<HTMLInputElement> document.getElementById('axis')).value])
            break
        case 'rotate':
            ret.push(['angle', (<HTMLInputElement> document.getElementById('angle')).value])
            break
        case 'wave':
            ret.push(['waveAxis', (<HTMLInputElement> document.getElementById('waveAxis')).value])
            ret.push(['waveOffset', (<HTMLInputElement> document.getElementById('waveOffset')).value])
            ret.push(['waveType', (<HTMLInputElement> document.getElementById('waveType')).value])
            ret.push(['amplitude', (<HTMLInputElement> document.getElementById('amplitude')).value])
            ret.push(['waveLength', (<HTMLInputElement> document.getElementById('waveLenght')).value])
            break
        case 'sphere':
            ret.push(['sphere_type', (<HTMLInputElement> document.getElementById('sphere_type')).value])
            break
        case 'sepia':
            break
        case 'mozaic':
            break
        case 'twist':
            ret.push(['maxAngle', (<HTMLInputElement> document.getElementById('maxAngle')).value])
            break
        case 'halftoning':
            ret.push(['spread', (<HTMLInputElement> document.getElementById('spread')).value])
            ret.push(['dotSize', (<HTMLInputElement> document.getElementById('dotSize')).value])
            break
    }
    return ret
}

function deleteImage() {
    img.deleteImage(chosenImage.value.id, document.getElementById("show"))
    
}

function updateRangeInput(sliderInput : HTMLElement | null) {
    (<HTMLInputElement> sliderInput?.querySelector("input[type=range]")).value = (<HTMLInputElement> sliderInput?.querySelector("input[type=number]")).value
}

function updateTextInput(sliderInput : HTMLElement | null) {
    (<HTMLInputElement> sliderInput?.querySelector("input[type=number]")).value = (<HTMLInputElement> sliderInput?.querySelector("input[type=range]")).value
}

function updateShowTooltip() {
    showTooltip.value = (<HTMLInputElement> document.querySelector("#showTooltip")).checked
}

</script>

<template>
<div v-if="chosenImage != undefined" id="divShow" class="fixedContainer">
    <ul class="metaData">
        <li>{{ `Image : ${chosenImage.name}` }}</li>
        <li>{{ `Id : ${chosenImage.id}` }}</li>
        <li>{{ `Size : ${imageSize}` }}</li>
        <li>{{ `Type : ${chosenImage.type}` }}</li>
        <li>{{ `Weight : ${(img.getModifiedImageRef().value.size/1000).toFixed(2)} Kb` }}</li>
    </ul>
    <div>
        <select id="algoSelector" style="margin-right: 1em;" @click="getAlgo()">
            <option v-for="algo in algoList" :value="algo.value"> {{ algo.name }} </option>
        </select>
        <input type="checkbox" id="showTooltip" @input="updateShowTooltip()">
        <label for="showTooltip"> Show Tooltip </label>
    </div>
    <div class="paramsContainer">
        <template v-if="algoValue === 'addLuminosityRGB'">
            <div class="sliderInput">
                Gain : 
                <input type="range" min="-255" max="255" value="0">
                <input type="number" min="-255" max="255" value="0" id="gain">
            </div>
        </template>

        <template v-else-if="algoValue === 'equalize'">
            <select id="canal">
                <option value="V"> V </option>
                <option value="S"> S </option>
            </select>
        </template>

        <template v-else-if="algoValue === 'hueFilter'">
            <div class="sliderInput">
                Teinte : 
                <input type="range" min="0" max="360" value="0">
                <input type="number" min="0" max="360" value="0" id="hue">
            </div>
        </template>

        <template v-else-if="algoValue === 'gradientImageSobel'">
        </template>

        <template v-else-if="algoValue === 'blur'">
            <select id="type">
                <option value="G"> Gaussian</option>
                <option value="M"> Mean </option>
            </select>
            <input type="number" id="size" placeholder="Radius">
        </template>

        <template v-else-if="algoValue === 'rainbow'">
            <select id="direction">
                <option value="V"> Vertical </option>
                <option value="H"> Horizontal </option>
                <option value="C"> Centered </option>
            </select>
        </template>

        <template v-else-if="algoValue === 'hueSelector'">
            <div class="sliderInput">
                Minimum hue : 
                <input type="range" min="0" max="360" value="0">
                <input type="number" min="0" max="360" value="0" id="min">
            </div>
            <div class="sliderInput">
                Maximum hue : 
                <input type="range" min="0" max="360" value="0">
                <input type="number" min="0" max="360" value="0" id="max">
            </div>
        </template>

        <template v-else-if="algoValue === 'scale'">
            <input type="number" id="width" placeholder="Width">
            <input type="number" id="height" placeholder="Height">
        </template>

        <template v-else-if="algoValue === 'reverseHue'">
        </template>

        <template v-else-if="algoValue === 'negative'">
        </template>

        <template v-else-if="algoValue === 'flip'">
            <select id="axis">
                <option value="V"> Vertical </option>
                <option value="H"> Horizontal </option>
            </select>
        </template>

        <template v-else-if="algoValue === 'rotate'">
            <div class="sliderInput">
                Rotate angle : 
                <input type="range" min="-180" max="180" value="0">
                <input type="number" min="-180" max="180" value="0" id="angle">
            </div>
        </template>

        <template v-else-if="algoValue === 'wave'" key="wave">
            <select id="waveAxis">
                <option value="V"> Vertical </option>
                <option value="H"> Horizontal </option>
            </select>
            <select id="waveType">
                <option value="C"> Curve </option>
                <option value="R"> Rectangle </option>
                <option value="T"> Triangle </option>
            </select>
            <input type="number" id="waveOffset" placeholder="Offset" key="waveOffset">
            <input type="number" id="amplitude" placeholder="Amplitude" key="amplitude">
            <input type="number" id="waveLenght" placeholder="Length" key="waveLenght">
        </template>

        <template v-else-if="algoValue === 'sphere'">
            <select id="sphere_type">
                <option value="S"> Sphere </option>
                <option value="E"> Ellipse </option>
            </select>
        </template>

        <template v-else-if="algoValue === 'sepia'">
        </template>

        <template v-else-if="algoValue === 'mozaic'">
        </template>

        <template v-else-if="algoValue === 'twist'">
            <input type="number" id="maxAngle" placeholder="Angle">
        </template>

        <template v-else-if="algoValue === 'halftoning'" key="halftoning">
            <input type="number" id="dotSize" placeholder="Dot Size" key="dotSize">
            <div class="sliderInput" key="spread">
                Spread of dots : 
                <input type="range" min="0" max="10" value="1">
                <input type="number" min="0" max="0" value="1" id="spread" >
            </div>
        </template>
    </div>

    <input type="checkbox" id="chainAlgo" value="true">
    <label for="chainAlgo"> Reuse image </label>
    <button @click="showImage(getParams(algoValue))"> Use algorithm </button>
    <div id="inputApply">
        <button  v-if="img.getHistoryCursor() > 0" @click="showPrev()">Previous</button>
        <button  v-if="img.getHistoryCursor() < img.getImageHistorySize() - 1" @click="showNext()">Next</button>
        <button @click="changeDownload(true)"> Download Image </button>
        <button v-on:click="http.submitFile(img.getModifiedImageRef().value, chosenImage.name)">Submit</button>
        <button @click="deleteImage()"> Delete Image </button>
    </div>
    <div id="errorPrompt"></div>
</div>
<div id="imgDivShow">
    <img id="show"/>
</div>
<div v-if="showTooltip" class="tooltip">
    <p v-if="algoValue === 'addLuminosityRGB'">
        Increase the luminosity of each pixel by the gain (can be negative)
    </p>
    <p v-else-if="algoValue === 'equalize'">
        Equalize the image histogram dynamically on the specified HSV band
    </p>
    <p v-else-if="algoValue === 'hueFilter'">
        Change the hue in degree of each pixel of the image to the selected one
    </p>
    <p v-else-if="algoValue === 'gradientImageSobel'">
        Show the outline of the image using a Sobel filter
    </p>
    <p v-else-if="algoValue === 'blur'">
        Apply either a Gaussian blur or a mean filter with the specified radius. The bigger the radius is, the blurrier the image will be
    </p>
    <p v-else-if="algoValue === 'rainbow'">
        Apply a rainbow filter on the specified axis
    </p>
    <p v-else-if="algoValue === 'hueSelector'">
        Desaturate all pixels with hue that isn't betwin the min and max values. If the maximum value is bigger than the minimum value, desaturate pixels with hue betwin min and max values.
    </p>
    <p v-else-if="algoValue === 'scale'">
        Change the dimension of the image to the specified height and width
    </p>
    <p v-else-if="algoValue === 'reverseHue'">
        Change the color of all pixel's to their opposite color in the Hue spectrum
    </p>
    <p v-else-if="algoValue === 'negative'">
        Change the color of each pixel into their respective negative
    </p>
    <p v-else-if="algoValue === 'flip'">
        Flip the image, either horizontally or vertically
    </p>
    <p v-else-if="algoValue === 'rotate'">
        Rotate the image by the specified amount in degree counterclockwise 
    </p>
    <p v-else-if="algoValue === 'wave'">
        Distorts the image into a wave on the specified axis. The waves can be offset by the amount of pixel specified, they can be curved, rectangular or triangular and have the specified lentgh and amplitude
    </p>
    <p v-else-if="algoValue === 'sphere'">
        Turn the image either into a perfect sphere, or an ellipse
    </p>
    <p v-else-if="algoValue === 'sepia'">
        Apply a sepia filter
    </p>
    <p v-else-if="algoValue === 'mozaic'">
        Divide the image into 4 smaller ones
    </p>
    <p v-else-if="algoValue === 'twist'">
        Twist the image in the specified angle in degree counterclockwise
    </p>
    <p v-else-if="algoValue === 'halftoning'">
        Apply the threshold Halftoning algorithm (or dithering) with 2 dimensional Gaussian function.
    </p>
</div>
<div v-if="showDownload" class="downloadContainer">
    <div>
        File name (leave empty for the original name) : 
        <input id="downloadNameInput" type="text">
        <button @click="downloadImage()"> Download </button>
        <button @click="changeDownload(false)"> Cancel </button>
    </div>
</div>
</template>