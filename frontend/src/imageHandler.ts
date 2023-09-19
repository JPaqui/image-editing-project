import { Ref, ref } from 'vue'
import * as http from './http-api'

const imageList = ref()

const imageSize = ref()

const chosenImage = ref()

const images = ref()
images.value = {}

const modifiedImage = ref()
let imageHistory : Blob[]
let historyCursor = -1

// Refresh imageList and images
export function refreshImageList() {
    http.getImageList().then( function(newImageList) {
        imageList.value = newImageList
        for (const image of newImageList) {
            http.getImage(image.id, undefined, false).then( function(imageValue) {
                images.value[image.id] = imageValue
            })
        }
    })
}

// Delete the image with the specified id from the server and then update the imageList
export function deleteImage(id : number, imageEl : HTMLElement | null) {
    chosenImage.value = undefined
    images.value[id] = null
    imageEl?.setAttribute("src", "")
    http.deleteImage(id).then(function() {
        refreshImageList()
    })
}

// Send a request to the server with the specified parameters and put the result in the show element
export function showAlgo(imageEl : HTMLElement | null, param : string[][], chainAlgo : boolean) {
  http.removeErrorMessage()
  http.getImage(chosenImage.value.id, param, chainAlgo)
    .then(function (imageValue) {
      if (imageValue === null) return
      modifiedImage.value = imageValue
      saveImageInHistory(modifiedImage.value)
      showImage(imageEl)
    }
  )
}

// Return the ref containing the current image size
export function getImageSizeRef() {
  return imageSize
}

// Returns the ref of the selected image in the gallery
export function getChosenImage() {
  return chosenImage
}

// Return the ref containing the current image
export function getModifiedImageRef() {
  return modifiedImage
}

// Set chosenImage
export function setChosenImage(image : any) {
  chosenImage.value = image
  modifiedImage.value = images.value[image.id]
  initialiseHistory()
}

//Get image size
export function getImageSize(imageData : Blob, imageSize : Ref) {
  let img = document.createElement('img')
  img.src = URL.createObjectURL(imageData)
  img.onload = function () {
    let strTab = imageSize.value.split('*')
    imageSize.value = `${img.width}*${img.height}*${strTab[2]}`
  }
}

// Returns the ref of imageList
export function getImageListRef() { return imageList }

// Set the image with the id "id" in the gallery (the image is taken from the images.value array)
export function setImageInGallery(id : number | string) {
  if (images.value[id] == null) return
  const reader = new window.FileReader()
  reader.readAsDataURL(images.value[id])
  reader.onload = function() {
    const imageDataUrl = (reader.result as string)
    document.getElementById('img' + id)?.setAttribute("src", imageDataUrl)
  }
}

// Set the image stored in modifiedImage inside of the element imageEl
export function showImage(imageEl : HTMLElement | null) {
  http.removeErrorMessage()
  const reader = new window.FileReader()
  imageSize.value = chosenImage.value.size
  getImageSize(modifiedImage.value, imageSize)
  reader.readAsDataURL(modifiedImage.value)
  reader.onload = function() {
    const imageDataUrl = (reader.result as string)
    imageEl?.setAttribute("src", imageDataUrl)
  }
}

//  Return the index of the current image in the history
export function getHistoryCursor(){
  return historyCursor
}

// Return the amount of image stored in the history
export function getImageHistorySize(){
  return imageHistory.length
}

// Empty and initialize the history
export function initialiseHistory() {
  imageHistory = []
  saveImageInHistory(modifiedImage.value)
  historyCursor = 0

}

// Add the current image in the history and increase the cursor
export function saveImageInHistory(image : Blob) {
  while (historyCursor < imageHistory.length-1) {
    imageHistory.pop()
  }
  imageHistory.push(image)
  historyCursor++
}

// Put the previous image in history in imageEl and decrease historyCursor
export function retrievePrevImage(imageEl : HTMLElement | null) {
  if (historyCursor > 0){
    historyCursor--
    modifiedImage.value = imageHistory[historyCursor]
    showImage(imageEl)
  }
}

// Put the next image in history in imageEl and increase historyCursor
export function retrieveNextImage(imageEl : HTMLElement | null) {
  if (historyCursor < imageHistory.length - 1){
    historyCursor++
    modifiedImage.value = imageHistory[historyCursor]
    showImage(imageEl)
  }
}