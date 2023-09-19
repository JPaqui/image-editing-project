import axios from 'axios'
import * as img from './imageHandler'

//Remove all error messages
export function removeErrorMessage() {
  document.querySelectorAll("#errorPrompt > p")
    .forEach( errorMessage => {
      errorMessage.remove()
    })
}

// Callback function when there is a request error
function handleError(error : any) {
  const p = document.createElement("p")
  switch (error.response.status)  {
    case 400:
      p.textContent = "Error : Invalid parameters"
      break
    case 404:
      p.textContent = "Error : The image you are trying to get doesn't exist (maybe try reloading the page)"
      break
    case 415:
      p.textContent = "Error : Unsupported media type"
      break
    case 500:
      p.textContent = "Error : Something went wrong server side, please contact the administrator"
      break
  }
  document.querySelector("#errorPrompt")?.appendChild(p)
  return null
}

// Returns the imageList from the backend server
export async function getImageList() {
  return await axios.get('/images')
      .then(function (response) {
          return response.data
      })
}


// Perform a request to the server with the parameter specified and returns the result as a blob
export async function getImage(id : number, stringParams : string[][] | undefined, chainAlgo : boolean) {
  if (chainAlgo) {
    let formData = new FormData()
    formData.append('file', img.getModifiedImageRef().value)
    if (stringParams !== undefined)
      for (const param of stringParams)
        formData.append(param[0], param[1])
    return await axios.post("/images", formData, { headers: { 'Content-Type': 'multipart/form-data' }, responseType: "blob"})
      .then(function (response) {
        return response.data
      })
      .catch(handleError)
  } else {
    const urlParam = new URLSearchParams(stringParams)
    return await axios.get('/images/' + id, {responseType : "blob", params : urlParam})
      .then(function (response) {
        return response.data
      })
      .catch(handleError)
  }
}

// Sends a request to the backend server to delete the image with the id "id"
export async function deleteImage(id : number) { 
  axios.delete("/images/" + id).catch(handleError)
}

// Sends a request to put the file "file" in the backend server
export function submitFile(file : string, fileName? : string) {
  let formData = new FormData()
  formData.append('file', file)
  if (typeof fileName !== "undefined") formData.append('fileName', fileName)
  axios.post('/images',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
      }
    }
  ).then(function(){
      img.refreshImageList()
  })
}