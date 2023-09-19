package pdl.backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void reset() {
        // reset Image class static counter
        ReflectionTestUtils.setField(Image.class, "count", 0L);
    }

    @Test
    @Order(1)
    public void getImageListShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void getImageShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void getImageShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void createImageShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "download.jpeg", MediaType.IMAGE_JPEG_VALUE, "test".getBytes());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(5)
    public void createImageShouldReturnUnsupportedMediaType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "besoins.pdf", MediaType.APPLICATION_PDF_VALUE, "test".getBytes());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @Order(6)
    public void algorithmNotFoundShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "jeej"))
                .andExpect(status().isNotFound());
    }


    @Test
    @Order(7)
    public void addLuminosityShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "addLuminosityRGB")
                        .param("gain", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    public void addLuminosityShouldReturnNotEnoughParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "addLuminosityRGB"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void addLuminosityShouldReturnUnknownParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "addLuminosityRGB")
                        .param("abcd", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void addLuminosityShouldReturnNotValidParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "addLuminosityRGB")
                        .param("gain", "abcd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void addLuminosityShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "addLuminosityRGB")
                        .param("gain", "100"))
                .andExpect(status().isNotFound());
    }


    @Test
    @Order(12)
    public void equalizeSShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "equalize")
                        .param("canal", "S"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    public void equalizeVShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "equalize")
                        .param("canal", "V"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void equalizeShouldReturnUnknownParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "equalize")
                        .param("abcde", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    public void equalizeShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "equalize")
                        .param("canal", "S"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(16)
    public void hueFilterShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "hueFilter")
                        .param("hue", "270"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(17)
    public void hueFilterShouldReturnNotEnoughParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "hueFilter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    public void hueFilterShouldReturnUnknownParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "hueFilter")
                        .param("abcde", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(19)
    public void hueFilterShouldReturnNotValidParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "hueFilter")
                        .param("hue", "abcde"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    public void hueFilterShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "hueFilter")
                        .param("hue", "270"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(21)
    public void blurMReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "blur")
                        .param("type", "M")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(22)
    public void blurGReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "blur")
                        .param("type", "G")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(23)
    public void blurShouldReturnNotEnoughParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "blur"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    public void blurShouldReturnUnknownParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "blur")
                        .param("abcde", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(25)
    public void blurShouldReturnNotValidParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "blur")
                        .param("type", "abcde")
                        .param("size", "abcde"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    public void blurReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "blur")
                        .param("type", "M")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(27)
    public void gradientImageSobelShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "gradientImageSobel"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(28)
    public void gradientImageSobelShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "gradientImageSobel"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(29)
    public void rainbowVShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "rainbow")
                        .param("direction", "V"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void rainbowHShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "rainbow")
                        .param("direction", "H"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(31)
    public void rainbowShouldReturnUnknownParameterBadRequest() throws Exception {
        this.mockMvc.perform(get("/images/0")
                        .param("algorithm", "rainbow")
                        .param("abcd", "V"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(32)
    public void rainbowShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get("/images/-1")
                        .param("algorithm", "rainbow")
                        .param("direction", ""))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(33)
    public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
        this.mockMvc.perform(delete("/images"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Order(34)
    public void deleteImageShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(delete("/images/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(35)
    public void deleteImageShouldReturnSuccess() throws Exception {
        this.mockMvc.perform(delete("/images/0"))
                .andExpect(status().isOk());
    }
}